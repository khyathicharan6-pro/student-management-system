package com.sms.repository;

import com.sms.entity.TimetableSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;

@Repository
public interface TimetableSlotRepository extends JpaRepository<TimetableSlot, String> {
    List<TimetableSlot> findByClassId(String classId);
    List<TimetableSlot> findByTeacherId(String teacherId);
    List<TimetableSlot> findByClassIdAndWeekday(String classId, String weekday);
    List<TimetableSlot> findByTeacherIdAndWeekday(String teacherId, String weekday);

    // Section Conflict: Does this class have an overlapping slot on this weekday?
    @Query("SELECT t FROM TimetableSlot t WHERE t.classId = :classId AND t.weekday = :weekday " +
           "AND (:id IS NULL OR t.id <> :id) " +
           "AND (t.startTime < :endTime AND t.endTime > :startTime)")
    List<TimetableSlot> findClassConflicts(@Param("classId") String classId,
                                           @Param("weekday") String weekday,
                                           @Param("startTime") LocalTime startTime,
                                           @Param("endTime") LocalTime endTime,
                                           @Param("id") String id);

    // Teacher Conflict: Does this teacher have an overlapping slot on this weekday?
    @Query("SELECT t FROM TimetableSlot t WHERE t.teacherId = :teacherId AND t.weekday = :weekday " +
           "AND (:id IS NULL OR t.id <> :id) " +
           "AND (t.startTime < :endTime AND t.endTime > :startTime)")
    List<TimetableSlot> findTeacherConflicts(@Param("teacherId") String teacherId,
                                             @Param("weekday") String weekday,
                                             @Param("startTime") LocalTime startTime,
                                             @Param("endTime") LocalTime endTime,
                                             @Param("id") String id);

    // Room Conflict: Does this classroom have an overlapping slot on this weekday?
    @Query("SELECT t FROM TimetableSlot t WHERE LOWER(t.classroom) = LOWER(:classroom) AND t.weekday = :weekday " +
           "AND (:id IS NULL OR t.id <> :id) " +
           "AND (t.startTime < :endTime AND t.endTime > :startTime)")
    List<TimetableSlot> findRoomConflicts(@Param("classroom") String classroom,
                                          @Param("weekday") String weekday,
                                          @Param("startTime") LocalTime startTime,
                                          @Param("endTime") LocalTime endTime,
                                          @Param("id") String id);
}

