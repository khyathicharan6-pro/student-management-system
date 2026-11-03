package com.sms.service;

import com.sms.dto.GradeScaleDto;
import com.sms.entity.GradeScale;
import com.sms.repository.GradeScaleRepository;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GradeService {

    private final GradeScaleRepository gradeScaleRepository;
    private final AuditService auditService;

    public List<GradeScale> getAllGradeScales() {
        return gradeScaleRepository.findAllByOrderByMinPercentageDesc();
    }

    public GradeEvaluation evaluateGrade(BigDecimal percentage) {
        List<GradeScale> scales = gradeScaleRepository.findAllByOrderByMinPercentageDesc();
        if (scales.isEmpty()) {
            return GradeEvaluation.builder()
                    .gradeLetter(percentage.compareTo(new BigDecimal(40)) >= 0 ? "Pass" : "Fail")
                    .gradePoint(new BigDecimal("0.0"))
                    .passing(percentage.compareTo(new BigDecimal(40)) >= 0)
                    .build();
        }

        for (GradeScale scale : scales) {
            if (percentage.compareTo(scale.getMinPercentage()) >= 0 &&
                percentage.compareTo(scale.getMaxPercentage()) <= 0) {
                return GradeEvaluation.builder()
                        .gradeLetter(scale.getGradeLetter())
                        .gradePoint(scale.getGradePoint())
                        .description(scale.getDescription())
                        .passing(scale.isPassing())
                        .build();
            }
        }

        // Below lowest scale
        GradeScale lowest = scales.get(scales.size() - 1);
        return GradeEvaluation.builder()
                .gradeLetter(lowest.getGradeLetter())
                .gradePoint(lowest.getGradePoint())
                .description(lowest.getDescription())
                .passing(lowest.isPassing())
                .build();
    }

    @Transactional
    public GradeScale saveGradeScale(GradeScaleDto dto, String actorId) {
        GradeScale scale;
        if (dto.getId() != null && !dto.getId().isBlank()) {
            scale = gradeScaleRepository.findById(dto.getId())
                    .orElseGet(GradeScale::new);
        } else {
            scale = new GradeScale();
        }

        scale.setName(dto.getName());
        scale.setMinPercentage(dto.getMinPercentage());
        scale.setMaxPercentage(dto.getMaxPercentage());
        scale.setGradeLetter(dto.getGradeLetter().trim().toUpperCase());
        scale.setGradePoint(dto.getGradePoint());
        scale.setDescription(dto.getDescription());
        scale.setPassing(dto.isPassing());

        gradeScaleRepository.save(scale);
        auditService.log(actorId, null, "SAVE_GRADE_SCALE", "grade_scales", scale.getId(), null, 
                "Letter: " + scale.getGradeLetter() + " (" + scale.getMinPercentage() + "-" + scale.getMaxPercentage() + "%)");
        return scale;
    }

    @Getter
    @Builder
    public static class GradeEvaluation {
        private String gradeLetter;
        private BigDecimal gradePoint;
        private String description;
        private boolean passing;
    }
}

