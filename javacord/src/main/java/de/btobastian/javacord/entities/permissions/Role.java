package de.btobastian.javacord.entities.permissions;

import de.btobastian.javacord.entities.DiscordEntity;
import de.btobastian.javacord.entities.Mentionable;
import de.btobastian.javacord.entities.Server;
import de.btobastian.javacord.entities.User;

import java.awt.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This class represents a Discord role, e.g. "moderator".
 */
public interface Role extends DiscordEntity, Mentionable {

    /**
     * Gets the server of the role.
     *
     * @return The server of the role.
     */
    Server getServer();

    /**
     * Gets the name of the role.
     *
     * @return The name of the role.
     */
    String getName();

    /**
     * Gets the position of the role.
     *
     * @return The position of the role.
     */
    int getPosition();

    /**
     * Gets the color of the role.
     *
     * @return The color of the role.
     */
    Optional<Color> getColor();

    /**
     * Check if this role is pinned in the user listing (sometimes called "hoist").
     *
     * @return Whether this role is pinned in the user listing or not.
     */
    boolean isDisplayedSeparately();

    /**
     * Gets a collection with all users who have this role.
     *
     * @return A collection with all users who have this role.
     */
    Collection<User> getUsers();

    /**
     * Gets the permissions of the role.
     *
     * @return The permissions of the role.
     */
    Permissions getPermissions();

    /**
     * Gets the allowed permissions of the role.
     *
     * @return The allowed permissions of the role.
     */
    default Collection<PermissionType> getAllowedPermissions() {
        return Arrays.stream(PermissionType.values())
                .filter(type -> getPermissions().getState(type) == PermissionState.ALLOWED)
                .collect(Collectors.toSet());
    }

    /**
     * Gets the unset permissions of the role.
     *
     * @return The unset permissions of the role.
     */
    default Collection<PermissionType> getUnsetPermissions() {
        return Arrays.stream(PermissionType.values())
                .filter(type -> getPermissions().getState(type) == PermissionState.NONE)
                .collect(Collectors.toSet());
    }

    @Override
    default String getMentionTag() {
        return "<@&" + getId() + ">";
    }

}
