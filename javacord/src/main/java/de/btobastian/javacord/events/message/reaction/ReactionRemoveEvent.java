package de.btobastian.javacord.events.message.reaction;

import de.btobastian.javacord.DiscordApi;
import de.btobastian.javacord.entities.User;
import de.btobastian.javacord.entities.channels.TextChannel;
import de.btobastian.javacord.entities.message.emoji.Emoji;

/**
 * A reaction add event.
 */
public class ReactionRemoveEvent extends SingleReactionEvent {

    /**
     * The user of the event.
     */
    private final User user;

    /**
     * Creates a new reaction remove event.
     *
     * @param api The discord api instance.
     * @param messageId The id of the message.
     * @param channel The text channel in which the message was sent.
     * @param emoji The emoji.
     * @param user The user who removed the reaction.
     */
    public ReactionRemoveEvent(DiscordApi api, long messageId, TextChannel channel, Emoji emoji, User user) {
        super(api, messageId, channel, emoji);
        this.user = user;
    }

    /**
     * Gets the user who removed the reaction.
     *
     * @return The user who removed the reaction.
     */
    public User getUser() {
        return user;
    }

}
