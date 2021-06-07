package org.javacord.core.util.handler.guild;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.logging.log4j.Logger;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.GroupChannel;
import org.javacord.api.entity.channel.PrivateChannel;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.channel.VoiceChannel;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.channel.server.voice.ServerVoiceChannelMemberJoinEvent;
import org.javacord.api.event.channel.server.voice.ServerVoiceChannelMemberLeaveEvent;
import org.javacord.api.event.server.VoiceStateUpdateEvent;
import org.javacord.core.audio.AudioConnectionImpl;
import org.javacord.core.entity.channel.GroupChannelImpl;
import org.javacord.core.entity.channel.PrivateChannelImpl;
import org.javacord.core.entity.channel.ServerVoiceChannelImpl;
import org.javacord.core.entity.server.ServerImpl;
import org.javacord.core.entity.user.Member;
import org.javacord.core.entity.user.MemberImpl;
import org.javacord.core.event.channel.server.voice.ServerVoiceChannelMemberJoinEventImpl;
import org.javacord.core.event.channel.server.voice.ServerVoiceChannelMemberLeaveEventImpl;
import org.javacord.core.event.server.VoiceStateUpdateEventImpl;
import org.javacord.core.event.user.UserChangeDeafenedEventImpl;
import org.javacord.core.event.user.UserChangeMutedEventImpl;
import org.javacord.core.event.user.UserChangeSelfDeafenedEventImpl;
import org.javacord.core.event.user.UserChangeSelfMutedEventImpl;
import org.javacord.core.util.event.DispatchQueueSelector;
import org.javacord.core.util.gateway.PacketHandler;
import org.javacord.core.util.logging.LoggerUtil;

import java.util.Optional;

/**
 * Handles the voice state update packet.
 */
public class VoiceStateUpdateHandler extends PacketHandler {

    private static final Logger logger = LoggerUtil.getLogger(VoiceStateUpdateHandler.class);

    /**
     * Creates a new instance of this class.
     *
     * @param api The api.
     */
    public VoiceStateUpdateHandler(DiscordApi api) {
        super(api, true, "VOICE_STATE_UPDATE");
    }

    @Override
    public void handle(JsonNode packet) {
        if (!packet.hasNonNull("user_id")) {
            return;
        }

        long userId = packet.get("user_id").asLong();
        if (packet.hasNonNull("guild_id")) {
            handleServerVoiceChannel(packet, userId);
        } else if (packet.hasNonNull("channel_id")) {
            long channelId = packet.get("channel_id").asLong();
            Optional<VoiceChannel> optionalChannel = api.getVoiceChannelById(channelId);
            if (optionalChannel.isPresent()) {
                VoiceChannel voiceChannel = optionalChannel.get();
                if (voiceChannel instanceof PrivateChannel) {
                    handlePrivateChannel(userId, ((PrivateChannelImpl) voiceChannel));
                } else if (voiceChannel instanceof GroupChannel) {
                    handleGroupChannel(userId, ((GroupChannelImpl) voiceChannel));
                }
            } else {
                LoggerUtil.logMissingChannel(logger, channelId);
            }
        }

        if (api.getYourself().getId() == userId) {
            handleSelf(packet);
        }
    }

    private void handleSelf(JsonNode packet) {
        // We need the session id to connect to an audio websocket
        String sessionId = packet.get("session_id").asText();
        long channelId = packet.get("channel_id").asLong();
        Optional<ServerVoiceChannel> optionalChannel = api.getServerVoiceChannelById(channelId);
        if (optionalChannel.isPresent()) {
            ServerVoiceChannel channel = optionalChannel.get();
            dispatchVoiceStateUpdateEvent(
                    channel, channel.getServer(), packet.get("session_id").asText());

            AudioConnectionImpl pendingAudioConnection =
                    api.getPendingAudioConnectionByServerId(channel.getServer().getId());
            if (pendingAudioConnection != null) {
                pendingAudioConnection.setSessionId(sessionId);
                pendingAudioConnection.tryConnect();
            }

            channel.getServer().getAudioConnection().ifPresent(connection -> {
                ((AudioConnectionImpl) connection).setSessionId(sessionId);
                ((AudioConnectionImpl) connection).tryConnect();
            });
        } else {
            LoggerUtil.logMissingChannel(logger, channelId);
        }
    }

    private void handleServerVoiceChannel(JsonNode packet, long userId) {
        api.getPossiblyUnreadyServerById(packet.get("guild_id").asLong())
                .map(ServerImpl.class::cast).ifPresent(server -> {

                    // We grab the old states prior to replacing the Member in the cache since we pull
                    // the old states from the cache.
                    boolean newSelfMuted = packet.get("self_mute").asBoolean();
                    boolean oldSelfMuted = server.isSelfMuted(userId);
                    boolean newSelfDeafened = packet.get("self_deaf").asBoolean();
                    boolean oldSelfDeafened = server.isSelfDeafened(userId);
                    boolean newMuted = packet.get("mute").asBoolean();
                    boolean oldMuted = server.isMuted(userId);
                    boolean newDeafened = packet.get("deaf").asBoolean();
                    boolean oldDeafened = server.isDeafened(userId);

                    // Check the cache first to see if we can update an existing Member object.
                    MemberImpl oldMember = (MemberImpl) server.getRealMemberById(userId).orElse(null);
                    if (oldMember == null && !packet.hasNonNull("member")) {
                        // If there is no member in the cache and we don't receive a member field,
                        // we will log it and return. I'm actually not sure if this is normal behavior;
                        // Maybe the logging should be removed if it is, but the data being lost (non-cacheable)
                        // is still notable.
                        logger.warn("Received VOICE_STATE_UPDATE packet without "
                                + "member field or member in the cache: {}", packet);
                        return;
                    }

                    // For the update, prioritizes using the Member in the cache;
                    // If not present, we use the member in the packet.
                    MemberImpl newMember = ((oldMember != null)
                            ? oldMember
                            : new MemberImpl(api, server, packet.get("member"), null))
                                .setMuted(newMuted)
                                .setDeafened(newDeafened)
                                .setSelfMuted(newSelfMuted)
                                .setSelfDeafened(newSelfDeafened);

                    // Update the cache with the new member prior to dispatching any events.
                    api.addMemberToCacheOrReplaceExisting(newMember);

                    Optional<ServerVoiceChannelImpl> oldChannel = server
                            .getConnectedVoiceChannel(userId)
                            .map(ServerVoiceChannelImpl.class::cast);

                    Optional<ServerVoiceChannelImpl> newChannel;
                    if (packet.hasNonNull("channel_id")) {
                        newChannel = server
                                .getVoiceChannelById(packet.get("channel_id").asLong())
                                .map(ServerVoiceChannelImpl.class::cast);
                    } else {
                        newChannel = Optional.empty();
                    }

                    if (!newChannel.equals(oldChannel)) {
                        oldChannel.ifPresent(channel -> {
                            channel.removeConnectedUser(userId);
                            dispatchServerVoiceChannelMemberLeaveEvent(
                                    newMember, newChannel.orElse(null), channel, server);
                        });

                        newChannel.ifPresent(channel -> {
                            channel.addConnectedUser(userId);
                            dispatchServerVoiceChannelMemberJoinEvent(newMember, channel, oldChannel.orElse(null),
                                    server);
                        });
                    }

                    if (newSelfMuted != oldSelfMuted) {
                        UserChangeSelfMutedEventImpl event = new UserChangeSelfMutedEventImpl(newMember, oldMember);
                        api.getEventDispatcher()
                                .dispatchUserChangeSelfMutedEvent(server, server, newMember.getUser(), event);
                    }

                    if (newSelfDeafened != oldSelfDeafened) {
                        UserChangeSelfDeafenedEventImpl event = new UserChangeSelfDeafenedEventImpl(
                                newMember, oldMember);
                        api.getEventDispatcher()
                                .dispatchUserChangeSelfDeafenedEvent(server, server, newMember.getUser(), event);
                    }

                    if (newMuted != oldMuted) {
                        UserChangeMutedEventImpl event = new UserChangeMutedEventImpl(newMember, oldMember);
                        api.getEventDispatcher()
                                .dispatchUserChangeMutedEvent(server, server, newMember.getUser(), event);
                    }

                    if (newDeafened != oldDeafened) {
                        UserChangeDeafenedEventImpl event = new UserChangeDeafenedEventImpl(newMember, oldMember);
                        api.getEventDispatcher()
                                .dispatchUserChangeDeafenedEvent(server, server, newMember.getUser(), event);
                    }
                });
    }

    private void handlePrivateChannel(long userId, PrivateChannelImpl channel) {
        //channel.addConnectedUser(user);
        //dispatchVoiceChannelMemberJoinEvent(user, channel);
    }

    private void handleGroupChannel(long userId, GroupChannelImpl channel) {
        //channel.addConnectedUser(user);
        //dispatchVoiceChannelMemberJoinEvent(user, channel);
    }

    private void dispatchVoiceStateUpdateEvent(ServerVoiceChannel newChannel, Server server, String sessionId) {
        VoiceStateUpdateEvent event = new VoiceStateUpdateEventImpl(newChannel, sessionId);

        api.getEventDispatcher().dispatchVoiceStateUpdateEvent((DispatchQueueSelector) server, newChannel, event);
    }

    private void dispatchServerVoiceChannelMemberJoinEvent(
            Member member, ServerVoiceChannel newChannel, ServerVoiceChannel oldChannel, Server server) {
        ServerVoiceChannelMemberJoinEvent event = new ServerVoiceChannelMemberJoinEventImpl(
                member, newChannel, oldChannel);

        api.getEventDispatcher().dispatchServerVoiceChannelMemberJoinEvent(
                (DispatchQueueSelector) server, server, newChannel, member.getUser(), event);
    }

    private void dispatchServerVoiceChannelMemberLeaveEvent(
            Member member, ServerVoiceChannel newChannel, ServerVoiceChannel oldChannel, Server server) {
        ServerVoiceChannelMemberLeaveEvent event = new ServerVoiceChannelMemberLeaveEventImpl(
                member, newChannel, oldChannel);

        api.getEventDispatcher().dispatchServerVoiceChannelMemberLeaveEvent(
                (DispatchQueueSelector) server, server, oldChannel, member.getUser(), event);
    }

}
