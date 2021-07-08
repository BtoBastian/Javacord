package org.javacord.api.entity.message;

import org.javacord.api.entity.emoji.Emoji;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;
import org.javacord.api.listener.message.UncachedMessageAttachableListenerManager;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * This class provides methods to interact with messages without having an instance of it.
 */
public interface UncachedMessageUtil extends UncachedMessageAttachableListenerManager {

    /**
     * Cross posts the message if it is in a announcement channel.
     *
     * @param channelId The id of the message's channel.
     * @param messageId The id of the message.
     * @return The new message object.
     */
    default CompletableFuture<Message> crossPost(long channelId, long messageId) {
        return crossPost(Long.toUnsignedString(channelId), Long.toUnsignedString(messageId));
    }

    /**
     * Cross posts the message if it is in a announcement channel.
     *
     * @param channelId The id of the message's channel.
     * @param messageId The id of the message.
     * @return The new message object.
     */
    CompletableFuture<Message> crossPost(String channelId, String messageId);

    /**
     * Deletes the message.
     *
     * @param channelId The id of the message's channel.
     * @param messageId The id of the message.
     * @return A future to tell us if the deletion was successful.
     */
    CompletableFuture<Void> delete(long channelId, long messageId);

    /**
     * Deletes the message.
     *
     * @param channelId The id of the message's channel.
     * @param messageId The id of the message.
     * @return A future to tell us if the deletion was successful.
     */
    CompletableFuture<Void> delete(String channelId, String messageId);

    /**
     * Deletes the message.
     *
     * @param channelId The id of the message's channel.
     * @param messageId The id of the message.
     * @param reason The audit log reason for the deletion.
     * @return A future to tell us if the deletion was successful.
     */
    CompletableFuture<Void> delete(long channelId, long messageId, String reason);

    /**
     * Deletes the message.
     *
     * @param channelId The id of the message's channel.
     * @param messageId The id of the message.
     * @param reason The audit log reason for the deletion.
     * @return A future to tell us if the deletion was successful.
     */
    CompletableFuture<Void> delete(String channelId, String messageId, String reason);

    /**
     * Deletes the message sent by a webhook.
     *
     * @param webhookId The id of the webhook.
     * @param webhookToken The token of the webhook.
     * @param messageId The id of the message.
     * @param reason The audit log reason for the deletion.
     * @return A future to tell us if the deletion was successful.
     */
    CompletableFuture<Void> delete(long webhookId, String webhookToken, long messageId, String reason);

    /**
     * Deletes multiple messages at once.
     * This method does not have a size or age restriction.
     * Messages younger than two weeks are sent in batches of 100 messages to the bulk delete API,
     * older messages are deleted with individual delete requests.
     *
     * @param channelId The id of the message's channel.
     * @param messageIds The ids of the messages to delete.
     * @return A future to tell us if the deletion was successful.
     */
    CompletableFuture<Void> delete(long channelId, long... messageIds);

    /**
     * Deletes multiple messages at once.
     * This method does not have a size or age restriction.
     * Messages younger than two weeks are sent in batches of 100 messages to the bulk delete API,
     * older messages are deleted with individual delete requests.
     *
     * @param channelId The id of the message's channel.
     * @param messageIds The ids of the messages to delete.
     * @return A future to tell us if the deletion was successful.
     */
    CompletableFuture<Void> delete(String channelId, String... messageIds);

    /**
     * Deletes multiple messages at once.
     * This method does not have a size or age restriction.
     * Messages younger than two weeks are sent in batches of 100 messages to the bulk delete API,
     * older messages are deleted with individual delete requests.
     *
     * @param messages The messages to delete.
     * @return A future to tell us if the deletion was successful.
     */
    CompletableFuture<Void> delete(Message... messages);

    /**
     * Deletes multiple messages at once.
     * This method does not have a size or age restriction.
     * Messages younger than two weeks are sent in batches of 100 messages to the bulk delete API,
     * older messages are deleted with individual delete requests.
     *
     * @param messages The messages to delete.
     * @return A future to tell us if the deletion was successful.
     */
    CompletableFuture<Void> delete(Iterable<Message> messages);

    /**
     * Updates the content of the message.
     *
     * @param channelId The id of the message's channel.
     * @param messageId The id of the message.
     * @param content The new content of the message.
     * @return A future to check if the update was successful.
     */
    CompletableFuture<Message> edit(long channelId, long messageId, String content);

    /**
     * Updates the content of the message.
     *
     * @param channelId The id of the message's channel.
     * @param messageId The id of the message.
     * @param content The new content of the message.
     * @return A future to check if the update was successful.
     */
    CompletableFuture<Message> edit(String channelId, String messageId, String content);

    /**
     * Updates the embed of the message.
     *
     * @param channelId The id of the message's channel.
     * @param messageId The id of the message.
     * @param embed The new embed of the message.
     * @return A future to check if the update was successful.
     */
    CompletableFuture<Message> edit(long channelId, long messageId, EmbedBuilder embed);

    /**
     * Updates the embed of the message.
     *
     * @param channelId The id of the message's channel.
     * @param messageId The id of the message.
     * @param embed The new embed of the message.
     * @return A future to check if the update was successful.
     */
    CompletableFuture<Message> edit(String channelId, String messageId, EmbedBuilder embed);

    /**
     * Updates the content and the embed of the message.
     *
     * @param channelId The id of the message's channel.
     * @param messageId The id of the message.
     * @param content The new content of the message.
     * @param embed The new embed of the message.
     * @return A future to check if the update was successful.
     */
    CompletableFuture<Message> edit(long channelId, long messageId, String content, EmbedBuilder embed);

    /**
     * Updates the content and the embed of the message.
     *
     * @param channelId The id of the message's channel.
     * @param messageId The id of the message.
     * @param content The new content of the message.
     * @param embed The new embed of the message.
     * @return A future to check if the update was successful.
     */
    CompletableFuture<Message> edit(String channelId, String messageId, String content, EmbedBuilder embed);

    /**
     * Updates the content and the embed of the message.
     *
     * @param channelId The id of the message's channel.
     * @param messageId The id of the message.
     * @param content The new content of the message.
     * @param updateContent Whether to update or remove the content.
     * @param embed The new embed of the message.
     * @param updateEmbed Whether to update or remove the embed.
     * @return A future to check if the update was successful.
     */
    CompletableFuture<Message> edit(long channelId, long messageId, String content, boolean updateContent,
                                 EmbedBuilder embed, boolean updateEmbed);

    /**
     * Updates the content and the embed of the message.
     *
     * @param channelId The id of the message's channel.
     * @param messageId The id of the message.
     * @param content The new content of the message.
     * @param updateContent Whether to update or remove the content.
     * @param embed The new embed of the message.
     * @param updateEmbed Whether to update or remove the embed.
     * @return A future to check if the update was successful.
     */
    CompletableFuture<Message> edit(String channelId, String messageId, String content, boolean updateContent,
                                 EmbedBuilder embed, boolean updateEmbed);

    /**
     * Updates the content and the embed of the message sent by a webhook.
     *
     * @param webhookId The id of the webhook channel.
     * @param webhookToken The token of the webhook.
     * @param messageId The id of the message.
     * @param content The new content of the message.
     * @param updateContent Whether to update or remove the content.
     * @param embed The new embed of the message.
     * @param updateEmbed Whether to update or remove the embed.
     * @return A future to check if the update was successful.
     */
    CompletableFuture<Message> edit(long webhookId, String webhookToken, long messageId, String content,
                                 boolean updateContent, EmbedBuilder embed, boolean updateEmbed);

    /**
     * Removes the content of the message.
     *
     * @param channelId The id of the message's channel.
     * @param messageId The id of the message.
     * @return A future to check if the removal was successful.
     */
    CompletableFuture<Message> removeContent(long channelId, long messageId);

    /**
     * Removes the content of the message.
     *
     * @param channelId The id of the message's channel.
     * @param messageId The id of the message.
     * @return A future to check if the removal was successful.
     */
    CompletableFuture<Message> removeContent(String channelId, String messageId);

    /**
     * Removes the embed of the message.
     *
     * @param channelId The id of the message's channel.
     * @param messageId The id of the message.
     * @return A future to check if the removal was successful.
     */
    CompletableFuture<Message> removeEmbed(long channelId, long messageId);

    /**
     * Removes the embed of the message.
     *
     * @param channelId The id of the message's channel.
     * @param messageId The id of the message.
     * @return A future to check if the removal was successful.
     */
    CompletableFuture<Message> removeEmbed(String channelId, String messageId);

    /**
     * Removes the content and embed of the message.
     *
     * @param channelId The id of the message's channel.
     * @param messageId The id of the message.
     * @return A future to check if the removal was successful.
     */
    CompletableFuture<Message> removeContentAndEmbed(long channelId, long messageId);

    /**
     * Removes the content and embed of the message.
     *
     * @param channelId The id of the message's channel.
     * @param messageId The id of the message.
     * @return A future to check if the removal was successful.
     */
    CompletableFuture<Message> removeContentAndEmbed(String channelId, String messageId);

    /**
     * Adds a unicode reaction to the message.
     *
     * @param channelId The id of the message's channel.
     * @param messageId The id of the message.
     * @param unicodeEmoji The unicode emoji string.
     * @return A future to tell us if the action was successful.
     */
    CompletableFuture<Void> addReaction(long channelId, long messageId, String unicodeEmoji);

    /**
     * Adds a unicode reaction to the message.
     *
     * @param channelId The id of the message's channel.
     * @param messageId The id of the message.
     * @param unicodeEmoji The unicode emoji string.
     * @return A future to tell us if the action was successful.
     */
    CompletableFuture<Void> addReaction(String channelId, String messageId, String unicodeEmoji);

    /**
     * Adds a reaction to the message.
     *
     * @param channelId The id of the message's channel.
     * @param messageId The id of the message.
     * @param emoji The emoji.
     * @return A future to tell us if the action was successful.
     */
    CompletableFuture<Void> addReaction(long channelId, long messageId, Emoji emoji);

    /**
     * Adds a reaction to the message.
     *
     * @param channelId The id of the message's channel.
     * @param messageId The id of the message.
     * @param emoji The emoji.
     * @return A future to tell us if the action was successful.
     */
    CompletableFuture<Void> addReaction(String channelId, String messageId, Emoji emoji);

    /**
     * Deletes all reactions on this message.
     *
     * @param channelId The id of the message's channel.
     * @param messageId The id of the message.
     * @return A future to tell us if the deletion was successful.
     */
    CompletableFuture<Void> removeAllReactions(long channelId, long messageId);

    /**
     * Deletes all reactions on this message.
     *
     * @param channelId The id of the message's channel.
     * @param messageId The id of the message.
     * @return A future to tell us if the deletion was successful.
     */
    CompletableFuture<Void> removeAllReactions(String channelId, String messageId);

    /**
     * Pins this message.
     *
     * @param channelId The id of the message's channel.
     * @param messageId The id of the message.
     * @return A future to tell us if the pin was successful.
     */
    CompletableFuture<Void> pin(long channelId, long messageId);

    /**
     * Pins this message.
     *
     * @param channelId The id of the message's channel.
     * @param messageId The id of the message.
     * @return A future to tell us if the pin was successful.
     */
    CompletableFuture<Void> pin(String channelId, String messageId);

    /**
     * Unpins this message.
     *
     * @param channelId The id of the message's channel.
     * @param messageId The id of the message.
     * @return A future to tell us if the action was successful.
     */
    CompletableFuture<Void> unpin(long channelId, long messageId);

    /**
     * Unpins this message.
     *
     * @param channelId The id of the message's channel.
     * @param messageId The id of the message.
     * @return A future to tell us if the action was successful.
     */
    CompletableFuture<Void> unpin(String channelId, String messageId);

    /**
     * Gets a list with all users who reacted with the given emoji.
     *
     * @param channelId The id of the message's channel.
     * @param messageId The id of the message.
     * @param emoji The emoji of the reaction.
     * @return A list with all users who reacted with the given emoji
     */
    CompletableFuture<List<User>> getUsersWhoReactedWithEmoji(long channelId, long messageId, Emoji emoji);

    /**
     * Gets a list with all users who reacted with the given emoji.
     *
     * @param channelId The id of the message's channel.
     * @param messageId The id of the message.
     * @param emoji The emoji of the reaction.
     * @return A list with all users who reacted with the given emoji
     */
    CompletableFuture<List<User>> getUsersWhoReactedWithEmoji(String channelId, String messageId, Emoji emoji);

    /**
     * Removes the reaction of the given user.
     *
     * @param channelId The id of the message's channel.
     * @param messageId The id of the message.
     * @param emoji The emoji of the reaction.
     * @param userId The id of the user to remove.
     * @return A future to tell us if the action was successful.
     */
    CompletableFuture<Void> removeUserReactionByEmoji(long channelId, long messageId, Emoji emoji, long userId);

    /**
     * Removes the reaction of the given user.
     *
     * @param channelId The id of the message's channel.
     * @param messageId The id of the message.
     * @param emoji The emoji of the reaction.
     * @param userId The id of the user to remove.
     * @return A future to tell us if the action was successful.
     */
    CompletableFuture<Void> removeUserReactionByEmoji(String channelId, String messageId, Emoji emoji, String userId);

}
