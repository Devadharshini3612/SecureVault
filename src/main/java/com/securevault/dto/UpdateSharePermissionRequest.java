package com.securevault.dto;

import com.securevault.enums.Permission;
import jakarta.validation.constraints.NotNull;

/**
 * UpdateSharePermissionRequest
 *
 * Request DTO for updating share permission.
 */
public class UpdateSharePermissionRequest {

    @NotNull(message = "Permission is required")
    private Permission permission;

    // Constructors
    public UpdateSharePermissionRequest() {
    }

    public UpdateSharePermissionRequest(Permission permission) {
        this.permission = permission;
    }

    // Getters and Setters
    public Permission getPermission() {
        return permission;
    }

    public void setPermission(Permission permission) {
        this.permission = permission;
    }
}
