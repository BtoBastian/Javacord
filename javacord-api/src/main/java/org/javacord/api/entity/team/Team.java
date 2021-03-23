package org.javacord.api.entity.team;

import org.javacord.api.entity.DiscordEntity;
import org.javacord.api.entity.Icon;
import org.javacord.api.entity.user.User;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface Team extends DiscordEntity {
    /**
     * Gets the icon of the team.
     *
     * @return The icon of the team.
     */
    Optional<Icon> getIcon();

    /**
     * Gets the members of the team.
     *
     * @return The members of the team.
     */
    Collection<TeamMember> getTeamMembers();

    /**
     * Gets the id of the owner.
     *
     * @return The if of the owner.
     */
    long getOwnerId();

    /**
     * Gets the owner of the team.
     *
     * @return The owner of the team.
     */
    default CompletableFuture<User> getOwner() {
        return getApi().getUserById(getOwnerId());
    }
}
