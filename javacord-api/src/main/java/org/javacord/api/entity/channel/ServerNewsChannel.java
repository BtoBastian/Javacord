package org.javacord.api.entity.channel;

import java.util.concurrent.CompletableFuture;

public interface ServerNewsChannel extends ServerTextChannel {

    /**
     * Follows this news channel to send the messages to a target channel.
     *
     * @param targetChannel The target channel to send the messages to.
     * @return The id of the created webhook.
     */
    CompletableFuture<Long> followWith(ServerTextChannel targetChannel);
}
