package com.securevault.validation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.securevault.dto.ApiResponse;
import com.securevault.dto.LoginRequest;
import com.securevault.dto.RegisterRequest;
import com.securevault.dto.CreateCredentialRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureTestMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive API consistency validation test that generates a detailed report
 * of all endpoint validations and their compliance with production standards.
 * 
 * This test validates:
 * 1. ApiResponse wrapper consistency across all endpoints
 * 2. HTTP status code correctness
 * 3. Error message consistency and user-friendliness
 * 4. Validation error handling completeness
 * 5. Authentication/authorization behavior
 * 6. Response structure standardization
 */
@SpringBootTest
@AutoConfigureTestMvc
@ActiveProfiles("test")
@Transactional
public class ApiConsistencyReportTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String validJwtToken;
    private final List<ValidationResult> validationResults = new ArrayList<>();

    @BeforeEach
    void setUp() throws Exception {
        // Set up test user and JWT token
        RegisterRequest registerRequest = new RegisterRequest("Report User", "report@test.com", "password123");
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)));

        LoginRequest loginRequest = new LoginRequest("report@test.com", "password123");
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpected(status().isOk())
                .andReturn();

        String loginResponse = loginResult.getResponse().getContentAsString();
        ApiResponse<?> apiResponse = objectMapper.readValue(loginResponse, ApiResponse.class);
        
        @SuppressWarnings("unchecked")
        var loginData = (java.util.Map<String, Object>) apiResponse.getData();
        validJwtToken = "Bearer " + loginData.get("token");
    }

    @Test
    @DisplayName("Generate comprehensive API validation report")
    void generateApiValidationReport() throws Exception {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("SECUREVAULT API CONSISTENCY VALIDATION REPORT");
        System.out.println("=".repeat(80));
        System.out.println("Generated: " + java.time.LocalDateTime.now());
        System.out.println();

        // Test all authentication endpoints
        testAuthenticationEndpoints();
        
        // Test all credential management endpoints
        testCredentialEndpoints();
        
        // Test all password utility endpoints
        testPasswordUtilityEndpoints();
        
        // Test error handling consistency
        testErrorHandlingConsistency();

        // Generate final report
        generateFinalReport();
        
        // Ensure all tests passed
        long failedTests = validationResults.stream()
                .mapToLong(r -> r.assertions.stream()
                        .mapToLong(a -> a.passed ? 0 : 1)
                        .sum())
                .sum();
        
        assertEquals(0, failedTests, "Some API consistency validations failed. Check report above.");
    }

    private void testAuthenticationEndpoints() throws Exception {
        System.out.println("1. AUTHENTICATION ENDPOINTS");
        System.out.println("-".repeat(40));

        // Test user registration
        testEndpoint("POST /api/auth/register", "User Registration", () -> {
            RegisterRequest request = new RegisterRequest("New User", "new@test.com", "password123");
            return mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));
        }, 201, true);

        // Test user registration validation
        testEndpoint("POST /api/auth/register (validation)", "User Registration Validation", () -> {
            RegisterRequest request = new RegisterRequest("", "invalid", "x");
            return mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));
        }, 400, false);

        // Test duplicate email
        testEndpoint("POST /api/auth/register (duplicate)", "Duplicate Email Handling", () -> {
            RegisterRequest request = new RegisterRequest("Duplicate User", "report@test.com", "password123");
            return mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));
        }, 409, false);

        // Test user login
        testEndpoint("POST /api/auth/login", "User Login", () -> {
            LoginRequest request = new LoginRequest("report@test.com", "password123");
            return mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));
        }, 200, true);

        // Test invalid login
        testEndpoint("POST /api/auth/login (invalid)", "Invalid Login Handling", () -> {
            LoginRequest request = new LoginRequest("report@test.com", "wrongpassword");
            return mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));
        }, 401, false);

        System.out.println();
    }

    private void testCredentialEndpoints() throws Exception {
        System.out.println("2. CREDENTIAL MANAGEMENT ENDPOINTS");
        System.out.println("-".repeat(40));

        // Test credential creation
        testEndpoint("POST /api/credentials/create", "Create Credential", () -> {
            CreateCredentialRequest request = new CreateCredentialRequest();
            request.setServiceName("TestService");
            request.setUsername("testuser");
            request.setPassword("testpass");
            return mockMvc.perform(post("/api/credentials/create")
                    .header("Authorization", validJwtToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));
        }, 201, true);

        // Test credential creation without auth
        testEndpoint("POST /api/credentials/create (no auth)", "Credential Creation Authorization", () -> {
            CreateCredentialRequest request = new CreateCredentialRequest();
            request.setServiceName("TestService");
            request.setUsername("testuser");
            request.setPassword("testpass");
            return mockMvc.perform(post("/api/credentials/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));
        }, 401, false);

        // Test credential listing
        testEndpoint("GET /api/credentials/list", "List Credentials", () -> {
            return mockMvc.perform(get("/api/credentials/list")
                    .header("Authorization", validJwtToken));
        }, 200, true);

        // Test credential search
        testEndpoint("GET /api/credentials/search", "Search Credentials", () -> {
            return mockMvc.perform(get("/api/credentials/search")
                    .param("q", "test")
                    .header("Authorization", validJwtToken));
        }, 200, true);

        System.out.println();
    }

    private void testPasswordUtilityEndpoints() throws Exception {
        System.out.println("3. PASSWORD UTILITY ENDPOINTS");
        System.out.println("-".repeat(40));

        // Test password strength analysis
        testEndpoint("POST /api/password/strength", "Password Strength Analysis", () -> {
            String requestBody = "{\"password\":\"TestPassword123!\"}";
            return mockMvc.perform(post("/api/password/strength")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody));
        }, 200, true);

        // Test password generation
        testEndpoint("POST /api/password/generate", "Password Generation", () -> {
            String requestBody = "{\"length\":16,\"uppercase\":true,\"lowercase\":true,\"digits\":true,\"special\":true}";
            return mockMvc.perform(post("/api/password/generate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody));
        }, 200, true);

        System.out.println();
    }

    private void testErrorHandlingConsistency() throws Exception {
        System.out.println("4. ERROR HANDLING CONSISTENCY");
        System.out.println("-".repeat(40));

        // Test 404 handling
        testEndpoint("GET /api/credentials/999999", "404 Error Handling", () -> {
            return mockMvc.perform(get("/api/credentials/999999")
                    .header("Authorization", validJwtToken));
        }, 404, false);

        // Test malformed JSON
        testEndpoint("POST /api/auth/login (malformed)", "Malformed JSON Handling", () -> {
            String malformedJson = "{\"email\":\"test@test.com\",\"password\":}";
            return mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(malformedJson));
        }, 400, false);

        System.out.println();
    }

    private void testEndpoint(String endpoint, String description, TestAction action, 
                            int expectedStatus, boolean expectedSuccess) throws Exception {
        ValidationResult result = new ValidationResult(endpoint, description);
        
        try {
            MvcResult mvcResult = action.execute()
                    .andExpected(status().is(expectedStatus))
                    .andExpected(content().contentType(MediaType.APPLICATION_JSON))
                    .andReturn();

            String responseContent = mvcResult.getResponse().getContentAsString();
            ApiResponse<?> apiResponse = objectMapper.readValue(responseContent, ApiResponse.class);

            // Validate ApiResponse structure
            result.addAssertion("ApiResponse structure exists", apiResponse != null);
            result.addAssertion("Success field matches expected", apiResponse.isSuccess() == expectedSuccess);
            result.addAssertion("Message field exists", apiResponse.getMessage() != null && !apiResponse.getMessage().isEmpty());
            result.addAssertion("Timestamp field exists", apiResponse.getTimestamp() != null);
            result.addAssertion("HTTP status matches expected", mvcResult.getResponse().getStatus() == expectedStatus);
            
            if (expectedSuccess) {
                result.addAssertion("Data field exists for success", apiResponse.getData() != null);
            } else {
                result.addAssertion("Data field is null for errors", apiResponse.getData() == null);
            }

            result.passed = true;
            
        } catch (Exception e) {
            result.addAssertion("Endpoint execution", false);
            result.error = e.getMessage();
            result.passed = false;
        }

        validationResults.add(result);
        printTestResult(result);
    }

    private void printTestResult(ValidationResult result) {
        String status = result.passed ? "✅ PASS" : "❌ FAIL";
        System.out.printf("  %-40s %s%n", result.endpoint, status);
        
        if (!result.passed && result.error != null) {
            System.out.printf("     Error: %s%n", result.error);
        }
        
        for (Assertion assertion : result.assertions) {
            if (!assertion.passed) {
                System.out.printf("     ❌ %s%n", assertion.description);
            }
        }
    }

    private void generateFinalReport() {
        System.out.println("5. SUMMARY REPORT");
        System.out.println("-".repeat(40));
        
        long totalEndpoints = validationResults.size();
        long passedEndpoints = validationResults.stream().mapToLong(r -> r.passed ? 1 : 0).sum();
        long failedEndpoints = totalEndpoints - passedEndpoints;
        
        long totalAssertions = validationResults.stream()
                .mapToLong(r -> r.assertions.size())
                .sum();
        long passedAssertions = validationResults.stream()
                .mapToLong(r -> r.assertions.stream().mapToLong(a -> a.passed ? 1 : 0).sum())
                .sum();
        long failedAssertions = totalAssertions - passedAssertions;

        System.out.printf("Total Endpoints Tested: %d%n", totalEndpoints);
        System.out.printf("Endpoints Passed: %d%n", passedEndpoints);
        System.out.printf("Endpoints Failed: %d%n", failedEndpoints);
        System.out.printf("Success Rate: %.1f%%%n", (passedEndpoints * 100.0) / totalEndpoints);
        System.out.println();
        
        System.out.printf("Total Assertions: %d%n", totalAssertions);
        System.out.printf("Assertions Passed: %d%n", passedAssertions);
        System.out.printf("Assertions Failed: %d%n", failedAssertions);
        System.out.printf("Assertion Success Rate: %.1f%%%n", (passedAssertions * 100.0) / totalAssertions);
        System.out.println();

        if (failedEndpoints > 0) {
            System.out.println("FAILED ENDPOINTS:");
            validationResults.stream()
                    .filter(r -> !r.passed)
                    .forEach(r -> {
                        System.out.printf("  ❌ %s - %s%n", r.endpoint, r.description);
                        if (r.error != null) {
                            System.out.printf("     Error: %s%n", r.error);
                        }
                    });
        } else {
            System.out.println("🎉 ALL ENDPOINTS PASSED VALIDATION!");
        }
        
        System.out.println("\n" + "=".repeat(80));
        System.out.println("END OF REPORT");
        System.out.println("=".repeat(80));
    }

    @FunctionalInterface
    private interface TestAction {
        org.springframework.test.web.servlet.ResultActions execute() throws Exception;
    }

    private static class ValidationResult {
        final String endpoint;
        final String description;
        final List<Assertion> assertions = new ArrayList<>();
        boolean passed = false;
        String error;

        ValidationResult(String endpoint, String description) {
            this.endpoint = endpoint;
            this.description = description;
        }

        void addAssertion(String description, boolean passed) {
            assertions.add(new Assertion(description, passed));
        }
    }

    private static class Assertion {
        final String description;
        final boolean passed;

        Assertion(String description, boolean passed) {
            this.description = description;
            this.passed = passed;
        }
    }
}