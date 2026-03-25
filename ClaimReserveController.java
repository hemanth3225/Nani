package com.claims.controller;

import com.claims.dto.request.ClaimReserveRequest;
import com.claims.dto.response.ClaimReserveResponse;
import com.claims.service.ClaimReserveServiceImpl;
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
@RequestMapping("/api/reserves")
@RequiredArgsConstructor
public class ClaimReserveController {

    private final ClaimReserveServiceImpl claimReserveServiceImpl;

    // -------------------------------------------------------
    // CRUD Endpoints
    // -------------------------------------------------------

    /**
     * POST /api/reserves
     * Create a new reserve entry for a claim.
     */
    @PostMapping
    public ResponseEntity<ClaimReserveResponse> createClaimReserve(
            @Valid @RequestBody ClaimReserveRequest request) {
        ClaimReserveResponse response = claimReserveServiceImpl.createClaimReserve(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * GET /api/reserves/{reserveId}
     * Get a single reserve record by ID.
     */
    @GetMapping("/{reserveId}")
    public ResponseEntity<ClaimReserveResponse> getClaimReserveById(@PathVariable Long reserveId) {
        return ResponseEntity.ok(claimReserveServiceImpl.getClaimReserveById(reserveId));
    }

    /**
     * GET /api/reserves
     * Get all reserve records in the system.
     */
    @GetMapping
    public ResponseEntity<List<ClaimReserveResponse>> getAllClaimReserves() {
        return ResponseEntity.ok(claimReserveServiceImpl.getAllClaimReserves());
    }

    /**
     * PUT /api/reserves/{reserveId}
     * Update an existing reserve record.
     */
    @PutMapping("/{reserveId}")
    public ResponseEntity<ClaimReserveResponse> updateClaimReserve(
            @PathVariable Long reserveId,
            @Valid @RequestBody ClaimReserveRequest request) {
        return ResponseEntity.ok(claimReserveServiceImpl.updateClaimReserve(reserveId, request));
    }

    /**
     * DELETE /api/reserves/{reserveId}
     * Delete a reserve record by ID.
     */
    @DeleteMapping("/{reserveId}")
    public ResponseEntity<Map<String, String>> deleteClaimReserve(@PathVariable Long reserveId) {
        claimReserveServiceImpl.deleteClaimReserve(reserveId);
        return ResponseEntity.ok(Map.of(
                "message", "ClaimReserve deleted successfully",
                "reserveId", String.valueOf(reserveId)
        ));
    }

    // -------------------------------------------------------
    // Query Endpoints
    // -------------------------------------------------------

    /**
     * GET /api/reserves/claim/{claimId}
     * Get all reserve records for a specific claim.
     */
    @GetMapping("/claim/{claimId}")
    public ResponseEntity<List<ClaimReserveResponse>> getReservesByClaimId(
            @PathVariable String claimId) {
        return ResponseEntity.ok(claimReserveServiceImpl.getReservesByClaimId(claimId));
    }

    /**
     * GET /api/reserves/date-range?startDate=2024-01-01&endDate=2024-06-30
     * Get all reserves updated within a date range.
     */
    @GetMapping("/date-range")
    public ResponseEntity<List<ClaimReserveResponse>> getReservesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(claimReserveServiceImpl.getReservesByDateRange(startDate, endDate));
    }

    // -------------------------------------------------------
    // Analytics Endpoints
    // -------------------------------------------------------

    /**
     * GET /api/reserves/claim/{claimId}/latest
     * Get the most recent reserve entry for a specific claim.
     */
    @GetMapping("/claim/{claimId}/latest")
    public ResponseEntity<ClaimReserveResponse> getLatestReserveForClaim(
            @PathVariable String claimId) {
        return ResponseEntity.ok(claimReserveServiceImpl.getLatestReserveForClaim(claimId));
    }

    /**
     * GET /api/reserves/analytics/total
     * Get the total reserve amount across all claims in the system.
     */
    @GetMapping("/analytics/total")
    public ResponseEntity<Map<String, Object>> getTotalReserveAmount() {
        BigDecimal total = claimReserveServiceImpl.getTotalReserveAmount();
        return ResponseEntity.ok(Map.of("totalReserveAmount", total));
    }

    /**
     * GET /api/reserves/claim/{claimId}/history
     * Get the full reserve history for a claim sorted chronologically.
     */
    @GetMapping("/claim/{claimId}/history")
    public ResponseEntity<List<ClaimReserveResponse>> getReserveHistory(
            @PathVariable String claimId) {
        return ResponseEntity.ok(claimReserveServiceImpl.getReserveHistoryByClaimId(claimId));
    }

    /**
     * GET /api/reserves/analytics/summary
     * Get the latest reserve amount per claim across all claims.
     */
    @GetMapping("/analytics/summary")
    public ResponseEntity<Map<String, BigDecimal>> getLatestReserveSummaryAllClaims() {
        return ResponseEntity.ok(claimReserveServiceImpl.getLatestReserveSummaryAllClaims());
    }

    /**
     * GET /api/reserves/claim/{claimId}/trend/monthly
     * Get the month-over-month reserve trend for a specific claim.
     */
    @GetMapping("/claim/{claimId}/trend/monthly")
    public ResponseEntity<Map<String, BigDecimal>> getMonthlyReserveTrend(
            @PathVariable String claimId) {
        return ResponseEntity.ok(claimReserveServiceImpl.getMonthlyReserveTrendByClaimId(claimId));
    }

    /**
     * GET /api/reserves/claim/{claimId}/adequacy?totalCost=12000.00
     * Check if the reserve is adequate compared to the total cost incurred.
     * Returns adequacy status: ADEQUATE or UNDER_RESERVED.
     */
    @GetMapping("/claim/{claimId}/adequacy")
    public ResponseEntity<Map<String, Object>> getReserveAdequacy(
            @PathVariable String claimId,
            @RequestParam BigDecimal totalCost) {
        return ResponseEntity.ok(claimReserveServiceImpl.getReserveAdequacyForClaim(claimId, totalCost));
    }
}