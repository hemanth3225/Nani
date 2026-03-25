package com.claims.controller;

import com.claims.dto.request.AgingRecordRequest;
import com.claims.dto.response.AgingRecordResponse;
import com.claims.enums.AgingBucket;
import com.claims.service.AgingRecordServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/aging")
@RequiredArgsConstructor
public class AgingRecordController {

    private final AgingRecordServiceImpl agingRecordServiceImpl;

    // -------------------------------------------------------
    // CRUD Endpoints
    // -------------------------------------------------------

    /**
     * POST /api/aging
     * Create a new aging record for a claim.
     * Note: AgingBucket is auto-derived from agingDays on the server side.
     */
    @PostMapping
    public ResponseEntity<AgingRecordResponse> createAgingRecord(
            @Valid @RequestBody AgingRecordRequest request) {
        AgingRecordResponse response = agingRecordServiceImpl.createAgingRecord(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * GET /api/aging/{agingId}
     * Get a single aging record by ID.
     */
    @GetMapping("/{agingId}")
    public ResponseEntity<AgingRecordResponse> getAgingRecordById(@PathVariable Long agingId) {
        return ResponseEntity.ok(agingRecordServiceImpl.getAgingRecordById(agingId));
    }

    /**
     * GET /api/aging
     * Get all aging records in the system.
     */
    @GetMapping
    public ResponseEntity<List<AgingRecordResponse>> getAllAgingRecords() {
        return ResponseEntity.ok(agingRecordServiceImpl.getAllAgingRecords());
    }

    /**
     * PUT /api/aging/{agingId}
     * Update an existing aging record.
     * Note: AgingBucket is re-derived automatically from the updated agingDays.
     */
    @PutMapping("/{agingId}")
    public ResponseEntity<AgingRecordResponse> updateAgingRecord(
            @PathVariable Long agingId,
            @Valid @RequestBody AgingRecordRequest request) {
        return ResponseEntity.ok(agingRecordServiceImpl.updateAgingRecord(agingId, request));
    }

    /**
     * DELETE /api/aging/{agingId}
     * Delete an aging record by ID.
     */
    @DeleteMapping("/{agingId}")
    public ResponseEntity<Map<String, String>> deleteAgingRecord(@PathVariable Long agingId) {
        agingRecordServiceImpl.deleteAgingRecord(agingId);
        return ResponseEntity.ok(Map.of(
                "message", "AgingRecord deleted successfully",
                "agingId", String.valueOf(agingId)
        ));
    }

    // -------------------------------------------------------
    // Query Endpoints
    // -------------------------------------------------------

    /**
     * GET /api/aging/claim/{claimId}
     * Get all aging records for a specific claim.
     */
    @GetMapping("/claim/{claimId}")
    public ResponseEntity<List<AgingRecordResponse>> getAgingRecordsByClaimId(
            @PathVariable String claimId) {
        return ResponseEntity.ok(agingRecordServiceImpl.getAgingRecordsByClaimId(claimId));
    }

    /**
     * GET /api/aging/bucket/{agingBucket}
     * Get all claims in a specific aging bucket.
     * Valid values: BUCKET_0_30 / BUCKET_31_60 / BUCKET_61_90 / BUCKET_90_PLUS
     */
    @GetMapping("/bucket/{agingBucket}")
    public ResponseEntity<List<AgingRecordResponse>> getAgingRecordsByBucket(
            @PathVariable AgingBucket agingBucket) {
        return ResponseEntity.ok(agingRecordServiceImpl.getAgingRecordsByBucket(agingBucket));
    }

    /**
     * GET /api/aging/escalation?days=60
     * Get all claims aged beyond a specified number of days.
     * Default threshold for escalation is typically 60 days.
     */
    @GetMapping("/escalation")
    public ResponseEntity<List<AgingRecordResponse>> getClaimsAgedBeyond(
            @RequestParam Integer days) {
        return ResponseEntity.ok(agingRecordServiceImpl.getClaimsAgedBeyond(days));
    }

    // -------------------------------------------------------
    // Analytics Endpoints
    // -------------------------------------------------------

    /**
     * GET /api/aging/analytics/distribution
     * Get count of claims in each aging bucket.
     * Example: { "BUCKET_0_30": 120, "BUCKET_31_60": 45 }
     */
    @GetMapping("/analytics/distribution")
    public ResponseEntity<Map<String, Long>> getAgingBucketDistribution() {
        return ResponseEntity.ok(agingRecordServiceImpl.getAgingBucketDistribution());
    }

    /**
     * GET /api/aging/claim/{claimId}/summary
     * Get the aging summary for a specific claim.
     * Includes days aged, bucket, and whether escalation is required.
     */
    @GetMapping("/claim/{claimId}/summary")
    public ResponseEntity<Map<String, Object>> getAgingSummaryForClaim(
            @PathVariable String claimId) {
        return ResponseEntity.ok(agingRecordServiceImpl.getAgingSummaryForClaim(claimId));
    }

    /**
     * GET /api/aging/analytics/portfolio-health
     * Get the overall aging health of the entire   portfolio.
     * Includes total claims, average aging days, critical claims count,
     * and bucket distribution percentages. Ideal for executive dashboards.
     */
    @GetMapping("/analytics/portfolio-health")
    public ResponseEntity<Map<String, Object>> getPortfolioAgingHealth() {
        return ResponseEntity.ok(agingRecordServiceImpl.getPortfolioAgingHealth());
    }
}
