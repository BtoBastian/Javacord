package org.javacord.api.entity.channel;

import org.javacord.api.entity.Mentionable;
import org.javacord.api.entity.webhook.WebhookBuilder;
import org.javacord.api.listener.channel.server.text.ServerTextChannelAttachableListenerManager;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * This class represents a server text channel.
 */
public interface ServerTextChannel extends ServerChannel, TextChannel, Mentionable, Categorizable,
                                           ServerTextChannelAttachableListenerManager {

    @Override
    default ChannelType getType() {
        return ChannelType.SERVER_TEXT_CHANNEL;
    }

    /**
     * Checks is the channel is "not safe for work".
     *
     * @return Whether the channel is "not safe for work" or not.
     */
    boolean isNsfw();

    /**
     * Gets the topic of the channel.
     *
     * @return The topic of the channel.
     */
    String getTopic();

    /**
     * Creates a webhook builder for this channel.
     *
     * @return A webhook builder.
     */
    default WebhookBuilder createWebhookBuilder() {
        return new WebhookBuilder(this);
    }

    /**
     * Creates an updater for this channel.
     *
     * @return An updater for this channel.
     */
    default ServerTextChannelUpdater createUpdater() {
        return new ServerTextChannelUpdater(this);
    }

    /**
     * Updates the topic of the channel.
     *
     * <p>If you want to update several settings at once, it's recommended to use the
     * {@link ServerTextChannelUpdater} from {@link #createUpdater()} which provides a better performance!
     *
     * @param topic The new topic of the channel.
     * @return A future to check if the update was successful.
     */
    default CompletableFuture<Void> updateTopic(String topic) {
        return createUpdater().setTopic(topic).update();
    }

    /**
     * Updates the nsfw flag of the channel.
     *
     * <p>If you want to update several settings at once, it's recommended to use the
     * {@link ServerTextChannelUpdater} from {@link #createUpdater()} which provides a better performance!
     *
     * @param nsfw The new nsfw flag of the channel.
     * @return A future to check if the update was successful.
     */
    default CompletableFuture<Void> updateNsfwFlag(boolean nsfw) {
        return createUpdater().setNsfwFlag(nsfw).update();
    }

    /**
     * {@inheritDoc}
     *
     * <p>If you want to update several settings at once, it's recommended to use the
     * {@link ServerTextChannelUpdater} from {@link #createUpdater()} which provides a better performance!
     *
     * @param category The new category of the channel.
     * @return A future to check if the update was successful.
     */
    default CompletableFuture<Void> updateCategory(ChannelCategory category) {
        return createUpdater().setCategory(category).update();
    }

    /**
     * {@inheritDoc}
     *
     * <p>If you want to update several settings at once, it's recommended to use the
     * {@link ServerTextChannelUpdater} from {@link #createUpdater()} which provides a better performance!
     *
     * @return A future to check if the update was successful.
     */
    default CompletableFuture<Void> removeCategory() {
        return createUpdater().removeCategory().update();
    }

    /**
     * Gets the delay for slowmode.
     *
     * @return The delay in seconds.
     */
    int getSlowmodeDelayInSeconds();

    /**
     * Check whether slowmode is activated for this channel.
     *
     * @return Whether this channel enforces a slowmode.
     */
    default boolean hasSlowmode() {
        return getSlowmodeDelayInSeconds() != 0;
    }

    /**
     * Set a slowmode for this channel.
     *
     * <p>If you want to update several settings at once, it's recommended to use the
     * {@link ServerTextChannelUpdater} from {@link #createUpdater()} which provides a better performance!
     *
     * @param delay The slowmode delay in seconds.
     *
     * @return A future to check if the update was successful.
     */
    default CompletableFuture<Void> updateSlowmodeDelayInSeconds(int delay) {
        return createUpdater().setSlowmodeDelayInSeconds(delay).update();
    }

    /**
     * Deactivate slowmode for this channel.
     *
     * <p>If you want to update several settings at once, it's recommended to use the
     * {@link ServerTextChannelUpdater} from {@link #createUpdater()} which provides a better performance!
     *
     * @return A future to check if the update was successful.
     */
    default CompletableFuture<Void> unsetSlowmode() {
        return createUpdater().unsetSlowmode().update();
    }

    @Override
    default Optional<ServerTextChannel> getCurrentCachedInstance() {
        return getApi().getServerById(getServer().getId()).flatMap(server -> server.getTextChannelById(getId()));
    }

    @Override
    default CompletableFuture<ServerTextChannel> getLatestInstance() {
        Optional<ServerTextChannel> currentCachedInstance = getCurrentCachedInstance();
        if (currentCachedInstance.isPresent()) {
            return CompletableFuture.completedFuture(currentCachedInstance.get());
        } else {
            CompletableFuture<ServerTextChannel> result = new CompletableFuture<>();
            result.completeExceptionally(new NoSuchElementException());
            return result;
        }
    }

}
