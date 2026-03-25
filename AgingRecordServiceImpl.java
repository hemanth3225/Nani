package com.claims.service;

import com.claims.dto.request.AgingRecordRequest;
import com.claims.dto.response.AgingRecordResponse;
import com.claims.enums.AgingBucket;
import com.claims.entity.AgingRecord;
import com.claims.exception.ResourceNotFoundException;
import com.claims.repository.AgingRecordRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AgingRecordServiceImpl implements AgingRecordService {

    private final AgingRecordRepository agingRecordRepository;
    private final ModelMapper modelMapper;

    @Autowired
    public AgingRecordServiceImpl(AgingRecordRepository agingRecordRepository, ModelMapper modelMapper) {
        this.agingRecordRepository = agingRecordRepository;
        this.modelMapper = modelMapper;
    }

    // -------------------------------------------------------
    // CRUD Operations
    // -------------------------------------------------------

    @Override
    public AgingRecordResponse createAgingRecord(AgingRecordRequest request) {
        AgingRecord record = modelMapper.map(request, AgingRecord.class);
        record.setAgingBucket(deriveBucket(request.getAgingDays()));
        AgingRecord saved = agingRecordRepository.save(record);
        return modelMapper.map(saved, AgingRecordResponse.class);
    }

    @Override
    public AgingRecordResponse getAgingRecordById(Long agingId) {
        AgingRecord record = findAgingByIdOrThrow(agingId);
        return modelMapper.map(record, AgingRecordResponse.class);
    }

    @Override
    public List<AgingRecordResponse> getAllAgingRecords() {
        return agingRecordRepository.findAll()
                .stream()
                .map(record -> modelMapper.map(record, AgingRecordResponse.class))
                .collect(Collectors.toList());
    }

    @Override
    public AgingRecordResponse updateAgingRecord(Long agingId, AgingRecordRequest request) {
        AgingRecord existing = findAgingByIdOrThrow(agingId);
        modelMapper.map(request, existing);
        existing.setAgingBucket(deriveBucket(request.getAgingDays()));
        AgingRecord updated = agingRecordRepository.save(existing);
        return modelMapper.map(updated, AgingRecordResponse.class);
    }

    @Override
    public void deleteAgingRecord(Long agingId) {
        AgingRecord existing = findAgingByIdOrThrow(agingId);
        agingRecordRepository.delete(existing);
    }

    // -------------------------------------------------------
    // Query Operations
    // -------------------------------------------------------

    @Override
    public List<AgingRecordResponse> getAgingRecordsByClaimId(String claimId) {
        List<AgingRecord> records = agingRecordRepository.findByClaimId(claimId);
        if (records.isEmpty()) {
            throw new ResourceNotFoundException("No aging records found for claimId: " + claimId);
        }
        return records.stream()
                .map(record -> modelMapper.map(record, AgingRecordResponse.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<AgingRecordResponse> getAgingRecordsByBucket(AgingBucket agingBucket) {
        List<AgingRecord> records = agingRecordRepository.findByAgingBucket(agingBucket);
        if (records.isEmpty()) {
            throw new ResourceNotFoundException("No aging records found for bucket: " + agingBucket);
        }
        return records.stream()
                .map(record -> modelMapper.map(record, AgingRecordResponse.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<AgingRecordResponse> getClaimsAgedBeyond(Integer days) {
        if (days < 0) {
            throw new IllegalArgumentException("Days threshold cannot be negative");
        }
        List<AgingRecord> records = agingRecordRepository.findByAgingDaysGreaterThan(days);
        if (records.isEmpty()) {
            throw new ResourceNotFoundException("No claims found aged beyond " + days + " days");
        }
        return records.stream()
                .map(record -> modelMapper.map(record, AgingRecordResponse.class))
                .collect(Collectors.toList());
    }

    // -------------------------------------------------------
    // Analytics
    // -------------------------------------------------------

    @Override
    public Map<String, Long> getAgingBucketDistribution() {
        List<Object[]> results = agingRecordRepository.countByAgingBucket();
        if (results.isEmpty()) {
            throw new ResourceNotFoundException("No aging records found in the system");
        }
        Map<String, Long> distribution = new LinkedHashMap<>();
        for (Object[] row : results) {
            AgingBucket bucket = (AgingBucket) row[0];
            Long count = (Long) row[1];
            distribution.put(bucket.name(), count);
        }
        return distribution;
    }

    @Override
    public Map<String, Object> getAgingSummaryForClaim(String claimId) {
        List<AgingRecord> records = agingRecordRepository.findByClaimId(claimId);
        if (records.isEmpty()) {
            throw new ResourceNotFoundException("No aging records found for claimId: " + claimId);
        }
        AgingRecord mostAged = records.stream()
                .max((a, b) -> Integer.compare(a.getAgingDays(), b.getAgingDays()))
                .orElseThrow();

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("claimId", claimId);
        summary.put("agingDays", mostAged.getAgingDays());
        summary.put("agingBucket", mostAged.getAgingBucket().name());
        summary.put("escalationRequired", mostAged.getAgingDays() > 60);
        return summary;
    }

    @Override
    public Map<String, Object> getPortfolioAgingHealth() {
        List<AgingRecord> allRecords = agingRecordRepository.findAll();
        if (allRecords.isEmpty()) {
            throw new ResourceNotFoundException("No aging records found in the system");
        }
        long totalClaims = allRecords.size();
        double averageAgingDays = allRecords.stream()
                .mapToInt(AgingRecord::getAgingDays)
                .average()
                .orElse(0.0);
        long criticalClaims = allRecords.stream()
                .filter(r -> r.getAgingBucket() == AgingBucket.BUCKET_90_PLUS)
                .count();
        Map<String, String> bucketPercentages = new LinkedHashMap<>();
        for (AgingBucket bucket : AgingBucket.values()) {
            long count = allRecords.stream()
                    .filter(r -> r.getAgingBucket() == bucket)
                    .count();
            double percentage = (count * 100.0) / totalClaims;
            bucketPercentages.put(bucket.name(), String.format("%.1f%%", percentage));
        }
        Map<String, Object> health = new LinkedHashMap<>();
        health.put("totalClaims", totalClaims);
        health.put("averageAgingDays", String.format("%.1f", averageAgingDays));
        health.put("criticalClaims_90Plus", criticalClaims);
        health.put("bucketDistributionPercentage", bucketPercentages);
        return health;
    }

    // -------------------------------------------------------
    // Private Helpers
    // -------------------------------------------------------

    private AgingBucket deriveBucket(int agingDays) {
        if (agingDays <= 30) return AgingBucket.BUCKET_0_30;
        else if (agingDays <= 60) return AgingBucket.BUCKET_31_60;
        else if (agingDays <= 90) return AgingBucket.BUCKET_61_90;
        else return AgingBucket.BUCKET_90_PLUS;
    }

    private AgingRecord findAgingByIdOrThrow(Long agingId) {
        return agingRecordRepository.findById(agingId)
                .orElseThrow(() -> new ResourceNotFoundException("AgingRecord", "agingId", agingId));
    }
}

