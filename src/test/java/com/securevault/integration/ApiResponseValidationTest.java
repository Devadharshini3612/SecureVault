package com.securevault.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.securevault.dto.ApiResponse;
import com.securevault.dto.LoginRequest;
import com.securevault.dto.RegisterRequest;
import com.securevault.dto.CreateCredentialRequest;
import com.securevault.dto.UpdateCredentialRequest;
import com.securevault.dto.PasswordStrengthRequest;
import com.securevault.enums.Category;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests to verify all endpoints use consistent ApiResponse format
 * and proper validation error handling.
 * 
 * This test suite validates:
 * 1. All endpoints return standardized ApiResponse wrapper
 * 2. Bean validation annotations work correctly (@Valid, @NotBlank, @Email, etc.)
 * 3. Custom exceptions are handled by GlobalExceptionHandler
 * 4. HTTP status codes are appropriate
 * 5. Response structure is consistent across all endpoints
 * 6. Error messages are user-friendly and informative
 */
@SpringBootTest
@AutoConfigureTestMvc
@ActiveProfiles("test")
@Transactional
public class ApiResponseValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String validJwtToken;

    @BeforeEach
    void setUp() throws Exception {
        // Register a test user and get JWT token for authenticated endpoints
        RegisterRequest registerRequest = new RegisterRequest("Test User", "test@example.com", "password123");
        
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        // Login to get JWT token
        LoginRequest loginRequest = new LoginRequest("test@example.com", "password123");
        
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String loginResponse = loginResult.getResponse().getContentAsString();
        ApiResponse<?> apiResponse = objectMapper.readValue(loginResponse, ApiResponse.class);
        
        // Extract token from the response data
        @SuppressWarnings("unchecked")
        var loginData = (java.util.Map<String, Object>) apiResponse.getData();
        validJwtToken = "Bearer " + loginData.get("token");
    }

    // ========================================
    // USER AUTHENTICATION ENDPOINTS
    // ========================================

    @Test
    @DisplayName("POST /api/auth/register - Valid request should return standardized success response")
    void testRegisterValid() throws Exception {
        RegisterRequest request = new RegisterRequest("John Doe", "john@example.com", "password123");

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Verify ApiResponse structure
        String response = result.getResponse().getContentAsString();
        ApiResponse<?> apiResponse = objectMapper.readValue(response, ApiResponse.class);
        
        assertTrue(apiResponse.isSuccess());
        assertEquals("User registered successfully", apiResponse.getMessage());
        assertNotNull(apiResponse.getData());
        assertNotNull(apiResponse.getTimestamp());
    }

    @Test
    @DisplayName("POST /api/auth/register - Invalid request should return validation errors")
    void testRegisterValidationErrors() throws Exception {
        RegisterRequest request = new RegisterRequest("", "invalid-email", "short");

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpected(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Verify ApiResponse structure for validation errors
        String response = result.getResponse().getContentAsString();
        ApiResponse<?> apiResponse = objectMapper.readValue(response, ApiResponse.class);
        
        assertFalse(apiResponse.isSuccess());
        assertTrue(apiResponse.getMessage().contains("Validation failed"));
        assertNotNull(apiResponse.getFieldErrors());
        assertNotNull(apiResponse.getTimestamp());
    }

    @Test
    @DisplayName("POST /api/auth/register - Duplicate email should return conflict error")
    void testRegisterDuplicateEmail() throws Exception {
        RegisterRequest request = new RegisterRequest("Another User", "test@example.com", "password123");

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpected(status().isConflict())
                .andExpected(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Verify ApiResponse structure for business logic errors
        String response = result.getResponse().getContentAsString();
        ApiResponse<?> apiResponse = objectMapper.readValue(response, ApiResponse.class);
        
        assertFalse(apiResponse.isSuccess());
        assertTrue(apiResponse.getMessage().contains("Email already registered"));
        assertNotNull(apiResponse.getTimestamp());
    }

    @Test
    @DisplayName("POST /api/auth/login - Valid credentials should return success with JWT")
    void testLoginValid() throws Exception {
        LoginRequest request = new LoginRequest("test@example.com", "password123");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpected(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Verify ApiResponse structure and JWT token presence
        String response = result.getResponse().getContentAsString();
        ApiResponse<?> apiResponse = objectMapper.readValue(response, ApiResponse.class);
        
        assertTrue(apiResponse.isSuccess());
        assertEquals("Login successful", apiResponse.getMessage());
        
        @SuppressWarnings("unchecked")
        var loginData = (java.util.Map<String, Object>) apiResponse.getData();
        assertNotNull(loginData.get("token"));
        assertNotNull(loginData.get("email"));
        assertNotNull(loginData.get("userId"));
    }

    @Test
    @DisplayName("POST /api/auth/login - Invalid credentials should return unauthorized error")
    void testLoginInvalidCredentials() throws Exception {
        LoginRequest request = new LoginRequest("test@example.com", "wrongpassword");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpected(status().isUnauthorized())
                .andExpected(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Verify ApiResponse structure for authentication errors
        String response = result.getResponse().getContentAsString();
        ApiResponse<?> apiResponse = objectMapper.readValue(response, ApiResponse.class);
        
        assertFalse(apiResponse.isSuccess());
        assertTrue(apiResponse.getMessage().contains("Invalid password"));
        assertNotNull(apiResponse.getTimestamp());
    }

    // ========================================
    // CREDENTIAL MANAGEMENT ENDPOINTS
    // ========================================

    @Test
    @DisplayName("POST /api/credentials/create - Valid request should create credential")
    void testCreateCredentialValid() throws Exception {
        CreateCredentialRequest request = new CreateCredentialRequest();
        request.setServiceName("Gmail");
        request.setUsername("user@gmail.com");
        request.setPassword("secretpassword");
        request.setCategory(Category.PERSONAL);

        MvcResult result = mockMvc.perform(post("/api/credentials/create")
                .header("Authorization", validJwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpected(status().isCreated())
                .andExpected(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Verify ApiResponse structure for credential creation
        String response = result.getResponse().getContentAsString();
        ApiResponse<?> apiResponse = objectMapper.readValue(response, ApiResponse.class);
        
        assertTrue(apiResponse.isSuccess());
        assertEquals("Credential created successfully", apiResponse.getMessage());
        assertNotNull(apiResponse.getData());
        
        // Verify credential response structure
        @SuppressWarnings("unchecked")
        var credentialData = (java.util.Map<String, Object>) apiResponse.getData();
        assertNotNull(credentialData.get("credentialId"));
        assertNotNull(credentialData.get("serviceName"));
        assertNotNull(credentialData.get("username"));
        assertNotNull(credentialData.get("password"));
    }

    @Test
    @DisplayName("POST /api/credentials/create - Missing JWT should return unauthorized")
    void testCreateCredentialMissingAuth() throws Exception {
        CreateCredentialRequest request = new CreateCredentialRequest();
        request.setServiceName("Gmail");
        request.setUsername("user@gmail.com");
        request.setPassword("secretpassword");

        mockMvc.perform(post("/api/credentials/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpected(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/credentials/create - Invalid request should return validation errors")
    void testCreateCredentialValidationErrors() throws Exception {
        CreateCredentialRequest request = new CreateCredentialRequest();
        request.setServiceName(""); // Invalid - empty
        request.setUsername(""); // Invalid - empty
        request.setPassword(""); // Invalid - empty

        MvcResult result = mockMvc.perform(post("/api/credentials/create")
                .header("Authorization", validJwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpected(status().isBadRequest())
                .andExpected(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Verify validation error response
        String response = result.getResponse().getContentAsString();
        ApiResponse<?> apiResponse = objectMapper.readValue(response, ApiResponse.class);
        
        assertFalse(apiResponse.isSuccess());
        assertTrue(apiResponse.getMessage().contains("Validation failed"));
        assertNotNull(apiResponse.getFieldErrors());
    }

    // ========================================
    // PASSWORD UTILITY ENDPOINTS
    // ========================================

    @Test
    @DisplayName("POST /api/password/strength - Valid request should analyze password")
    void testPasswordStrengthValid() throws Exception {
        PasswordStrengthRequest request = new PasswordStrengthRequest("MyStrongPassword123!");

        MvcResult result = mockMvc.perform(post("/api/password/strength")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpected(status().isOk())
                .andExpected(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Verify ApiResponse structure for password analysis
        String response = result.getResponse().getContentAsString();
        ApiResponse<?> apiResponse = objectMapper.readValue(response, ApiResponse.class);
        
        assertTrue(apiResponse.isSuccess());
        assertTrue(apiResponse.getMessage().contains("Password strength analyzed"));
        assertNotNull(apiResponse.getData());
    }

    @Test
    @DisplayName("POST /api/password/strength - Empty password should return validation error")
    void testPasswordStrengthValidationError() throws Exception {
        PasswordStrengthRequest request = new PasswordStrengthRequest("");

        MvcResult result = mockMvc.perform(post("/api/password/strength")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpected(status().isBadRequest())
                .andExpected(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Verify validation error response
        String response = result.getResponse().getContentAsString();
        ApiResponse<?> apiResponse = objectMapper.readValue(response, ApiResponse.class);
        
        assertFalse(apiResponse.isSuccess());
        assertTrue(apiResponse.getMessage().contains("Password is required"));
    }

    // ========================================
    // ERROR HANDLING TESTS
    // ========================================

    @Test
    @DisplayName("GET /api/credentials/999999 - Non-existent credential should return not found")
    void testCredentialNotFound() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/credentials/999999")
                .header("Authorization", validJwtToken))
                .andExpected(status().isNotFound())
                .andExpected(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Verify 404 error response
        String response = result.getResponse().getContentAsString();
        ApiResponse<?> apiResponse = objectMapper.readValue(response, ApiResponse.class);
        
        assertFalse(apiResponse.isSuccess());
        assertTrue(apiResponse.getMessage().contains("not found"));
        assertNotNull(apiResponse.getTimestamp());
    }

    @Test
    @DisplayName("POST /api/auth/login - Invalid JSON should return bad request")
    void testInvalidJsonFormat() throws Exception {
        String invalidJson = "{\"email\": \"test@example.com\", \"password\":}"; // Missing value

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpected(status().isBadRequest());
    }

    // ========================================
    // CONSISTENCY VERIFICATION HELPERS
    // ========================================

    /**
     * Helper method to verify ApiResponse structure is consistent
     */
    private void verifyApiResponseStructure(ApiResponse<?> response, boolean expectedSuccess) {
        assertNotNull(response);
        assertEquals(expectedSuccess, response.isSuccess());
        assertNotNull(response.getMessage());
        assertNotNull(response.getTimestamp());
        
        if (expectedSuccess) {
            assertNotNull(response.getData());
        }
    }
}