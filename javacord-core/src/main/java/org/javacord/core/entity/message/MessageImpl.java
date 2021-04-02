package org.javacord.core.entity.message;

import com.fasterxml.jackson.databind.JsonNode;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.DiscordEntity;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.emoji.CustomEmoji;
import org.javacord.api.entity.emoji.Emoji;
import org.javacord.api.entity.emoji.UnicodeEmoji;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageActivity;
import org.javacord.api.entity.message.MessageAttachment;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.entity.message.MessageType;
import org.javacord.api.entity.message.Reaction;
import org.javacord.api.entity.message.embed.Embed;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.user.User;
import org.javacord.api.util.DiscordRegexPattern;
import org.javacord.core.DiscordApiImpl;
import org.javacord.core.entity.message.embed.EmbedImpl;
import org.javacord.core.entity.server.ServerImpl;
import org.javacord.core.entity.user.MemberImpl;
import org.javacord.core.entity.user.UserImpl;
import org.javacord.core.listener.message.InternalMessageAttachableListenerManager;
import org.javacord.core.util.cache.MessageCacheImpl;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;

/**
 * The implementation of {@link Message}.
 */
public class MessageImpl implements Message, InternalMessageAttachableListenerManager {

    /**
     * The discord api instance.
     */
    private final DiscordApiImpl api;

    /**
     * The channel of the message.
     */
    private final TextChannel channel;

    /**
     * The id of the message.
     */
    private final long id;

    /**
     * The content of the message.
     */
    private volatile String content;

    /**
     * The type of the message.
     */
    private final MessageType type;

    /**
     * The pinned flag of the message.
     */
    private volatile boolean pinned;

    /**
     * The text-to-speech flag of the message.
     */
    private volatile boolean tts;

    /**
     * If the message mentions everyone or not.
     */
    private volatile boolean mentionsEveryone;

    /**
     * Gets the last edit time.
     */
    private volatile Instant lastEditTime;

    /**
     * The author of the message.
     */
    private final MessageAuthor author;

    /**
     * The activity of the message.
     */
    private final MessageActivityImpl activity;

    /**
     * The nonce of the message.
     */
    private final String nonce;

    /**
     * The id of the message referenced via message reply.
     */
    private final Long referencedMessageId;
    /**
     * The message referenced via message reply.
     */
    private final Message referencedMessage;

    /**
     * If the message should be cached forever or not.
     */
    private volatile boolean cacheForever = false;

    /**
     * A list with all embeds.
     */
    private final List<Embed> embeds = new ArrayList<>();

    /**
     * A list with all reactions.
     */
    private final List<Reaction> reactions = new ArrayList<>();

    /**
     * The attachments of the message.
     */
    private final List<MessageAttachment> attachments = new ArrayList<>();

    /**
     * The users mentioned in this message.
     */
    private final List<User> mentions = new ArrayList<>();

    /**
     * The roles mentioned in this message.
     */
    private final List<Role> roleMentions = new ArrayList<>();

    /**
     * Creates a new message object.
     *
     * @param api The discord api instance.
     * @param channel The channel of the message.
     * @param data The json data of the message.
     */
    public MessageImpl(DiscordApiImpl api, TextChannel channel, JsonNode data) {
        this.api = api;
        this.channel = channel;

        id = data.get("id").asLong();
        content = data.get("content").asText();

        pinned = data.get("pinned").asBoolean(false);
        tts = data.get("tts").asBoolean(false);
        mentionsEveryone = data.get("mention_everyone").asBoolean(false);

        lastEditTime = data.has("edited_timestamp") && !data.get("edited_timestamp").isNull()
                ? OffsetDateTime.parse(data.get("edited_timestamp").asText()).toInstant()
                : null;

        type = MessageType.byType(data.get("type").asInt(), data.has("webhook_id"));

        Long webhookId = data.has("webhook_id") ? data.get("webhook_id").asLong() : null;
        author = new MessageAuthorImpl(this, webhookId, data);

        MessageCacheImpl cache = (MessageCacheImpl) channel.getMessageCache();
        cache.addMessage(this);

        if (data.has("embeds")) {
            for (JsonNode embedJson : data.get("embeds")) {
                Embed embed = new EmbedImpl(embedJson);
                embeds.add(embed);
            }
        }

        if (data.has("reactions")) {
            for (JsonNode reactionJson : data.get("reactions")) {
                Reaction reaction = new ReactionImpl(this, reactionJson);
                reactions.add(reaction);
            }
        }

        if (data.has("attachments")) {
            for (JsonNode attachmentJson : data.get("attachments")) {
                MessageAttachment attachment = new MessageAttachmentImpl(this, attachmentJson);
                attachments.add(attachment);
            }
        }

        if (data.has("mentions")) {
            for (JsonNode mentionJson : data.get("mentions")) {
                User user = new UserImpl(api, mentionJson, (MemberImpl) null,
                        getServer().map(ServerImpl.class::cast).orElse(null));
                mentions.add(user);
            }
        }

        if (data.hasNonNull("mention_roles")) {
            getServer().ifPresent(server -> {
                for (JsonNode roleMentionJson : data.get("mention_roles")) {
                    server.getRoleById(roleMentionJson.asText()).ifPresent(roleMentions::add);
                }
            });
        }

        if (data.hasNonNull("activity")) {
            activity = new MessageActivityImpl(this, data.get("activity"));
        } else {
            activity = null;
        }

        if (data.hasNonNull("nonce")) {
            nonce = data.get("nonce").asText();
        } else {
            nonce = null;
        }

        if (data.hasNonNull("message_reference")) {
            referencedMessageId = data.get("message_reference").get("message_id").asLong();
        } else {
            referencedMessageId = null;
        }

        if (data.hasNonNull("referenced_message")) {
            referencedMessage = api.getOrCreateMessage(channel, data.get("referenced_message"));
        } else {
            referencedMessage = null;
        }
    }

    /**
     * Sets the content of the message.
     *
     * @param content The content to set.
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * Sets the pinned flag if the message.
     *
     * @param pinned The pinned flag to set.
     */
    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }

    /**
     * Sets the mentions everyone flag.
     *
     * @param mentionsEveryone The mentions everyone flag to set.
     */
    public void setMentionsEveryone(boolean mentionsEveryone) {
        this.mentionsEveryone = mentionsEveryone;
    }

    /**
     * Sets the last edit time of the message.
     *
     * @param lastEditTime The last edit time of the message.
     */
    public void setLastEditTime(Instant lastEditTime) {
        this.lastEditTime = lastEditTime;
    }

    /**
     * Sets the embeds of the message.
     *
     * @param embeds The embeds to set.
     */
    public void setEmbeds(List<Embed> embeds) {
        this.embeds.clear();
        this.embeds.addAll(embeds);
    }

    /**
     * Set the tts flag for the message.
     *
     * @param tts The new flag.
     */
    public void setTts(boolean tts) {
        this.tts = tts;
    }

    /**
     * Adds an emoji to the list of reactions.
     *
     * @param emoji The emoji.
     * @param you Whether this reaction is used by you or not.
     */
    public void addReaction(Emoji emoji, boolean you) {
        Optional<Reaction> reaction = reactions.stream().filter(r -> emoji.equalsEmoji(r.getEmoji())).findAny();
        reaction.ifPresent(r -> ((ReactionImpl) r).incrementCount(you));
        if (!reaction.isPresent()) {
            reactions.add(new ReactionImpl(this, emoji, 1, you));
        }
    }

    /**
     * Removes an emoji from the list of reactions.
     *
     * @param emoji The emoji.
     * @param you Whether this reaction is used by you or not.
     */
    public void removeReaction(Emoji emoji, boolean you) {
        Optional<Reaction> reaction = reactions.stream().filter(r -> emoji.equalsEmoji(r.getEmoji())).findAny();
        reaction.ifPresent(r -> ((ReactionImpl) r).decrementCount(you));
        reactions.removeIf(r -> r.getCount() <= 0);
    }

    /**
     * Removes all reaction from this message.
     */
    public void removeAllReactionsFromCache() {
        reactions.clear();
    }

    @Override
    public DiscordApi getApi() {
        return api;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getContent() {
        return content;
    }

    @Override
    public Optional<Instant> getLastEditTimestamp() {
        return Optional.ofNullable(lastEditTime);
    }

    @Override
    public List<MessageAttachment> getAttachments() {
        return Collections.unmodifiableList(attachments);
    }

    @Override
    public List<CustomEmoji> getCustomEmojis() {
        String content = getContent();
        List<CustomEmoji> emojis = new ArrayList<>();
        Matcher customEmoji = DiscordRegexPattern.CUSTOM_EMOJI.matcher(content);
        while (customEmoji.find()) {
            long id = Long.parseLong(customEmoji.group("id"));
            String name = customEmoji.group("name");
            boolean animated = customEmoji.group(0).charAt(1) == 'a';
            // TODO Maybe it would be better to cache the custom emoji objects inside the message object
            CustomEmoji emoji = ((DiscordApiImpl) getApi()).getKnownCustomEmojiOrCreateCustomEmoji(id, name, animated);
            emojis.add(emoji);
        }
        return Collections.unmodifiableList(emojis);
    }

    @Override
    public MessageType getType() {
        return type;
    }

    @Override
    public TextChannel getChannel() {
        return channel;
    }

    @Override
    public Optional<MessageActivity> getActivity() {
        return Optional.ofNullable(activity);
    }

    @Override
    public boolean isPinned() {
        return pinned;
    }

    @Override
    public boolean isTts() {
        return tts;
    }

    @Override
    public boolean mentionsEveryone() {
        return mentionsEveryone;
    }

    @Override
    public List<Embed> getEmbeds() {
        return Collections.unmodifiableList(new ArrayList<>(embeds));
    }

    @Override
    public MessageAuthor getAuthor() {
        return author;
    }

    @Override
    public Optional<User> getUserAuthor() {
        return author.asUser();
    }

    @Override
    public Optional<Long> getReferencedMessageId() {
        return Optional.ofNullable(referencedMessageId);
    }

    @Override
    public Optional<Message> getReferencedMessage() {
        return Optional.ofNullable(referencedMessage);
    }

    @Override
    public boolean isCachedForever() {
        return cacheForever;
    }

    @Override
    public void setCachedForever(boolean cachedForever) {
        this.cacheForever = cachedForever;
        if (cachedForever) {
            // Just make sure it's in the cache
            ((MessageCacheImpl) channel.getMessageCache()).addMessage(this);
            ((MessageCacheImpl) channel.getMessageCache()).addCacheForeverMessage(this);
        } else {
            ((MessageCacheImpl) channel.getMessageCache()).removeCacheForeverMessage(this);
        }
    }

    @Override
    public List<Reaction> getReactions() {
        return Collections.unmodifiableList(new ArrayList<>(reactions));
    }

    @Override
    public List<User> getMentionedUsers() {
        return Collections.unmodifiableList(new ArrayList<>(mentions));
    }

    @Override
    public List<Role> getMentionedRoles() {
        return Collections.unmodifiableList(new ArrayList<>(roleMentions));
    }

    @Override
    public Optional<String> getNonce() {
        return Optional.ofNullable(nonce);
    }

    @Override
    public CompletableFuture<Void> addReactions(String... unicodeEmojis) {
        return addReactions(Arrays.stream(unicodeEmojis).map(UnicodeEmoji::fromString).toArray(Emoji[]::new));
    }

    @Override
    public CompletableFuture<Void> removeReactionByEmoji(User user, String unicodeEmoji) {
        return removeReactionByEmoji(user, UnicodeEmoji.fromString(unicodeEmoji));
    }

    @Override
    public CompletableFuture<Void> removeReactionByEmoji(String unicodeEmoji) {
        return removeReactionByEmoji(UnicodeEmoji.fromString(unicodeEmoji));
    }

    @Override
    public CompletableFuture<Void> removeReactionsByEmoji(User user, String... unicodeEmojis) {
        return removeReactionsByEmoji(user,
                Arrays.stream(unicodeEmojis).map(UnicodeEmoji::fromString).toArray(Emoji[]::new));
    }

    @Override
    public CompletableFuture<Void> removeReactionsByEmoji(String... unicodeEmojis) {
        return removeReactionsByEmoji(
                Arrays.stream(unicodeEmojis).map(UnicodeEmoji::fromString).toArray(Emoji[]::new));
    }

    @Override
    public CompletableFuture<Void> removeOwnReactionByEmoji(String unicodeEmoji) {
        return removeOwnReactionByEmoji(UnicodeEmoji.fromString(unicodeEmoji));
    }

    @Override
    public CompletableFuture<Void> removeOwnReactionsByEmoji(String... unicodeEmojis) {
        return removeOwnReactionsByEmoji(
                Arrays.stream(unicodeEmojis).map(UnicodeEmoji::fromString).toArray(Emoji[]::new));
    }

    @Override
    public int compareTo(Message otherMessage) {
        return Long.compareUnsigned(getId(), otherMessage.getId());
    }

    @Override
    public boolean equals(Object o) {
        return (this == o)
               || !((o == null)
                    || (getClass() != o.getClass())
                    || (getId() != ((DiscordEntity) o).getId()));
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    @Override
    public String toString() {
        return String.format("Message (id: %s, content: %s)", getIdAsString(), getContent());
    }
}
