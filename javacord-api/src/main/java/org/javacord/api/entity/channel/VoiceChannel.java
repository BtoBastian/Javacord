package org.javacord.api.entity.channel;

import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.user.User;
import org.javacord.api.listener.channel.VoiceChannelAttachableListenerManager;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * This class represents a voice channel.
 */
public interface VoiceChannel extends Channel, VoiceChannelAttachableListenerManager {

    /**
     * Checks if the given user can connect to the voice channel.
     * In private chats (private channel or group channel) this always returns <code>true</code> if the user is
     * part of the chat.
     *
     * @param user The user to check.
     * @return Whether the given user can connect or not.
     * @deprecated Use {@link ServerVoiceChannel#canConnect(User)} instead.
     */
    @Deprecated
    default boolean canConnect(User user) {
        if (!canSee(user)) {
            return false;
        }
        Optional<ServerVoiceChannel> severVoiceChannel = asServerVoiceChannel();
        return !severVoiceChannel.isPresent()
                || severVoiceChannel.get().hasAnyPermission(user,
                PermissionType.ADMINISTRATOR,
                PermissionType.CONNECT);
    }

    /**
     * Checks if the user of the connected account can connect to the voice channel.
     * In private chats (private channel or group channel) this always returns {@code true} if the user is
     * part of the chat.
     *
     * @return Whether the user of the connected account can connect or not.
     * @deprecated Use {@link ServerVoiceChannel#canYouConnect()} instead.
     */
    @Deprecated
    default boolean canYouConnect() {
        return canConnect(getApi().getYourself());
    }

    /**
     * Checks if the given user can mute other users in this voice channel.
     * In private chats (private channel or group channel) this always returns <code>false</code>.
     *
     * @param user The user to check.
     * @return Whether the given user can mute other users or not.
     * @deprecated Use {@link ServerVoiceChannel#canMuteUsers(User)} instead.
     */
    @Deprecated
    default boolean canMuteUsers(User user) {
        if (!canConnect(user) || getType() == ChannelType.PRIVATE_CHANNEL || getType() == ChannelType.GROUP_CHANNEL) {
            return false;
        }
        Optional<ServerVoiceChannel> serverVoiceChannel = asServerVoiceChannel();
        return !serverVoiceChannel.isPresent()
                || serverVoiceChannel.get().hasAnyPermission(user,
                PermissionType.ADMINISTRATOR,
                PermissionType.MUTE_MEMBERS);
    }

    /**
     * Checks if the user of the connected account can mute other users in this voice channel.
     * In private chats (private channel or group channel) this always returns {@code false}.
     *
     * @return Whether the user of the connected account can mute other users or not.
     * @deprecated Use {@link ServerVoiceChannel#canYouMuteUsers()} instead.
     */
    @Deprecated
    default boolean canYouMuteUsers() {
        return canMuteUsers(getApi().getYourself());
    }

    @Override
    default Optional<? extends VoiceChannel> getCurrentCachedInstance() {
        return getApi().getVoiceChannelById(getId());
    }

    @Override
    default CompletableFuture<? extends VoiceChannel> getLatestInstance() {
        Optional<? extends VoiceChannel> currentCachedInstance = getCurrentCachedInstance();
        if (currentCachedInstance.isPresent()) {
            return CompletableFuture.completedFuture(currentCachedInstance.get());
        } else {
            CompletableFuture<? extends VoiceChannel> result = new CompletableFuture<>();
            result.completeExceptionally(new NoSuchElementException());
            return result;
        }
    }

}
