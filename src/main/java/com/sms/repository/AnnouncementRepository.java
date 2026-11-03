package com.sms.repository;

import com.sms.entity.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, String> {
    List<Announcement> findAllByOrderByPublishDateDesc();

    @Query("SELECT a FROM Announcement a WHERE (a.expiryDate IS NULL OR a.expiryDate > :now) " +
           "AND (a.targetClassId IS NULL OR a.targetClassId = :classId) " +
           "AND (a.targetRole = 'ALL' OR a.targetRole = :role) " +
           "ORDER BY a.publishDate DESC")
    List<Announcement> findActiveAnnouncements(@Param("now") LocalDateTime now,
                                              @Param("classId") String classId,
                                              @Param("role") String role);
}

