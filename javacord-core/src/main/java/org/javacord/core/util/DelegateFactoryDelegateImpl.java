package org.javacord.core.util;

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
import org.javacord.api.util.internal.DelegateFactoryDelegate;
import org.javacord.api.util.logging.internal.ExceptionLoggerDelegate;
import org.javacord.core.AccountUpdaterDelegateImpl;
import org.javacord.core.DiscordApiBuilderDelegateImpl;
import org.javacord.core.DiscordApiImpl;
import org.javacord.core.audio.AudioSourceBaseDelegateImpl;
import org.javacord.core.entity.channel.ChannelCategoryBuilderDelegateImpl;
import org.javacord.core.entity.channel.ServerChannelUpdaterDelegateImpl;
import org.javacord.core.entity.channel.ServerTextChannelBuilderDelegateImpl;
import org.javacord.core.entity.channel.ServerTextChannelUpdaterDelegateImpl;
import org.javacord.core.entity.channel.ServerVoiceChannelBuilderDelegateImpl;
import org.javacord.core.entity.channel.ServerVoiceChannelUpdaterDelegateImpl;
import org.javacord.core.entity.emoji.CustomEmojiBuilderDelegateImpl;
import org.javacord.core.entity.emoji.CustomEmojiUpdaterDelegateImpl;
import org.javacord.core.entity.message.MessageBuilderDelegateImpl;
import org.javacord.core.entity.message.embed.EmbedBuilderDelegateImpl;
import org.javacord.core.entity.message.mention.AllowedMentionsBuilderDelegateImpl;
import org.javacord.core.entity.permission.PermissionsBuilderDelegateImpl;
import org.javacord.core.entity.permission.RoleBuilderDelegateImpl;
import org.javacord.core.entity.permission.RoleUpdaterDelegateImpl;
import org.javacord.core.entity.server.ServerBuilderDelegateImpl;
import org.javacord.core.entity.server.ServerImpl;
import org.javacord.core.entity.server.ServerUpdaterDelegateImpl;
import org.javacord.core.entity.server.invite.InviteBuilderDelegateImpl;
import org.javacord.core.entity.webhook.WebhookBuilderDelegateImpl;
import org.javacord.core.entity.webhook.WebhookUpdaterDelegateImpl;
import org.javacord.core.util.exception.DiscordExceptionValidatorImpl;
import org.javacord.core.util.logging.ExceptionLoggerDelegateImpl;

/**
 * The implementation of {@link DelegateFactoryDelegate}.
 */
public class DelegateFactoryDelegateImpl implements DelegateFactoryDelegate {

    @Override
    public DiscordApiBuilderDelegate createDiscordApiBuilderDelegate() {
        return new DiscordApiBuilderDelegateImpl();
    }

    @Override
    public EmbedBuilderDelegate createEmbedBuilderDelegate() {
        return new EmbedBuilderDelegateImpl();
    }

    @Override
    public AllowedMentionsBuilderDelegate createAllowedMentionsBuilderDelegate() {
        return new AllowedMentionsBuilderDelegateImpl();
    }

    @Override
    public MessageBuilderDelegate createMessageBuilderDelegate() {
        return new MessageBuilderDelegateImpl();
    }

    @Override
    public PermissionsBuilderDelegate createPermissionsBuilderDelegate() {
        return new PermissionsBuilderDelegateImpl();
    }

    @Override
    public PermissionsBuilderDelegate createPermissionsBuilderDelegate(Permissions permissions) {
        return new PermissionsBuilderDelegateImpl(permissions);
    }

    @Override
    public ChannelCategoryBuilderDelegate createChannelCategoryBuilderDelegate(Server server) {
        return new ChannelCategoryBuilderDelegateImpl((ServerImpl) server);
    }

    @Override
    public ServerTextChannelBuilderDelegate createServerTextChannelBuilderDelegate(Server server) {
        return new ServerTextChannelBuilderDelegateImpl((ServerImpl) server);
    }

    @Override
    public ServerVoiceChannelBuilderDelegate createServerVoiceChannelBuilderDelegate(Server server) {
        return new ServerVoiceChannelBuilderDelegateImpl((ServerImpl) server);
    }

    @Override
    public CustomEmojiBuilderDelegate createCustomEmojiBuilderDelegate(Server server) {
        return new CustomEmojiBuilderDelegateImpl((ServerImpl) server);
    }

    @Override
    public WebhookBuilderDelegate createWebhookBuilderDelegate(ServerTextChannel channel) {
        return new WebhookBuilderDelegateImpl(channel);
    }

    @Override
    public ServerBuilderDelegate createServerBuilderDelegate(DiscordApi api) {
        return new ServerBuilderDelegateImpl((DiscordApiImpl) api);
    }

    @Override
    public RoleBuilderDelegate createRoleBuilderDelegate(Server server) {
        return new RoleBuilderDelegateImpl((ServerImpl) server);
    }

    @Override
    public InviteBuilderDelegate createInviteBuilderDelegate(ServerChannel channel) {
        return new InviteBuilderDelegateImpl(channel);
    }

    @Override
    public AccountUpdaterDelegate createAccountUpdaterDelegate(DiscordApi api) {
        return new AccountUpdaterDelegateImpl(((DiscordApiImpl) api));
    }

    @Override
    public ServerChannelUpdaterDelegate createServerChannelUpdaterDelegate(ServerChannel channel) {
        return new ServerChannelUpdaterDelegateImpl(channel);
    }

    @Override
    public ServerTextChannelUpdaterDelegate createServerTextChannelUpdaterDelegate(ServerTextChannel channel) {
        return new ServerTextChannelUpdaterDelegateImpl(channel);
    }

    @Override
    public ServerVoiceChannelUpdaterDelegate createServerVoiceChannelUpdaterDelegate(ServerVoiceChannel channel) {
        return new ServerVoiceChannelUpdaterDelegateImpl(channel);
    }

    @Override
    public CustomEmojiUpdaterDelegate createCustomEmojiUpdaterDelegate(KnownCustomEmoji emoji) {
        return new CustomEmojiUpdaterDelegateImpl(emoji);
    }

    @Override
    public RoleUpdaterDelegate createRoleUpdaterDelegate(Role role) {
        return new RoleUpdaterDelegateImpl(role);
    }

    @Override
    public ServerUpdaterDelegate createServerUpdaterDelegate(Server server) {
        return new ServerUpdaterDelegateImpl(server);
    }

    @Override
    public WebhookUpdaterDelegate createWebhookUpdaterDelegate(Webhook webhook) {
        return new WebhookUpdaterDelegateImpl(webhook);
    }

    @Override
    public AudioSourceBaseDelegate createAudioSourceBaseDelegate(DiscordApi api) {
        return new AudioSourceBaseDelegateImpl(api);
    }

    @Override
    public ExceptionLoggerDelegate createExceptionLoggerDelegate() {
        return new ExceptionLoggerDelegateImpl();
    }

    @Override
    public DiscordExceptionValidator createDiscordExceptionValidator() {
        return new DiscordExceptionValidatorImpl();
    }

}
