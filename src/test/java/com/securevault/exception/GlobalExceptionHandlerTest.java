package com.securevault.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.securevault.dto.ApiResponse;
import com.securevault.dto.LoginRequest;
import com.securevault.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
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
 * Tests to verify GlobalExceptionHandler properly converts all exceptions
 * into standardized ApiResponse format with appropriate HTTP status codes.
 * 
 * This ensures that:
 * 1. Custom exceptions are caught and converted to ApiResponse
 * 2. Bean validation errors are handled consistently
 * 3. HTTP status codes match the exception types
 * 4. Error messages are user-friendly and informative
 * 5. All error responses follow the same structure
 */
@SpringBootTest
@AutoConfigureTestMvc
@ActiveProfiles("test")
@Transactional
public class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // ========================================
    // CUSTOM EXCEPTION HANDLING TESTS
    // ========================================

    @Test
    @DisplayName("DuplicateEmailException should return 409 Conflict with ApiResponse")
    void testDuplicateEmailException() throws Exception {
        // First register a user
        RegisterRequest firstRequest = new RegisterRequest("First User", "duplicate@test.com", "password123");
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(firstRequest)))
                .andExpect(status().isCreated());

        // Try to register with same email
        RegisterRequest duplicateRequest = new RegisterRequest("Second User", "duplicate@test.com", "password456");
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Verify ApiResponse structure for DuplicateEmailException
        String response = result.getResponse().getContentAsString();
        ApiResponse<?> apiResponse = objectMapper.readValue(response, ApiResponse.class);
        
        assertFalse(apiResponse.isSuccess());
        assertTrue(apiResponse.getMessage().contains("Email already registered") || 
                  apiResponse.getMessage().contains("duplicate") ||
                  apiResponse.getMessage().contains("exists"));
        assertNotNull(apiResponse.getTimestamp());
        assertNull(apiResponse.getData());
    }

    @Test
    @DisplayName("UserNotFoundException should return 404 Not Found with ApiResponse")
    void testUserNotFoundException() throws Exception {
        LoginRequest request = new LoginRequest("nonexistent@test.com", "password123");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Verify ApiResponse structure for UserNotFoundException
        String response = result.getResponse().getContentAsString();
        ApiResponse<?> apiResponse = objectMapper.readValue(response, ApiResponse.class);
        
        assertFalse(apiResponse.isSuccess());
        assertTrue(apiResponse.getMessage().contains("User not found") || 
                  apiResponse.getMessage().contains("not found"));
        assertNotNull(apiResponse.getTimestamp());
        assertNull(apiResponse.getData());
    }

    @Test
    @DisplayName("InvalidCredentialsException should return 401 Unauthorized with ApiResponse")
    void testInvalidCredentialsException() throws Exception {
        // First register a user
        RegisterRequest registerRequest = new RegisterRequest("Test User", "valid@test.com", "correctpass");
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        // Try to login with wrong password
        LoginRequest loginRequest = new LoginRequest("valid@test.com", "wrongpassword");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Verify ApiResponse structure for InvalidCredentialsException
        String response = result.getResponse().getContentAsString();
        ApiResponse<?> apiResponse = objectMapper.readValue(response, ApiResponse.class);
        
        assertFalse(apiResponse.isSuccess());
        assertTrue(apiResponse.getMessage().contains("Invalid password") || 
                  apiResponse.getMessage().contains("Invalid credentials") ||
                  apiResponse.getMessage().contains("incorrect"));
        assertNotNull(apiResponse.getTimestamp());
        assertNull(apiResponse.getData());
    }

    // ========================================
    // BEAN VALIDATION EXCEPTION TESTS
    // ========================================

    @Test
    @DisplayName("MethodArgumentNotValidException should return 400 Bad Request with field errors")
    void testMethodArgumentNotValidException() throws Exception {
        // Create request with multiple validation errors
        RegisterRequest invalidRequest = new RegisterRequest("", "not-an-email", "short");

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Verify ApiResponse structure for validation errors
        String response = result.getResponse().getContentAsString();
        ApiResponse<?> apiResponse = objectMapper.readValue(response, ApiResponse.class);
        
        assertFalse(apiResponse.isSuccess());
        assertTrue(apiResponse.getMessage().contains("Validation failed") || 
                  apiResponse.getMessage().contains("validation"));
        assertNotNull(apiResponse.getTimestamp());
        assertNull(apiResponse.getData());
        assertNotNull(apiResponse.getFieldErrors());
        assertFalse(apiResponse.getFieldErrors().isEmpty());
    }

    @Test
    @DisplayName("Empty request body should return validation error")
    void testEmptyRequestBody() throws Exception {
        RegisterRequest emptyRequest = new RegisterRequest(null, null, null);

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emptyRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Verify validation error handling
        String response = result.getResponse().getContentAsString();
        ApiResponse<?> apiResponse = objectMapper.readValue(response, ApiResponse.class);
        
        assertFalse(apiResponse.isSuccess());
        assertNotNull(apiResponse.getTimestamp());
    }

    // ========================================
    // GENERIC EXCEPTION HANDLING TESTS
    // ========================================

    @Test
    @DisplayName("Malformed JSON should return 400 Bad Request")
    void testMalformedJsonException() throws Exception {
        String malformedJson = "{\"name\": \"Test\", \"email\":}"; // Missing value

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(malformedJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Missing Content-Type should be handled gracefully")
    void testMissingContentType() throws Exception {
        RegisterRequest request = new RegisterRequest("Test User", "test@test.com", "password123");

        mockMvc.perform(post("/api/auth/register")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnsupportedMediaType());
    }

    // ========================================
    // CONSISTENCY VERIFICATION TESTS
    // ========================================

    @Test
    @DisplayName("All error responses should have consistent structure")
    void testErrorResponseConsistency() throws Exception {
        // Test different types of errors and verify they all follow same structure
        
        // 1. Validation error
        RegisterRequest invalidRequest = new RegisterRequest("", "invalid", "x");
        MvcResult validationResult = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andReturn();
        
        ApiResponse<?> validationResponse = objectMapper.readValue(
            validationResult.getResponse().getContentAsString(), ApiResponse.class);
        
        verifyErrorResponseStructure(validationResponse);

        // 2. Business logic error (duplicate email)
        RegisterRequest firstUser = new RegisterRequest("First", "consistency@test.com", "password123");
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(firstUser)))
                .andExpect(status().isCreated());

        RegisterRequest duplicateUser = new RegisterRequest("Second", "consistency@test.com", "password456");
        MvcResult duplicateResult = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicateUser)))
                .andExpect(status().isConflict())
                .andReturn();
        
        ApiResponse<?> duplicateResponse = objectMapper.readValue(
            duplicateResult.getResponse().getContentAsString(), ApiResponse.class);
        
        verifyErrorResponseStructure(duplicateResponse);

        // 3. Authentication error
        LoginRequest invalidLogin = new LoginRequest("nonexistent@test.com", "wrongpass");
        MvcResult authResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidLogin)))
                .andExpect(status().isNotFound())
                .andReturn();
        
        ApiResponse<?> authResponse = objectMapper.readValue(
            authResult.getResponse().getContentAsString(), ApiResponse.class);
        
        verifyErrorResponseStructure(authResponse);
    }

    /**
     * Helper method to verify all error responses follow the same structure
     */
    private void verifyErrorResponseStructure(ApiResponse<?> response) {
        assertNotNull(response, "Response should not be null");
        assertFalse(response.isSuccess(), "Error response should have success=false");
        assertNotNull(response.getMessage(), "Error response should have a message");
        assertNotNull(response.getTimestamp(), "Error response should have a timestamp");
        assertNull(response.getData(), "Error response should have data=null");
        
        // Verify timestamp is recent (within last minute)
        assertTrue(response.getTimestamp().isAfter(java.time.LocalDateTime.now().minusMinutes(1)),
                "Timestamp should be recent");
        
        // Verify message is not empty
        assertFalse(response.getMessage().trim().isEmpty(), 
                "Error message should not be empty");
    }
}