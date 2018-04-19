package org.javacord.api.entity.message;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.DiscordEntity;
import org.javacord.api.entity.UpdatableFromCache;
import org.javacord.api.entity.channel.GroupChannel;
import org.javacord.api.entity.channel.PrivateChannel;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.emoji.CustomEmoji;
import org.javacord.api.entity.emoji.Emoji;
import org.javacord.api.entity.message.embed.Embed;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.listener.ObjectAttachableListener;
import org.javacord.api.listener.message.CachedMessagePinListener;
import org.javacord.api.listener.message.CachedMessageUnpinListener;
import org.javacord.api.listener.message.MessageAttachableListener;
import org.javacord.api.listener.message.MessageDeleteListener;
import org.javacord.api.listener.message.MessageEditListener;
import org.javacord.api.listener.message.reaction.ReactionAddListener;
import org.javacord.api.listener.message.reaction.ReactionRemoveAllListener;
import org.javacord.api.listener.message.reaction.ReactionRemoveListener;
import org.javacord.api.util.DiscordRegexPattern;
import org.javacord.api.util.event.ListenerManager;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * This class represents a Discord message.
 */
public interface Message extends DiscordEntity, Comparable<Message>, UpdatableFromCache<Message> {

    Pattern ESCAPED_CHARACTER =
            Pattern.compile("\\\\(?<char>[^a-zA-Z0-9\\p{javaWhitespace}\\xa0\\u2007\\u202E\\u202F])");

    /**
     * Deletes the message.
     *
     * @param api The discord api instance.
     * @param channelId The id of the message's channel.
     * @param messageId The id of the message.
     * @return A future to tell us if the deletion was successful.
     */
    static CompletableFuture<Void> delete(DiscordApi api, long channelId, long messageId) {
        return api.getUncachedMessageUtil().delete(channelId, messageId);
    }

    /**
     * Deletes the message.
     *
     * @param api The discord api instance.
     * @param channelId The id of the message's channel.
     * @param messageId The id of the message.
     * @return A future to tell us if the deletion was successful.
     */
    static CompletableFuture<Void> delete(DiscordApi api, String channelId, String messageId) {
        return api.getUncachedMessageUtil().delete(channelId, messageId, null);
    }

    /**
     * Deletes the message.
     *
     * @param api The discord api instance.
     * @param channelId The id of the message's channel.
     * @param messageId The id of the message.
     * @param reason The audit log reason for the deletion.
     * @return A future to tell us if the deletion was successful.
     */
    static CompletableFuture<Void> delete(DiscordApi api, long channelId, long messageId, String reason) {
        return api.getUncachedMessageUtil().delete(channelId, messageId, reason);
    }

    /**
     * Deletes the message.
     *
     * @param api The discord api instance.
     * @param channelId The id of the message's channel.
     * @param messageId The id of the message.
     * @param reason The audit log reason for the deletion.
     * @return A future to tell us if the deletion was successful.
     */
    static CompletableFuture<Void> delete(DiscordApi api, String channelId, String messageId, String reason) {
        return api.getUncachedMessageUtil().delete(channelId, messageId, reason);
    }

    /**
     * Deletes multiple messages at once.
     * This method does not have a size or age restriction.
     * Messages younger than two weeks are sent in batches of 100 messages to the bulk delete API,
     * older messages are deleted with individual delete requests.
     *
     * @param api The discord api instance.
     * @param channelId The id of the message's channel.
     * @param messageIds The ids of the messages to delete.
     * @return A future to tell us if the deletion was successful.
     */
    static CompletableFuture<Void> deleteAll(DiscordApi api, long channelId, long... messageIds) {
        return api.getUncachedMessageUtil().deleteAll(channelId, messageIds);
    }

    /**
     * Deletes multiple messages at once.
     * This method does not have a size or age restriction.
     * Messages younger than two weeks are sent in batches of 100 messages to the bulk delete API,
     * older messages are deleted with individual delete requests.
     *
     * @param api The discord api instance.
     * @param channelId The id of the message's channel.
     * @param messageIds The ids of the messages to delete.
     * @return A future to tell us if the deletion was successful.
     */
    static CompletableFuture<Void> deleteAll(DiscordApi api, String channelId, String... messageIds) {
        return api.getUncachedMessageUtil().deleteAll(channelId, messageIds);
    }

    /**
     * Deletes multiple messages at once.
     * This method does not have a size or age restriction.
     * Messages younger than two weeks are sent in batches of 100 messages to the bulk delete API,
     * older messages are deleted with individual delete requests.
     *
     * @param api The discord api instance.
     * @param messages The messages to delete.
     * @return A future to tell us if the deletion was successful.
     */
    static CompletableFuture<Void> deleteAll(DiscordApi api, Message... messages) {
        return api.getUncachedMessageUtil().deleteAll(messages);
    }

    /**
     * Deletes multiple messages at once.
     * This method does not have a size or age restriction.
     * Messages younger than two weeks are sent in batches of 100 messages to the bulk delete API,
     * older messages are deleted with individual delete requests.
     *
     * @param api The discord api instance.
     * @param messages The messages to delete.
     * @return A future to tell us if the deletion was successful.
     */
    static CompletableFuture<Void> deleteAll(DiscordApi api, Iterable<Message> messages) {
        return api.getUncachedMessageUtil().deleteAll(messages);
    }

    /**
     * Updates the content of the message.
     *
     * @param api The discord api instance.
     * @param channelId The id of the message's channel.
     * @param messageId The id of the message.
     * @param content The new content of the message.
     * @return A future to check if the update was successful.
     */
    static CompletableFuture<Void> edit(DiscordApi api, long channelId, long messageId, String content) {
        return api.getUncachedMessageUtil().edit(channelId, messageId, content, true, null, false);
    }

    /**
     * Updates the content of the message.
     *
     * @param api The discord api instance.
     * @param channelId The id of the message's channel.
     * @param messageId The id of the message.
     * @param content The new content of the message.
     * @return A future to check if the update was successful.
     */
    static CompletableFuture<Void> edit(DiscordApi api, String channelId, String messageId, String content) {
        return api.getUncachedMessageUtil().edit(channelId, messageId, content, true, null, false);
    }

    /**
     * Updates the embed of the message.
     *
     * @param api The discord api instance.
     * @param channelId The id of the message's channel.
     * @param messageId The id of the message.
     * @param embed The new embed of the message.
     * @return A future to check if the update was successful.
     */
    static CompletableFuture<Void> edit(DiscordApi api, long channelId, long messageId, EmbedBuilder embed) {
        return api.getUncachedMessageUtil().edit(channelId, messageId, null, false, embed, true);
    }

    /**
     * Updates the embed of the message.
     *
     * @param api The discord api instance.
     * @param channelId The id of the message's channel.
     * @param messageId The id of the message.
     * @param embed The new embed of the message.
     * @return A future to check if the update was successful.
     */
    static CompletableFuture<Void> edit(DiscordApi api, String channelId, String messageId, EmbedBuilder embed) {
        return api.getUncachedMessageUtil().edit(channelId, messageId, null, false, embed, true);
    }

    /**
     * Updates the content and the embed of the message.
     *
     * @param api The discord api instance.
     * @param channelId The id of the message's channel.
     * @param messageId The id of the message.
     * @param content The new content of the message.
     * @param embed The new embed of the message.
     * @return A future to check if the update was successful.
     */
    static CompletableFuture<Void> edit(
            DiscordApi api, long channelId, long messageId, String content, EmbedBuilder embed) {
        return api.getUncachedMessageUtil().edit(channelId, messageId, content, true, embed, true);
    }

    /**
     * Updates the content and the embed of the message.
     *
     * @param api The discord api instance.
     * @param channelId The id of the message's channel.
     * @param messageId The id of the message.
     * @param content The new content of the message.
     * @param embed The new embed of the message.
     * @return A future to check if the update was successful.
     */
    static CompletableFuture<Void> edit(
            DiscordApi api, String channelId, String messageId, String content, EmbedBuilder embed) {
        return api.getUncachedMessageUtil().edit(channelId, messageId, content, true, embed, true);
    }

    /**
     * Updates the content and the embed of the message.
     *
     * @param api The discord api instance.
     * @param channelId The id of the message's channel.
     * @param messageId The id of the message.
     * @param content The new content of the message.
     * @param updateContent Whether to update or remove the content.
     * @param embed The new embed of the message.
     * @param updateEmbed Whether to update or remove the embed.
     * @return A future to check if the update was successful.
     */
    static CompletableFuture<Void> edit(DiscordApi api, long channelId, long messageId, String content,
                                        boolean updateContent, EmbedBuilder embed, boolean updateEmbed) {
        return api.getUncachedMessageUtil().edit(channelId, messageId, content, updateContent, embed, updateEmbed);
    }

    /**
     * Updates the content and the embed of the message.
     *
     * @param api The discord api instance.
     * @param channelId The id of the message's channel.
     * @param messageId The id of the message.
     * @param content The new content of the message.
     * @param updateContent Whether to update or remove the content.
     * @param embed The new embed of the message.
     * @param updateEmbed Whether to update or remove the embed.
     * @return A future to check if the update was successful.
     */
    static CompletableFuture<Void> edit(DiscordApi api, String channelId, String messageId, String content,
                                        boolean updateContent, EmbedBuilder embed, boolean updateEmbed) {
        return api.getUncachedMessageUtil().edit(channelId, messageId, content, updateContent, embed, updateEmbed);
    }

    /**
     * Removes the content of the message.
     *
     * @param api The discord api instance.
     * @param channelId The id of the message's channel.
     * @param messageId The id of the message.
     * @return A future to check if the removal was successful.
     */
    static CompletableFuture<Void> removeContent(DiscordApi api, long channelId, long messageId) {
        return api.getUncachedMessageUtil().edit(channelId, messageId, null, true, null, false);
    }

    /**
     * Removes the content of the message.
     *
     * @param api The discord api instance.
     * @param channelId The id of the message's channel.
     * @param messageId The id of the message.
     * @return A future to check if the removal was successful.
     */
    static CompletableFuture<Void> removeContent(DiscordApi api, String channelId, String messageId) {
        return api.getUncachedMessageUtil().edit(channelId, messageId, null, true, null, false);
    }

    /**
     * Removes the embed of the message.
     *
     * @param api The discord api instance.
     * @param channelId The id of the message's channel.
     * @param messageId The id of the message.
     * @return A future to check if the removal was successful.
     */
    static CompletableFuture<Void> removeEmbed(DiscordApi api, long channelId, long messageId) {
        return api.getUncachedMessageUtil().edit(channelId, messageId, null, false, null, true);
    }

    /**
     * Removes the embed of the message.
     *
     * @param api The discord api instance.
     * @param channelId The id of the message's channel.
     * @param messageId The id of the message.
     * @return A future to check if the removal was successful.
     */
    static CompletableFuture<Void> removeEmbed(DiscordApi api, String channelId, String messageId) {
        return api.getUncachedMessageUtil().edit(channelId, messageId, null, false, null, true);
    }

    /**
     * Removes the content and embed of the message.
     *
     * @param api The discord api instance.
     * @param channelId The id of the message's channel.
     * @param messageId The id of the message.
     * @return A future to check if the removal was successful.
     */
    static CompletableFuture<Void> removeContentAndEmbed(DiscordApi api, long channelId, long messageId) {
        return api.getUncachedMessageUtil().edit(channelId, messageId, null, true, null, true);
    }

    /**
     * Removes the content and embed of the message.
     *
     * @param api The discord api instance.
     * @param channelId The id of the message's channel.
     * @param messageId The id of the message.
     * @return A future to check if the removal was successful.
     */
    static CompletableFuture<Void> removeContentAndEmbed(DiscordApi api, String channelId, String messageId) {
        return api.getUncachedMessageUtil().edit(channelId, messageId, null, true, null, true);
    }

    /**
     * Adds a unicode reaction to the message.
     *
     * @param api The discord api instance.
     * @param channelId The id of the message's channel.
     * @param messageId The id of the message.
     * @param unicodeEmoji The unicode emoji string.
     * @return A future to tell us if the action was successful.
     */
    static CompletableFuture<Void> addReaction(DiscordApi api, long channelId, long messageId, String unicodeEmoji) {
        return api.getUncachedMessageUtil().addReaction(channelId, messageId, unicodeEmoji);
    }

    /**
     * Adds a unicode reaction to the message.
     *
     * @param api The discord api instance.
     * @param channelId The id of the message's channel.
     * @param messageId The id of the message.
     * @param unicodeEmoji The unicode emoji string.
     * @return A future to tell us if the action was successful.
     */
    static CompletableFuture<Void> addReaction(
            DiscordApi api, String channelId, String messageId, String unicodeEmoji) {
        return api.getUncachedMessageUtil().addReaction(channelId, messageId, unicodeEmoji);
    }

    /**
     * Adds a reaction to the message.
     *
     * @param api The discord api instance.
     * @param channelId The id of the message's channel.
     * @param messageId The id of the message.
     * @param emoji The emoji.
     * @return A future to tell us if the action was successful.
     */
    static CompletableFuture<Void> addReaction(DiscordApi api, long channelId, long messageId, Emoji emoji) {
        return api.getUncachedMessageUtil().addReaction(channelId, messageId, emoji);
    }

    /**
     * Adds a reaction to the message.
     *
     * @param api The discord api instance.
     * @param channelId The id of the message's channel.
     * @param messageId The id of the message.
     * @param emoji The emoji.
     * @return A future to tell us if the action was successful.
     */
    static CompletableFuture<Void> addReaction(DiscordApi api, String channelId, String messageId, Emoji emoji) {
        return api.getUncachedMessageUtil().addReaction(channelId, messageId, emoji);
    }

    /**
     * Deletes all reactions on this message.
     *
     * @param api The discord api instance.
     * @param channelId The id of the message's channel.
     * @param messageId The id of the message.
     * @return A future to tell us if the deletion was successful.
     */
    static CompletableFuture<Void> removeAllReactions(DiscordApi api, long channelId, long messageId) {
        return api.getUncachedMessageUtil().removeAllReactions(channelId, messageId);
    }

    /**
     * Deletes all reactions on this message.
     *
     * @param api The discord api instance.
     * @param channelId The id of the message's channel.
     * @param messageId The id of the message.
     * @return A future to tell us if the deletion was successful.
     */
    static CompletableFuture<Void> removeAllReactions(DiscordApi api, String channelId, String messageId) {
        return api.getUncachedMessageUtil().removeAllReactions(channelId, messageId);
    }

    /**
     * Pins this message.
     *
     * @param api The discord api instance.
     * @param channelId The id of the message's channel.
     * @param messageId The id of the message.
     * @return A future to tell us if the pin was successful.
     */
    static CompletableFuture<Void> pin(DiscordApi api, long channelId, long messageId) {
        return api.getUncachedMessageUtil().pin(channelId, messageId);
    }

    /**
     * Pins this message.
     *
     * @param api The discord api instance.
     * @param channelId The id of the message's channel.
     * @param messageId The id of the message.
     * @return A future to tell us if the pin was successful.
     */
    static CompletableFuture<Void> pin(DiscordApi api, String channelId, String messageId) {
        return api.getUncachedMessageUtil().pin(channelId, messageId);
    }

    /**
     * Unpins this message.
     *
     * @param api The discord api instance.
     * @param channelId The id of the message's channel.
     * @param messageId The id of the message.
     * @return A future to tell us if the action was successful.
     */
    static CompletableFuture<Void> unpin(DiscordApi api, long channelId, long messageId) {
        return api.getUncachedMessageUtil().unpin(channelId, messageId);
    }

    /**
     * Unpins this message.
     *
     * @param api The discord api instance.
     * @param channelId The id of the message's channel.
     * @param messageId The id of the message.
     * @return A future to tell us if the action was successful.
     */
    static CompletableFuture<Void> unpin(DiscordApi api, String channelId, String messageId) {
        return api.getUncachedMessageUtil().unpin(channelId, messageId);
    }

    /**
     * Adds a listener, which listens to message deletions of a specific message.
     *
     * @param api The discord api instance.
     * @param messageId The id of the message which should be listened to.
     * @param listener The listener to add.
     * @return The manager of the listener.
     */
    static ListenerManager<MessageDeleteListener> addMessageDeleteListener(
            DiscordApi api, long messageId, MessageDeleteListener listener) {
        return api.getUncachedMessageUtil().addMessageDeleteListener(messageId, listener);
    }

    /**
     * Adds a listener, which listens to message deletions of a specific message.
     *
     * @param api The discord api instance.
     * @param message The message which should be listened to.
     * @param listener The listener to add.
     * @return The manager of the listener.
     */
    static ListenerManager<MessageDeleteListener> addMessageDeleteListener(
            DiscordApi api, Message message, MessageDeleteListener listener) {
        return api.getUncachedMessageUtil().addMessageDeleteListener(message, listener);
    }

    /**
     * Gets a list with all registered message delete listeners of a specific message.
     *
     * @param api The discord api instance.
     * @param messageId The id of the message.
     * @return A list with all registered message delete listeners.
     */
    static List<MessageDeleteListener> getMessageDeleteListeners(DiscordApi api, long messageId) {
        return api.getUncachedMessageUtil().getMessageDeleteListeners(messageId);
    }

    /**
     * Gets a list with all registered message delete listeners of a specific message.
     *
     * @param api The discord api instance.
     * @param messageId The id of the message.
     * @return A list with all registered message delete listeners.
     */
    static List<MessageDeleteListener> getMessageDeleteListeners(DiscordApi api, String messageId) {
        return api.getUncachedMessageUtil().getMessageDeleteListeners(messageId);
    }

    /**
     * Gets a list with all registered message delete listeners of a specific message.
     *
     * @param api The discord api instance.
     * @param message The message.
     * @return A list with all registered message delete listeners.
     */
    static List<MessageDeleteListener> getMessageDeleteListeners(DiscordApi api, Message message) {
        return api.getUncachedMessageUtil().getMessageDeleteListeners(message);
    }

    /**
     * Adds a listener, which listens to message edits of a specific message.
     *
     * @param api The discord api instance.
     * @param messageId The id of the message which should be listened to.
     * @param listener The listener to add.
     * @return The manager of the listener.
     */
    static ListenerManager<MessageEditListener> addMessageEditListener(DiscordApi api, long messageId,
                                                                       MessageEditListener listener) {
        return api.getUncachedMessageUtil().addMessageEditListener(messageId, listener);
    }

    /**
     * Adds a listener, which listens to message edits of a specific message.
     *
     * @param api The discord api instance.
     * @param message The message which should be listened to.
     * @param listener The listener to add.
     * @return The manager of the listener.
     */
    static ListenerManager<MessageEditListener> addMessageEditListener(DiscordApi api, Message message,
                                                                       MessageEditListener listener) {
        return api.getUncachedMessageUtil().addMessageEditListener(message, listener);
    }

    /**
     * Gets a list with all registered message edit listeners of a specific message.
     *
     * @param api The discord api instance.
     * @param messageId The id of the message.
     * @return A list with all registered message edit listeners.
     */
    static List<MessageEditListener> getMessageEditListeners(DiscordApi api, long messageId) {
        return api.getUncachedMessageUtil().getMessageEditListeners(messageId);
    }

    /**
     * Gets a list with all registered message edit listeners of a specific message.
     *
     * @param api The discord api instance.
     * @param messageId The id of the message.
     * @return A list with all registered message edit listeners.
     */
    static List<MessageEditListener> getMessageEditListeners(DiscordApi api, String messageId) {
        return api.getUncachedMessageUtil().getMessageEditListeners(messageId);
    }

    /**
     * Gets a list with all registered message edit listeners of a specific message.
     *
     * @param api The discord api instance.
     * @param message The message.
     * @return A list with all registered message edit listeners.
     */
    static List<MessageEditListener> getMessageEditListeners(DiscordApi api, Message message) {
        return api.getUncachedMessageUtil().getMessageEditListeners(message);
    }

    /**
     * Adds a listener, which listens to reactions being added to a specific message.
     *
     * @param api The discord api instance.
     * @param messageId The id of the message which should be listened to.
     * @param listener The listener to add.
     * @return The manager of the listener.
     */
    static ListenerManager<ReactionAddListener> addReactionAddListener(DiscordApi api, long messageId,
                                                                       ReactionAddListener listener) {
        return api.getUncachedMessageUtil().addReactionAddListener(messageId, listener);
    }

    /**
     * Adds a listener, which listens to reactions being added to a specific message.
     *
     * @param api The discord api instance.
     * @param message The message which should be listened to.
     * @param listener The listener to add.
     * @return The manager of the listener.
     */
    static ListenerManager<ReactionAddListener> addReactionAddListener(
            DiscordApi api, Message message, ReactionAddListener listener) {
        return api.getUncachedMessageUtil().addReactionAddListener(message, listener);
    }

    /**
     * Gets a list with all registered reaction add listeners of a specific message.
     *
     * @param api The discord api instance.
     * @param messageId The id of the message.
     * @return A list with all registered reaction add listeners.
     */
    static List<ReactionAddListener> getReactionAddListeners(DiscordApi api, long messageId) {
        return api.getUncachedMessageUtil().getReactionAddListeners(messageId);
    }

    /**
     * Gets a list with all registered reaction add listeners of a specific message.
     *
     * @param api The discord api instance.
     * @param messageId The id of the message.
     * @return A list with all registered reaction add listeners.
     */
    static List<ReactionAddListener> getReactionAddListeners(DiscordApi api, String messageId) {
        return api.getUncachedMessageUtil().getReactionAddListeners(messageId);
    }

    /**
     * Gets a list with all registered reaction add listeners of a specific message.
     *
     * @param api The discord api instance.
     * @param message The message.
     * @return A list with all registered reaction add listeners.
     */
    static List<ReactionAddListener> getReactionAddListeners(DiscordApi api, Message message) {
        return api.getUncachedMessageUtil().getReactionAddListeners(message);
    }

    /**
     * Adds a listener, which listens to reactions being removed from a specific message.
     *
     * @param api The discord api instance.
     * @param messageId The id of the message which should be listened to.
     * @param listener The listener to add.
     * @return The manager of the listener.
     */
    static ListenerManager<ReactionRemoveListener> addReactionRemoveListener(
            DiscordApi api, long messageId, ReactionRemoveListener listener) {
        return api.getUncachedMessageUtil().addReactionRemoveListener(messageId, listener);
    }

    /**
     * Adds a listener, which listens to reactions being removed from a specific message.
     *
     * @param api The discord api instance.
     * @param message The message which should be listened to.
     * @param listener The listener to add.
     * @return The manager of the listener.
     */
    static ListenerManager<ReactionRemoveListener> addReactionRemoveListener(
            DiscordApi api, Message message, ReactionRemoveListener listener) {
        return api.getUncachedMessageUtil().addReactionRemoveListener(message, listener);
    }

    /**
     * Gets a list with all registered reaction remove listeners of a specific message.
     *
     * @param api The discord api instance.
     * @param messageId The id of the message.
     * @return A list with all registered reaction remove listeners.
     */
    static List<ReactionRemoveListener> getReactionRemoveListeners(DiscordApi api, long messageId) {
        return api.getUncachedMessageUtil().getReactionRemoveListeners(messageId);
    }

    /**
     * Gets a list with all registered reaction remove listeners of a specific message.
     *
     * @param api The discord api instance.
     * @param messageId The id of the message.
     * @return A list with all registered reaction remove listeners.
     */
    static List<ReactionRemoveListener> getReactionRemoveListeners(DiscordApi api, String messageId) {
        return api.getUncachedMessageUtil().getReactionRemoveListeners(messageId);
    }

    /**
     * Gets a list with all registered reaction remove listeners of a specific message.
     *
     * @param api The discord api instance.
     * @param message The message.
     * @return A list with all registered reaction remove listeners.
     */
    static List<ReactionRemoveListener> getReactionRemoveListeners(DiscordApi api, Message message) {
        return api.getUncachedMessageUtil().getReactionRemoveListeners(message);
    }

    /**
     * Adds a listener, which listens to all reactions being removed from a specific message at once.
     *
     * @param api The discord api instance.
     * @param messageId The id of the message which should be listened to.
     * @param listener The listener to add.
     * @return The manager of the listener.
     */
    static ListenerManager<ReactionRemoveAllListener> addReactionRemoveAllListener(
            DiscordApi api, long messageId, ReactionRemoveAllListener listener) {
        return api.getUncachedMessageUtil().addReactionRemoveAllListener(messageId, listener);
    }

    /**
     * Adds a listener, which listens to all reactions being removed from a specific message at once.
     *
     * @param api The discord api instance.
     * @param message The message which should be listened to.
     * @param listener The listener to add.
     * @return The manager of the listener.
     */
    static ListenerManager<ReactionRemoveAllListener> addReactionRemoveAllListener(
            DiscordApi api, Message message, ReactionRemoveAllListener listener) {
        return api.getUncachedMessageUtil().addReactionRemoveAllListener(message, listener);
    }

    /**
     * Gets a list with all registered reaction remove all listeners of a specific message.
     *
     * @param api The discord api instance.
     * @param messageId The id of the message.
     * @return A list with all registered reaction remove all listeners.
     */
    static List<ReactionRemoveAllListener> getReactionRemoveAllListeners(DiscordApi api, long messageId) {
        return api.getUncachedMessageUtil().getReactionRemoveAllListeners(messageId);
    }

    /**
     * Gets a list with all registered reaction remove all listeners of a specific message.
     *
     * @param api The discord api instance.
     * @param messageId The id of the message.
     * @return A list with all registered reaction remove all listeners.
     */
    static List<ReactionRemoveAllListener> getReactionRemoveAllListeners(DiscordApi api, String messageId) {
        return api.getUncachedMessageUtil().getReactionRemoveAllListeners(messageId);
    }

    /**
     * Gets a list with all registered reaction remove all listeners of a specific message.
     *
     * @param api The discord api instance.
     * @param message The message.
     * @return A list with all registered reaction remove all listeners.
     */
    static List<ReactionRemoveAllListener> getReactionRemoveAllListeners(DiscordApi api, Message message) {
        return api.getUncachedMessageUtil().getReactionRemoveAllListeners(message);
    }

    /**
     * Adds a listener that implements one or more {@code MessageAttachableListener}s to the message with the given id.
     * Adding a listener multiple times will only add it once
     * and return the same listener managers on each invocation.
     * The order of invocation is according to first addition.
     *
     * @param api The discord api instance.
     * @param messageId The id of the message which should be listened to.
     * @param listener The listener to add.
     * @param <T> The type of the listener.
     * @return The managers for the added listener.
     */
    static <T extends MessageAttachableListener & ObjectAttachableListener> Collection<ListenerManager<T>>
    addMessageAttachableListener(DiscordApi api, long messageId, T listener) {
        return api.getUncachedMessageUtil().addMessageAttachableListener(messageId, listener);
    }

    /**
     * Adds a listener that implements one or more {@code MessageAttachableListener}s to the message with the given id.
     * Adding a listener multiple times will only add it once
     * and return the same listener managers on each invocation.
     * The order of invocation is according to first addition.
     *
     * @param api The discord api instance.
     * @param messageId The id of the message which should be listened to.
     * @param listener The listener to add.
     * @param <T> The type of the listener.
     * @return The managers for the added listener.
     */
    static <T extends MessageAttachableListener & ObjectAttachableListener> Collection<ListenerManager<T>>
    addMessageAttachableListener(DiscordApi api, String messageId, T listener) {
        return api.getUncachedMessageUtil().addMessageAttachableListener(messageId, listener);
    }

    /**
     * Adds a listener that implements one or more {@code MessageAttachableListener}s to the given message.
     * Adding a listener multiple times will only add it once
     * and return the same listener managers on each invocation.
     * The order of invocation is according to first addition.
     *
     * @param api The discord api instance.
     * @param message The message which should be listened to.
     * @param listener The listener to add.
     * @param <T> The type of the listener.
     * @return The managers for the added listener.
     */
    static <T extends MessageAttachableListener & ObjectAttachableListener> Collection<ListenerManager<T>>
    addMessageAttachableListener(DiscordApi api, Message message, T listener) {
        return api.getUncachedMessageUtil().addMessageAttachableListener(message, listener);
    }

    /**
     * Removes a {@code MessageAttachableListener} from the message with the given id.
     *
     * @param api The discord api instance.
     * @param messageId The id of the message.
     * @param listenerClass The listener class.
     * @param listener The listener to remove.
     * @param <T> The type of the listener.
     */
    static <T extends MessageAttachableListener & ObjectAttachableListener> void removeListener(
            DiscordApi api, long messageId, Class<T> listenerClass, T listener) {
        api.getUncachedMessageUtil().removeListener(messageId, listenerClass, listener);
    }

    /**
     * Removes a {@code MessageAttachableListener} from the message with the given id.
     *
     * @param api The discord api instance.
     * @param messageId The id of the message.
     * @param listenerClass The listener class.
     * @param listener The listener to remove.
     * @param <T> The type of the listener.
     */
    static <T extends MessageAttachableListener & ObjectAttachableListener> void removeListener(
            DiscordApi api, String messageId, Class<T> listenerClass, T listener) {
        api.getUncachedMessageUtil().removeListener(messageId, listenerClass, listener);
    }

    /**
     * Removes a listener that implements one or more {@code MessageAttachableListener}s from the given message.
     *
     * @param api The discord api instance.
     * @param message The message.
     * @param listenerClass The listener class.
     * @param listener The listener to remove.
     * @param <T> The type of the listener.
     */
    static <T extends MessageAttachableListener & ObjectAttachableListener> void removeListener(
            DiscordApi api, Message message, Class<T> listenerClass, T listener) {
        api.getUncachedMessageUtil().removeListener(message, listenerClass, listener);
    }

    /**
     * Removes a listener that implements one or more {@code MessageAttachableListener}s from the message with the given
     * id.
     *
     * @param api The discord api instance.
     * @param messageId The id of the message.
     * @param listener The listener to remove.
     * @param <T> The type of the listener.
     */
    static <T extends MessageAttachableListener & ObjectAttachableListener> void removeMessageAttachableListener(
            DiscordApi api, long messageId, T listener) {
        api.getUncachedMessageUtil().removeMessageAttachableListener(messageId, listener);
    }

    /**
     * Removes a listener that implements one or more {@code MessageAttachableListener}s from the message with the given
     * id.
     *
     * @param api The discord api instance.
     * @param messageId The id of the message.
     * @param listener The listener to remove.
     * @param <T> The type of the listener.
     */
    static <T extends MessageAttachableListener & ObjectAttachableListener> void removeMessageAttachableListener(
            DiscordApi api, String messageId, T listener) {
        api.getUncachedMessageUtil().removeMessageAttachableListener(messageId, listener);
    }

    /**
     * Removes a listener that implements one or more {@code MessageAttachableListener}s from the given message.
     *
     * @param api The discord api instance.
     * @param message The message.
     * @param listener The listener to remove.
     * @param <T> The type of the listener.
     */
    static <T extends MessageAttachableListener & ObjectAttachableListener> void removeMessageAttachableListener(
            DiscordApi api, Message message, T listener) {
        api.getUncachedMessageUtil().removeMessageAttachableListener(message, listener);
    }

    /**
     * Gets a map with all registered listeners that implement one or more {@code MessageAttachableListener}s and their
     * assigned listener classes they listen to for the message with the given id.
     *
     * @param api The discord api instance.
     * @param messageId The id of the message.
     * @param <T> The type of the listeners.
     * @return A map with all registered listeners that implement one or more {@code MessageAttachableListener}s and
     * their assigned listener classes they listen to.
     */
    static <T extends MessageAttachableListener & ObjectAttachableListener> Map<T, List<Class<T>>>
    getMessageAttachableListeners(DiscordApi api, long messageId) {
        return api.getUncachedMessageUtil().getMessageAttachableListeners(messageId);
    }

    /**
     * Gets a map with all registered listeners that implement one or more {@code MessageAttachableListener}s and their
     * assigned listener classes they listen to for the message with the given id.
     *
     * @param api The discord api instance.
     * @param messageId The id of the message.
     * @param <T> The type of the listeners.
     * @return A map with all registered listeners that implement one or more {@code MessageAttachableListener}s and
     * their assigned listener classes they listen to.
     */
    static <T extends MessageAttachableListener & ObjectAttachableListener> Map<T, List<Class<T>>>
    getMessageAttachableListeners(DiscordApi api, String messageId) {
        return api.getUncachedMessageUtil().getMessageAttachableListeners(messageId);
    }

    /**
     * Gets a map with all registered listeners that implement one or more {@code MessageAttachableListener}s and their
     * assigned listener classes they listen to for the given message.
     *
     * @param api The discord api instance.
     * @param message The message.
     * @param <T> The type of the listeners.
     * @return A map with all registered listeners that implement one or more {@code MessageAttachableListener}s and
     * their assigned listener classes they listen to.
     */
    static <T extends MessageAttachableListener & ObjectAttachableListener> Map<T, List<Class<T>>>
    getMessageAttachableListeners(DiscordApi api, Message message) {
        return api.getUncachedMessageUtil().getMessageAttachableListeners(message);
    }

    /**
     * Gets the content of the message.
     *
     * @return The content of the message.
     */
    String getContent();

    /**
     * Gets the last time the message was edited.
     *
     * @return The last time the message was edited.
     */
    Optional<Instant> getLastEditTimestamp();

    /**
     * Gets the attachments of the message.
     *
     * @return The attachments of the message.
     */
    List<MessageAttachment> getAttachments();

    /**
     * Gets the readable content of the message, which replaces all mentions etc. with the actual name.
     * The replacement happens as following:
     * <ul>
     * <li><b>User mentions</b>:
     * <code>@nickname</code> if the user has a nickname, <code>@name</code> if the user has no nickname, unchanged if
     * the user is not in the cache.
     * <li><b>Channel mentions</b>:
     * <code>#name</code> if the text channel exists in the server, otherwise <code>#deleted-channel</code>
     * <li><b>Custom emoji</b>:
     * <code>:name:</code>. If the emoji is known, the real name is used, otherwise the name from the mention tag.
     * </ul>
     *
     * @return The readable content of the message.
     */
    default String getReadableContent() {
        String content = getContent();
        Matcher userMention = DiscordRegexPattern.USER_MENTION.matcher(content);
        while (userMention.find()) {
            String userId = userMention.group("id");
            Optional<User> userOptional = getApi().getCachedUserById(userId);
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                String userName = getServer().map(user::getDisplayName).orElseGet(user::getName);
                content = userMention.replaceFirst("@" + userName);
                userMention.reset(content);
            }
        }
        Matcher roleMention = DiscordRegexPattern.ROLE_MENTION.matcher(content);
        while (roleMention.find()) {
            String roleId = roleMention.group("id");
            String roleName = getServer()
                    .flatMap(server -> server
                            .getRoleById(roleId)
                            .map(Role::getName))
                    .orElse("deleted-role");
            content = roleMention.replaceFirst("@" + roleName);
            roleMention.reset(content);
        }
        Matcher channelMention = DiscordRegexPattern.CHANNEL_MENTION.matcher(content);
        while (channelMention.find()) {
            String channelId = channelMention.group("id");
            String channelName = getServer()
                    .flatMap(server -> server
                            .getTextChannelById(channelId)
                            .map(ServerChannel::getName))
                    .orElse("deleted-channel");
            content = channelMention.replaceFirst("#" + channelName);
            channelMention.reset(content);
        }
        Matcher customEmoji = DiscordRegexPattern.CUSTOM_EMOJI.matcher(content);
        while (customEmoji.find()) {
            String emojiId = customEmoji.group("id");
            String name = getApi()
                    .getCustomEmojiById(emojiId)
                    .map(CustomEmoji::getName)
                    .orElseGet(() -> customEmoji.group("name"));
            content = customEmoji.replaceFirst(":" + name + ":");
            customEmoji.reset(content);
        }
        return ESCAPED_CHARACTER.matcher(content).replaceAll("${char}");
    }

    /**
     * Returns whether or not the Message has any attachment
     *
     * @return True, when the message has any attachment
     */
    default boolean hasAttachments() {
        return getAttachments().isEmpty();
    }

    /**
     * Returns whether or not the Message has any reactions
     *
     * @return True, when the message has any reactions
     */
    default boolean hasReactions() {
        return getReactions().isEmpty();
    }

    /**
     * Returns whether or not the Message has any embeds
     *
     * @return True, when the message has any embeds
     */
    default boolean hasEmbeds() {
        return getEmbeds().isEmpty();
    }

    /**
     * Returns whether or not the Message has any users mentioned
     *
     * @return True, when the message has any users mentioned
     */
    default boolean hasUserMentions() {
        return getMentionedUsers().isEmpty();
    }

    /**
     * Returns whether or not the Message has any roles mentioned
     *
     * @return True, when the message has any roles mentioned
     */
    default boolean hasRoleMentions() {
        return getMentionedRoles().isEmpty();
    }

    /**
     * Returns whether or not the Message has any channels mentioned
     *
     * @return True, when the message has any channels mentioned
     */
    default boolean hasChannelMentions() {
        return getMentionedChannels().isEmpty();
    }

    /**
     * Gets a list of all custom emojis in the message.
     *
     * @return The list of custom emojis in the message.
     */
    List<CustomEmoji> getCustomEmojis();

    /**
     * Gets the type of the message.
     *
     * @return The type of the message.
     */
    MessageType getType();

    /**
     * Gets the text channel of the message.
     *
     * @return The text channel of the message.
     */
    TextChannel getChannel();

    /**
     * Checks if the message is pinned.
     *
     * @return Whether the message is pinned or not.
     */
    boolean isPinned();

    /**
     * Gets a list with all embeds of the message.
     *
     * @return A list with all embeds of the message.
     */
    List<Embed> getEmbeds();

    /**
     * Gets the user author of the message.
     * The author is not present, if it's a webhook.
     *
     * @return The user author of the message.
     */
    Optional<User> getUserAuthor();

    /**
     * Gets the author of the message.
     * Might be a user or a webhook.
     *
     * @return The author of the message.
     */
    MessageAuthor getAuthor();

    /**
     * Checks if the message is kept in cache forever.
     *
     * @return Whether the message is kept in cache forever or not.
     */
    boolean isCachedForever();

    /**
     * Sets if the the message is kept in cache forever.
     *
     * @param cachedForever  Whether the message should be kept in cache forever or not.
     */
    void setCachedForever(boolean cachedForever);

    /**
     * Gets a list with all reactions of the message.
     *
     * @return A list which contains all reactions of the message.
     */
    List<Reaction> getReactions();

    /**
     * Gets a list with all users mentioned in this message.
     *
     * @return A list with all users mentioned in this message.
     */
    List<User> getMentionedUsers();

    /**
     * Gets a list with all roles mentioned in this message.
     *
     * @return A list with all roles mentioned in this message.
     */
    List<Role> getMentionedRoles();

    /**
     * Gets a list with all channels mentioned in this message.
     *
     * @return A list with all channels mentioned in this message.
     */
    default List<ServerTextChannel> getMentionedChannels() {
        List<ServerTextChannel> mentionedChannels = new ArrayList<>();
        Matcher channelMention = DiscordRegexPattern.CHANNEL_MENTION.matcher(getContent());
        while (channelMention.find()) {
            String channelId = channelMention.group("id");
            getApi().getServerTextChannelById(channelId)
                    .filter(channel -> !mentionedChannels.contains(channel))
                    .ifPresent(mentionedChannels::add);
        }
        return Collections.unmodifiableList(mentionedChannels);
    }

    /**
     * Returns <code>true</code> if the message was sent as a private message, returns <code>false</code> if not.
     *
     * @return Whether or not the message was sent as a private message.
     */
    default boolean isPrivate() {
        return getChannel() instanceof PrivateChannel;
    }

    /**
     * Gets a reaction by its emoji.
     *
     * @param emoji The emoji of the reaction.
     * @return The reaction for the given emoji.
     */
    default Optional<Reaction> getReactionByEmoji(Emoji emoji) {
        return getReactions().stream().filter(reaction -> reaction.getEmoji().equals(emoji)).findAny();
    }

    /**
     * Gets a reaction by its unicode emoji.
     *
     * @param unicodeEmoji The unicode emoji of the reaction.
     * @return The reaction for the given emoji.
     */
    default Optional<Reaction> getReactionByEmoji(String unicodeEmoji) {
        return getReactions().stream()
                .filter(reaction -> unicodeEmoji.equals(reaction.getEmoji().asUnicodeEmoji().orElse(null))).findAny();
    }

    /**
     * Adds a unicode reaction to the message.
     *
     * @param unicodeEmoji The unicode emoji string.
     * @return A future to tell us if the action was successful.
     */
    default CompletableFuture<Void> addReaction(String unicodeEmoji) {
        return Message.addReaction(getApi(), getChannel().getId(), getId(), unicodeEmoji);
    }

    /**
     * Adds a reaction to the message.
     *
     * @param emoji The emoji.
     * @return A future to tell us if the action was successful.
     */
    default CompletableFuture<Void> addReaction(Emoji emoji) {
        return Message.addReaction(getApi(), getChannel().getId(), getId(), emoji);
    }

    /**
     * Adds reactions to the message.
     *
     * @param emojis The emojis.
     * @return A future to tell us if the action was successful.
     */
    default CompletableFuture<Void> addReactions(Emoji... emojis) {
        return CompletableFuture.allOf(
                Arrays.stream(emojis)
                        .map(this::addReaction)
                        .toArray(CompletableFuture[]::new));
    }

    /**
     * Adds unicode reactions to the message.
     *
     * @param unicodeEmojis The unicode emoji strings.
     * @return A future to tell us if the action was successful.
     */
    CompletableFuture<Void> addReactions(String... unicodeEmojis);

    /**
     * Deletes all reactions on this message.
     *
     * @return A future to tell us if the deletion was successful.
     */
    default CompletableFuture<Void> removeAllReactions() {
        return Message.removeAllReactions(getApi(), getChannel().getId(), getId());
    }

    /**
     * Removes a user from the list of reactors of a given emoji reaction.
     *
     * @param user The user to remove.
     * @param emoji The emoji of the reaction.
     * @return A future to tell us if the deletion was successful.
     */
    default CompletableFuture<Void> removeReactionByEmoji(User user, Emoji emoji) {
        return Reaction.removeUser(getApi(), getChannel().getId(), getId(), emoji, user);
    }

    /**
     * Removes a user from the list of reactors of the given emoji reactions.
     *
     * @param user The user to remove.
     * @param emojis The emojis of the reactions.
     * @return A future to tell us if the deletion was successful.
     */
    default CompletableFuture<Void> removeReactionsByEmoji(User user, Emoji... emojis) {
        return CompletableFuture.allOf(
                Arrays.stream(emojis)
                        .map(emoji -> removeReactionByEmoji(user, emoji))
                        .toArray(CompletableFuture[]::new));
    }

    /**
     * Removes a user from the list of reactors of a given unicode emoji reaction.
     *
     * @param user The user to remove.
     * @param unicodeEmoji The unicode emoji of the reaction.
     * @return A future to tell us if the deletion was successful.
     */
    CompletableFuture<Void> removeReactionByEmoji(User user, String unicodeEmoji);

    /**
     * Removes a user from the list of reactors of the given unicode emoji reactions.
     *
     * @param unicodeEmojis The unicode emojis of the reactions.
     * @param user The user to remove.
     * @return A future to tell us if the deletion was successful.
     */
    CompletableFuture<Void> removeReactionsByEmoji(User user, String... unicodeEmojis);

    /**
     * Removes all reactors of a given emoji reaction.
     *
     * @param emoji The emoji of the reaction.
     * @return A future to tell us if the deletion was successful.
     */
    default CompletableFuture<Void> removeReactionByEmoji(Emoji emoji) {
        return getReactionByEmoji(emoji).map(Reaction::remove).orElseGet(() -> CompletableFuture.completedFuture(null));
    }

    /**
     * Removes all reactors of the given emoji reactions.
     *
     * @param emojis The emojis of the reactions.
     * @return A future to tell us if the deletion was successful.
     */
    default CompletableFuture<Void> removeReactionsByEmoji(Emoji... emojis) {
        return CompletableFuture.allOf(
                Arrays.stream(emojis)
                        .map(this::removeReactionByEmoji)
                        .toArray(CompletableFuture[]::new));
    }

    /**
     * Removes all reactors of a given unicode emoji reaction.
     *
     * @param unicodeEmoji The unicode emoji of the reaction.
     * @return A future to tell us if the deletion was successful.
     */
    CompletableFuture<Void> removeReactionByEmoji(String unicodeEmoji);

    /**
     * Removes all reactors of the given unicode emoji reactions.
     *
     * @param unicodeEmojis The unicode emojis of the reactions.
     * @return A future to tell us if the deletion was successful.
     */
    CompletableFuture<Void> removeReactionsByEmoji(String... unicodeEmojis);

    /**
     * Removes you from the list of reactors of a given emoji reaction.
     *
     * @param emoji The emoji of the reaction.
     * @return A future to tell us if the deletion was successful.
     */
    default CompletableFuture<Void> removeOwnReactionByEmoji(Emoji emoji) {
        return removeReactionByEmoji(getApi().getYourself(), emoji);
    }

    /**
     * Removes you from the list of reactors of the given emoji reactions.
     *
     * @param emojis The emojis of the reactions.
     * @return A future to tell us if the deletion was successful.
     */
    default CompletableFuture<Void> removeOwnReactionsByEmoji(Emoji... emojis) {
        return removeReactionsByEmoji(getApi().getYourself(), emojis);
    }

    /**
     * Removes you from the list of reactors of a given unicode emoji reaction.
     *
     * @param unicodeEmoji The unicode emoji of the reaction.
     * @return A future to tell us if the deletion was successful.
     */
    CompletableFuture<Void> removeOwnReactionByEmoji(String unicodeEmoji);

    /**
     * Removes you from the list of reactors of the given unicode emoji reactions.
     *
     * @param unicodeEmojis The unicode emojis of the reactions.
     * @return A future to tell us if the deletion was successful.
     */
    CompletableFuture<Void> removeOwnReactionsByEmoji(String... unicodeEmojis);

    /**
     * Gets the server text channel of the message.
     * Only present if the message was sent in a server.
     *
     * @return The server text channel.
     */
    default Optional<ServerTextChannel> getServerTextChannel() {
        return Optional.ofNullable(getChannel() instanceof ServerTextChannel ? (ServerTextChannel) getChannel() : null);
    }

    /**
     * Gets the private channel of the message.
     * Only present if the message was sent in a private conversation.
     *
     * @return The private channel.
     */
    default Optional<PrivateChannel> getPrivateChannel() {
        return Optional.ofNullable(getChannel() instanceof PrivateChannel ? (PrivateChannel) getChannel() : null);
    }

    /**
     * Gets the group channel of the message.
     * Only present if the message was sent in a group channel.
     *
     * @return The group channel.
     */
    default Optional<GroupChannel> getGroupChannel() {
        return Optional.ofNullable(getChannel() instanceof GroupChannel ? (GroupChannel) getChannel() : null);
    }

    /**
     * Gets the server of the message.
     *
     * @return The server of the message.
     */
    default Optional<Server> getServer() {
        return getServerTextChannel().map(ServerChannel::getServer);
    }

    /**
     * Updates the content of the message.
     *
     * @param content The new content of the message.
     * @return A future to check if the update was successful.
     */
    default CompletableFuture<Void> edit(String content) {
        return Message.edit(getApi(), getChannel().getId(), getId(), content, true, null, false);
    }

    /**
     * Updates the embed of the message.
     *
     * @param embed The new embed of the message.
     * @return A future to check if the update was successful.
     */
    default CompletableFuture<Void> edit(EmbedBuilder embed) {
        return Message.edit(getApi(), getChannel().getId(), getId(), null, false, embed, true);
    }

    /**
     * Updates the content and the embed of the message.
     *
     * @param content The new content of the message.
     * @param embed The new embed of the message.
     * @return A future to check if the update was successful.
     */
    default CompletableFuture<Void> edit(String content, EmbedBuilder embed) {
        return Message.edit(getApi(), getChannel().getId(), getId(), content, true, embed, true);
    }

    /**
     * Removes the content of the message.
     *
     * @return A future to check if the removal was successful.
     */
    default CompletableFuture<Void> removeContent() {
        return Message.edit(getApi(), getChannel().getId(), getId(), null, true, null, false);
    }

    /**
     * Removes the embed of the message.
     *
     * @return A future to check if the removal was successful.
     */
    default CompletableFuture<Void> removeEmbed() {
        return Message.edit(getApi(), getChannel().getId(), getId(), null, false, null, true);
    }

    /**
     * Removes the content and embed of the message.
     *
     * @return A future to check if the removal was successful.
     */
    default CompletableFuture<Void> removeContentAndEmbed() {
        return Message.edit(getApi(), getChannel().getId(), getId(), null, true, null, true);
    }

    /**
     * Deletes the message.
     *
     * @return A future to tell us if the deletion was successful.
     */
    default CompletableFuture<Void> delete() {
       return delete(null);
    }

    /**
     * Deletes the message.
     *
     * @param reason The audit log reason for the deletion.
     * @return A future to tell us if the deletion was successful.
     */
    default CompletableFuture<Void> delete(String reason) {
        return Message.delete(getApi(), getChannel().getId(), getId(), reason);
    }

    /**
     * Pins this message.
     *
     * @return A future to tell us if the pin was successful.
     */
    default CompletableFuture<Void> pin() {
        return Message.pin(getApi(), getChannel().getId(), getId());
    }

    /**
     * Unpins this message.
     *
     * @return A future to tell us if the action was successful.
     */
    default CompletableFuture<Void> unpin() {
        return Message.unpin(getApi(), getChannel().getId(), getId());
    }

    /**
     * Gets up to a given amount of messages before this message.
     *
     * @param limit The limit of messages to get.
     * @return The messages.
     * @see TextChannel#getMessagesBefore(int, long)
     * @see #getMessagesBeforeAsStream()
     */
    default CompletableFuture<MessageSet> getMessagesBefore(int limit) {
        return getChannel().getMessagesBefore(limit, this);
    }

    /**
     * Gets messages before this message until one that meets the given condition is found.
     * If no message matches the condition, an empty set is returned.
     *
     * @param condition The abort condition for when to stop retrieving messages.
     * @return The messages.
     * @see TextChannel#getMessagesBefore(int, long)
     * @see #getMessagesBeforeAsStream()
     */
    default CompletableFuture<MessageSet> getMessagesBeforeUntil(Predicate<Message> condition) {
        return getChannel().getMessagesBeforeUntil(condition, this);
    }

    /**
     * Gets messages before this message while they meet the given condition.
     * If the first message does not match the condition, an empty set is returned.
     *
     * @param condition The condition that has to be met.
     * @return The messages.
     * @see TextChannel#getMessagesBeforeWhile(Predicate, long)
     * @see #getMessagesBeforeAsStream()
     */
    default CompletableFuture<MessageSet> getMessagesBeforeWhile(Predicate<Message> condition) {
        return getChannel().getMessagesBeforeWhile(condition, this);
    }

    /**
     * Gets a stream of messages before this message sorted from newest to oldest.
     * <p>
     * The messages are retrieved in batches synchronously from Discord,
     * so consider not using this method from a listener directly.
     *
     * @return The stream.
     * @see TextChannel#getMessagesBeforeAsStream(long)
     * @see #getMessagesBefore(int)
     */
    default Stream<Message> getMessagesBeforeAsStream() {
        return getChannel().getMessagesBeforeAsStream(this);
    }

    /**
     * Gets up to a given amount of messages after this message.
     *
     * @param limit The limit of messages to get.
     * @return The messages.
     * @see TextChannel#getMessagesAfter(int, long)
     * @see #getMessagesAfterAsStream()
     */
    default CompletableFuture<MessageSet> getMessagesAfter(int limit) {
        return getChannel().getMessagesAfter(limit, this);
    }

    /**
     * Gets messages after this message until one that meets the given condition is found.
     * If no message matches the condition, an empty set is returned.
     *
     * @param condition The abort condition for when to stop retrieving messages.
     * @return The messages.
     * @see TextChannel#getMessagesAfter(int, long)
     * @see #getMessagesAfterAsStream()
     */
    default CompletableFuture<MessageSet> getMessagesAfterUntil(Predicate<Message> condition) {
        return getChannel().getMessagesAfterUntil(condition, this);
    }

    /**
     * Gets messages after this message while they meet the given condition.
     * If the first message does not match the condition, an empty set is returned.
     *
     * @param condition The condition that has to be met.
     * @return The messages.
     * @see TextChannel#getMessagesAfterWhile(Predicate, long)
     * @see #getMessagesAfterAsStream()
     */
    default CompletableFuture<MessageSet> getMessagesAfterWhile(Predicate<Message> condition) {
        return getChannel().getMessagesAfterWhile(condition, this);
    }

    /**
     * Gets a stream of messages after this message sorted from oldest to newest.
     * <p>
     * The messages are retrieved in batches synchronously from Discord,
     * so consider not using this method from a listener directly.
     *
     * @return The stream.
     * @see TextChannel#getMessagesAfterAsStream(long)
     * @see #getMessagesAfter(int)
     */
    default Stream<Message> getMessagesAfterAsStream() {
        return getChannel().getMessagesAfterAsStream(this);
    }

    /**
     * Gets up to a given amount of messages around this message.
     * This message will be part of the result in addition to the messages around and does not count towards the limit.
     * Half of the messages will be older than this message and half of the message will be newer.
     * If there aren't enough older or newer messages, the actual amount of messages will be less than the given limit.
     * It's also not guaranteed to be perfectly balanced.
     *
     * @param limit The limit of messages to get.
     * @return The messages.
     * @see TextChannel#getMessagesAround(int, long)
     * @see #getMessagesAroundAsStream()
     */
    default CompletableFuture<MessageSet> getMessagesAround(int limit) {
        return getChannel().getMessagesAround(limit, this);
    }

    /**
     * Gets messages around this message until one that meets the given condition is found.
     * If no message matches the condition, an empty set is returned.
     * This message will be part of the result in addition to the messages around and is matched against the condition
     * and will abort retrieval.
     * Half of the messages will be older than this message and half of the message will be newer.
     * If there aren't enough older or newer messages, the actual amount of messages will be less than the given limit.
     * It's also not guaranteed to be perfectly balanced.
     *
     * @param condition The abort condition for when to stop retrieving messages.
     * @return The messages.
     * @see TextChannel#getMessagesAround(int, long)
     * @see #getMessagesAroundAsStream()
     */
    default CompletableFuture<MessageSet> getMessagesAroundUntil(Predicate<Message> condition) {
        return getChannel().getMessagesAroundUntil(condition, this);
    }

    /**
     * Gets messages around this message while they meet the given condition.
     * If this message does not match the condition, an empty set is returned.
     * This message will be part of the result in addition to the messages around and is matched against the condition
     * and will abort retrieval.
     * Half of the messages will be older than this message and half of the message will be newer.
     * If there aren't enough older or newer messages, the actual amount of messages will be less than the given limit.
     * It's also not guaranteed to be perfectly balanced.
     *
     * @param condition The condition that has to be met.
     * @return The messages.
     * @see TextChannel#getMessagesAroundWhile(Predicate, long)
     * @see #getMessagesAroundAsStream()
     */
    default CompletableFuture<MessageSet> getMessagesAroundWhile(Predicate<Message> condition) {
        return getChannel().getMessagesAroundWhile(condition, this);
    }

    /**
     * Gets a stream of messages around this message. The first message in the stream will be this message.
     * After that you will always get an older message and a newer message alternating as long as on both sides
     * messages are available. If only on one side further messages are available, only those are delivered further on.
     * It's not guaranteed to be perfectly balanced.
     * <p>
     * The messages are retrieved in batches synchronously from Discord,
     * so consider not using this method from a listener directly.
     *
     * @return The stream.
     * @see TextChannel#getMessagesAroundAsStream(long)
     * @see #getMessagesAround(int)
     */
    default Stream<Message> getMessagesAroundAsStream() {
        return getChannel().getMessagesAroundAsStream(this);
    }

    /**
     * Gets all messages between this messages and the given message, excluding the boundaries.
     *
     * @param other The id of the other boundary messages.
     * @return The messages.
     * @see TextChannel#getMessagesBetween(long, long)
     * @see #getMessagesBetweenAsStream(long)
     */
    default CompletableFuture<MessageSet> getMessagesBetween(long other) {
        return getChannel().getMessagesBetween(getId(), other);
    }

    /**
     * Gets all messages between this message and the given message, excluding the boundaries, until one that meets the
     * given condition is found.
     * If no message matches the condition, an empty set is returned.
     *
     * @param other The id of the other boundary messages.
     * @param condition The abort condition for when to stop retrieving messages.
     * @return The messages.
     * @see TextChannel#getMessagesBetweenUntil(Predicate, long, long)
     * @see #getMessagesBetweenAsStream(long)
     */
    default CompletableFuture<MessageSet> getMessagesBetweenUntil(long other, Predicate<Message> condition) {
        return getChannel().getMessagesBetweenUntil(condition, getId(), other);
    }

    /**
     * Gets all messages between this message and the given message, excluding the boundaries, while they meet the
     * given condition.
     * If the first message does not match the condition, an empty set is returned.
     *
     * @param other The id of the other boundary messages.
     * @param condition The condition that has to be met.
     * @return The messages.
     * @see TextChannel#getMessagesBetweenWhile(Predicate, long, long)
     * @see #getMessagesBetweenAsStream(long)
     */
    default CompletableFuture<MessageSet> getMessagesBetweenWhile(long other, Predicate<Message> condition) {
        return getChannel().getMessagesBetweenWhile(condition, getId(), other);
    }

    /**
     * Gets a stream of all messages between this message and the given message, excluding the boundaries, sorted from
     * this message to the given message.
     * <p>
     * The messages are retrieved in batches synchronously from Discord,
     * so consider not using this method from a listener directly.
     *
     * @param other The id of the other boundary messages.
     * @return The stream.
     * @see TextChannel#getMessagesBetweenAsStream(long, long)
     * @see #getMessagesBetween(long)
     */
    default Stream<Message> getMessagesBetweenAsStream(long other) {
        return getChannel().getMessagesBetweenAsStream(getId(), other);
    }

    /**
     * Gets all messages between this messages and the given message, excluding the boundaries.
     *
     * @param other The other boundary messages.
     * @return The messages.
     * @see TextChannel#getMessagesBetween(long, long)
     * @see #getMessagesBetweenAsStream(long)
     */
    default CompletableFuture<MessageSet> getMessagesBetween(Message other) {
        return getMessagesBetween(other.getId());
    }

    /**
     * Gets all messages between this message and the given message, excluding the boundaries, until one that meets the
     * given condition is found.
     * If no message matches the condition, an empty set is returned.
     *
     * @param other The other boundary messages.
     * @param condition The abort condition for when to stop retrieving messages.
     * @return The messages.
     * @see TextChannel#getMessagesBetweenUntil(Predicate, long, long)
     * @see #getMessagesBetweenAsStream(long)
     */
    default CompletableFuture<MessageSet> getMessagesBetweenUntil(Message other, Predicate<Message> condition) {
        return getMessagesBetweenUntil(other.getId(), condition);
    }

    /**
     * Gets all messages between this message and the given message, excluding the boundaries, while they meet the
     * given condition.
     * If the first message does not match the condition, an empty set is returned.
     *
     * @param other The other boundary messages.
     * @param condition The condition that has to be met.
     * @return The messages.
     * @see TextChannel#getMessagesBetweenWhile(Predicate, long, long)
     * @see #getMessagesBetweenAsStream(long)
     */
    default CompletableFuture<MessageSet> getMessagesBetweenWhile(Message other, Predicate<Message> condition) {
        return getMessagesBetweenWhile(other.getId(), condition);
    }

    /**
     * Gets a stream of all messages between this message and the given message, excluding the boundaries, sorted from
     * this message to the given message.
     * <p>
     * The messages are retrieved in batches synchronously from Discord,
     * so consider not using this method from a listener directly.
     *
     * @param other The other boundary messages.
     * @return The stream.
     * @see TextChannel#getMessagesBetweenAsStream(long, long)
     * @see #getMessagesBetween(long)
     */
    default Stream<Message> getMessagesBetweenAsStream(Message other) {
        return getMessagesBetweenAsStream(other.getId());
    }

    /**
     * Checks if the given user is allowed to add <b>new</b> reactions to the message.
     *
     * @param user The user to check.
     * @return Whether the given user is allowed to add <b>new</b> reactions to the message or not.
     */
    default boolean canAddNewReactions(User user) {
        Optional<ServerTextChannel> channel = getServerTextChannel();
        return !channel.isPresent()
                || channel.get().hasPermission(user, PermissionType.ADMINISTRATOR)
                || channel.get().hasPermissions(user,
                    PermissionType.READ_MESSAGES,
                    PermissionType.READ_MESSAGE_HISTORY,
                    PermissionType.ADD_REACTIONS);
    }

    /**
     * Checks if the user of the connected account is allowed to add <b>new</b> reactions to the message.
     *
     * @return Whether the user of the connected account is allowed to add <b>new</b> reactions to the message or not.
     */
    default boolean canYouAddNewReactions() {
        return canAddNewReactions(getApi().getYourself());
    }

    /**
     * Checks if the given user can delete this message.
     *
     * @param user The user to check.
     * @return Whether the given user can delete the message or not.
     */
    default boolean canDelete(User user) {
        // You cannot delete messages in channels you cannot see
        if (!getChannel().canSee(user)) {
            return false;
        }
        // The user can see the message and is the author
        if (getAuthor().asUser().orElse(null) == user) {
            return true;
        }
        return getServerTextChannel().map(channel -> channel.canManageMessages(user)).orElse(false);
    }

    /**
     * Checks if the user of the connected account can delete this message.
     *
     * @return Whether the user of the connected account can delete the message or not.
     */
    default boolean canYouDelete() {
        return canDelete(getApi().getYourself());
    }

    /**
     * Adds a listener, which listens to this message being deleted.
     *
     * @param listener The listener to add.
     * @return The manager of the listener.
     */
    default ListenerManager<MessageDeleteListener> addMessageDeleteListener(MessageDeleteListener listener) {
        return addMessageDeleteListener(getApi(), getId(), listener);
    }

    /**
     * Gets a list with all registered message delete listeners.
     *
     * @return A list with all registered message delete listeners.
     */
    default List<MessageDeleteListener> getMessageDeleteListeners() {
        return getMessageDeleteListeners(getApi(), getId());
    }

    /**
     * Adds a listener, which listens to this message being edited.
     *
     * @param listener The listener to add.
     * @return The manager of the listener.
     */
    default ListenerManager<MessageEditListener> addMessageEditListener(MessageEditListener listener) {
        return addMessageEditListener(getApi(), getId(), listener);
    }

    /**
     * Gets a list with all registered message edit listeners.
     *
     * @return A list with all registered message edit listeners.
     */
    default List<MessageEditListener> getMessageEditListeners() {
        return getMessageEditListeners(getApi(), getId());
    }

    /**
     * Adds a listener, which listens to reactions being added to this message.
     *
     * @param listener The listener to add.
     * @return The manager of the listener.
     */
    default ListenerManager<ReactionAddListener> addReactionAddListener(ReactionAddListener listener) {
        return addReactionAddListener(getApi(), getId(), listener);
    }

    /**
     * Gets a list with all registered reaction add listeners.
     *
     * @return A list with all registered reaction add listeners.
     */
    default List<ReactionAddListener> getReactionAddListeners() {
        return getReactionAddListeners(getApi(), getId());
    }

    /**
     * Adds a listener, which listens to reactions being removed from this message.
     *
     * @param listener The listener to add.
     * @return The manager of the listener.
     */
    default ListenerManager<ReactionRemoveListener> addReactionRemoveListener(ReactionRemoveListener listener) {
        return addReactionRemoveListener(getApi(), getId(), listener);
    }

    /**
     * Gets a list with all registered reaction remove listeners.
     *
     * @return A list with all registered reaction remove listeners.
     */
    default List<ReactionRemoveListener> getReactionRemoveListeners() {
        return getReactionRemoveListeners(getApi(), getId());
    }

    /**
     * Adds a listener, which listens to all reactions being removed at once from this message.
     *
     * @param listener The listener to add.
     * @return The manager of the listener.
     */
    default ListenerManager<ReactionRemoveAllListener> addReactionRemoveAllListener(
            ReactionRemoveAllListener listener) {
        return addReactionRemoveAllListener(getApi(), getId(), listener);
    }

    /**
     * Gets a list with all registered reaction remove all listeners.
     *
     * @return A list with all registered reaction remove all listeners.
     */
    default List<ReactionRemoveAllListener> getReactionRemoveAllListeners() {
        return getReactionRemoveAllListeners(getApi(), getId());
    }

    /**
     * Adds a listener, which listens to this message getting pinned while it is in the cache.
     *
     * @param listener The listener to add.
     * @return The manager of the listener.
     */
    ListenerManager<CachedMessagePinListener> addCachedMessagePinListener(CachedMessagePinListener listener);

    /**
     * Gets a list with all registered cached message pin listeners.
     *
     * @return A list with all registered cached message pin listeners.
     */
    List<CachedMessagePinListener> getCachedMessagePinListeners();

    /**
     * Adds a listener, which listens to this message getting unpinned while it is in the cache.
     *
     * @param listener The listener to add.
     * @return The manager of the listener.
     */
    ListenerManager<CachedMessageUnpinListener> addCachedMessageUnpinListener(CachedMessageUnpinListener listener);

    /**
     * Gets a list with all registered cached message unpin listeners.
     *
     * @return A list with all registered cached message unpin listeners.
     */
    List<CachedMessageUnpinListener> getCachedMessageUnpinListeners();

    /**
     * Adds a listener that implements one or more {@code MessageAttachableListener}s.
     * Adding a listener multiple times will only add it once
     * and return the same listener managers on each invocation.
     * The order of invocation is according to first addition.
     *
     * @param listener The listener to add.
     * @param <T> The type of the listener.
     * @return The managers for the added listener.
     */
    default <T extends MessageAttachableListener & ObjectAttachableListener> Collection<ListenerManager<T>>
    addMessageAttachableListener(T listener) {
        return addMessageAttachableListener(getApi(), getId(), listener);
    }

    /**
     * Removes a listener that implements one or more {@code MessageAttachableListener}s.
     *
     * @param listener The listener to remove.
     * @param <T> The type of the listener.
     */
    default <T extends MessageAttachableListener & ObjectAttachableListener> void removeMessageAttachableListener(
            T listener) {
        removeMessageAttachableListener(getApi(), getId(), listener);
    }

    /**
     * Gets a map with all registered listeners that implement one or more {@code MessageAttachableListener}s and their
     * assigned listener classes they listen to.
     *
     * @param <T> The type of the listeners.
     * @return A map with all registered listeners that implement one or more {@code MessageAttachableListener}s and
     * their assigned listener classes they listen to.
     */
    default <T extends MessageAttachableListener & ObjectAttachableListener> Map<T, List<Class<T>>>
    getMessageAttachableListeners() {
        return getMessageAttachableListeners(getApi(), getId());
    }

    /**
     * Removes a listener from this message.
     *
     * @param listenerClass The listener class.
     * @param listener The listener to remove.
     * @param <T> The type of the listener.
     */
    default <T extends MessageAttachableListener & ObjectAttachableListener> void removeListener(
            Class<T> listenerClass, T listener) {
        removeListener(getApi(), getId(), listenerClass, listener);
    }

    @Override
    default Optional<Message> getCurrentCachedInstance() {
        return getApi().getCachedMessageById(getId());
    }

    @Override
    default CompletableFuture<Message> getLatestInstance() {
        return getChannel().getMessageById(getId());
    }

}
