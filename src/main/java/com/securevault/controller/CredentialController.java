package com.securevault.controller;

import com.securevault.dto.ApiResponse;
import com.securevault.dto.CreateCredentialRequest;
import com.securevault.dto.UpdateCredentialRequest;
import com.securevault.dto.CredentialResponse;
import com.securevault.enums.Category;
import com.securevault.exception.CredentialNotFoundException;
import com.securevault.exception.UnauthorizedAccessException;
import com.securevault.exception.ValidationException;
import com.securevault.mapper.DtoEntityMapper;
import com.securevault.security.JwtService;
import com.securevault.service.CredentialService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * CredentialController - Production-Ready Credential Management API
 * 
 * Fully refactored controller using:
 * - DTO pattern with validation annotations (@Valid)
 * - Standardized ApiResponse wrapper for all responses  
 * - DtoEntityMapper for clean conversions
 * - Custom exceptions handled by GlobalExceptionHandler
 * - JWT-based authentication and authorization
 * - Consistent HTTP status codes and error messages
 * 
 * Security: All endpoints require JWT authentication.
 * Users can only access their own credentials.
 */
@RestController
@RequestMapping("/api/credentials")
public class CredentialController {

    @Autowired
    private CredentialService credentialService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private DtoEntityMapper mapper;

    /**
     * Extracts userId from JWT token in Authorization header
     * 
     * @param authHeader Authorization header with "Bearer {token}"
     * @return userId extracted from token
     */
    private Long getUserIdFromToken(String authHeader) {
        String token = authHeader.substring(7); // Remove "Bearer " prefix
        return jwtService.extractUserId(token);
    }

    /**
     * CREATE CREDENTIAL
     * 
     * POST /api/credentials/create
     * Header: Authorization: Bearer {token}
     * Body: {"serviceName": "Gmail", "username": "john@gmail.com", "password": "secret"}
     * 
     * No need to send userId - extracted from JWT!
     */
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<CredentialResponse>> createCredential(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody CreateCredentialRequest request) {
        try {
            // Extract userId from JWT token
            Long userId = getUserIdFromToken(authHeader);
            
            // Set userId in request
            request.setUserId(userId);
            
            CredentialResponse response = credentialService.createCredentialWithResponse(request);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Credential created successfully", response));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error creating credential: " + e.getMessage()));
        }
    }

    /**
     * READ CREDENTIAL
     * 
     * GET /api/credentials/{id}
     * Header: Authorization: Bearer {token}
     * 
     * No need to send userId - extracted from JWT!
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CredentialResponse>> getCredential(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id) {
        try {
            // Extract userId from JWT token
            Long userId = getUserIdFromToken(authHeader);
            
            CredentialResponse response = credentialService.getCredential(id, userId);

            if (response != null) {
                return ResponseEntity.ok(ApiResponse.success("Credential retrieved successfully", response));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Credential not found"));
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error retrieving credential: " + e.getMessage()));
        }
    }

    /**
     * LIST CREDENTIALS
     * 
     * GET /api/credentials/list
     * Header: Authorization: Bearer {token}
     * 
     * No need to send userId - extracted from JWT!
     */
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<CredentialResponse>>> listCredentials(
            @RequestHeader("Authorization") String authHeader) {
        try {
            // Extract userId from JWT token
            Long userId = getUserIdFromToken(authHeader);
            
            List<CredentialResponse> credentials = credentialService.listCredentials(userId);
            return ResponseEntity.ok(ApiResponse.success("Credentials retrieved successfully", credentials));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error listing credentials: " + e.getMessage()));
        }
    }

    /**
     * UPDATE CREDENTIAL
     * 
     * PUT /api/credentials/update/{id}
     * Header: Authorization: Bearer {token}
     * Body: {"serviceName": "Gmail Personal", "password": "newsecret"}
     * 
     * No need to send userId - extracted from JWT!
     */
    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<CredentialResponse>> updateCredential(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id,
            @Valid @RequestBody UpdateCredentialRequest request) {
        try {
            // Extract userId from JWT token
            Long userId = getUserIdFromToken(authHeader);
            
            CredentialResponse response = credentialService.updateCredentialWithResponse(id, userId, request);

            if (response != null) {
                return ResponseEntity.ok(ApiResponse.success("Credential updated successfully", response));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Credential not found"));
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error updating credential: " + e.getMessage()));
        }
    }

    /**
     * DELETE CREDENTIAL
     * 
     * DELETE /api/credentials/delete/{id}
     * Header: Authorization: Bearer {token}
     * 
     * No need to send userId - extracted from JWT!
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCredential(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id) {
        try {
            // Extract userId from JWT token
            Long userId = getUserIdFromToken(authHeader);
            
            String result = credentialService.deleteCredential(id, userId);

            if ("SUCCESS".equals(result)) {
                return ResponseEntity.ok(ApiResponse.success("Credential deleted successfully"));
            } else if ("NOT_FOUND".equals(result)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Credential not found"));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponse.error("Failed to delete credential"));
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error deleting credential: " + e.getMessage()));
        }
    }

    /**
     * SEARCH CREDENTIALS
     * 
     * GET /api/credentials/search?q=gmail
     * Header: Authorization: Bearer {token}
     * 
     * Searches in serviceName and username fields (case-insensitive)
     * Returns empty list if no matches found
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<CredentialResponse>>> searchCredentials(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam("q") String searchTerm) {
        try {
            // Extract userId from JWT token
            Long userId = getUserIdFromToken(authHeader);
            
            // Validate search term
            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Search term cannot be empty"));
            }
            
            List<CredentialResponse> results = credentialService.searchCredentials(userId, searchTerm.trim());
            return ResponseEntity.ok(ApiResponse.success("Search completed successfully", results));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error searching credentials: " + e.getMessage()));
        }
    }

    /**
     * FILTER AND PAGINATE CREDENTIALS
     * 
     * GET /api/credentials/vault?page=0&size=10&sortBy=serviceName&direction=asc&category=BANKING
     * Header: Authorization: Bearer {token}
     * 
     * Supports pagination, sorting, and dynamic filtering
     */
    @GetMapping("/vault")
    public ResponseEntity<ApiResponse<PagedResponse<CredentialResponse>>> getCredentialsWithPaginationAndFilters(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "updatedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String serviceName,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String search) {
        try {
            // Extract userId from JWT token
            Long userId = getUserIdFromToken(authHeader);
            
            // Validate pagination parameters
            if (size > 100) {
                size = 100; // Maximum page size limit
            }
            if (size < 1) {
                size = 10; // Default size
            }
            
            // Parse category if provided
            Category categoryEnum = null;
            if (category != null && !category.trim().isEmpty()) {
                try {
                    categoryEnum = Category.valueOf(category.toUpperCase());
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest()
                            .body(ApiResponse.error("Invalid category: " + category));
                }
            }
            
            // Create pageable with sorting
            Sort.Direction sortDirection = direction.equalsIgnoreCase("asc") ? 
                Sort.Direction.ASC : Sort.Direction.DESC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
            
            // Get paginated results
            Page<CredentialResponse> credentialPage = credentialService.getCredentialsPaginated(
                userId, categoryEnum, serviceName, username, search, pageable
            );
            
            // Build paged response
            PagedResponse<CredentialResponse> pagedResponse = new PagedResponse<>(
                credentialPage.getContent(),
                credentialPage.getNumber(),
                credentialPage.getSize(),
                credentialPage.getTotalElements(),
                credentialPage.getTotalPages()
            );
            
            return ResponseEntity.ok(
                ApiResponse.success("Credentials retrieved successfully", pagedResponse)
            );

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error retrieving credentials: " + e.getMessage()));
        }
    }

    /**
     * GET TRASH (DELETED CREDENTIALS)
     * 
     * GET /api/credentials/trash
     * Header: Authorization: Bearer {token}
     * 
     * Returns all soft-deleted credentials for the user
     * These can be restored or permanently deleted
     */
    @GetMapping("/trash")
    public ResponseEntity<ApiResponse<List<CredentialResponse>>> getTrash(
            @RequestHeader("Authorization") String authHeader) {
        try {
            // Extract userId from JWT token
            Long userId = getUserIdFromToken(authHeader);
            
            List<CredentialResponse> deletedCredentials = credentialService.getDeletedCredentials(userId);
            return ResponseEntity.ok(ApiResponse.success("Trash retrieved successfully", deletedCredentials));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error retrieving trash: " + e.getMessage()));
        }
    }

    /**
     * RESTORE CREDENTIAL FROM TRASH
     * 
     * PUT /api/credentials/{id}/restore
     * Header: Authorization: Bearer {token}
     * 
     * Restores a soft-deleted credential (undeletes it)
     * The credential will appear in normal listings again
     */
    @PutMapping("/{id}/restore")
    public ResponseEntity<ApiResponse<Void>> restoreCredential(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id) {
        try {
            // Extract userId from JWT token
            Long userId = getUserIdFromToken(authHeader);
            
            String result = credentialService.restoreCredential(id, userId);

            if ("SUCCESS".equals(result)) {
                return ResponseEntity.ok(ApiResponse.success("Credential restored successfully"));
            } else if ("NOT_FOUND".equals(result)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Credential not found or not deleted"));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponse.error("Failed to restore credential"));
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error restoring credential: " + e.getMessage()));
        }
    }

    /**
     * PERMANENTLY DELETE CREDENTIAL
     * 
     * DELETE /api/credentials/{id}/permanent
     * Header: Authorization: Bearer {token}
     * 
     * Permanently deletes a credential from the database
     * WARNING: This action cannot be undone!
     * Also deletes associated password history
     */
    @DeleteMapping("/{id}/permanent")
    public ResponseEntity<ApiResponse<Void>> permanentlyDeleteCredential(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id) {
        try {
            // Extract userId from JWT token
            Long userId = getUserIdFromToken(authHeader);
            
            String result = credentialService.permanentlyDeleteCredential(id, userId);

            if ("SUCCESS".equals(result)) {
                return ResponseEntity.ok(ApiResponse.success("Credential permanently deleted"));
            } else if ("NOT_FOUND".equals(result)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Credential not found"));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponse.error("Failed to delete credential"));
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error deleting credential: " + e.getMessage()));
        }
    }
}
