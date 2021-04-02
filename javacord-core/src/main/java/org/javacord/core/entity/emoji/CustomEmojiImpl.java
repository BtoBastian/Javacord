package org.javacord.core.entity.emoji;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.logging.log4j.Logger;
import org.javacord.api.DiscordApi;
import org.javacord.api.Javacord;
import org.javacord.api.entity.Icon;
import org.javacord.api.entity.emoji.CustomEmoji;
import org.javacord.core.DiscordApiImpl;
import org.javacord.core.entity.IconImpl;
import org.javacord.core.util.logging.LoggerUtil;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

/**
 * The implementation of {@link CustomEmoji}.
 */
public class CustomEmojiImpl implements CustomEmoji {

    /**
     * The logger of this class.
     */
    private static final Logger logger = LoggerUtil.getLogger(CustomEmojiImpl.class);

    /**
     * The discord api instance.
     */
    private final DiscordApiImpl api;

    /**
     * The id of the emoji.
     */
    private final long id;

    /**
     * The name of the emoji.
     */
    protected volatile String name;

    /**
     * Whether the emoji is animated or not.
     */
    private final boolean animated;

    /**
     * Creates a new custom emoji.
     *
     * @param api The discord api instance.
     * @param data The json data of the emoji.
     */
    public CustomEmojiImpl(DiscordApiImpl api, JsonNode data) {
        this.api = api;
        id = data.get("id").asLong();
        JsonNode nameNode = data.get("name");
        name = nameNode == null ? "" : nameNode.asText();
        // Animated field may be missing, default to false
        JsonNode animatedNode = data.get("animated");
        animated = animatedNode != null && animatedNode.asBoolean();
    }

    /**
     * Creates a new custom emoji.
     *
     * @param api The discord api instance.
     * @param id The id of the emoji.
     * @param name The name of the emoji.
     * @param animated Whether the emoji is animated or not.
     */
    public CustomEmojiImpl(DiscordApiImpl api, long id, String name, boolean animated) {
        this.api = api;
        this.id = id;
        this.name = name;
        this.animated = animated;
    }

    @Override
    public DiscordApi getApi() {
        return api;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Icon getImage() {
        String urlString = "https://" + Javacord.DISCORD_CDN_DOMAIN
                + "/emojis/" + getIdAsString() + (isAnimated() ? ".gif" : ".png");
        try {
            return new IconImpl(getApi(), new URL(urlString));
        } catch (MalformedURLException e) {
            logger.warn("Seems like the url of the avatar is malformed! Please contact the developer!", e);
            return null;
        }
    }

    @Override
    public boolean isAnimated() {
        return animated;
    }

    @Override
    public boolean equals(Object o) {
        return (this == o)
               || ((o instanceof CustomEmoji) && equalsEmoji((CustomEmoji) o));
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    @Override
    public String toString() {
        return String.format("CustomEmoji (id: %s, name: %s, animated: %b)", getIdAsString(), getName(), isAnimated());
    }

}
