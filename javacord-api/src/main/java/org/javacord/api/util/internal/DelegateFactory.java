package org.javacord.api.util.internal;

import org.javacord.api.DiscordApi;
import org.javacord.api.audio.internal.AudioSourceBaseDelegate;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.channel.internal.ChannelCategoryBuilderDelegate;
import org.javacord.api.entity.channel.internal.ServerChannelUpdaterDelegate;
import org.javacord.api.entity.channel.internal.ServerTextChannelBuilderDelegate;
import org.javacord.api.entity.channel.internal.ServerTextChannelUpdaterDelegate;
import org.javacord.api.entity.channel.internal.ServerVoiceChannelBuilderDelegate;
import org.javacord.api.entity.channel.internal.ServerVoiceChannelUpdaterDelegate;
import org.javacord.api.entity.emoji.KnownCustomEmoji;
import org.javacord.api.entity.emoji.internal.CustomEmojiBuilderDelegate;
import org.javacord.api.entity.emoji.internal.CustomEmojiUpdaterDelegate;
import org.javacord.api.entity.message.embed.internal.EmbedBuilderDelegate;
import org.javacord.api.entity.message.internal.MessageBuilderDelegate;
import org.javacord.api.entity.message.mention.internal.AllowedMentionsBuilderDelegate;
import org.javacord.api.entity.permission.Permissions;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.permission.internal.PermissionsBuilderDelegate;
import org.javacord.api.entity.permission.internal.RoleBuilderDelegate;
import org.javacord.api.entity.permission.internal.RoleUpdaterDelegate;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.server.internal.ServerBuilderDelegate;
import org.javacord.api.entity.server.internal.ServerUpdaterDelegate;
import org.javacord.api.entity.server.invite.internal.InviteBuilderDelegate;
import org.javacord.api.entity.webhook.Webhook;
import org.javacord.api.entity.webhook.internal.WebhookBuilderDelegate;
import org.javacord.api.entity.webhook.internal.WebhookUpdaterDelegate;
import org.javacord.api.internal.AccountUpdaterDelegate;
import org.javacord.api.internal.DiscordApiBuilderDelegate;
import org.javacord.api.util.exception.DiscordExceptionValidator;
import org.javacord.api.util.logging.internal.ExceptionLoggerDelegate;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * This class is used by Javacord internally.
 * You probably won't need it ever.
 */
public class DelegateFactory {

    /**
     * The factory builder delegate. It's used to create new factories.
     */
    private static final DelegateFactoryDelegate delegateFactoryDelegate;

    /**
     * The exception logger delegate.
     */
    private static final ExceptionLoggerDelegate exceptionLoggerDelegate;

    /**
     * The discord exception validator.
     */
    private static final DiscordExceptionValidator discordExceptionValidator;

    // Load it static, because it has a better performance to load it only once
    static {
        ServiceLoader<DelegateFactoryDelegate> delegateServiceLoader =
                ServiceLoader.load(DelegateFactoryDelegate.class, DelegateFactory.class.getClassLoader());
        Iterator<DelegateFactoryDelegate> delegateIterator = delegateServiceLoader.iterator();
        if (delegateIterator.hasNext()) {
            delegateFactoryDelegate = delegateIterator.next();
            if (delegateIterator.hasNext()) {
                throw new IllegalStateException("Found more than one DelegateFactoryDelegate implementation!");
            }
        } else {
            throw new IllegalStateException("No DelegateFactoryDelegate implementation was found!");
        }
        exceptionLoggerDelegate = delegateFactoryDelegate.createExceptionLoggerDelegate();
        discordExceptionValidator = delegateFactoryDelegate.createDiscordExceptionValidator();
    }

    private DelegateFactory() {
        throw new UnsupportedOperationException();
    }

    /**
     * Creates a new discord api builder delegate.
     *
     * @return A new discord api builder delegate.
     */
    public static DiscordApiBuilderDelegate createDiscordApiBuilderDelegate() {
        return delegateFactoryDelegate.createDiscordApiBuilderDelegate();
    }

    /**
     * Creates a new embed builder delegate.
     *
     * @return A new embed builder delegate.
     */
    public static EmbedBuilderDelegate createEmbedBuilderDelegate() {
        return delegateFactoryDelegate.createEmbedBuilderDelegate();
    }

    /**
     * Creates a new mention builder delegate.
     *
     * @return A new mention builder delegate.
     */
    public static AllowedMentionsBuilderDelegate createAllowedMentionsBuilderDelegate() {
        return delegateFactoryDelegate.createAllowedMentionsBuilderDelegate();
    }

    /**
     * Creates a new message builder delegate.
     *
     * @return A new message builder delegate.
     */
    public static MessageBuilderDelegate createMessageBuilderDelegate() {
        return delegateFactoryDelegate.createMessageBuilderDelegate();
    }

    /**
     * Creates a new permissions builder delegate.
     *
     * @return A new permissions builder delegate.
     */
    public static PermissionsBuilderDelegate createPermissionsBuilderDelegate() {
        return delegateFactoryDelegate.createPermissionsBuilderDelegate();
    }

    /**
     * Creates a new permissions builder delegate initialized with the given permissions.
     *
     * @param permissions The permissions which should be copied.
     * @return A new permissions builder delegate initialized with the given permissions.
     */
    public static PermissionsBuilderDelegate createPermissionsBuilderDelegate(Permissions permissions) {
        return delegateFactoryDelegate.createPermissionsBuilderDelegate(permissions);
    }

    /**
     * Creates a new channel category builder delegate.
     *
     * @param server The server of the channel category.
     * @return A new channel category builder delegate.
     */
    public static ChannelCategoryBuilderDelegate createChannelCategoryBuilderDelegate(Server server) {
        return delegateFactoryDelegate.createChannelCategoryBuilderDelegate(server);
    }

    /**
     * Creates a new server text channel builder delegate.
     *
     * @param server The server of the server text channel.
     * @return A new server text channel builder delegate.
     */
    public static ServerTextChannelBuilderDelegate createServerTextChannelBuilderDelegate(Server server) {
        return delegateFactoryDelegate.createServerTextChannelBuilderDelegate(server);
    }

    /**
     * Creates a new server voice channel builder delegate.
     *
     * @param server The server of the server voice channel.
     * @return A new server voice channel builder delegate.
     */
    public static ServerVoiceChannelBuilderDelegate createServerVoiceChannelBuilderDelegate(Server server) {
        return delegateFactoryDelegate.createServerVoiceChannelBuilderDelegate(server);
    }

    /**
     * Creates a new custom emoji builder delegate.
     *
     * @param server The server of the custom emoji.
     * @return A new custom emoji builder delegate.
     */
    public static CustomEmojiBuilderDelegate createCustomEmojiBuilderDelegate(Server server) {
        return delegateFactoryDelegate.createCustomEmojiBuilderDelegate(server);
    }

    /**
     * Creates a new webhook builder delegate.
     *
     * @param channel The server text channel of the webhook.
     * @return A new webhook builder delegate.
     */
    public static WebhookBuilderDelegate createWebhookBuilderDelegate(ServerTextChannel channel) {
        return delegateFactoryDelegate.createWebhookBuilderDelegate(channel);
    }

    /**
     * Creates a new server builder delegate.
     *
     * @param api The discord api instance.
     * @return A new server builder delegate.
     */
    public static ServerBuilderDelegate createServerBuilderDelegate(DiscordApi api) {
        return delegateFactoryDelegate.createServerBuilderDelegate(api);
    }

    /**
     * Creates a new role builder delegate.
     *
     * @param server The server for which the role should be created.
     * @return A new role builder delegate.
     */
    public static RoleBuilderDelegate createRoleBuilderDelegate(Server server) {
        return delegateFactoryDelegate.createRoleBuilderDelegate(server);
    }

    /**
     * Creates a new invite builder delegate.
     *
     * @param channel The channel for the invite.
     * @return A new invite builder delegate.
     */
    public static InviteBuilderDelegate createInviteBuilderDelegate(ServerChannel channel) {
        return delegateFactoryDelegate.createInviteBuilderDelegate(channel);
    }

    /**
     * Creates a new account updater delegate.
     *
     * @param api The discord api instance.
     * @return A new account updater delegate.
     */
    public static AccountUpdaterDelegate createAccountUpdaterDelegate(DiscordApi api) {
        return delegateFactoryDelegate.createAccountUpdaterDelegate(api);
    }

    /**
     * Creates a new server channel updater delegate.
     *
     * @param channel The channel to update.
     * @return A new server channel updater delegate.
     */
    public static ServerChannelUpdaterDelegate createServerChannelUpdaterDelegate(ServerChannel channel) {
        return delegateFactoryDelegate.createServerChannelUpdaterDelegate(channel);
    }

    /**
     * Creates a new server text channel updater delegate.
     *
     * @param channel The channel to update.
     * @return A new server text channel updater delegate.
     */
    public static ServerTextChannelUpdaterDelegate createServerTextChannelUpdaterDelegate(ServerTextChannel channel) {
        return delegateFactoryDelegate.createServerTextChannelUpdaterDelegate(channel);
    }

    /**
     * Creates a new server voice channel updater delegate.
     *
     * @param channel The channel to update.
     * @return A new server voice channel updater delegate.
     */
    public static ServerVoiceChannelUpdaterDelegate createServerVoiceChannelUpdaterDelegate(
            ServerVoiceChannel channel) {
        return delegateFactoryDelegate.createServerVoiceChannelUpdaterDelegate(channel);
    }

    /**
     * Creates a new custom emoji updater delegate.
     *
     * @param emoji The custom emoji to update.
     * @return A new custom emoji updater delegate.
     */
    public static CustomEmojiUpdaterDelegate createCustomEmojiUpdaterDelegate(KnownCustomEmoji emoji) {
        return delegateFactoryDelegate.createCustomEmojiUpdaterDelegate(emoji);
    }

    /**
     * Creates a new role updater delegate.
     *
     * @param role The role to update.
     * @return A new role updater delegate.
     */
    public static RoleUpdaterDelegate createRoleUpdaterDelegate(Role role) {
        return delegateFactoryDelegate.createRoleUpdaterDelegate(role);
    }

    /**
     * Creates a new server updater delegate.
     *
     * @param server The server to update.
     * @return A new server updater delegate.
     */
    public static ServerUpdaterDelegate createServerUpdaterDelegate(Server server) {
        return delegateFactoryDelegate.createServerUpdaterDelegate(server);
    }

    /**
     * Creates a new webhook updater delegate.
     *
     * @param webhook The webhook to update.
     * @return A new webhook updater delegate.
     */
    public static WebhookUpdaterDelegate createWebhookUpdaterDelegate(Webhook webhook) {
        return delegateFactoryDelegate.createWebhookUpdaterDelegate(webhook);
    }

    /**
     * Creates a new audio source base delegate.
     *
     * @param api The discord api instance.
     * @return A new audio source base delegate.
     */
    public static AudioSourceBaseDelegate createAudioSourceBaseDelegate(DiscordApi api) {
        return delegateFactoryDelegate.createAudioSourceBaseDelegate(api);
    }

    /**
     * Gets the exception logger delegate.
     *
     * @return The exception logger delegate.
     */
    public static ExceptionLoggerDelegate getExceptionLoggerDelegate() {
        return exceptionLoggerDelegate;
    }

    /**
     * Gets the discord exception validator.
     *
     * @return The discord exception validator.
     */
    public static DiscordExceptionValidator getDiscordExceptionValidator() {
        return discordExceptionValidator;
    }

}
