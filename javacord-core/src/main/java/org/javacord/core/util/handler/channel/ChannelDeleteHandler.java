package org.javacord.core.util.handler.channel;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.logging.log4j.Logger;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.Channel;
import org.javacord.api.entity.channel.ChannelCategory;
import org.javacord.api.entity.channel.ChannelType;
import org.javacord.api.entity.channel.GroupChannel;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.channel.VoiceChannel;
import org.javacord.api.event.channel.group.GroupChannelDeleteEvent;
import org.javacord.api.event.channel.server.ServerChannelDeleteEvent;
import org.javacord.core.event.channel.group.GroupChannelDeleteEventImpl;
import org.javacord.core.event.channel.server.ServerChannelDeleteEventImpl;
import org.javacord.core.util.event.DispatchQueueSelector;
import org.javacord.core.util.gateway.PacketHandler;
import org.javacord.core.util.logging.LoggerUtil;

import java.util.Collections;

/**
 * Handles the channel delete packet.
 */
public class ChannelDeleteHandler extends PacketHandler {

    private static final Logger logger = LoggerUtil.getLogger(ChannelDeleteHandler.class);

    /**
     * Creates a new instance of this class.
     *
     * @param api The api.
     */
    public ChannelDeleteHandler(DiscordApi api) {
        super(api, true, "CHANNEL_DELETE");
    }

    @Override
    public void handle(JsonNode packet) {
        ChannelType type = ChannelType.fromId(packet.get("type").asInt());
        switch (type) {
            case SERVER_TEXT_CHANNEL:
            case SERVER_NEWS_CHANNEL:
                handleServerTextChannel(packet);
                break;
            case PRIVATE_CHANNEL:
                handlePrivateChannel(packet);
                break;
            case SERVER_VOICE_CHANNEL:
                handleServerVoiceChannel(packet);
                break;
            case GROUP_CHANNEL:
                handleGroupChannel(packet);
                break;
            case CHANNEL_CATEGORY:
                handleCategory(packet);
                break;
            case SERVER_STORE_CHANNEL:
                // TODO Handle store channels
                logger.debug("Received CHANNEL_DELETE packet for a store channel. These are not supported in this"
                        + " Javacord version and get ignored!");
                break;
            default:
                logger.warn("Unknown or unexpected channel type. Your Javacord version might be out of date!");
        }
        api.removeChannelFromCache(packet.get("id").asLong());
    }

    /**
     * Handles server text channel deletion.
     *
     * @param channelJson The channel data.
     */
    private void handleCategory(JsonNode channelJson) {
        long serverId = channelJson.get("guild_id").asLong();
        long channelId = channelJson.get("id").asLong();
        api.getPossiblyUnreadyServerById(serverId)
                .flatMap(server -> server.getChannelCategoryById(channelId))
                .ifPresent(this::dispatchServerChannelDeleteEvent);
        api.removeObjectListeners(ChannelCategory.class, channelId);
        api.removeObjectListeners(ServerChannel.class, channelId);
        api.removeObjectListeners(Channel.class, channelId);
    }

    /**
     * Handles server text channel deletion.
     *
     * @param channelJson The channel data.
     */
    private void handleServerTextChannel(JsonNode channelJson) {
        long serverId = channelJson.get("guild_id").asLong();
        long channelId = channelJson.get("id").asLong();
        api.getPossiblyUnreadyServerById(serverId)
                .flatMap(server -> server.getTextChannelById(channelId))
                .ifPresent(channel -> {
                    dispatchServerChannelDeleteEvent(channel);
                    api.forEachCachedMessageWhere(
                            msg -> msg.getChannel().getId() == channelId,
                            msg -> api.removeMessageFromCache(msg.getId())
                    );
                });
        api.removeObjectListeners(ServerTextChannel.class, channelId);
        api.removeObjectListeners(ServerChannel.class, channelId);
        api.removeObjectListeners(TextChannel.class, channelId);
        api.removeObjectListeners(Channel.class, channelId);
    }

    /**
     * Handles server voice channel deletion.
     *
     * @param channelJson The channel data.
     */
    private void handleServerVoiceChannel(JsonNode channelJson) {
        long serverId = channelJson.get("guild_id").asLong();
        long channelId = channelJson.get("id").asLong();
        api.getPossiblyUnreadyServerById(serverId)
                .flatMap(server -> server.getVoiceChannelById(channelId))
                .ifPresent(this::dispatchServerChannelDeleteEvent);
        api.removeObjectListeners(ServerVoiceChannel.class, channelId);
        api.removeObjectListeners(ServerChannel.class, channelId);
        api.removeObjectListeners(VoiceChannel.class, channelId);
        api.removeObjectListeners(Channel.class, channelId);
    }

    /**
     * Handles a private channel deletion.
     *
     * @param channel The channel data.
     */
    private void handlePrivateChannel(JsonNode channel) {
        // TODO Do we even have to handle private channel deletes?
        // UserImpl recipient = new UserImpl(api, channel.get("recipients").get(0));
        // recipient.getPrivateChannel().ifPresent(privateChannel -> {
        //     PrivateChannelDeleteEvent event = new PrivateChannelDeleteEventImpl(privateChannel);
        //
        //     api.getEventDispatcher().dispatchPrivateChannelDeleteEvent(api, privateChannel, recipient, event);
        //     long channelId = privateChannel.getId();
        //     api.removeObjectListeners(PrivateChannel.class, channelId);
        //     api.removeObjectListeners(VoiceChannel.class, channelId);
        //     api.removeObjectListeners(TextChannel.class, channelId);
        //     api.removeObjectListeners(Channel.class, channelId);
        //     api.forEachCachedMessageWhere(
        //             msg -> msg.getChannel().getId() == privateChannel.getId(),
        //             msg -> api.removeMessageFromCache(msg.getId())
        //     );
        //     recipient.setChannel(null);
        // });
    }

    /**
     * Handles a group channel deletion.
     *
     * @param channel The channel data.
     */
    private void handleGroupChannel(JsonNode channel) {
        long channelId = channel.get("id").asLong();

        api.getGroupChannelById(channelId).ifPresent(groupChannel -> {
            GroupChannelDeleteEvent event = new GroupChannelDeleteEventImpl(groupChannel);

            api.getEventDispatcher().dispatchGroupChannelDeleteEvent(
                    api, Collections.singleton(groupChannel), groupChannel.getMembers(), event);
            api.removeObjectListeners(GroupChannel.class, channelId);
            api.removeObjectListeners(VoiceChannel.class, channelId);
            api.removeObjectListeners(TextChannel.class, channelId);
            api.removeObjectListeners(Channel.class, channelId);
            api.forEachCachedMessageWhere(
                    msg -> msg.getChannel().getId() == groupChannel.getId(),
                    msg -> api.removeMessageFromCache(msg.getId())
            );
        });
    }

    /**
     * Dispatches a server channel delete event.
     *
     * @param channel The channel of the event.
     */
    private void dispatchServerChannelDeleteEvent(ServerChannel channel) {
        ServerChannelDeleteEvent event = new ServerChannelDeleteEventImpl(channel);

        api.getEventDispatcher().dispatchServerChannelDeleteEvent(
                (DispatchQueueSelector) channel.getServer(), channel.getServer(), channel, event);
    }

}
