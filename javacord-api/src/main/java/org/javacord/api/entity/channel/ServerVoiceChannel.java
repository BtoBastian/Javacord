package org.javacord.api.entity.channel;

import org.javacord.api.audio.AudioConnection;
import org.javacord.api.entity.user.User;
import org.javacord.api.listener.channel.server.voice.ServerVoiceChannelAttachableListenerManager;

import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * This class represents a server voice channel.
 */
public interface ServerVoiceChannel extends ServerChannel, VoiceChannel, Categorizable,
                                            ServerVoiceChannelAttachableListenerManager {

    @Override
    default ChannelType getType() {
        return ChannelType.SERVER_VOICE_CHANNEL;
    }

    /**
     * Connects to the voice channel and disconnects any existing connections in the server.
     *
     * @return The audio connection.
     */
    CompletableFuture<AudioConnection> connect();

    /**
     * Gets the bitrate (int bits) of the channel.
     *
     * @return The bitrate of the channel.
     */
    int getBitrate();

    /**
     * Gets the user limit of the channel.
     *
     * @return The user limit.
     */
    Optional<Integer> getUserLimit();

    /**
     * Gets the ids of the users that are connected to this server voice channel.
     *
     * @return The ids of the users that are connected to this server voice channel.
     */
    Collection<Long> getConnectedUserIds();

    /**
     * Gets the users that are connected to this server voice channel.
     *
     * @return The users that are connected to this server voice channel.
     */
    Collection<User> getConnectedUsers();

    /**
     * Checks whether the user with the given id is connected to this channel.
     *
     * @param userId The id of the user to check.
     * @return Whether the user with the given id is connected to this channel or not.
     */
    boolean isConnected(long userId);

    /**
     * Checks whether the given user is connected to this channel.
     *
     * @param user The user to check.
     * @return Whether the given user is connected to this channel or not.
     */
    default boolean isConnected(User user) {
        return isConnected(user.getId());
    }

    /**
     * Gets the ids of the users that are self muted in this server voice channel.
     *
     * @return The ids of the users that are self muted in this server voice channel.
     */
    Collection<Long> getSelfMutedUserIds();

    /**
     * Gets the users that are self muted in this server voice channel.
     *
     * @return The users that are self muted in this server voice channel.
     */
    Collection<User> getSelfMutedUsers();

    /**
     * Checks whether the user with the given id is self muted in this channel.
     *
     * @param userId The id of the user to check.
     * @return Whether the user with the given id is self muted in this channel or not.
     */
    boolean isSelfMuted(long userId);

    /**
     * Checks whether the given user is self muted in this channel.
     *
     * @param user The user to check.
     * @return Whether the given user is self muted in this channel or not.
     */
    default boolean isSelfMuted(User user) {
        return isSelfMuted(user.getId());
    }

    /**
     * Gets the ids of the users that are self deafened in this server voice channel.
     *
     * @return The ids of the users that are self deafened in this server voice channel.
     */
    Collection<Long> getSelfDeafenedUserIds();

    /**
     * Gets the users that are self deafened in this server voice channel.
     *
     * @return The users that are self deafened in this server voice channel.
     */
    Collection<User> getSelfDeafenedUsers();

    /**
     * Checks whether the user with the given id is self deafened in this channel.
     *
     * @param userId The id of the user to check.
     * @return Whether the user with the given id is self deafened in this channel or not.
     */
    boolean isSelfDeafened(long userId);

    /**
     * Checks whether the given user is self deafened in this channel.
     *
     * @param user The user to check.
     * @return Whether the given user is self deafened in this channel or not.
     */
    default boolean isSelfDeafened(User user) {
        return isSelfDeafened(user.getId());
    }

    /**
     * Creates an updater for this channel.
     *
     * @return An updater for this channel.
     */
    default ServerVoiceChannelUpdater createUpdater() {
        return new ServerVoiceChannelUpdater(this);
    }

    /**
     * Updates the bitrate of the channel.
     *
     * <p>If you want to update several settings at once, it's recommended to use the
     * {@link ServerVoiceChannelUpdater} from {@link #createUpdater()} which provides a better performance!
     *
     * @param bitrate The new bitrate of the channel.
     * @return A future to check if the update was successful.
     */
    default CompletableFuture<Void> updateBitrate(int bitrate) {
        return createUpdater().setBitrate(bitrate).update();
    }

    /**
     * Updates the user limit of the channel.
     *
     * <p>If you want to update several settings at once, it's recommended to use the
     * {@link ServerVoiceChannelUpdater} from {@link #createUpdater()} which provides a better performance!
     *
     * @param userLimit The new user limit of the channel.
     * @return A future to check if the update was successful.
     */
    default CompletableFuture<Void> updateUserLimit(int userLimit) {
        return createUpdater().setUserLimit(userLimit).update();
    }

    /**
     * Removes the user limit of the channel.
     *
     * <p>If you want to update several settings at once, it's recommended to use the
     * {@link ServerVoiceChannelUpdater} from {@link #createUpdater()} which provides a better performance!
     *
     * @return A future to check if the update was successful.
     */
    default CompletableFuture<Void> removeUserLimit() {
        return createUpdater().removeUserLimit().update();
    }

    /**
     * {@inheritDoc}
     *
     * <p>If you want to update several settings at once, it's recommended to use the
     * {@link ServerVoiceChannelUpdater} from {@link #createUpdater()} which provides a better performance!
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
     * {@link ServerVoiceChannelUpdater} from {@link #createUpdater()} which provides a better performance!
     *
     * @return A future to check if the update was successful.
     */
    default CompletableFuture<Void> removeCategory() {
        return createUpdater().removeCategory().update();
    }

    @Override
    default Optional<ServerVoiceChannel> getCurrentCachedInstance() {
        return getApi().getServerById(getServer().getId()).flatMap(server -> server.getVoiceChannelById(getId()));
    }

    @Override
    default CompletableFuture<ServerVoiceChannel> getLatestInstance() {
        Optional<ServerVoiceChannel> currentCachedInstance = getCurrentCachedInstance();
        if (currentCachedInstance.isPresent()) {
            return CompletableFuture.completedFuture(currentCachedInstance.get());
        } else {
            CompletableFuture<ServerVoiceChannel> result = new CompletableFuture<>();
            result.completeExceptionally(new NoSuchElementException());
            return result;
        }
    }

}
