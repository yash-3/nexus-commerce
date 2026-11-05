package com.nexus.compliance.dto;

public record ServiceAuditDetail(
    String serviceName,
    boolean architectureDocFound,
    boolean openApiSpecFound,
    String complianceStatus, // COMPLIANT, PARTIAL_DOCUMENTATION, MISSING_DOCUMENTATION
    String riskLevel,        // LOW, MEDIUM, HIGH, CRITICAL
    String summaryAnalysis
) {}
