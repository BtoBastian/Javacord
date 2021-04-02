package org.javacord.core.event.message;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.emoji.Emoji;
import org.javacord.api.entity.emoji.UnicodeEmoji;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.Reaction;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageEvent;
import org.javacord.core.event.EventImpl;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * The implementation of {@link MessageEvent}.
 */
public abstract class MessageEventImpl extends EventImpl implements MessageEvent {

    /**
     * The id of the message.
     */
    private final long messageId;

    /**
     * The text channel in which the message was sent.
     */
    private final TextChannel channel;

    /**
     * Creates a new message event.
     *
     * @param message The message.
     */
    public MessageEventImpl(Message message) {
        this(message.getApi(), message.getId(), message.getChannel());
    }

    /**
     * Creates a new message event.
     *
     * @param api The discord api instance.
     * @param messageId The id of the message.
     * @param channel The text channel in which the message was sent.
     */
    public MessageEventImpl(DiscordApi api, long messageId, TextChannel channel) {
        super(api);
        this.messageId = messageId;
        this.channel = channel;
    }

    @Override
    public long getMessageId() {
        return messageId;
    }

    @Override
    public TextChannel getChannel() {
        return channel;
    }

    @Override
    public Optional<Server> getServer() {
        return getChannel().asServerChannel().map(ServerChannel::getServer);
    }

    @Override
    public CompletableFuture<Void> deleteMessage() {
        return deleteMessage(null);
    }

    @Override
    public CompletableFuture<Void> deleteMessage(String reason) {
        return Message.delete(getApi(), getChannel().getId(), getMessageId(), reason);
    }

    @Override
    public CompletableFuture<Message> editMessage(String content) {
        return Message.edit(getApi(), getChannel().getId(), getMessageId(), content, null);
    }

    @Override
    public CompletableFuture<Message> editMessage(EmbedBuilder embed) {
        return Message.edit(getApi(), getChannel().getId(), getMessageId(), null, embed);
    }

    @Override
    public CompletableFuture<Message> editMessage(String content, EmbedBuilder embed) {
        return Message.edit(getApi(), getChannel().getId(), getMessageId(), content, embed);
    }

    @Override
    public CompletableFuture<Void> addReactionToMessage(String unicodeEmoji) {
        return Message.addReaction(getApi(), getChannel().getId(), getMessageId(), unicodeEmoji);
    }

    @Override
    public CompletableFuture<Void> addReactionToMessage(Emoji emoji) {
        return Message.addReaction(getApi(), getChannel().getId(), getMessageId(), emoji);
    }

    @Override
    public CompletableFuture<Void> addReactionsToMessage(Emoji... emojis) {
        return CompletableFuture.allOf(Arrays.stream(emojis)
                                               .map(this::addReactionToMessage)
                                               .toArray(CompletableFuture[]::new));
    }

    @Override
    public CompletableFuture<Void> addReactionsToMessage(String... unicodeEmojis) {
        return addReactionsToMessage(Arrays.stream(unicodeEmojis)
                                             .map(UnicodeEmoji::fromString)
                                             .toArray(Emoji[]::new));
    }

    @Override
    public CompletableFuture<Void> removeAllReactionsFromMessage() {
        return Message.removeAllReactions(getApi(), getChannel().getId(), getMessageId());
    }

    @Override
    public CompletableFuture<Void> removeReactionByEmojiFromMessage(User user, Emoji emoji) {
        return Reaction.removeUser(getApi(), getChannel().getId(), getMessageId(), emoji, user.getId());
    }

    @Override
    public CompletableFuture<Void> removeReactionByEmojiFromMessage(User user, String unicodeEmoji) {
        return removeReactionByEmojiFromMessage(user, UnicodeEmoji.fromString(unicodeEmoji));
    }

    @Override
    public CompletableFuture<Void> removeReactionByEmojiFromMessage(Emoji emoji) {
        return Reaction.getUsers(getApi(), getChannel().getId(), getMessageId(), emoji)
                .thenCompose(users -> CompletableFuture.allOf(
                        users.stream()
                                .map(user -> Reaction.removeUser(getApi(), getChannel().getId(),
                                        getMessageId(), emoji, user.getId()))
                                .toArray(CompletableFuture[]::new)));
    }

    @Override
    public CompletableFuture<Void> removeReactionByEmojiFromMessage(String unicodeEmoji) {
        return removeReactionByEmojiFromMessage(UnicodeEmoji.fromString(unicodeEmoji));
    }

    @Override
    public CompletableFuture<Void> removeReactionsByEmojiFromMessage(User user, Emoji... emojis) {
        return CompletableFuture.allOf(Arrays.stream(emojis)
                .map(emoji -> removeReactionByEmojiFromMessage(user, emoji))
                .toArray(CompletableFuture[]::new));
    }

    @Override
    public CompletableFuture<Void> removeReactionsByEmojiFromMessage(User user, String... unicodeEmojis) {
        return removeReactionsByEmojiFromMessage(
                user, Arrays.stream(unicodeEmojis)
                        .map(UnicodeEmoji::fromString)
                        .toArray(Emoji[]::new));
    }

    @Override
    public CompletableFuture<Void> removeReactionsByEmojiFromMessage(Emoji... emojis) {
        return CompletableFuture.allOf(
                Arrays.stream(emojis)
                        .map(this::removeReactionByEmojiFromMessage)
                        .toArray(CompletableFuture[]::new));
    }

    @Override
    public CompletableFuture<Void> removeReactionsByEmojiFromMessage(String... unicodeEmojis) {
        return removeReactionsByEmojiFromMessage(Arrays.stream(unicodeEmojis)
                                                         .map(UnicodeEmoji::fromString)
                                                         .toArray(Emoji[]::new));
    }

    @Override
    public CompletableFuture<Void> removeOwnReactionByEmojiFromMessage(Emoji emoji) {
        return removeReactionByEmojiFromMessage(getApi().getYourself(), emoji);
    }

    @Override
    public CompletableFuture<Void> removeOwnReactionByEmojiFromMessage(String unicodeEmoji) {
        return removeOwnReactionByEmojiFromMessage(UnicodeEmoji.fromString(unicodeEmoji));
    }

    @Override
    public CompletableFuture<Void> removeOwnReactionsByEmojiFromMessage(Emoji... emojis) {
        return removeReactionsByEmojiFromMessage(getApi().getYourself(), emojis);
    }

    @Override
    public CompletableFuture<Void> removeOwnReactionsByEmojiFromMessage(String... unicodeEmojis) {
        return removeOwnReactionsByEmojiFromMessage(Arrays.stream(unicodeEmojis)
                                                            .map(UnicodeEmoji::fromString)
                                                            .toArray(Emoji[]::new));
    }

    @Override
    public CompletableFuture<Void> pinMessage() {
        return Message.pin(getApi(), getChannel().getId(), getMessageId());
    }

    @Override
    public CompletableFuture<Void> unpinMessage() {
        return Message.unpin(getApi(), getChannel().getId(), getMessageId());
    }

}
