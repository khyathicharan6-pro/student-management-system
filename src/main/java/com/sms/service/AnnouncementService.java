package com.sms.service;

import com.sms.dto.AnnouncementDto;
import com.sms.entity.Announcement;
import com.sms.entity.Enrollment;
import com.sms.repository.AnnouncementRepository;
import com.sms.repository.EnrollmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final AuditService auditService;

    public List<Announcement> getAllAnnouncements() {
        return announcementRepository.findAllByOrderByPublishDateDesc();
    }

    public List<Announcement> getActiveAnnouncementsForStudent(String studentId) {
        String classId = enrollmentRepository.findByStudentId(studentId).stream()
                .findFirst()
                .map(Enrollment::getClassId)
                .orElse(null);

        return announcementRepository.findActiveAnnouncements(LocalDateTime.now(), classId, "STUDENT");
    }

    @Transactional
    public Announcement createOrUpdate(AnnouncementDto dto, String authorId) {
        Announcement announcement;
        if (dto.getId() != null && !dto.getId().isBlank()) {
            announcement = announcementRepository.findById(dto.getId()).orElseGet(Announcement::new);
        } else {
            announcement = new Announcement();
            announcement.setAuthorId(authorId);
            announcement.setPublishDate(LocalDateTime.now());
        }

        announcement.setTitle(dto.getTitle().trim());
        announcement.setContent(dto.getContent().trim());
        announcement.setTargetClassId((dto.getTargetClassId() != null && !dto.getTargetClassId().isBlank()) ? dto.getTargetClassId() : null);
        announcement.setTargetRole((dto.getTargetRole() != null && !dto.getTargetRole().isBlank()) ? dto.getTargetRole() : "ALL");
        announcement.setExpiryDate(dto.getExpiryDate());

        announcementRepository.save(announcement);
        auditService.log(authorId, null, "SAVE_ANNOUNCEMENT", "announcements", announcement.getId(), null, "Title: " + announcement.getTitle());
        return announcement;
    }

    @Transactional
    public void delete(String id, String actorId) {
        announcementRepository.deleteById(id);
        auditService.log(actorId, null, "DELETE_ANNOUNCEMENT", "announcements", id, null, "Deleted announcement");
    }
}

