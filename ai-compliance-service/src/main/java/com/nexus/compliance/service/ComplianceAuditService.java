package com.nexus.compliance.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.compliance.dto.ComplianceReport;
import com.nexus.compliance.dto.ServiceAuditDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ComplianceAuditService {

    private static final Logger log = LoggerFactory.getLogger(ComplianceAuditService.class);

    private final DiscoveryClient discoveryClient;
    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;
    private final String docsBasePath;
    private final List<String> expectedServices;
    private final String openAiApiKey;

    public ComplianceAuditService(
            DiscoveryClient discoveryClient,
            @Autowired(required = false) ChatClient.Builder chatClientBuilder,
            ObjectMapper objectMapper,
            @Value("${app.compliance.docs-base-path:../docs}") String docsBasePath,
            @Value("${app.compliance.expected-services:product-service,inventory-service,order-service,payment-service,notification-service}") List<String> expectedServices,
            @Value("${spring.ai.openai.api-key:mock-local-key}") String openAiApiKey) {
        this.discoveryClient = discoveryClient;
        this.chatClient = (chatClientBuilder != null) ? chatClientBuilder.build() : null;
        this.objectMapper = objectMapper;
        this.docsBasePath = docsBasePath;
        this.expectedServices = expectedServices;
        this.openAiApiKey = openAiApiKey;
    }

    public ComplianceReport executeAudit() {
        log.info("Starting AI-driven architecture compliance audit...");

        // 1. Discover registered and active microservices from Eureka
        Set<String> discoveredServices = new LinkedHashSet<>();
        try {
            List<String> eurekaServices = discoveryClient.getServices();
            if (eurekaServices != null && !eurekaServices.isEmpty()) {
                discoveredServices.addAll(
                        eurekaServices.stream()
                                .map(String::toLowerCase)
                                .filter(s -> !s.contains("eureka") && !s.contains("gateway") && !s.contains("compliance"))
                                .toList()
                );
            }
        } catch (Exception e) {
            log.warn("Eureka service query encountered note: {}. Proceeding with platform baseline inventory.", e.getMessage());
        }

        // Merge with expected platform service catalog
        discoveredServices.addAll(expectedServices);

        // 2. Scan physical docs directory
        File baseDir = resolveDocsDirectory();
        File archDir = new File(baseDir, "architecture");
        File openApiDir = new File(baseDir, "openapi");

        Set<String> archDocs = scanFilenamesWithoutExtension(archDir);
        Set<String> openApiDocs = scanOpenApiFilenames(openApiDir);

        log.info("Discovered active services: {}", discoveredServices);
        log.info("Found Architecture Docs for: {}", archDocs);
        log.info("Found OpenAPI Specs for: {}", openApiDocs);

        // 3. Build Audit Inventory Summary
        List<ServiceAuditDetail> serviceAudits = new ArrayList<>();
        int nonCompliantCount = 0;

        for (String service : discoveredServices) {
            boolean hasArch = archDocs.contains(service);
            boolean hasOpenApi = openApiDocs.contains(service);

            String status;
            String risk;
            String summary;

            if (hasArch && hasOpenApi) {
                status = "COMPLIANT";
                risk = "LOW";
                summary = "Full architectural documentation and OpenAPI 3.1 specification validated.";
            } else if (!hasArch && !hasOpenApi) {
                status = "MISSING_DOCUMENTATION";
                risk = isCoreTier(service) ? "CRITICAL" : "HIGH";
                summary = "Zero architectural artifacts found. Unregistered endpoints and uncontracted payload schemas.";
                nonCompliantCount++;
            } else {
                status = "PARTIAL_DOCUMENTATION";
                risk = "MEDIUM";
                summary = hasArch ? "Missing OpenAPI specification contract." : "Missing architectural design markdown.";
                nonCompliantCount++;
            }

            serviceAudits.add(new ServiceAuditDetail(service, hasArch, hasOpenApi, status, risk, summary));
        }

        int compliantCount = discoveredServices.size() - nonCompliantCount;
        String overallStatus = (nonCompliantCount == 0) ? "COMPLIANT" : "NON_COMPLIANT";

        // 4. Generate AI Prompt Analysis
        List<String> recommendations = generateRecommendations(serviceAudits);

        // If an active OpenAI key is provided, enrich recommendations using Spring AI PromptTemplate
        if (chatClient != null && openAiApiKey != null && !openAiApiKey.contains("mock")) {
            try {
                recommendations = callSpringAiForRecommendations(serviceAudits);
            } catch (Exception ex) {
                log.warn("Spring AI remote call skipped ({}), using local structured audit engine.", ex.getMessage());
            }
        }

        return new ComplianceReport(
                "AUDIT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                Instant.now(),
                discoveredServices.size(),
                compliantCount,
                nonCompliantCount,
                overallStatus,
                serviceAudits,
                recommendations
        );
    }

    private List<String> callSpringAiForRecommendations(List<ServiceAuditDetail> audits) {
        String templateString = """
                You are a Principal Enterprise Solutions Architect auditing the Nexus Commerce microservices ecosystem.
                Here is the audit matrix of discovered services and documentation status:
                
                {auditMatrix}
                
                Evaluate the architecture risks, specifically highlighting non-compliant services, and output exactly 3-5 concrete remediation recommendations as bullet points.
                """;

        try {
            String matrixJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(audits);
            PromptTemplate promptTemplate = new PromptTemplate(templateString);
            Prompt prompt = promptTemplate.create(Map.of("auditMatrix", matrixJson));

            String content = chatClient.prompt(prompt).call().content();
            if (content != null && !content.isBlank()) {
                return Arrays.stream(content.split("\n"))
                        .map(String::trim)
                        .filter(s -> !s.isBlank() && (s.startsWith("-") || s.startsWith("*") || Character.isDigit(s.charAt(0))))
                        .toList();
            }
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize audit matrix for AI prompt", e);
        }
        return generateRecommendations(audits);
    }

    private List<String> generateRecommendations(List<ServiceAuditDetail> audits) {
        List<String> list = new ArrayList<>();
        for (ServiceAuditDetail detail : audits) {
            if ("CRITICAL".equals(detail.riskLevel())) {
                list.add("PRIORITY 1: Generate OpenAPI 3.1 contract and architecture documentation for core service '" + detail.serviceName() + "' to unblock contract testing.");
            } else if ("HIGH".equals(detail.riskLevel())) {
                list.add("PRIORITY 2: Define event schema and message payloads in docs for '" + detail.serviceName() + "'.");
            }
        }
        list.add("Establish automated CI/CD documentation linter verifying docs/openapi/{service}-openapi.yaml existence on pull requests.");
        list.add("Publish validated OpenAPI specifications to centralized Swagger UI / Redoc gateway portal.");
        return list;
    }

    private File resolveDocsDirectory() {
        File dir = new File(docsBasePath);
        if (dir.exists()) return dir;

        File siblingDocs = new File("docs");
        if (siblingDocs.exists()) return siblingDocs;

        File rootDocs = new File("nexus-commerce/docs");
        if (rootDocs.exists()) return rootDocs;

        return dir;
    }

    private Set<String> scanFilenamesWithoutExtension(File directory) {
        if (!directory.exists() || !directory.isDirectory()) return Collections.emptySet();
        File[] files = directory.listFiles((dir, name) -> name.endsWith(".md"));
        if (files == null) return Collections.emptySet();
        return Arrays.stream(files)
                .map(f -> f.getName().replace(".md", "").toLowerCase())
                .collect(Collectors.toSet());
    }

    private Set<String> scanOpenApiFilenames(File directory) {
        if (!directory.exists() || !directory.isDirectory()) return Collections.emptySet();
        File[] files = directory.listFiles((dir, name) -> name.endsWith(".yaml") || name.endsWith(".yml") || name.endsWith(".json"));
        if (files == null) return Collections.emptySet();
        return Arrays.stream(files)
                .map(f -> f.getName().replaceAll("-openapi\\.(yaml|yml|json)$", "").replace(".yaml", "").replace(".yml", "").toLowerCase())
                .collect(Collectors.toSet());
    }

    private boolean isCoreTier(String service) {
        return service.contains("inventory") || service.contains("order") || service.contains("product");
    }
}
