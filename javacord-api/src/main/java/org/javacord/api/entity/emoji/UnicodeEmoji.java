package org.javacord.api.entity.emoji;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The representation of a unicode {@link Emoji}.
 */
public class UnicodeEmoji implements Emoji {

    /**
     * A static map which contains all unicode emojis.
     */
    // Can be static, because unicode emojis are really unique, even for different discord accounts/shards.
    private static final ConcurrentHashMap<String, UnicodeEmoji> unicodeEmojis = new ConcurrentHashMap<>();

    /**
     * The emoji string.
     */
    private final String emoji;

    /**
     * Creates a new unicode emoji.
     *
     * @param emoji The emoji string.
     */
    private UnicodeEmoji(String emoji) {
        this.emoji = emoji;
    }

    /**
     * Gets a unicode emoji by its string representation.
     *
     * @param emoji The emoji string.
     * @return The object, representing the emoji from the given string.
     */
    public static UnicodeEmoji fromString(String emoji) {
        return unicodeEmojis.computeIfAbsent(emoji, key -> new UnicodeEmoji(emoji));
    }

    @Override
    public String getMentionTag() {
        return emoji;
    }

    @Override
    public Optional<String> asUnicodeEmoji() {
        return Optional.of(emoji);
    }

    @Override
    public Optional<CustomEmoji> asCustomEmoji() {
        return Optional.empty();
    }

    @Override
    public Optional<KnownCustomEmoji> asKnownCustomEmoji() {
        return Optional.empty();
    }

    @Override
    public boolean isAnimated() {
        return false;
    }

    @Override
    public String toString() {
        return String.format("UnicodeEmoji (emoji: %s)", emoji);
    }

    @Override
    public boolean equals(Object obj) {
        return  (this == obj)
                || ((obj instanceof Emoji) && equalsEmoji((Emoji) obj));
    }

    @Override
    public int hashCode() {
        return emoji.hashCode();
    }
}
