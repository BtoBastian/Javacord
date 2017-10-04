package de.btobastian.javacord.entities.channels;

import de.btobastian.javacord.entities.DiscordEntity;

import java.util.Optional;

/**
 * The class represents a channel.
 */
public interface Channel extends DiscordEntity {

    /**
     * Gets the type of the channel.
     *
     * @return The type of the channel.
     */
    ChannelType getType();

    /**
     * Gets the channel as group channel.
     *
     * @return The channel as group channel.
     */
    default Optional<GroupChannel> asGroupChannel() {
        if (this instanceof GroupChannel) {
            return Optional.of((GroupChannel) this);
        }
        return Optional.empty();
    }

    /**
     * Gets the channel as private channel.
     *
     * @return The channel as private channel.
     */
    default Optional<PrivateChannel> asPrivateChannel() {
        if (this instanceof PrivateChannel) {
            return Optional.of((PrivateChannel) this);
        }
        return Optional.empty();
    }

    /**
     * Gets the channel as server channel.
     *
     * @return The channel as server channel.
     */
    default Optional<ServerChannel> asServerChannel() {
        if (this instanceof ServerChannel) {
            return Optional.of((ServerChannel) this);
        }
        return Optional.empty();
    }

    /**
     * Gets the channel as channel category.
     *
     * @return The channel as channel category.
     */
    default Optional<ChannelCategory> asChannelCategory() {
        if (this instanceof ChannelCategory) {
            return Optional.of((ChannelCategory) this);
        }
        return Optional.empty();
    }

    /**
     * Gets the channel as server text channel.
     *
     * @return The channel as server text channel.
     */
    default Optional<ServerTextChannel> asServerTextChannel() {
        if (this instanceof ServerTextChannel) {
            return Optional.of((ServerTextChannel) this);
        }
        return Optional.empty();
    }

    /**
     * Gets the channel as server voice channel.
     *
     * @return The channel as server voice channel.
     */
    default Optional<ServerVoiceChannel> asServerVoiceChannel() {
        if (this instanceof ServerVoiceChannel) {
            return Optional.of((ServerVoiceChannel) this);
        }
        return Optional.empty();
    }

    /**
     * Gets the channel as text channel.
     *
     * @return The channel as text channel.
     */
    default Optional<TextChannel> asTextChannel() {
        if (this instanceof GroupChannel) {
            return Optional.of((TextChannel) this);
        }
        return Optional.empty();
    }

    /**
     * Gets the channel as voice channel.
     *
     * @return The channel as voice channel.
     */
    default Optional<VoiceChannel> asVoiceChannel() {
        if (this instanceof VoiceChannel) {
            return Optional.of((VoiceChannel) this);
        }
        return Optional.empty();
    }

}
