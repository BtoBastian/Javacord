package de.btobastian.javacord.events.message;

import de.btobastian.javacord.DiscordApi;
import de.btobastian.javacord.entities.message.Message;

/**
 * A message create event.
 */
public class MessageCreateEvent extends CertainMessageEvent {

    /**
     * Creates a new event instance.
     *
     * @param api The discord api instance.
     * @param message The created message.
     */
    public MessageCreateEvent(DiscordApi api, Message message) {
        super(api, message);
    }

}
