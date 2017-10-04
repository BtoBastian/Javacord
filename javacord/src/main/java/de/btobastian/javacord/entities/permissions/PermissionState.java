package de.btobastian.javacord.entities.permissions;

/**
 * This enum represents the state of a {@link PermissionType}.
 */
public enum PermissionState {

    /**
     * The given {@link PermissionType type} is not set.
     */
    NONE(),

    /**
     * The given {@link PermissionType type} is allowed.
     */
    ALLOWED(),

    /**
     * The given {@link PermissionType type} is denied.
     * This is only for overwritten permissions!
     */
    DENIED()

}