package com.sms.repository;

import com.sms.entity.GradeScale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GradeScaleRepository extends JpaRepository<GradeScale, String> {
    List<GradeScale> findAllByOrderByMinPercentageDesc();
}

