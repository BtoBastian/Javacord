package org.javacord.core.entity.message.embed.sent;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Optional;
import org.javacord.api.entity.message.embed.draft.EmbedDraftAuthor;
import org.javacord.api.entity.message.embed.sent.SentEmbed;
import org.javacord.api.entity.message.embed.sent.SentEmbedAuthor;

/**
 * The implementation of {@link SentEmbedAuthor}.
 */
public class SentEmbedAuthorImpl extends SentEmbedMemberImpl<EmbedDraftAuthor, SentEmbedAuthor>
        implements SentEmbedAuthor {

    private final String name;
    private final String url;
    private final String iconUrl;
    private final String proxyIconUrl;

    /**
     * Creates a new embed author.
     *
     * @param data The json data of the author.
     */
    public SentEmbedAuthorImpl(SentEmbed parent, JsonNode data) {
        super(parent, EmbedDraftAuthor.class, SentEmbedAuthor.class);

        name = data.path("name").asText(null);
        url = data.path("url").asText(null);
        iconUrl = data.path("icon_url").asText(null);
        proxyIconUrl = data.path("proxy_icon_url").asText(null);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Optional<String> getUrl() {
        return Optional.ofNullable(url);
    }

    @Override
    public Optional<String> getIconUrl() {
        return Optional.ofNullable(iconUrl);
    }

    @Override
    public Optional<String> getProxyIconUrl() {
        return Optional.ofNullable(proxyIconUrl);
    }
}
