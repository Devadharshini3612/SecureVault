package com.securevault.mapper;

import com.securevault.dto.RegisterRequest;
import com.securevault.dto.UserResponse;
import com.securevault.entity.User;

/**
 * UserMapper
 *
 * Manual mapper class for converting between User entity and User DTOs.
 * This provides clean separation between presentation layer (DTOs) and
 * persistence layer (Entities).
 *
 * Benefits:
 * - Centralized mapping logic
 * - Easier to maintain and test
 * - Clear separation of concerns
 * - Prevents direct entity exposure in controllers
 */
public class UserMapper {

    /**
     * Convert RegisterRequest DTO to User entity
     * Note: Password will be hashed in the service layer
     *
     * @param request the registration request DTO
     * @return User entity ready for persistence
     */
    public static User toEntity(RegisterRequest request) {
        if (request == null) {
            return null;
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPasswordHash(request.getPassword()); // Will be hashed in service
        
        return user;
    }

    /**
     * Convert User entity to UserResponse DTO
     * Security: Excludes password hash from response
     *
     * @param user the user entity
     * @return UserResponse DTO safe for API responses
     */
    public static UserResponse toResponse(User user) {
        if (user == null) {
            return null;
        }

        return new UserResponse(
            user.getUserId(),
            user.getName(),
            user.getEmail()
        );
    }

    /**
     * Update existing User entity with data from RegisterRequest
     * Useful for profile update operations
     *
     * @param user the existing user entity
     * @param request the update request
     */
    public static void updateEntityFromRequest(User user, RegisterRequest request) {
        if (user == null || request == null) {
            return;
        }

        if (request.getName() != null && !request.getName().isEmpty()) {
            user.setName(request.getName());
        }
        
        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            user.setEmail(request.getEmail());
        }
        
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPasswordHash(request.getPassword()); // Will be hashed in service
        }
    }
}
