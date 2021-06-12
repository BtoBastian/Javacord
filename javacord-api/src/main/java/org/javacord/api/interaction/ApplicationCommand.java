package org.javacord.api.interaction;

import org.javacord.api.entity.DiscordEntity;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ApplicationCommand extends DiscordEntity {

    /**
     * Gets the unique id of this command.
     *
     * @return The unique id of this command.
     */
    long getId();

    /**
     * Gets the unique id of the application that this command belongs to.
     *
     * @return The unique application id.
     */
    long getApplicationId();

    /**
     * Gets the name of this command.
     *
     * @return The name of this command.
     */
    String getName();

    /**
     * Gets the description of this command.
     *
     * @return The description of this command.
     */
    String getDescription();

    /**
     * Gets a list with all options (i.e., parameters) for this command.
     *
     * @return A list with all options (i.e., parameters) for this command.
     */
    List<ApplicationCommandOption> getOptions();

    /**
     * Deletes this application command.
     *
     * @return A future to check if the deletion was successful.
     */
    CompletableFuture<Void> delete();

    /**
     * Creates an application command updater from this ApplicationCommand instance.
     *
     * @return The application command updater for this ApplicationCommand instance.
     */
    default ApplicationCommandUpdater createApplicationCommandUpdater() {
        return new ApplicationCommandUpdater(this.getId());
    }

}
