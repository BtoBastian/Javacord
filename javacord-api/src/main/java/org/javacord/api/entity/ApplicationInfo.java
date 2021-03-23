package org.javacord.api.entity;

import org.javacord.api.entity.team.Team;
import org.javacord.api.entity.user.User;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Represents the information of an application (aka. bot).
 * This class won't get updated after being fetched!
 */
public interface ApplicationInfo extends Nameable {

    /**
     * Gets the client id of the application.
     *
     * @return The client id of the application.
     */
    long getClientId();

    /**
     * Gets the description of the application.
     *
     * @return The description of the application.
     */
    String getDescription();

    /**
     * Check if the application's bot is public.
     *
     * @return Whether the application's bot is public or not.
     */
    boolean isPublicBot();

    /**
     * Check if the application's bot require OAuth2 code grant.
     *
     * @return Whether the application's bot require OAuth2 code grant or not.
     */
    boolean botRequiresCodeGrant();

    /**
     * Gets the name of the application owner.
     *
     * @return The name of the application owner.
     */
    String getOwnerName();

    /**
     * Gets the id of the application owner.
     *
     * @return The id of the application owner.
     */
    long getOwnerId();

    /**
     * Gets the discriminator of the application owner.
     *
     * @return The discriminator of the application owner.
     */
    String getOwnerDiscriminator();

    /**
     * Gets the owner of the application.
     *
     * @return The owner of the application.
     */
    CompletableFuture<User> getOwner();

    /**
     * Gets the team of the application.
     *
     * @return The team of the application.
     */

    Optional<Team> getTeam();

}
