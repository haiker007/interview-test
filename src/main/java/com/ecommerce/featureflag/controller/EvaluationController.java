package com.ecommerce.featureflag.controller;

import com.ecommerce.featureflag.dto.EvaluationRequest;
import com.ecommerce.featureflag.dto.EvaluationResponse;
import com.ecommerce.featureflag.model.EvaluationContext;
import com.ecommerce.featureflag.model.EvaluationResult;
import com.ecommerce.featureflag.service.FlagEvaluationService;
import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for flag evaluation endpoints.
 */
@RestController
@RequestMapping("/api/v1/flags")
@Validated
public class EvaluationController {

    private final FlagEvaluationService evaluationService;

    public EvaluationController(FlagEvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }

    /**
     * Evaluate a single flag.
     * GET /api/v1/flags/{flagKey}/evaluate?userId=xxx
     */
    @GetMapping("/{flagKey}/evaluate")
    @Counted(value = "flag.evaluation.requests", extraTags = {"endpoint", "single"})
    @Timed(value = "flag.evaluation.duration", histogram = true)
    public ResponseEntity<EvaluationResult> evaluateFlag(
            @PathVariable @NotBlank @Size(max = 128) String flagKey,
            @RequestParam(required = false) @Size(max = 128) String userId,
            @RequestParam(required = false) @Size(max = 128) String sessionId,
            @RequestParam(required = false) @Size(max = 64) String environment) {

        EvaluationContext context = buildContext(userId, sessionId, environment);
        EvaluationResult result = evaluationService.evaluate(flagKey, context);
        return ResponseEntity.ok(result);
    }

    /**
     * Evaluate multiple flags in batch.
     * POST /api/v1/flags/evaluate
     */
    @PostMapping("/evaluate")
    @Counted(value = "flag.evaluation.requests", extraTags = {"endpoint", "batch"})
    @Timed(value = "flag.evaluation.duration", histogram = true)
    public ResponseEntity<EvaluationResponse> evaluateFlags(@Valid @RequestBody EvaluationRequest request) {
        EvaluationContext context = buildContextFromRequest(request.getContext());
        List<EvaluationResult> results = evaluationService.evaluateAll(request.getFlags(), context);

        EvaluationResponse response = EvaluationResponse.builder()
                .results(results)
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Get detailed explanation for a flag evaluation.
     * GET /api/v1/flags/{flagKey}/explain?userId=xxx
     */
    @GetMapping("/{flagKey}/explain")
    @Counted(value = "flag.evaluation.requests", extraTags = {"endpoint", "explain"})
    @Timed(value = "flag.evaluation.duration", histogram = true)
    public ResponseEntity<EvaluationResult> explainFlag(
            @PathVariable @NotBlank @Size(max = 128) String flagKey,
            @RequestParam(required = false) @Size(max = 128) String userId,
            @RequestParam(required = false) @Size(max = 128) String sessionId,
            @RequestParam(required = false) @Size(max = 64) String environment,
            @RequestParam(name = "explain", required = false, defaultValue = "true") boolean explain) {

        EvaluationContext context = buildContext(userId, sessionId, environment);
        EvaluationResult result = evaluationService.evaluateWithExplanation(flagKey, context, explain);
        return ResponseEntity.ok(result);
    }

    private EvaluationContext buildContext(String userId, String sessionId, String environment) {
        return EvaluationContext.builder()
                .userId(userId)
                .sessionId(sessionId)
                .environment(environment)
                .build();
    }

    private EvaluationContext buildContextFromRequest(EvaluationRequest.Context ctx) {
        if (ctx == null) {
            return EvaluationContext.builder().build();
        }
        return EvaluationContext.builder()
                .userId(ctx.getUserId())
                .attributes(ctx.getAttributes())
                .sessionId(ctx.getSessionId())
                .environment(ctx.getEnvironment())
                .build();
    }
}
