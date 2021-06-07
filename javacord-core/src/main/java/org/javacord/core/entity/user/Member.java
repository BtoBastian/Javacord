package org.javacord.core.entity.user;

import org.javacord.api.entity.DiscordEntity;
import org.javacord.api.entity.Mentionable;
import org.javacord.api.entity.Permissionable;
import org.javacord.api.entity.message.Messageable;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

import java.awt.Color;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * A member of a server.
 */
public interface Member extends DiscordEntity, Messageable, Mentionable, Permissionable {

    @Override
    default String getMentionTag() {
        return "<@!" + getIdAsString() + ">";
    }

    /**
     * Gets the display name of this member.
     *
     * @return The display name of this member.
     */
    default String getDisplayName() {
        return getNickname().orElse(getUser().getName());
    }

    /**
     * Gets the server, this user is a part of.
     *
     * @return The server.
     */
    Server getServer();

    /**
     * Gets the user object linked to this member.
     *
     * @return The user.
     */
    User getUser();

    /**
     * Gets the nickname of this member.
     *
     * @return The nickname of this member.
     */
    Optional<String> getNickname();

    /**
     * Gets a sorted list (by position) with all roles of this member.
     *
     * @return A sorted list (by position) with all roles of this member.
     */
    List<Role> getRoles();

    /**
     * Checks if this member has the given role.
     *
     * @param role The role to check.
     * @return Whether the member has the role or not.
     */
    boolean hasRole(Role role);

    /**
     * Gets the displayed color of this member based on his roles.
     *
     * @return The color.
     */
    Optional<Color> getRoleColor();

    /**
     * Gets the timestamp of when this member joined the server.
     *
     * @return The timestamp of when this member joined the server.
     */
    Instant getJoinedAtTimestamp();

    /**
     * Gets the timestamp of when this member started boosting the server.
     *
     * @return The timestamp of when this member started boosting joined the server.
     */
    Optional<Instant> getServerBoostingSinceTimestamp();

    /**
     * Gets the muted state of this member.
     *
     * @return Whether or not this member is muted.
     */
    boolean isMuted();

    /**
     * Gets the deafened state of this member.
     *
     * @return Whether or not this member is deafened.
     */
    boolean isDeafened();

    /**
     * Gets the self-muted state of this member.
     *
     * @return Whether or not this member is self-muted.
     */
    boolean isSelfMuted();

    /**
     * Gets the self-deafened state of this member.
     *
     * @return Whether or not this member is self-deafened.
     */
    boolean isSelfDeafened();

}
