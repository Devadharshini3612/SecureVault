package com.securevault.enums;

/**
 * Permission
 *
 * Defines the permission levels for shared credentials.
 *
 * READ:
 * - View shared credential
 * - Cannot modify
 * - Cannot delete
 * - Cannot reshare
 *
 * EDIT:
 * - View credential
 * - Update credential
 * - Cannot delete ownership
 * - Cannot transfer ownership
 *
 * Only the owner can permanently delete a credential.
 */
public enum Permission {
    READ,
    EDIT
}
