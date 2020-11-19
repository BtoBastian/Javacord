package org.javacord.api.entity.message;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.DiscordEntity;
import org.javacord.api.entity.Icon;
import org.javacord.api.entity.Nameable;
import org.javacord.api.entity.channel.Categorizable;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.entity.webhook.Webhook;

import java.awt.Color;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * This class represents either a user or a webhook.
 *
 * <p>Do not confuse a webhook with a bot account which is also considered to be a user.
 */
public interface MessageAuthor extends DiscordEntity, Nameable {

    /**
     * Gets the message.
     *
     * @return The message.
     */
    Message getMessage();

    /**
     * Gets the display name of the author.
     *
     * @return The display name of the author.
     */
    default String getDisplayName() {
        Optional<Server> server = getMessage().getServer();
        Optional<User> user = asUser();
        if (user.isPresent()) {
            return server.map(s -> user.get().getDisplayName(s)).orElseGet(() -> user.get().getName());
        }
        return getName();
    }

    /**
     * If the author is a user, gets the discriminated name of the user, e. g. {@code Bastian#8222},
     * otherwise just gets the name of the author.
     *
     * @return The discriminated name of the user or the name of the author.
     */
    default String getDiscriminatedName() {
        return getDiscriminator().map(discriminator -> getName() + "#" + discriminator).orElseGet(this::getName);
    }

    /**
     * Gets the discriminator of the author if the author is a user.
     *
     * @return The discriminator of the author if the author is a user.
     */
    Optional<String> getDiscriminator();

    /**
     * Gets the avatar of the author.
     *
     * @return The avatar of the author.
     */
    Icon getAvatar();

    /**
     * Gets the voice channel this MessageAuthor (if it is an User)
     * is connected to on the server where the message has been sent.
     *
     * @return The server voice channel the MessageAuthor is connected to.
     */
    default Optional<ServerVoiceChannel> getConnectedVoiceChannel() {
        return getMessage().getServer().flatMap(server -> server.getConnectedVoiceChannel(getId()));
    }

    /**
     * Checks if the author of the message is a user.
     *
     * <p>A user might be a human user using any number of Discord client applications or a bot user connecting
     * via Discord's bot api. See {@link #isRegularUser} and {@link #isBotUser} for further detail.
     *
     * <p>The opposite of this method is {@link #isWebhook()}.
     *
     * @return Whether the author is a user or not.
     */
    boolean isUser();

    /**
     * Checks if the author is the owner of the current account.
     * Always returns <code>false</code> if logged in to a user account.
     *
     * @return Whether the author is the owner of the current account.
     */
    default boolean isBotOwner() {
        return isUser() && getApi().getOwnerId() == getId();
    }

    /**
     * Checks if the author is a bot user.
     *
     * <p>Keep in mind, that if this returns {@code false}, it does not mean that the message was sent by a regular
     * user. It's still possible that the message was sent via a webhook.
     *
     * @return Whether the author is a bot user.
     */
    default boolean isBotUser() {
        return asUser().map(User::isBot).orElse(false);
    }

    /**
     * Checks if the author is a regular user.
     *
     * <p>Keep in mind, that if this returns {@code false}, it does not mean that the message was sent by a bot
     * user. It's still possible that the message was sent via a webhook.
     *
     * @return Whether the author is a regular user.
     */
    default boolean isRegularUser() {
        return asUser().map(user -> !user.isBot()).orElse(false);
    }

    /**
     * Checks if the author can create new channels on the server where the message was sent.
     * Always returns {@code false} if the author is not a user or the message was not sent on a server.
     *
     * @return Whether the author can create channels on the server or not.
     */
    default boolean canCreateChannelsOnServer() {
        return getMessage()
                .getServer()
                .flatMap(server -> asUser().map(server::canCreateChannels))
                .orElse(false);
    }

    /**
     * Checks if the author can view the audit log of the server where the message was sent.
     * Always returns {@code false} if the author is not a user or the message was not sent on a server.
     *
     * @return Whether the author can view the audit log of the server or not.
     */
    default boolean canViewAuditLogOfServer() {
        return getMessage()
                .getServer()
                .flatMap(server -> asUser().map(server::canViewAuditLog))
                .orElse(false);
    }

    /**
     * Checks if the author can change its own nickname in the server where the message was sent.
     * Always returns {@code false} if the author is not a user or the message was not sent on a server.
     *
     * @return Whether the author can change its own nickname in the server or not.
     */
    default boolean canChangeOwnNicknameOnServer() {
        return getMessage()
                .getServer()
                .flatMap(server -> asUser().map(server::canChangeOwnNickname))
                .orElse(false);
    }

    /**
     * Checks if the author can manage nicknames on the server where the message was sent.
     * Always returns {@code false} if the author is not a user or the message was not sent on a server.
     *
     * @return Whether the author can manage nicknames on the server or not.
     */
    default boolean canManageNicknamesOnServer() {
        return getMessage()
                .getServer()
                .flatMap(server -> asUser().map(server::canManageNicknames))
                .orElse(false);
    }

    /**
     * Checks if the author can mute members on the server where the message was sent.
     * Always returns {@code false} if the author is not a user or the message was not sent on a server.
     *
     * @return Whether the author can mute members on the server or not.
     */
    default boolean canMuteMembersOnServer() {
        return getMessage()
                .getServer()
                .flatMap(server -> asUser().map(server::canMuteMembers))
                .orElse(false);
    }

    /**
     * Checks if the author can deafen members on the server where the message was sent.
     * Always returns {@code false} if the author is not a user or the message was not sent on a server.
     *
     * @return Whether the author can deafen members on the server or not.
     */
    default boolean canDeafenMembersOnServer() {
        return getMessage()
                .getServer()
                .flatMap(server -> asUser().map(server::canDeafenMembers))
                .orElse(false);
    }

    /**
     * Checks if the author can move members on the server where the message was sent.
     * Always returns {@code false} if the author is not a user or the message was not sent on a server.
     *
     * @return Whether the author can move members on the server or not.
     */
    default boolean canMoveMembersOnServer() {
        return getMessage()
                .getServer()
                .flatMap(server -> asUser().map(server::canMoveMembers))
                .orElse(false);
    }

    /**
     * Checks if the author can manage emojis on the server where the message was sent.
     * Always returns {@code false} if the author is not a user or the message was not sent on a server.
     *
     * @return Whether the author can manage emojis on the server or not.
     */
    default boolean canManageEmojisOnServer() {
        return getMessage()
                .getServer()
                .flatMap(server -> asUser().map(server::canManageEmojis))
                .orElse(false);
    }

    /**
     * Checks if the author can manage roles on the server where the message was sent.
     * Always returns {@code false} if the author is not a user or the message was not sent on a server.
     *
     * @return Whether the author can manage roles on the server or not.
     */
    default boolean canManageRolesOnServer() {
        return getMessage()
                .getServer()
                .flatMap(server -> asUser().map(server::canManageRoles))
                .orElse(false);
    }

    /**
     * Checks if the author can manage the server where the message was sent.
     * Always returns {@code false} if the author is not a user or the message was not sent on a server.
     *
     * @return Whether the author can manage the server or not.
     */
    default boolean canManageServer() {
        return getMessage()
                .getServer()
                .flatMap(server -> asUser().map(server::canManage))
                .orElse(false);
    }

    /**
     * Checks if the author can kick users from the server where the message was sent.
     * Always returns {@code false} if the author is not a user or the message was not sent on a server.
     *
     * @return Whether the author can kick users from the server or not.
     */
    default boolean canKickUsersFromServer() {
        return getMessage()
                .getServer()
                .flatMap(server -> asUser().map(server::canKickUsers))
                .orElse(false);
    }

    /**
     * Checks if the author can kick the user from the server where the message was sent.
     * This methods also considers the position of the user roles.
     * Always returns {@code false} if the author is not a user or the message was not sent on a server.
     *
     * @param userToKick The user which should be kicked.
     * @return Whether the author can kick the user from the server or not.
     */
    default boolean canKickUserFromServer(User userToKick) {
        return getMessage()
                .getServer()
                .flatMap(server -> asUser().map(user -> server.canKickUser(user, userToKick)))
                .orElse(false);
    }

    /**
     * Checks if the author can ban users from the server where the message was sent.
     * Always returns {@code false} if the author is not a user or the message was not sent on a server.
     *
     * @return Whether the author can ban users from the server or not.
     */
    default boolean canBanUsersFromServer() {
        return getMessage()
                .getServer()
                .flatMap(server -> asUser().map(server::canBanUsers))
                .orElse(false);
    }

    /**
     * Checks if the author can ban the user from the server where the message was sent.
     * This methods also considers the position of the user roles.
     * Always returns {@code false} if the author is not a user or the message was not sent on a server.
     *
     * @param userToBan The user which should be banned.
     * @return Whether the author can ban the user from the server or not.
     */
    default boolean canBanUserFromServer(User userToBan) {
        return getMessage()
                .getServer()
                .flatMap(server -> asUser().map(user -> server.canBanUser(user, userToBan)))
                .orElse(false);
    }

    /**
     * Checks if the author can see the channel where the message was sent.
     * In private chats (private channel or group channel) this always returns {@code true} if the user is
     * part of the chat.
     * Always returns {@code false} if the author is not a user or the message was not sent on a server.
     *
     * @return Whether the author can see the channel or not.
     */
    default boolean canSeeChannel() {
        return asUser()
                .map(getMessage().getChannel()::canSee)
                .orElse(false);
    }

    /**
     * Checks if the author can see all channels in the category of the channel where the message was sent.
     * Always returns {@code false} if the author is not a user.
     * Always returns {@code true} if the channel is not categorizable or has no category.
     *
     * @return Whether the user can see all channels in this category or not.
     */
    default boolean canSeeAllChannelsInCategory() {
        return getMessage()
                .getChannel()
                .asCategorizable()
                .flatMap(Categorizable::getCategory)
                .map(channelCategory -> asUser().map(channelCategory::canSeeAll).orElse(false))
                .orElse(true);
    }

    /**
     * Checks if the author can create an instant invite to the channel where the message was sent.
     * Always returns {@code false} if the author is not a user or the message was not sent on a server.
     *
     * @return Whether the author can create an instant invite to the channel or not.
     */
    default boolean canCreateInstantInviteToTextChannel() {
        return getMessage()
                .getChannel()
                .asServerChannel()
                .flatMap(serverChannel -> asUser().map(serverChannel::canCreateInstantInvite))
                .orElse(false);
    }

    /**
     * Checks if the author can send messages in the channel where the message was sent.
     * In private chats (private channel or group channel) this always returns {@code true} if the user is
     * part of the chat.
     * Please notice, this does not check if a user has blocked private messages!
     * Always returns {@code false} if the author is not a user.
     *
     * @return Whether the author can write messages in the channel or not.
     */
    default boolean canWriteInTextChannel() {
        return getMessage()
                .getChannel()
                .asTextChannel()
                .map(textChannel -> asUser().map(textChannel::canWrite).orElse(false))
                .orElseThrow(AssertionError::new);
    }

    /**
     * Checks if the author can use external emojis in the channel where the message was sent.
     * In private chats (private channel or group channel) this always returns {@code true} if the author is
     * part of the chat.
     * Please notice, this does not check if a user has blocked private messages!
     * It also doesn't check if the user is even able to send any external emojis (twitch subscription or nitro).
     * Always returns {@code false} if the author is not a user.
     *
     * @return Whether the author can use external emojis in the channel or not.
     */
    default boolean canUseExternalEmojisInTextChannel() {
        return getMessage()
                .getChannel()
                .asTextChannel()
                .map(textChannel -> asUser().map(textChannel::canUseExternalEmojis).orElse(false))
                .orElseThrow(AssertionError::new);
    }

    /**
     * Checks if the author can use embed links in the channel where the message was sent.
     * In private chats (private channel or group channel) this always returns {@code true} if the author is
     * part of the chat.
     * Please notice, this does not check if a user has blocked private messages!
     * Always returns {@code false} if the author is not a user.
     *
     * @return Whether the author can embed links in the channel or not.
     */
    default boolean canEmbedLinksInTextChannel() {
        return getMessage()
                .getChannel()
                .asTextChannel()
                .map(textChannel -> asUser().map(textChannel::canEmbedLinks).orElse(false))
                .orElseThrow(AssertionError::new);
    }

    /**
     * Checks if the author can read the message history of the channel where the message was sent.
     * In private chats (private channel or group channel) this always returns {@code true} if the user is
     * part of the chat.
     * Always returns {@code false} if the author is not a user.
     *
     * @return Whether the author can read the message history of the channel or not.
     */
    default boolean canReadMessageHistoryOfTextChannel() {
        return getMessage()
                .getChannel()
                .asTextChannel()
                .map(textChannel -> asUser().map(textChannel::canReadMessageHistory).orElse(false))
                .orElseThrow(AssertionError::new);
    }

    /**
     * Checks if the author can use tts (text to speech) in the channel where the message was sent.
     * In private chats (private channel or group channel) this always returns {@code true} if the user is
     * part of the chat.
     * Please notice, this does not check if a user has blocked private messages!
     * Always returns {@code false} if the author is not a user.
     *
     * @return Whether the author can use tts in the channel or not.
     */
    default boolean canUseTtsInTextChannel() {
        return getMessage()
                .getChannel()
                .asTextChannel()
                .map(textChannel -> asUser().map(textChannel::canUseTts).orElse(false))
                .orElseThrow(AssertionError::new);
    }

    /**
     * Checks if the author can attach files in the channel where the message was sent.
     * Always returns {@code false} if the author is not a user.
     *
     * @return Whether the author can attach files in the channel or not.
     */
    default boolean canAttachFilesToTextChannel() {
        return getMessage()
                .getChannel()
                .asTextChannel()
                .map(textChannel -> asUser().map(textChannel::canAttachFiles).orElse(false))
                .orElseThrow(AssertionError::new);
    }

    /**
     * Checks if the author is allowed to add <b>new</b> reactions to messages in the channel where the message was
     * sent. Always returns {@code false} if the author is not a user.
     *
     * @return Whether the author is allowed to add <b>new</b> reactions to messages in the channel or not.
     */
    default boolean canAddNewReactionsInTextChannel() {
        return getMessage()
                .getChannel()
                .asTextChannel()
                .map(textChannel -> asUser().map(textChannel::canAddNewReactions).orElse(false))
                .orElseThrow(AssertionError::new);
    }

    /**
     * Checks if the author can manage messages (delete or pin them or remove reactions of others) in the channel
     * where the message was sent. In private chats (private channel or group channel) this always returns {@code true}
     * if the user is part of the chat.
     * Always returns {@code false} if the author is not a user.
     *
     * @return Whether the author can manage messages in the channel or not.
     */
    default boolean canManageMessagesInTextChannel() {
        return getMessage()
                .getChannel()
                .asTextChannel()
                .map(textChannel -> asUser().map(textChannel::canManageMessages).orElse(false))
                .orElseThrow(AssertionError::new);
    }

    /**
     * Checks if the author can remove reactions of other users in the channel where the message was sent.
     * In private chats (private channel or group channel) this always returns {@code true} if the user is
     * part of the chat.
     * Always returns {@code false} if the author is not a user.
     *
     * @return Whether the author can remove reactions of others in the channel or not.
     */
    default boolean canRemoveReactionsOfOthersInTextChannel() {
        return getMessage()
                .getChannel()
                .asTextChannel()
                .map(textChannel -> asUser().map(textChannel::canRemoveReactionsOfOthers).orElse(false))
                .orElseThrow(AssertionError::new);
    }

    /**
     * Checks if the author can mention everyone (@everyone) in the channel where the message was sent.
     * In private chats (private channel or group channel) this always returns {@code true} if the user is
     * part of the chat.
     * Always returns {@code false} if the author is not a user.
     *
     * @return Whether the given user can mention everyone (@everyone) or not.
     */
    default boolean canMentionEveryoneInTextChannel() {
        return getMessage()
                .getChannel()
                .asTextChannel()
                .map(textChannel -> asUser().map(textChannel::canMentionEveryone).orElse(false))
                .orElseThrow(AssertionError::new);
    }

    /**
     * Checks if the author can connect to the voice channel where the message was sent.
     * In private chats (private channel or group channel) this always returns {@code true} if the user is
     * part of the chat.
     * Always returns {@code false} if the author is not a user or if the channel is not a voice channel.
     *
     * @return Whether the author can connect to the voice channel or not.
     */
    default boolean canConnectToVoiceChannel() {
        return getMessage()
                .getChannel()
                .asVoiceChannel()
                .flatMap(voiceChannel -> asUser().map(voiceChannel::canConnect))
                .orElse(false);
    }

    /**
     * Checks if the author can mute other users in the voice channel where the message was sent.
     * In private chats (private channel or group channel) this always returns @{code false}.
     * Always returns {@code false} if the author is not a user or if the channel is not a voice channel.
     *
     * @return Whether the author can mute other users in the voice channel or not.
     */
    default boolean canMuteUsersInVoiceChannel() {
        return getMessage()
                .getChannel()
                .asVoiceChannel()
                .flatMap(voiceChannel -> asUser().map(voiceChannel::canMuteUsers))
                .orElse(false);
    }

    /**
     * Checks if the author is allowed to add <b>new</b> reactions to the message.
     * Always returns {@code false} if the author is not a user.
     *
     * @return Whether the author is allowed to add <b>new</b> reactions to the message or not.
     */
    default boolean canAddNewReactionsToMessage() {
        return asUser()
                .map(getMessage()::canAddNewReactions)
                .orElse(false);
    }

    /**
     * Checks if the author can delete the message.
     * Always returns {@code false} if the author is not a user.
     *
     * @return Whether the author can delete the message or not.
     */
    default boolean canDeleteMessage() {
        return asUser()
                .map(getMessage()::canDelete)
                .orElse(false);
    }

    /**
     * Gets the displayed color if the author was a User on a Server.
     *
     * @return The display color, if applicable.
     */
    default Optional<Color> getRoleColor() {
        return getMessage()
                .getServer()
                .flatMap(server -> asUser().flatMap(server::getRoleColor));
    }

    /**
     * Checks if the author is an administrator of the server where the message was sent.
     * Always returns {@code false} if the author is not a user or the message was not sent on a server.
     *
     * @return Whether the author is an administrator of the server or not.
     */
    default boolean isServerAdmin() {
        return getMessage()
                .getServer()
                .flatMap(server -> asUser().map(server::isAdmin))
                .orElse(false);
    }

    /**
     * Gets the author as user.
     *
     * @return The author as user.
     */
    Optional<User> asUser();

    /**
     * Checks if the author is a webhook.
     *
     * <p>The opposite of this method is {@link #isUser()}.
     *
     * @return Whether the author is a webhook or not.
     */
    boolean isWebhook();

    /**
     * Gets the author as a webhook.
     *
     * @return The author as a webhook.
     */
    default Optional<CompletableFuture<Webhook>> asWebhook() {
        if (isWebhook()) {
            return Optional.of(getApi().getWebhookById(getId()));
        }
        return Optional.empty();
    }

    /**
     * Gets if this author is the user of the connected account.
     *
     * @return Whether this author is the user of the connected account or not.
     * @see DiscordApi#getYourself()
     */
    default boolean isYourself() {
        return asUser().map(User::isYourself).orElse(false);
    }

}
