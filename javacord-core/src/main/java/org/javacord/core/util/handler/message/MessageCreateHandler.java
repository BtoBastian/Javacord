package org.javacord.core.util.handler.message;

import com.fasterxml.jackson.databind.JsonNode;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.core.event.message.MessageCreateEventImpl;
import org.javacord.core.util.event.DispatchQueueSelector;
import org.javacord.core.util.gateway.PacketHandler;

import java.util.Optional;

/**
 * Handles the message create packet.
 */
public class MessageCreateHandler extends PacketHandler {

    /**
     * Creates a new instance of this class.
     *
     * @param api The api.
     */
    public MessageCreateHandler(DiscordApi api) {
        super(api, true, "MESSAGE_CREATE");
    }

    @Override
    public void handle(JsonNode packet) {
        api.getTextChannelById(packet.get("channel_id").asText()).ifPresent(channel -> {
            Message message = api.getOrCreateMessage(channel, packet);
            MessageCreateEvent event = new MessageCreateEventImpl(message);

            Optional<Server> optionalServer = channel.asServerChannel().map(ServerChannel::getServer);
            api.getEventDispatcher().dispatchMessageCreateEvent(
                    optionalServer.map(DispatchQueueSelector.class::cast).orElse(api),
                    message.getAuthor(), //MessageAuthor forwards the event to the Webhook or the User.
                    optionalServer.orElse(null),
                    channel,
                    null,
                    null,
                    event);
        });
    }

}
