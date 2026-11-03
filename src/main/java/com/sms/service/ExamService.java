package com.sms.service;

import com.sms.dto.AssessmentCreateDto;
import com.sms.dto.ExamCreateDto;
import com.sms.dto.MarkEntryDto;
import com.sms.entity.*;
import com.sms.repository.*;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExamService {

    private final ExamRepository examRepository;
    private final AssessmentRepository assessmentRepository;
    private final MarkRepository markRepository;
    private final SubjectRepository subjectRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final NotificationRepository notificationRepository;
    private final AuditService auditService;

    public List<Exam> getAllExams() {
        return examRepository.findAll();
    }

    public List<Exam> getActiveExams() {
        return examRepository.findByActiveTrue();
    }

    public Optional<Exam> getExamById(String id) {
        return examRepository.findById(id);
    }

    @Transactional
    public Exam createExam(ExamCreateDto dto, String actorId) {
        Exam exam = Exam.builder()
                .academicYearId(dto.getAcademicYearId())
                .semesterId(dto.getSemesterId())
                .name(dto.getName().trim())
                .examType(dto.getExamType().trim())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .active(true)
                .build();
        examRepository.save(exam);
        auditService.log(actorId, null, "CREATE_EXAM", "exams", exam.getId(), null, "Created " + exam.getName());
        return exam;
    }

    public List<Assessment> getAssessmentsForClass(String classId) {
        return assessmentRepository.findByClassId(classId);
    }

    public Optional<Assessment> getAssessmentById(String id) {
        return assessmentRepository.findById(id);
    }

    @Transactional
    public Assessment createAssessment(AssessmentCreateDto dto, String teacherId) {
        if (dto.getPassingMarks().compareTo(dto.getMaximumMarks()) > 0) {
            throw new IllegalArgumentException("Passing marks cannot exceed maximum marks.");
        }

        Assessment assessment = Assessment.builder()
                .examId(dto.getExamId())
                .classId(dto.getClassId())
                .subjectId(dto.getSubjectId())
                .createdByTeacherId(teacherId)
                .maximumMarks(dto.getMaximumMarks())
                .passingMarks(dto.getPassingMarks())
                .assessmentDate(dto.getAssessmentDate())
                .status(AssessmentStatus.DRAFT)
                .build();

        assessmentRepository.save(assessment);
        auditService.log(teacherId, null, "CREATE_ASSESSMENT", "assessments", assessment.getId(), null, 
                "Max: " + dto.getMaximumMarks() + ", Pass: " + dto.getPassingMarks());
        return assessment;
    }

    public List<Mark> getMarksForAssessment(String assessmentId) {
        return markRepository.findByAssessmentId(assessmentId);
    }

    @Transactional
    public void saveMarks(String assessmentId, List<MarkEntryDto.StudentScoreItem> scores, String teacherId) {
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assessment not found: " + assessmentId));

        for (MarkEntryDto.StudentScoreItem item : scores) {
            BigDecimal score = item.getMarksObtained() != null ? item.getMarksObtained() : BigDecimal.ZERO;
            if (score.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Marks cannot be negative.");
            }
            if (score.compareTo(assessment.getMaximumMarks()) > 0) {
                throw new IllegalArgumentException("Marks (" + score + ") cannot exceed maximum marks (" + assessment.getMaximumMarks() + ").");
            }

            Optional<Mark> existing = markRepository.findByAssessmentIdAndStudentId(assessmentId, item.getStudentId());
            if (existing.isPresent()) {
                Mark mark = existing.get();
                mark.setMarksObtained(score);
                mark.setAbsent(item.isAbsent());
                mark.setRemarks(item.getRemarks());
                markRepository.save(mark);
            } else {
                Mark mark = Mark.builder()
                        .assessmentId(assessmentId)
                        .studentId(item.getStudentId())
                        .marksObtained(score)
                        .absent(item.isAbsent())
                        .remarks(item.getRemarks())
                        .build();
                markRepository.save(mark);
            }
        }

        auditService.log(teacherId, null, "SAVE_DRAFT_MARKS", "assessments", assessmentId, null, "Saved scores for " + scores.size() + " students");
    }

    @Transactional
    public void publishMarks(String assessmentId, String teacherId) {
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assessment not found: " + assessmentId));

        assessment.setStatus(AssessmentStatus.PUBLISHED);
        assessment.setPublishedAt(LocalDateTime.now());
        assessment.setPublishedBy(teacherId);
        assessmentRepository.save(assessment);

        // Notify enrolled students
        List<Enrollment> enrollments = enrollmentRepository.findByClassId(assessment.getClassId());
        String subjectName = subjectRepository.findById(assessment.getSubjectId())
                .map(Subject::getName)
                .orElse("Academic Subject");

        for (Enrollment enr : enrollments) {
            studentProfileRepository.findById(enr.getStudentId()).ifPresent(student -> {
                if (student.getUser() != null) {
                    Notification notif = Notification.builder()
                            .userId(student.getUser().getId())
                            .title("New Examination Results Published")
                            .message("Your marks for " + subjectName + " have been published. Check your My Marks portal.")
                            .link("/student/marks")
                            .read(false)
                            .build();
                    notificationRepository.save(notif);
                }
            });
        }

        auditService.log(teacherId, null, "PUBLISH_MARKS", "assessments", assessmentId, "Status: DRAFT", "Status: PUBLISHED");
        log.info("Published marks for assessment {}", assessmentId);
    }

    public List<PublishedResultItem> getPublishedMarksForStudent(String studentId) {
        List<Mark> marks = markRepository.findByStudentId(studentId);
        List<PublishedResultItem> results = new ArrayList<>();

        for (Mark mark : marks) {
            assessmentRepository.findById(mark.getAssessmentId()).ifPresent(ass -> {
                if (ass.getStatus() == AssessmentStatus.PUBLISHED) {
                    String examName = examRepository.findById(ass.getExamId()).map(Exam::getName).orElse("Term Exam");
                    String subjectName = subjectRepository.findById(ass.getSubjectId()).map(Subject::getName).orElse("Subject");

                    double percentage = 0.0;
                    if (ass.getMaximumMarks().compareTo(BigDecimal.ZERO) > 0) {
                        percentage = mark.getMarksObtained()
                                .divide(ass.getMaximumMarks(), 4, java.math.RoundingMode.HALF_UP)
                                .multiply(new BigDecimal(100))
                                .doubleValue();
                    }

                    results.add(PublishedResultItem.builder()
                            .examName(examName)
                            .subjectName(subjectName)
                            .maximumMarks(ass.getMaximumMarks())
                            .passingMarks(ass.getPassingMarks())
                            .marksObtained(mark.getMarksObtained())
                            .percentage(Math.round(percentage * 10.0) / 10.0)
                            .absent(mark.isAbsent())
                            .passed(mark.getMarksObtained().compareTo(ass.getPassingMarks()) >= 0)
                            .publishedAt(ass.getPublishedAt())
                            .build());
                }
            });
        }
        return results;
    }

    @Getter
    @Builder
    public static class PublishedResultItem {
        private String examName;
        private String subjectName;
        private BigDecimal maximumMarks;
        private BigDecimal passingMarks;
        private BigDecimal marksObtained;
        private double percentage;
        private boolean absent;
        private boolean passed;
        private LocalDateTime publishedAt;
    }
}

