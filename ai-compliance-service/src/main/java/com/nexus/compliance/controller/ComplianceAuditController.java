package com.nexus.compliance.controller;

import com.nexus.compliance.dto.ComplianceReport;
import com.nexus.compliance.service.ComplianceAuditService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/compliance")
public class ComplianceAuditController {

    private final ComplianceAuditService complianceAuditService;

    public ComplianceAuditController(ComplianceAuditService complianceAuditService) {
        this.complianceAuditService = complianceAuditService;
    }

    @PostMapping("/audit")
    public ResponseEntity<ComplianceReport> executeAudit() {
        ComplianceReport report = complianceAuditService.executeAudit();
        return ResponseEntity.ok(report);
    }

    @GetMapping("/audit")
    public ResponseEntity<ComplianceReport> getAuditStatus() {
        return ResponseEntity.ok(complianceAuditService.executeAudit());
    }
}
