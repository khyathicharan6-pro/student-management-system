package com.sms.controller;

import com.sms.dto.*;
import com.sms.entity.*;
import com.sms.repository.*;
import com.sms.security.CustomUserDetails;
import com.sms.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Controller
@RequestMapping("/teacher")
@RequiredArgsConstructor
@Slf4j
public class TeacherPortalController {

    private final StudentService studentService;
    private final ClassService classService;
    private final SubjectService subjectService;
    private final AttendanceService attendanceService;
    private final ExamService examService;
    private final GradeService gradeService;
    private final TimetableService timetableService;
    private final AnnouncementService announcementService;
    private final TeacherProfileRepository teacherProfileRepository;
    private final DepartmentRepository departmentRepository;
    private final AcademicYearRepository academicYearRepository;
    private final SemesterRepository semesterRepository;
    private final AuditService auditService;

    private String getTeacherId(CustomUserDetails userDetails) {
        return teacherProfileRepository.findByUserId(userDetails.getId())
                .map(TeacherProfile::getId)
                .orElse("tp-001");
    }

    // =========================================================================
    // 1. DASHBOARD
    // =========================================================================
    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        String teacherId = getTeacherId(userDetails);
        List<ClassEntity> assignedClasses = classService.getClassesForTeacher(teacherId);
        List<Subject> assignedSubjects = subjectService.getSubjectsForTeacher(teacherId);
        List<StudentProfile> allStudents = studentService.getAllStudents();
        List<TimetableService.TimetableSlotView> schedule = timetableService.getTimetableForTeacher(teacherId);
        List<Announcement> announcements = announcementService.getAllAnnouncements();
        List<Exam> activeExams = examService.getActiveExams();

        model.addAttribute("userDetails", userDetails);
        model.addAttribute("activePage", "dashboard");
        model.addAttribute("totalStudents", allStudents.size());
        model.addAttribute("assignedClasses", assignedClasses.size());
        model.addAttribute("assignedSubjects", assignedSubjects.size());
        model.addAttribute("todayAttendanceRate", "94.2%");
        model.addAttribute("overallAttendanceRate", "91.8%");
        model.addAttribute("schedule", schedule);
        model.addAttribute("announcements", announcements);
        model.addAttribute("activeExams", activeExams);
        return "teacher/dashboard";
    }

    // =========================================================================
    // 2. STUDENT MANAGEMENT
    // =========================================================================
    @GetMapping("/students")
    public String students(@RequestParam(value = "search", required = false) String search,
                           @RequestParam(value = "classId", required = false) String classId,
                           @AuthenticationPrincipal CustomUserDetails userDetails,
                           Model model) {
        String teacherId = getTeacherId(userDetails);
        List<StudentProfile> students = studentService.searchStudents(search, classId);
        List<ClassEntity> classes = classService.getClassesForTeacher(teacherId);
        List<Department> departments = departmentRepository.findAll();

        model.addAttribute("userDetails", userDetails);
        model.addAttribute("activePage", "students");
        model.addAttribute("students", students);
        model.addAttribute("classes", classes);
        model.addAttribute("departments", departments);
        model.addAttribute("searchQuery", search);
        model.addAttribute("selectedClassId", classId);
        model.addAttribute("newStudent", new StudentCreateDto());
        return "teacher/students";
    }

    @PostMapping("/students/add")
    public String addStudent(@Valid @ModelAttribute("newStudent") StudentCreateDto dto,
                             BindingResult bindingResult,
                             @AuthenticationPrincipal CustomUserDetails userDetails,
                             RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", bindingResult.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/teacher/students";
        }
        try {
            studentService.createStudent(dto, userDetails.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Student " + dto.getFullName() + " created successfully with institutional login!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/teacher/students";
    }

    @PostMapping("/students/update")
    public String updateStudent(@RequestParam("id") String id,
                                @Valid @ModelAttribute StudentUpdateDto dto,
                                BindingResult bindingResult,
                                @AuthenticationPrincipal CustomUserDetails userDetails,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", bindingResult.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/teacher/students";
        }
        try {
            studentService.updateStudent(id, dto, userDetails.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Student profile updated successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/teacher/students";
    }

    @PostMapping("/students/toggle-status")
    public String toggleStudentStatus(@RequestParam("id") String id,
                                      @RequestParam("status") String status,
                                      @AuthenticationPrincipal CustomUserDetails userDetails,
                                      RedirectAttributes redirectAttributes) {
        try {
            studentService.toggleAccountStatus(id, AccountStatus.valueOf(status), userDetails.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Student account status changed to " + status + ".");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/teacher/students";
    }

    // =========================================================================
    // 3. CLASS & SECTION MANAGEMENT
    // =========================================================================
    @GetMapping("/classes")
    public String classes(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        String teacherId = getTeacherId(userDetails);
        List<ClassEntity> classes = classService.getAllClasses();
        List<Department> departments = departmentRepository.findAll();
        List<AcademicYear> academicYears = academicYearRepository.findAll();
        List<Semester> semesters = semesterRepository.findAll();

        model.addAttribute("userDetails", userDetails);
        model.addAttribute("activePage", "classes");
        model.addAttribute("classes", classes);
        model.addAttribute("departments", departments);
        model.addAttribute("academicYears", academicYears);
        model.addAttribute("semesters", semesters);
        model.addAttribute("newClass", new ClassCreateDto());
        return "teacher/classes";
    }

    @PostMapping("/classes/add")
    public String addClass(@Valid @ModelAttribute("newClass") ClassCreateDto dto,
                           BindingResult bindingResult,
                           @AuthenticationPrincipal CustomUserDetails userDetails,
                           RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", bindingResult.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/teacher/classes";
        }
        try {
            String teacherId = getTeacherId(userDetails);
            ClassEntity created = classService.createClass(dto, userDetails.getId());
            classService.assignTeacherToClass(created.getId(), teacherId, true, userDetails.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Class " + created.getName() + " created and assigned successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/teacher/classes";
    }

    // =========================================================================
    // 4. SUBJECT MANAGEMENT
    // =========================================================================
    @GetMapping("/subjects")
    public String subjects(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        List<Subject> subjects = subjectService.getAllSubjects();
        List<Department> departments = departmentRepository.findAll();
        List<ClassEntity> classes = classService.getAllClasses();

        model.addAttribute("userDetails", userDetails);
        model.addAttribute("activePage", "subjects");
        model.addAttribute("subjects", subjects);
        model.addAttribute("departments", departments);
        model.addAttribute("classes", classes);
        model.addAttribute("newSubject", new SubjectCreateDto());
        return "teacher/subjects";
    }

    @PostMapping("/subjects/add")
    public String addSubject(@Valid @ModelAttribute("newSubject") SubjectCreateDto dto,
                             BindingResult bindingResult,
                             @AuthenticationPrincipal CustomUserDetails userDetails,
                             RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", bindingResult.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/teacher/subjects";
        }
        try {
            subjectService.createSubject(dto, userDetails.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Subject " + dto.getCode() + " created successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/teacher/subjects";
    }

    @PostMapping("/subjects/assign-class")
    public String assignSubjectToClass(@RequestParam("classId") String classId,
                                       @RequestParam("subjectId") String subjectId,
                                       @AuthenticationPrincipal CustomUserDetails userDetails,
                                       RedirectAttributes redirectAttributes) {
        try {
            String teacherId = getTeacherId(userDetails);
            subjectService.assignSubjectToClass(classId, subjectId, teacherId, userDetails.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Subject allocated to class with instructor assignment.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/teacher/subjects";
    }

    // =========================================================================
    // 5. ATTENDANCE MANAGEMENT
    // =========================================================================
    @GetMapping("/attendance")
    public String attendance(@RequestParam(value = "classId", required = false) String classId,
                             @RequestParam(value = "subjectId", required = false) String subjectId,
                             @RequestParam(value = "date", required = false) String dateStr,
                             @AuthenticationPrincipal CustomUserDetails userDetails,
                             Model model) {
        String teacherId = getTeacherId(userDetails);
        List<ClassEntity> classes = classService.getClassesForTeacher(teacherId);
        List<Subject> subjects = subjectService.getSubjectsForTeacher(teacherId);

        LocalDate sessionDate = (dateStr != null && !dateStr.isBlank()) 
                ? LocalDate.parse(dateStr) 
                : LocalDate.now();

        List<StudentProfile> enrolledStudents = new ArrayList<>();
        AttendanceSession session = null;
        Map<String, AttendanceStatus> currentStatusMap = new HashMap<>();

        if (classId != null && !classId.isBlank() && subjectId != null && !subjectId.isBlank()) {
            enrolledStudents = classService.getEnrolledStudents(classId);
            session = attendanceService.getOrCreateSession(classId, subjectId, teacherId, sessionDate, 1);
            List<AttendanceRecord> existingRecords = attendanceService.getRecordsForSession(session.getId());
            for (AttendanceRecord rec : existingRecords) {
                currentStatusMap.put(rec.getStudentId(), rec.getStatus());
            }
        }

        model.addAttribute("userDetails", userDetails);
        model.addAttribute("activePage", "attendance");
        model.addAttribute("classes", classes);
        model.addAttribute("subjects", subjects);
        model.addAttribute("selectedClassId", classId);
        model.addAttribute("selectedSubjectId", subjectId);
        model.addAttribute("sessionDate", sessionDate.toString());
        model.addAttribute("enrolledStudents", enrolledStudents);
        model.addAttribute("session", session);
        model.addAttribute("statusMap", currentStatusMap);
        return "teacher/attendance";
    }

    @PostMapping("/attendance/save")
    public String saveAttendance(@RequestParam("sessionId") String sessionId,
                                 @RequestParam Map<String, String> formParams,
                                 @AuthenticationPrincipal CustomUserDetails userDetails,
                                 RedirectAttributes redirectAttributes) {
        try {
            Map<String, AttendanceStatus> studentStatusMap = new HashMap<>();
            for (Map.Entry<String, String> entry : formParams.entrySet()) {
                if (entry.getKey().startsWith("status_")) {
                    String studentId = entry.getKey().replace("status_", "");
                    try {
                        AttendanceStatus status = AttendanceStatus.valueOf(entry.getValue());
                        studentStatusMap.put(studentId, status);
                    } catch (Exception ignored) {}
                }
            }
            attendanceService.saveSessionAttendance(sessionId, studentStatusMap, userDetails.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Attendance records saved and percentages updated!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/teacher/attendance";
    }

    // =========================================================================
    // 6. EXAMINATIONS & MARKS
    // =========================================================================
    @GetMapping("/exams")
    public String exams(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        List<Exam> exams = examService.getAllExams();
        List<AcademicYear> academicYears = academicYearRepository.findAll();
        List<Semester> semesters = semesterRepository.findAll();
        List<ClassEntity> classes = classService.getAllClasses();
        List<Subject> subjects = subjectService.getAllSubjects();

        model.addAttribute("userDetails", userDetails);
        model.addAttribute("activePage", "exams");
        model.addAttribute("exams", exams);
        model.addAttribute("academicYears", academicYears);
        model.addAttribute("semesters", semesters);
        model.addAttribute("classes", classes);
        model.addAttribute("subjects", subjects);
        model.addAttribute("newExam", new ExamCreateDto());
        model.addAttribute("newAssessment", new AssessmentCreateDto());
        return "teacher/exams";
    }

    @PostMapping("/exams/add")
    public String addExam(@Valid @ModelAttribute("newExam") ExamCreateDto dto,
                          BindingResult bindingResult,
                          @AuthenticationPrincipal CustomUserDetails userDetails,
                          RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", bindingResult.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/teacher/exams";
        }
        try {
            examService.createExam(dto, userDetails.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Examination cycle created successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/teacher/exams";
    }

    @PostMapping("/exams/assessment/add")
    public String addAssessment(@Valid @ModelAttribute("newAssessment") AssessmentCreateDto dto,
                                BindingResult bindingResult,
                                @AuthenticationPrincipal CustomUserDetails userDetails,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", bindingResult.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/teacher/exams";
        }
        try {
            String teacherId = getTeacherId(userDetails);
            examService.createAssessment(dto, teacherId);
            redirectAttributes.addFlashAttribute("successMessage", "Subject assessment test added to exam!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/teacher/exams";
    }

    @GetMapping("/marks")
    public String marks(@RequestParam(value = "assessmentId", required = false) String assessmentId,
                        @RequestParam(value = "classId", required = false) String classId,
                        @AuthenticationPrincipal CustomUserDetails userDetails,
                        Model model) {
        String teacherId = getTeacherId(userDetails);
        List<ClassEntity> classes = classService.getClassesForTeacher(teacherId);
        List<Assessment> assessments = (classId != null && !classId.isBlank()) 
                ? examService.getAssessmentsForClass(classId) 
                : List.of();

        Assessment currentAssessment = null;
        List<StudentProfile> students = List.of();
        Map<String, Mark> marksMap = new HashMap<>();

        if (assessmentId != null && !assessmentId.isBlank()) {
            currentAssessment = examService.getAssessmentById(assessmentId).orElse(null);
            if (currentAssessment != null) {
                students = classService.getEnrolledStudents(currentAssessment.getClassId());
                List<Mark> existingMarks = examService.getMarksForAssessment(assessmentId);
                for (Mark m : existingMarks) {
                    marksMap.put(m.getStudentId(), m);
                }
            }
        }

        model.addAttribute("userDetails", userDetails);
        model.addAttribute("activePage", "marks");
        model.addAttribute("classes", classes);
        model.addAttribute("assessments", assessments);
        model.addAttribute("selectedClassId", classId);
        model.addAttribute("selectedAssessmentId", assessmentId);
        model.addAttribute("currentAssessment", currentAssessment);
        model.addAttribute("students", students);
        model.addAttribute("marksMap", marksMap);
        return "teacher/marks";
    }

    @PostMapping("/marks/save-draft")
    public String saveDraftMarks(@RequestParam("assessmentId") String assessmentId,
                                 @RequestParam Map<String, String> formParams,
                                 @AuthenticationPrincipal CustomUserDetails userDetails,
                                 RedirectAttributes redirectAttributes) {
        try {
            String teacherId = getTeacherId(userDetails);
            List<MarkEntryDto.StudentScoreItem> scoreItems = new ArrayList<>();

            for (Map.Entry<String, String> entry : formParams.entrySet()) {
                if (entry.getKey().startsWith("score_")) {
                    String studentId = entry.getKey().replace("score_", "");
                    BigDecimal score = BigDecimal.ZERO;
                    try {
                        score = new BigDecimal(entry.getValue());
                    } catch (Exception ignored) {}

                    boolean isAbsent = "on".equalsIgnoreCase(formParams.get("absent_" + studentId));
                    String remarks = formParams.get("remarks_" + studentId);

                    scoreItems.add(MarkEntryDto.StudentScoreItem.builder()
                            .studentId(studentId)
                            .marksObtained(score)
                            .absent(isAbsent)
                            .remarks(remarks)
                            .build());
                }
            }

            examService.saveMarks(assessmentId, scoreItems, teacherId);
            redirectAttributes.addFlashAttribute("successMessage", "Marks saved as Draft. Results remain hidden from students until published.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/teacher/marks?assessmentId=" + assessmentId;
    }

    @PostMapping("/marks/publish")
    public String publishMarks(@RequestParam("assessmentId") String assessmentId,
                               @AuthenticationPrincipal CustomUserDetails userDetails,
                               RedirectAttributes redirectAttributes) {
        try {
            String teacherId = getTeacherId(userDetails);
            examService.publishMarks(assessmentId, teacherId);
            redirectAttributes.addFlashAttribute("successMessage", "Marks officially PUBLISHED! Students can now view their verified results.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/teacher/marks?assessmentId=" + assessmentId;
    }

    // =========================================================================
    // 7. GRADE SCALES
    // =========================================================================
    @GetMapping("/grades")
    public String grades(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        List<GradeScale> scales = gradeService.getAllGradeScales();
        model.addAttribute("userDetails", userDetails);
        model.addAttribute("activePage", "grades");
        model.addAttribute("scales", scales);
        model.addAttribute("newScale", new GradeScaleDto());
        return "teacher/grades";
    }

    @PostMapping("/grades/save")
    public String saveGradeScale(@Valid @ModelAttribute("newScale") GradeScaleDto dto,
                                 BindingResult bindingResult,
                                 @AuthenticationPrincipal CustomUserDetails userDetails,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", bindingResult.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/teacher/grades";
        }
        try {
            gradeService.saveGradeScale(dto, userDetails.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Grade scale configuration updated!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/teacher/grades";
    }

    // =========================================================================
    // 8. TIMETABLE MANAGEMENT (Conflict Detection)
    // =========================================================================
    @GetMapping("/timetable")
    public String timetable(@RequestParam(value = "classId", required = false) String classId,
                            @AuthenticationPrincipal CustomUserDetails userDetails,
                            Model model) {
        String teacherId = getTeacherId(userDetails);
        List<ClassEntity> classes = classService.getAllClasses();
        List<Subject> subjects = subjectService.getAllSubjects();
        List<TeacherProfile> teachers = teacherProfileRepository.findAll();
        List<AcademicYear> academicYears = academicYearRepository.findAll();
        List<Semester> semesters = semesterRepository.findAll();

        String selectedClassId = (classId != null && !classId.isBlank()) 
                ? classId 
                : (!classes.isEmpty() ? classes.get(0).getId() : null);

        List<TimetableService.TimetableSlotView> slots = (selectedClassId != null) 
                ? timetableService.getTimetableForClass(selectedClassId) 
                : List.of();

        model.addAttribute("userDetails", userDetails);
        model.addAttribute("activePage", "timetable");
        model.addAttribute("classes", classes);
        model.addAttribute("subjects", subjects);
        model.addAttribute("teachers", teachers);
        model.addAttribute("academicYears", academicYears);
        model.addAttribute("semesters", semesters);
        model.addAttribute("selectedClassId", selectedClassId);
        model.addAttribute("slots", slots);
        model.addAttribute("newSlot", new TimetableSlotDto());
        return "teacher/timetable";
    }

    @PostMapping("/timetable/add")
    public String addTimetableSlot(@Valid @ModelAttribute("newSlot") TimetableSlotDto dto,
                                   BindingResult bindingResult,
                                   @AuthenticationPrincipal CustomUserDetails userDetails,
                                   RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", bindingResult.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/teacher/timetable?classId=" + dto.getClassId();
        }
        try {
            timetableService.createOrUpdateSlot(dto, userDetails.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Timetable period scheduled successfully without any room, teacher, or class conflict!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/teacher/timetable?classId=" + dto.getClassId();
    }

    @PostMapping("/timetable/delete")
    public String deleteTimetableSlot(@RequestParam("id") String id,
                                      @RequestParam("classId") String classId,
                                      @AuthenticationPrincipal CustomUserDetails userDetails,
                                      RedirectAttributes redirectAttributes) {
        try {
            timetableService.deleteSlot(id, userDetails.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Timetable slot deleted.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/teacher/timetable?classId=" + classId;
    }

    // =========================================================================
    // 9. ANNOUNCEMENTS
    // =========================================================================
    @GetMapping("/announcements")
    public String announcements(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        List<Announcement> list = announcementService.getAllAnnouncements();
        List<ClassEntity> classes = classService.getAllClasses();

        model.addAttribute("userDetails", userDetails);
        model.addAttribute("activePage", "announcements");
        model.addAttribute("announcements", list);
        model.addAttribute("classes", classes);
        model.addAttribute("newAnnouncement", new AnnouncementDto());
        return "teacher/announcements";
    }

    @PostMapping("/announcements/add")
    public String addAnnouncement(@Valid @ModelAttribute("newAnnouncement") AnnouncementDto dto,
                                  BindingResult bindingResult,
                                  @AuthenticationPrincipal CustomUserDetails userDetails,
                                  RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", bindingResult.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/teacher/announcements";
        }
        try {
            announcementService.createOrUpdate(dto, userDetails.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Announcement published successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/teacher/announcements";
    }

    @PostMapping("/announcements/delete")
    public String deleteAnnouncement(@RequestParam("id") String id,
                                     @AuthenticationPrincipal CustomUserDetails userDetails,
                                     RedirectAttributes redirectAttributes) {
        try {
            announcementService.delete(id, userDetails.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Announcement removed.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/teacher/announcements";
    }

    // =========================================================================
    // 10. REPORTS & ANALYTICS
    // =========================================================================
    @GetMapping("/reports")
    public String reports(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        List<ClassEntity> classes = classService.getAllClasses();
        List<StudentProfile> students = studentService.getAllStudents();
        List<AuditLog> auditLogs = auditService.getRecentLogs();

        model.addAttribute("userDetails", userDetails);
        model.addAttribute("activePage", "reports");
        model.addAttribute("classes", classes);
        model.addAttribute("totalStudents", students.size());
        model.addAttribute("auditLogs", auditLogs);
        return "teacher/reports";
    }

    // =========================================================================
    // 11. PROFILE & SETTINGS
    // =========================================================================
    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        String teacherId = getTeacherId(userDetails);
        TeacherProfile profile = teacherProfileRepository.findById(teacherId).orElse(null);
        Department dept = (profile != null) ? departmentRepository.findById(profile.getDepartmentId()).orElse(null) : null;

        model.addAttribute("userDetails", userDetails);
        model.addAttribute("activePage", "profile");
        model.addAttribute("profile", profile);
        model.addAttribute("department", dept);
        model.addAttribute("passwordDto", new ChangePasswordDto());
        return "teacher/profile";
    }

    @PostMapping("/profile/change-password")
    public String changePassword(@Valid @ModelAttribute("passwordDto") ChangePasswordDto dto,
                                 BindingResult bindingResult,
                                 @AuthenticationPrincipal CustomUserDetails userDetails,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", bindingResult.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/teacher/profile";
        }
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            redirectAttributes.addFlashAttribute("errorMessage", "New passwords do not match.");
            return "redirect:/teacher/profile";
        }
        boolean success = studentService.changePassword(userDetails.getId(), dto.getCurrentPassword(), dto.getNewPassword());
        if (success) {
            redirectAttributes.addFlashAttribute("successMessage", "Password changed successfully!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Current password is incorrect.");
        }
        return "redirect:/teacher/profile";
    }
}
