package org.javacord.api.entity.channel;

import org.javacord.api.entity.DiscordEntity;
import org.javacord.api.entity.UpdatableFromCache;
import org.javacord.api.entity.channel.internal.ChannelBase;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.user.User;
import org.javacord.api.listener.ChannelAttachableListener;
import org.javacord.api.listener.ObjectAttachableListener;
import org.javacord.api.util.Specifiable;
import org.javacord.api.util.event.ListenerManager;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * The class represents a channel.
 */
public interface Channel extends DiscordEntity, UpdatableFromCache, ChannelBase, Specifiable<ChannelBase> {

    /**
     * Gets the type of the channel.
     *
     * @return The type of the channel.
     */
    ChannelType getType();

    /**
     * Gets the channel as group channel.
     *
     * @return The channel as group channel.
     */
    default Optional<GroupChannel> asGroupChannel() {
        return as(GroupChannel.class);
    }

    /**
     * Gets the channel as private channel.
     *
     * @return The channel as private channel.
     */
    default Optional<PrivateChannel> asPrivateChannel() {
        return as(PrivateChannel.class);
    }

    /**
     * Gets the channel as server channel.
     *
     * @return The channel as server channel.
     */
    default Optional<ServerChannel> asServerChannel() {
        return as(ServerChannel.class);
    }

    /**
     * Gets the channel as channel category.
     *
     * @return The channel as channel category.
     */
    default Optional<ChannelCategory> asChannelCategory() {
        return as(ChannelCategory.class);
    }

    /**
     * Gets the channel as categorizable.
     *
     * @return The channel as categorizable.
     */
    default Optional<Categorizable> asCategorizable() {
        return as(Categorizable.class);
    }

    /**
     * Gets the channel as server text channel.
     *
     * @return The channel as server text channel.
     */
    default Optional<ServerTextChannel> asServerTextChannel() {
        return as(ServerTextChannel.class);
    }

    /**
     * Gets the channel as server voice channel.
     *
     * @return The channel as server voice channel.
     */
    default Optional<ServerVoiceChannel> asServerVoiceChannel() {
        return as(ServerVoiceChannel.class);
    }

    /**
     * Gets the channel as text channel.
     *
     * @return The channel as text channel.
     */
    default Optional<TextChannel> asTextChannel() {
        return as(TextChannel.class);
    }

    /**
     * Gets the channel as voice channel.
     *
     * @return The channel as voice channel.
     */
    default Optional<VoiceChannel> asVoiceChannel() {
        return as(VoiceChannel.class);
    }


    /**
     * Checks if the given user can see this channel.
     * In private chats (private channel or group channel) this always returns <code>true</code> if the user is
     * part of the chat.
     *
     * @param user The user to check.
     * @return Whether the given user can see this channel or not.
     */
    default boolean canSee(User user) {
        Optional<PrivateChannel> privateChannel = asPrivateChannel();
        if (privateChannel.isPresent()) {
            return user.isYourself() || privateChannel.get().getRecipient() == user;
        }
        Optional<GroupChannel> groupChannel = asGroupChannel();
        if (groupChannel.isPresent()) {
            return user.isYourself() || groupChannel.get().getMembers().contains(user);
        }
        Optional<ServerChannel> severChannel = asServerChannel();
        return !severChannel.isPresent()
                || severChannel.get().hasAnyPermission(user,
                                                       PermissionType.ADMINISTRATOR,
                                                       PermissionType.READ_MESSAGES);
    }

    /**
     * Checks if the user of the connected account can see this channel.
     * In private chats (private channel or group channel) this always returns {@code true} if the user is
     * part of the chat.
     *
     * @return Whether the user of the connected account can see this channel or not.
     */
    default boolean canYouSee() {
        return canSee(getApi().getYourself());
    }

    /**
     * Adds a listener that implements one or more {@code ChannelAttachableListener}s.
     * Adding a listener multiple times will only add it once
     * and return the same listener managers on each invocation.
     * The order of invocation is according to first addition.
     *
     * @param listener The listener to add.
     * @param <T> The type of the listener.
     * @return The managers for the added listener.
     */
    <T extends ChannelAttachableListener & ObjectAttachableListener> Collection<ListenerManager<T>>
            addChannelAttachableListener(T listener);

    /**
     * Removes a listener that implements one or more {@code ChannelAttachableListener}s.
     *
     * @param listener The listener to remove.
     * @param <T> The type of the listener.
     */
    <T extends ChannelAttachableListener & ObjectAttachableListener> void removeChannelAttachableListener(T listener);

    /**
     * Gets a map with all registered listeners that implement one or more {@code ChannelAttachableListener}s and
     * their assigned listener classes they listen to.
     *
     * @param <T> The type of the listeners.
     * @return A map with all registered listeners that implement one or more {@code ChannelAttachableListener}s
     * and their assigned listener classes they listen to.
     */
    <T extends ChannelAttachableListener & ObjectAttachableListener> Map<T, List<Class<T>>>
            getChannelAttachableListeners();

    /**
     * Removes a listener from this channel.
     *
     * @param listenerClass The listener class.
     * @param listener The listener to remove.
     * @param <T> The type of the listener.
     */
    <T extends ChannelAttachableListener & ObjectAttachableListener> void removeListener(
            Class<T> listenerClass, T listener);

    @Override
    default Optional<? extends Channel> getCurrentCachedInstance() {
        return getApi().getChannelById(getId());
    }

    @Override
    default CompletableFuture<? extends Channel> getLatestInstance() {
        Optional<? extends Channel> currentCachedInstance = getCurrentCachedInstance();
        if (currentCachedInstance.isPresent()) {
            return CompletableFuture.completedFuture(currentCachedInstance.get());
        } else {
            CompletableFuture<? extends Channel> result = new CompletableFuture<>();
            result.completeExceptionally(new NoSuchElementException());
            return result;
        }
    }

}
