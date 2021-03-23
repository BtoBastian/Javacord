package org.javacord.core.entity.channel;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.javacord.api.entity.channel.ChannelType;
import org.javacord.api.entity.channel.ServerNewsChannel;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.core.DiscordApiImpl;
import org.javacord.core.entity.server.ServerImpl;
import org.javacord.core.util.rest.RestEndpoint;
import org.javacord.core.util.rest.RestMethod;
import org.javacord.core.util.rest.RestRequest;

import java.util.concurrent.CompletableFuture;

public class ServerNewsChannelImpl extends ServerTextChannelImpl implements ServerNewsChannel {

    /**
     * Creates a new server text channel object.
     *
     * @param api    The discord api instance.
     * @param server The server of the channel.
     * @param data   The json data of the channel.
     */
    public ServerNewsChannelImpl(DiscordApiImpl api, ServerImpl server, JsonNode data) {
        super(api, server, data);
    }

    @Override
    public ChannelType getType() {
        return ChannelType.SERVER_NEWS_CHANNEL;
    }

    @Override
    public CompletableFuture<Long> followWith(ServerTextChannel targetChannel) {
        ObjectNode body = JsonNodeFactory.instance.objectNode();
        body.put("webhook_channel_id", targetChannel.getIdAsString());
        return new RestRequest<Long>(getApi(), RestMethod.POST, RestEndpoint.FOLLOW_NEWS_CHANNEL)
                .setUrlParameters(getIdAsString())
                .setBody(body)
                .execute(result -> result.getJsonBody().get("webhook_id").asLong());
    }
}
