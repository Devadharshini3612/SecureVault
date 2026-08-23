package com.securevault.specification;

import com.securevault.entity.Credential;
import com.securevault.enums.Category;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * CredentialSpecification
 *
 * JPA Specification builder for dynamic credential filtering.
 * Allows combining multiple filter criteria with AND logic.
 *
 * Usage Example:
 * Specification<Credential> spec = CredentialSpecification.builder()
 *     .withUserId(userId)
 *     .withCategory(Category.BANKING)
 *     .withServiceNameContaining("bank")
 *     .build();
 */
public class CredentialSpecification {

    /**
     * Filter by user ID (required for security)
     * Excludes soft-deleted credentials by default
     */
    public static Specification<Credential> hasUserId(Long userId) {
        return (root, query, cb) -> {
            if (userId == null) {
                return cb.conjunction();
            }
            return cb.and(
                cb.equal(root.get("userId"), userId),
                cb.equal(root.get("deleted"), false)
            );
        };
    }

    /**
     * Filter by category
     */
    public static Specification<Credential> hasCategory(Category category) {
        return (root, query, cb) -> {
            if (category == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("category"), category);
        };
    }

    /**
     * Filter by service name (case-insensitive partial match)
     */
    public static Specification<Credential> serviceNameContains(String serviceName) {
        return (root, query, cb) -> {
            if (serviceName == null || serviceName.trim().isEmpty()) {
                return cb.conjunction();
            }
            return cb.like(cb.lower(root.get("serviceName")), 
                          "%" + serviceName.toLowerCase() + "%");
        };
    }

    /**
     * Filter by username (case-insensitive partial match)
     */
    public static Specification<Credential> usernameContains(String username) {
        return (root, query, cb) -> {
            if (username == null || username.trim().isEmpty()) {
                return cb.conjunction();
            }
            return cb.like(cb.lower(root.get("username")), 
                          "%" + username.toLowerCase() + "%");
        };
    }

    /**
     * Search in both service name and username
     */
    public static Specification<Credential> searchTermMatches(String searchTerm) {
        return (root, query, cb) -> {
            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                return cb.conjunction();
            }
            String pattern = "%" + searchTerm.toLowerCase() + "%";
            return cb.or(
                cb.like(cb.lower(root.get("serviceName")), pattern),
                cb.like(cb.lower(root.get("username")), pattern)
            );
        };
    }

    /**
     * Combine multiple specifications with AND logic
     */
    public static Specification<Credential> buildSpecification(
            Long userId,
            Category category,
            String serviceName,
            String username,
            String searchTerm) {
        
        return Specification.where(hasUserId(userId))
                .and(category != null ? hasCategory(category) : null)
                .and(serviceName != null ? serviceNameContains(serviceName) : null)
                .and(username != null ? usernameContains(username) : null)
                .and(searchTerm != null ? searchTermMatches(searchTerm) : null);
    }
}
