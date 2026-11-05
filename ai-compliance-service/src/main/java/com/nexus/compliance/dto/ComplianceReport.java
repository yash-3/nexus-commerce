package com.nexus.compliance.dto;

import java.time.Instant;
import java.util.List;

public record ComplianceReport(
    String auditId,
    Instant timestamp,
    int totalServicesEvaluated,
    int compliantServicesCount,
    int nonCompliantServicesCount,
    String overallStatus,
    List<ServiceAuditDetail> serviceAudits,
    List<String> architecturalRecommendations
) {}
