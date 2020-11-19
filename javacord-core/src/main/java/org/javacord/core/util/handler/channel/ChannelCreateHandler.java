package org.javacord.core.util.handler.channel;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.logging.log4j.Logger;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.ChannelCategory;
import org.javacord.api.entity.channel.ChannelType;
import org.javacord.api.entity.channel.PrivateChannel;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.event.channel.server.ServerChannelCreateEvent;
import org.javacord.api.event.channel.user.PrivateChannelCreateEvent;
import org.javacord.core.entity.channel.PrivateChannelImpl;
import org.javacord.core.entity.server.ServerImpl;
import org.javacord.core.entity.user.MemberImpl;
import org.javacord.core.entity.user.UserImpl;
import org.javacord.core.event.channel.server.ServerChannelCreateEventImpl;
import org.javacord.core.event.channel.user.PrivateChannelCreateEventImpl;
import org.javacord.core.util.event.DispatchQueueSelector;
import org.javacord.core.util.gateway.PacketHandler;
import org.javacord.core.util.logging.LoggerUtil;

/**
 * Handles the channel create packet.
 */
public class ChannelCreateHandler extends PacketHandler {

    private static final Logger logger = LoggerUtil.getLogger(ChannelCreateHandler.class);

    /**
     * Creates a new instance of this class.
     *
     * @param api The api.
     */
    public ChannelCreateHandler(DiscordApi api) {
        super(api, true, "CHANNEL_CREATE");
    }

    @Override
    public void handle(JsonNode packet) {
        ChannelType type = ChannelType.fromId(packet.get("type").asInt());
        switch (type) {
            case SERVER_TEXT_CHANNEL:
                handleServerTextChannel(packet);
                break;
            case PRIVATE_CHANNEL:
                handlePrivateChannel(packet);
                break;
            case SERVER_VOICE_CHANNEL:
                handleServerVoiceChannel(packet);
                break;
            case GROUP_CHANNEL:
                logger.info("Received an update for a group channel. This should be impossible.");
                break;
            case CHANNEL_CATEGORY:
                handleChannelCategory(packet);
                break;
            case SERVER_NEWS_CHANNEL:
                logger.debug("Received CHANNEL_CREATE packet for a news channel. In this Javacord version it is "
                        + "treated as a normal text channel!");
                handleServerTextChannel(packet);
                break;
            case SERVER_STORE_CHANNEL:
                // TODO Handle store channels
                logger.debug("Received CHANNEL_CREATE packet for a store channel. These are not supported in this"
                        + " Javacord version and get ignored!");
                break;
            default:
                logger.warn("Unknown or unexpected channel type. Your Javacord version might be out of date!");
        }
    }

    /**
     * Handles channel category creation.
     *
     * @param channel The channel data.
     */
    private void handleChannelCategory(JsonNode channel) {
        long serverId = channel.get("guild_id").asLong();
        api.getPossiblyUnreadyServerById(serverId).ifPresent(server -> {
            ChannelCategory channelCategory = ((ServerImpl) server).getOrCreateChannelCategory(channel);
            ServerChannelCreateEvent event = new ServerChannelCreateEventImpl(channelCategory);
            api.getEventDispatcher().dispatchServerChannelCreateEvent((DispatchQueueSelector) server, server, event);
        });
    }

    /**
     * Handles server text channel creation.
     *
     * @param channel The channel data.
     */
    private void handleServerTextChannel(JsonNode channel) {
        long serverId = channel.get("guild_id").asLong();
        api.getPossiblyUnreadyServerById(serverId).ifPresent(server -> {
            ServerTextChannel textChannel = ((ServerImpl) server).getOrCreateServerTextChannel(channel);
            ServerChannelCreateEvent event = new ServerChannelCreateEventImpl(textChannel);

            api.getEventDispatcher().dispatchServerChannelCreateEvent((DispatchQueueSelector) server, server, event);
        });
    }

    /**
     * Handles server voice channel creation.
     *
     * @param channel The channel data.
     */
    private void handleServerVoiceChannel(JsonNode channel) {
        long serverId = channel.get("guild_id").asLong();
        api.getPossiblyUnreadyServerById(serverId).ifPresent(server -> {
            ServerVoiceChannel voiceChannel = ((ServerImpl) server).getOrCreateServerVoiceChannel(channel);
            ServerChannelCreateEvent event = new ServerChannelCreateEventImpl(voiceChannel);

            api.getEventDispatcher().dispatchServerChannelCreateEvent((DispatchQueueSelector) server, server, event);
        });
    }

    /**
     * Handles a private channel creation.
     *
     * @param channel The channel data.
     */
    private void handlePrivateChannel(JsonNode channel) {
        // A CHANNEL_CREATE packet is sent every time a bot account receives a message, see
        // https://github.com/hammerandchisel/discord-api-docs/issues/184

        UserImpl recipient = new UserImpl(api, channel.get("recipients").get(0), (MemberImpl) null, null);
        if (!recipient.getPrivateChannel().isPresent()) {
            PrivateChannel privateChannel = new PrivateChannelImpl(api, channel);
            PrivateChannelCreateEvent event = new PrivateChannelCreateEventImpl(privateChannel);

            api.getEventDispatcher().dispatchPrivateChannelCreateEvent(api, recipient, event);
        }
    }

}
