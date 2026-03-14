package com.ecommerce.featureflag.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for batch flag evaluation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationRequest {
    @NotEmpty(message = "flags cannot be empty")
    @Size(max = 100, message = "maximum 100 flags allowed per request")
    private List<String> flags;

    @Valid
    private Context context;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Context {
        @Size(max = 128, message = "userId must not exceed 128 characters")
        private String userId;

        @Size(max = 50, message = "maximum 50 attributes allowed")
        private java.util.Map<String, String> attributes;

        @Size(max = 128, message = "sessionId must not exceed 128 characters")
        private String sessionId;

        @Size(max = 64, message = "environment must not exceed 64 characters")
        private String environment;
    }
}
