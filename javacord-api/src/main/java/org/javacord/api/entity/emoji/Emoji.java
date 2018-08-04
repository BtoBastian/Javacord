package org.javacord.api.entity.emoji;

import org.javacord.api.entity.Mentionable;

import java.util.Optional;

/**
 * This class represents an emoji which can be a custom emoji (known or unknown) or a unicode emoji.
 */
public interface Emoji extends Mentionable {

    /**
     * Gets the emoji as unicode emoji.
     *
     * @return The emoji as unicode emoji.
     */
    Optional<String> asUnicodeEmoji();

    /**
     * Gets the emoji as custom emoji.
     *
     * @return The emoji as custom emoji.
     */
    Optional<CustomEmoji> asCustomEmoji();

    /**
     * Gets the emoji as known custom emoji.
     *
     * @return The emoji as known custom emoji.
     */
    Optional<KnownCustomEmoji> asKnownCustomEmoji();

    /**
     * Checks if the emoji is equal to the given emoji.
     * This can be used to safe some ugly optional checks.
     *
     * @param otherEmoji The emoji to compare with.
     * @return Whether the emoji is equal to the given emoji.
     */
    default boolean equalsUnicodeEmoji(Emoji otherEmoji) {
        if (otherEmoji.isUnicodeEmoji()) {
            return equalsUnicodeEmoji(otherEmoji.asUnicodeEmoji().orElse(""));
        }
        if (isUnicodeEmoji()) {
            // This is an unicode emoji and the other emoji is a custom emoji
            return false;
        }
        // Both are custom emojis, so we have to compare the id
        long thisId = asCustomEmoji().map(CustomEmoji::getId).orElseThrow(AssertionError::new);
        long otherId = asCustomEmoji().map(CustomEmoji::getId).orElseThrow(AssertionError::new);
        return thisId == otherId;
    }

    /**
     * Checks if the emoji is equal to the given unicode emoji.
     * This can be used to safe some ugly optional checks.
     *
     * @param otherUnicodeEmoji The unicode emoji to compare with.
     * @return Whether the emoji is equal to the given unicode emoji.
     */
    default boolean equalsUnicodeEmoji(String otherUnicodeEmoji) {
        return asUnicodeEmoji()
                .map(emoji -> emoji.equals(otherUnicodeEmoji))
                .orElse(false);
    }

    /**
     * Checks if the emoji is equal to the given emoji string.
     * This is useful when you only have a string that contains an emoji,
     * which may be a custom emoji, but may as well be an unicode emoji.
     * This can be used to safe some ugly optional checks.
     *
     * @param otherEmoji The emoji as its String representation.
     * @return Whether the emojis are equal or not.
     */
    default boolean equalsEmoji(String otherEmoji) {
        if (asCustomEmoji().isPresent()) {
            return asCustomEmoji().get()
                    .getMentionTag()
                    .equals(otherEmoji);
        } else {
            return equalsUnicodeEmoji(otherEmoji);
        }
    }

    /**
     * Checks if the emoji is animated.
     * Always returns <code>false</code> for unicode emojis.
     *
     * @return Whether the emoji is animated or not.
     */
    boolean isAnimated();

    /**
     * Checks if the emoji is a unicode.
     *
     * @return Whether the emoji is a unicode emoji or not.
     */
    default boolean isUnicodeEmoji() {
        return asUnicodeEmoji().isPresent();
    }

    /**
     * Checks if the emoji is a custom emoji.
     *
     * @return Whether the emoji is a custom emoji or not.
     */
    default boolean isCustomEmoji() {
        return asCustomEmoji().isPresent();
    }

    /**
     * Checks if the emoji is a known custom emoji.
     *
     * @return Whether the emoji is a known custom emoji or not.
     */
    default boolean isKnownCustomEmoji() {
        return asKnownCustomEmoji().isPresent();
    }

}
