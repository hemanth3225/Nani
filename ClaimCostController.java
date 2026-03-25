package com.claims.controller;

import com.claims.dto.request.ClaimCostRequest;
import com.claims.dto.response.ClaimCostResponse;
import com.claims.enums.CostType;
import com.claims.service.ClaimCostServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/costs")
@RequiredArgsConstructor
public class ClaimCostController {

    private final ClaimCostServiceImpl claimCostServiceImpl;

    // -------------------------------------------------------
    // CRUD Endpoints
    // -------------------------------------------------------

    /**
     * POST /api/costs
     * Create a new claim cost entry.
     */
    @PostMapping
    public ResponseEntity<ClaimCostResponse> createClaimCost(
            @Valid @RequestBody ClaimCostRequest request) {
        ClaimCostResponse response = claimCostServiceImpl.createClaimCost(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * GET /api/costs/{costId}
     * Get a single cost record by ID.
     */
    @GetMapping("/{costId}")
    public ResponseEntity<ClaimCostResponse> getClaimCostById(@PathVariable Long costId) {
        return ResponseEntity.ok(claimCostServiceImpl.getClaimCostById(costId));
    }

    /**
     * GET /api/costs
     * Get all cost records in the system.
     */
    @GetMapping
    public ResponseEntity<List<ClaimCostResponse>> getAllClaimCosts() {
        return ResponseEntity.ok(claimCostServiceImpl.getAllClaimCosts());
    }

    /**
     * PUT /api/costs/{costId}
     * Update an existing cost record.
     */
    @PutMapping("/{costId}")
    public ResponseEntity<ClaimCostResponse> updateClaimCost(
            @PathVariable Long costId,
            @Valid @RequestBody ClaimCostRequest request) {
        return ResponseEntity.ok(claimCostServiceImpl.updateClaimCost(costId, request));
    }

    /**
     * DELETE /api/costs/{costId}
     * Delete a cost record by ID.
     */
    @DeleteMapping("/{costId}")
    public ResponseEntity<Map<String, String>> deleteClaimCost(@PathVariable Long costId) {
        claimCostServiceImpl.deleteClaimCost(costId);
        return ResponseEntity.ok(Map.of(
                "message", "ClaimCost deleted successfully",
                "costId", String.valueOf(costId)
        ));
    }

    // -------------------------------------------------------
    // Query Endpoints
    // -------------------------------------------------------

    /**
     * GET /api/costs/claim/{claimId}
     * Get all costs recorded for a specific claim.
     */
    @GetMapping("/claim/{claimId}")
    public ResponseEntity<List<ClaimCostResponse>> getCostsByClaimId(@PathVariable String claimId) {
        return ResponseEntity.ok(claimCostServiceImpl.getCostsByClaimId(claimId));
    }

    /**
     * GET /api/costs/type/{costType}
     * Get all costs filtered by cost type (MEDICAL / LEGAL / REPAIR / SETTLEMENT).
     */
    @GetMapping("/type/{costType}")
    public ResponseEntity<List<ClaimCostResponse>> getCostsByCostType(
            @PathVariable CostType costType) {
        return ResponseEntity.ok(claimCostServiceImpl.getCostsByCostType(costType));
    }

    /**
     * GET /api/costs/claim/{claimId}/type/{costType}
     * Get all costs for a specific claim filtered by cost type.
     */
    @GetMapping("/claim/{claimId}/type/{costType}")
    public ResponseEntity<List<ClaimCostResponse>> getCostsByClaimIdAndType(
            @PathVariable String claimId,
            @PathVariable CostType costType) {
        return ResponseEntity.ok(claimCostServiceImpl.getCostsByClaimIdAndType(claimId, costType));
    }

    /**
     * GET /api/costs/date-range?startDate=2024-01-01&endDate=2024-06-30
     * Get all costs recorded within a date range.
     */
    @GetMapping("/date-range")
    public ResponseEntity<List<ClaimCostResponse>> getCostsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(claimCostServiceImpl.getCostsByDateRange(startDate, endDate));
    }

    // -------------------------------------------------------
    // Analytics Endpoints
    // -------------------------------------------------------

    /**
     * GET /api/costs/claim/{claimId}/total
     * Get the total cost amount for a specific claim.
     */
    @GetMapping("/claim/{claimId}/total")
    public ResponseEntity<Map<String, Object>> getTotalCostByClaimId(@PathVariable String claimId) {
        BigDecimal total = claimCostServiceImpl.getTotalCostByClaimId(claimId);
        return ResponseEntity.ok(Map.of(
                "claimId", claimId,
                "totalCost", total
        ));
    }

    /**
     * GET /api/costs/claim/{claimId}/breakdown
     * Get cost breakdown by type for a specific claim.
     * Example: { "MEDICAL": 5000.00, "LEGAL": 2000.00 }
     */
    @GetMapping("/claim/{claimId}/breakdown")
    public ResponseEntity<Map<String, BigDecimal>> getCostBreakdownByType(
            @PathVariable String claimId) {
        return ResponseEntity.ok(claimCostServiceImpl.getCostBreakdownByTypeForClaim(claimId));
    }

    /**
     * GET /api/costs/summary/by-type
     * Get total costs grouped by cost type across ALL claims.
     */
    @GetMapping("/summary/by-type")
    public ResponseEntity<Map<String, BigDecimal>> getOverallCostSummaryByType() {
        return ResponseEntity.ok(claimCostServiceImpl.getOverallCostSummaryByType());
    }

    /**
     * GET /api/costs/claim/{claimId}/trend/monthly
     * Get the monthly cost trend for a specific claim.
     */
    @GetMapping("/claim/{claimId}/trend/monthly")
    public ResponseEntity<Map<String, BigDecimal>> getMonthlyCostTrend(
            @PathVariable String claimId) {
        return ResponseEntity.ok(claimCostServiceImpl.getMonthlyCostTrendByClaimId(claimId));
    }

    /**
     * GET /api/costs/analytics/highest-cost-claim
     * Get the claim with the highest total cost.
     */
    @GetMapping("/analytics/highest-cost-claim")
    public ResponseEntity<Map<String, Object>> getHighestCostClaim() {
        return ResponseEntity.ok(claimCostServiceImpl.getHighestCostClaim());
    }
}

