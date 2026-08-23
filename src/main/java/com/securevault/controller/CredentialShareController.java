package com.securevault.controller;

import com.securevault.dto.ApiResponse;
import com.securevault.dto.ShareCredentialRequest;
import com.securevault.dto.ShareResponse;
import com.securevault.dto.UpdateSharePermissionRequest;
import com.securevault.security.JwtService;
import com.securevault.service.CredentialShareService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * CredentialShareController
 *
 * REST API for credential sharing operations.
 *
 * Endpoints:
 * - POST   /api/share - Share a credential
 * - GET    /api/share/received - View credentials shared with me
 * - PUT    /api/share/{shareId} - Update share permission
 * - DELETE /api/share/{shareId} - Revoke share
 */
@RestController
@RequestMapping("/api/share")
public class CredentialShareController {

    @Autowired
    private CredentialShareService shareService;

    @Autowired
    private JwtService jwtService;

    /**
     * Extract userId from JWT token
     */
    private Long getUserIdFromToken(String authHeader) {
        String token = authHeader.substring(7); // Remove "Bearer " prefix
        return jwtService.extractUserId(token);
    }

    /**
     * Share a credential with another user
     *
     * POST /api/share
     * Header: Authorization: Bearer {token}
     * Body: {
     *   "credentialId": 1,
     *   "recipientEmail": "user@example.com",  // OR "sharedWithUserId": 2
     *   "permission": "READ",
     *   "expiresAt": "2026-12-31T23:59:59" (optional)
     * }
     *
     * Business Rules:
     * - Only the owner can share
     * - Cannot share with yourself
     * - Cannot share deleted credentials
     * - Cannot share same credential twice to same user
     * - If recipientEmail is provided, will look up user by email
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ShareResponse>> shareCredential(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody ShareCredentialRequest request) {
        try {
            Long ownerId = getUserIdFromToken(authHeader);

            // Handle recipientEmail if provided (lookup user)
            Long sharedWithUserId = request.getSharedWithUserId();
            if (sharedWithUserId == null && request.getRecipientEmail() != null) {
                sharedWithUserId = shareService.getUserIdByEmail(request.getRecipientEmail());
                if (sharedWithUserId == null) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(ApiResponse.error("User with email '" + request.getRecipientEmail() + "' not found"));
                }
            }

            if (sharedWithUserId == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("Either sharedWithUserId or recipientEmail must be provided"));
            }

            ShareResponse response = shareService.shareCredential(
                request.getCredentialId(),
                ownerId,
                sharedWithUserId,
                request.getPermission(),
                request.getExpiresAt()
            );

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Credential shared successfully", response));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Error sharing credential: " + e.getMessage()));
        }
    }

    /**
     * Get all credentials shared WITH the logged-in user
     *
     * GET /api/share/received
     * Header: Authorization: Bearer {token}
     *
     * Returns:
     * - All active shares where this user is the recipient
     * - Excludes expired shares
     * - Excludes shares for deleted credentials
     */
    @GetMapping("/received")
    public ResponseEntity<ApiResponse<List<ShareResponse>>> getSharedWithMe(
            @RequestHeader("Authorization") String authHeader) {
        try {
            Long userId = getUserIdFromToken(authHeader);

            List<ShareResponse> shares = shareService.getSharedWithMe(userId);
            
            return ResponseEntity.ok(
                ApiResponse.success("Shared credentials retrieved successfully", shares)
            );

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error retrieving shared credentials: " + e.getMessage()));
        }
    }

    /**
     * Get all credentials that the logged-in user HAS SHARED (as owner)
     *
     * GET /api/share/my-shares
     * Header: Authorization: Bearer {token}
     *
     * Returns:
     * - All active shares where this user is the owner
     * - Shows who you've shared your credentials with
     */
    @GetMapping("/my-shares")
    public ResponseEntity<ApiResponse<List<ShareResponse>>> getMyShares(
            @RequestHeader("Authorization") String authHeader) {
        try {
            Long userId = getUserIdFromToken(authHeader);

            List<ShareResponse> shares = shareService.getMyShares(userId);
            
            return ResponseEntity.ok(
                ApiResponse.success("Your shared credentials retrieved successfully", shares)
            );

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error retrieving your shares: " + e.getMessage()));
        }
    }

    /**
     * Update share permission
     *
     * PUT /api/share/{shareId}
     * Header: Authorization: Bearer {token}
     * Body: {
     *   "permission": "EDIT"
     * }
     *
     * Authorization:
     * - Only the owner can update permissions
     * - Cannot update revoked shares
     */
    @PutMapping("/{shareId}")
    public ResponseEntity<ApiResponse<ShareResponse>> updateSharePermission(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long shareId,
            @Valid @RequestBody UpdateSharePermissionRequest request) {
        try {
            Long ownerId = getUserIdFromToken(authHeader);

            ShareResponse response = shareService.updateSharePermission(
                shareId,
                ownerId,
                request.getPermission()
            );

            return ResponseEntity.ok(
                ApiResponse.success("Share permission updated successfully", response)
            );

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Error updating share: " + e.getMessage()));
        }
    }

    /**
     * Revoke a share
     *
     * DELETE /api/share/{shareId}
     * Header: Authorization: Bearer {token}
     *
     * Authorization:
     * - Only the owner can revoke shares
     * - Revoked shares immediately lose access
     *
     * Effect:
     * - Sets share.active = false
     * - Sets share.revokedAt = current timestamp
     * - User immediately loses access
     */
    @DeleteMapping("/{shareId}")
    public ResponseEntity<ApiResponse<Void>> revokeShare(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long shareId) {
        try {
            Long ownerId = getUserIdFromToken(authHeader);

            shareService.revokeShare(shareId, ownerId);

            return ResponseEntity.ok(
                ApiResponse.success("Share revoked successfully")
            );

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Error revoking share: " + e.getMessage()));
        }
    }
}
