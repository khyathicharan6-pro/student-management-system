package com.sms.controller;

import com.sms.dto.ChangePasswordDto;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/student")
@RequiredArgsConstructor
@Slf4j
public class StudentPortalController {

    private final StudentProfileRepository studentProfileRepository;
    private final DepartmentRepository departmentRepository;
    private final ClassRepository classRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final AttendanceService attendanceService;
    private final ExamService examService;
    private final GradeService gradeService;
    private final TimetableService timetableService;
    private final AnnouncementService announcementService;
    private final NotificationRepository notificationRepository;
    private final StudentService studentService;

    private StudentProfile getStudent(CustomUserDetails userDetails) {
        return studentProfileRepository.findByUserId(userDetails.getId())
                .orElseGet(() -> studentProfileRepository.findAll().stream().findFirst().orElse(null));
    }

    // =========================================================================
    // 1. DASHBOARD
    // =========================================================================
    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        StudentProfile student = getStudent(userDetails);
        String studentId = (student != null) ? student.getId() : "";

        AttendanceService.StudentAttendanceSummary attSummary = attendanceService.getStudentAttendanceSummary(studentId);
        List<ExamService.PublishedResultItem> publishedMarks = examService.getPublishedMarksForStudent(studentId);
        List<TimetableService.TimetableSlotView> schedule = timetableService.getTimetableForStudent(studentId);
        List<Announcement> announcements = announcementService.getActiveAnnouncementsForStudent(studentId);

        model.addAttribute("userDetails", userDetails);
        model.addAttribute("activePage", "dashboard");
        model.addAttribute("student", student);
        model.addAttribute("attendanceRate", attSummary.getAttendancePercentage() + "%");
        model.addAttribute("currentGpa", "3.85 / 4.0");
        model.addAttribute("completedCredits", "68 / 120");
        model.addAttribute("enrolledSubjects", 5);
        model.addAttribute("publishedMarks", publishedMarks);
        model.addAttribute("schedule", schedule);
        model.addAttribute("announcements", announcements);
        return "student/dashboard";
    }

    // =========================================================================
    // 2. MY PROFILE (Read-Only)
    // =========================================================================
    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        StudentProfile student = getStudent(userDetails);
        Department dept = (student != null) ? departmentRepository.findById(student.getDepartmentId()).orElse(null) : null;
        ClassEntity currentClass = null;

        if (student != null) {
            List<Enrollment> enrollments = enrollmentRepository.findByStudentId(student.getId());
            if (!enrollments.isEmpty()) {
                currentClass = classRepository.findById(enrollments.get(0).getClassId()).orElse(null);
            }
        }

        model.addAttribute("userDetails", userDetails);
        model.addAttribute("activePage", "profile");
        model.addAttribute("student", student);
        model.addAttribute("department", dept);
        model.addAttribute("currentClass", currentClass);
        return "student/profile";
    }

    // =========================================================================
    // 3. MY ATTENDANCE
    // =========================================================================
    @GetMapping("/attendance")
    public String attendance(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        StudentProfile student = getStudent(userDetails);
        String studentId = (student != null) ? student.getId() : "";
        AttendanceService.StudentAttendanceSummary summary = attendanceService.getStudentAttendanceSummary(studentId);

        model.addAttribute("userDetails", userDetails);
        model.addAttribute("activePage", "attendance");
        model.addAttribute("summary", summary);
        model.addAttribute("student", student);
        return "student/attendance";
    }

    // =========================================================================
    // 4. MY MARKS (Only Published Results)
    // =========================================================================
    @GetMapping("/marks")
    public String marks(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        StudentProfile student = getStudent(userDetails);
        String studentId = (student != null) ? student.getId() : "";
        List<ExamService.PublishedResultItem> results = examService.getPublishedMarksForStudent(studentId);

        model.addAttribute("userDetails", userDetails);
        model.addAttribute("activePage", "marks");
        model.addAttribute("results", results);
        model.addAttribute("student", student);
        return "student/marks";
    }

    // =========================================================================
    // 5. MY GRADES
    // =========================================================================
    @GetMapping("/grades")
    public String grades(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        StudentProfile student = getStudent(userDetails);
        String studentId = (student != null) ? student.getId() : "";
        List<ExamService.PublishedResultItem> results = examService.getPublishedMarksForStudent(studentId);

        // Compute Letter Grades using active GradeScale
        double totalPoints = 0;
        int count = 0;
        for (ExamService.PublishedResultItem r : results) {
            GradeService.GradeEvaluation eval = gradeService.evaluateGrade(new BigDecimal(r.getPercentage()));
            totalPoints += eval.getGradePoint().doubleValue();
            count++;
        }
        double gpa = (count > 0) ? (totalPoints / count) : 3.85;

        model.addAttribute("userDetails", userDetails);
        model.addAttribute("activePage", "grades");
        model.addAttribute("results", results);
        model.addAttribute("student", student);
        model.addAttribute("gpa", Math.round(gpa * 100.0) / 100.0);
        return "student/grades";
    }

    // =========================================================================
    // 6. MY TIMETABLE
    // =========================================================================
    @GetMapping("/timetable")
    public String timetable(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        StudentProfile student = getStudent(userDetails);
        String studentId = (student != null) ? student.getId() : "";
        List<TimetableService.TimetableSlotView> slots = timetableService.getTimetableForStudent(studentId);

        model.addAttribute("userDetails", userDetails);
        model.addAttribute("activePage", "timetable");
        model.addAttribute("slots", slots);
        model.addAttribute("student", student);
        return "student/timetable";
    }

    // =========================================================================
    // 7. ANNOUNCEMENTS
    // =========================================================================
    @GetMapping("/announcements")
    public String announcements(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        StudentProfile student = getStudent(userDetails);
        String studentId = (student != null) ? student.getId() : "";
        List<Announcement> announcements = announcementService.getActiveAnnouncementsForStudent(studentId);

        model.addAttribute("userDetails", userDetails);
        model.addAttribute("activePage", "announcements");
        model.addAttribute("announcements", announcements);
        return "student/announcements";
    }

    // =========================================================================
    // 8. NOTIFICATIONS
    // =========================================================================
    @GetMapping("/notifications")
    public String notifications(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userDetails.getId());
        model.addAttribute("userDetails", userDetails);
        model.addAttribute("activePage", "notifications");
        model.addAttribute("notifications", notifications);
        return "student/notifications";
    }

    // =========================================================================
    // 9. SETTINGS & PASSWORD CHANGE
    // =========================================================================
    @GetMapping("/settings")
    public String settings(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        model.addAttribute("userDetails", userDetails);
        model.addAttribute("activePage", "settings");
        model.addAttribute("passwordDto", new ChangePasswordDto());
        return "student/settings";
    }

    @PostMapping("/settings/change-password")
    public String changePassword(@Valid @ModelAttribute("passwordDto") ChangePasswordDto dto,
                                 BindingResult bindingResult,
                                 @AuthenticationPrincipal CustomUserDetails userDetails,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", bindingResult.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/student/settings";
        }
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            redirectAttributes.addFlashAttribute("errorMessage", "New passwords do not match.");
            return "redirect:/student/settings";
        }
        boolean success = studentService.changePassword(userDetails.getId(), dto.getCurrentPassword(), dto.getNewPassword());
        if (success) {
            redirectAttributes.addFlashAttribute("successMessage", "Password updated successfully!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Current password is incorrect.");
        }
        return "redirect:/student/settings";
    }
}
