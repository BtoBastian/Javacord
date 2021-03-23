package org.javacord.api.entity.team;

import org.javacord.api.entity.DiscordEntity;
import org.javacord.api.entity.user.User;

import java.util.concurrent.CompletableFuture;

public interface TeamMember extends DiscordEntity {

    /**
     * Gets the state of the membership.
     *
     * @return The state of the membership.
     */
    TeamMembershipState getMembershipState();

    /**
     * Gets the member as user.
     *
     * @return The member as user.
     */
    CompletableFuture<User> getUser();
}
