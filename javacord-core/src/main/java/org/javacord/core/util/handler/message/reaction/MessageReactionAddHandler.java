package org.javacord.core.util.handler.message.reaction;

import com.fasterxml.jackson.databind.JsonNode;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.emoji.Emoji;
import org.javacord.api.entity.emoji.UnicodeEmoji;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.message.reaction.ReactionAddEvent;
import org.javacord.core.entity.message.MessageImpl;
import org.javacord.core.entity.server.ServerImpl;
import org.javacord.core.entity.user.Member;
import org.javacord.core.entity.user.MemberImpl;
import org.javacord.core.event.message.reaction.ReactionAddEventImpl;
import org.javacord.core.util.event.DispatchQueueSelector;
import org.javacord.core.util.gateway.PacketHandler;

import java.util.Optional;

/**
 * Handles the message reaction add packet.
 */
public class MessageReactionAddHandler extends PacketHandler {

    /**
     * Creates a new instance of this class.
     *
     * @param api The api.
     */
    public MessageReactionAddHandler(DiscordApi api) {
        super(api, true, "MESSAGE_REACTION_ADD");
    }

    @Override
    public void handle(JsonNode packet) {
        api.getTextChannelById(packet.get("channel_id").asText()).ifPresent(channel -> {
            long messageId = packet.get("message_id").asLong();
            long userId = packet.get("user_id").asLong();
            Server server = channel.asServerChannel().map(ServerChannel::getServer).orElse(null);
            Member member = null;
            if (packet.hasNonNull("member")) {
                member = new MemberImpl(api, (ServerImpl) server, packet.get("member"), null);
            }
            Optional<Message> message = api.getCachedMessageById(messageId);

            Emoji emoji;
            JsonNode emojiJson = packet.get("emoji");
            if (!emojiJson.has("id") || emojiJson.get("id").isNull()) {
                emoji = UnicodeEmoji.fromString(emojiJson.get("name").asText());
            } else {
                emoji = api.getKnownCustomEmojiOrCreateCustomEmoji(emojiJson);
            }

            message.ifPresent(msg -> ((MessageImpl) msg).addReaction(emoji, userId == api.getYourself().getId()));

            ReactionAddEvent event = new ReactionAddEventImpl(api, messageId, channel, emoji, userId, member);

            Optional<Server> optionalServer = channel.asServerChannel().map(ServerChannel::getServer);
            api.getEventDispatcher().dispatchReactionAddEvent(
                    optionalServer.map(DispatchQueueSelector.class::cast).orElse(api),
                    messageId,
                    optionalServer.orElse(null),
                    channel,
                    userId,
                    event);
        });
    }

}
