package com.ecommerce.featureflag.dto;

import com.ecommerce.featureflag.model.EvaluationResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for batch flag evaluation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationResponse {
    private List<EvaluationResult> results;
}
