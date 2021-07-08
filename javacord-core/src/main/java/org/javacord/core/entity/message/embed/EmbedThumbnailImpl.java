package org.javacord.core.entity.message.embed;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.logging.log4j.Logger;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.embed.EmbedThumbnail;
import org.javacord.core.util.FileContainer;
import org.javacord.core.util.logging.LoggerUtil;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

/**
 * The implementation of {@link EmbedThumbnail}.
 */
public class EmbedThumbnailImpl implements EmbedThumbnail {

    /**
     * The logger of this class.
     */
    private static final Logger logger = LoggerUtil.getLogger(EmbedThumbnailImpl.class);

    private final String url;
    private final String proxyUrl;
    private final int height;
    private final int width;

    /**
     * Creates a new embed thumbnail.
     *
     * @param data The json data of the thumbnail.
     */
    public EmbedThumbnailImpl(JsonNode data) {
        url = data.has("url") ? data.get("url").asText() : null;
        proxyUrl = data.has("proxy_url") ? data.get("proxy_url").asText() : null;
        height = data.has("height") ? data.get("height").asInt() : -1;
        width = data.has("width") ? data.get("width").asInt() : -1;
    }

    @Override
    public URL getUrl() {
        if (url == null) {
            return null;
        }
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            logger.warn("Seems like the url of the embed thumbnail is malformed! Please contact the developer!", e);
            return null;
        }
    }

    @Override
    public URL getProxyUrl() {
        if (proxyUrl == null) {
            return null;
        }
        try {
            return new URL(proxyUrl);
        } catch (MalformedURLException e) {
            logger.warn("Seems like the embed thumbnail's proxy url is malformed! Please contact the developer!", e);
            return null;
        }
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public int getWidth() {
        return width;
    }
    
    @Override
    public CompletableFuture<BufferedImage> downloadAsBufferedImage(DiscordApi api) {
        return new FileContainer(getUrl()).asBufferedImage(api);
    }

    @Override
    public CompletableFuture<byte[]> downloadAsByteArray(DiscordApi api) {
        return new FileContainer(getUrl()).asByteArray(api);
    }

    @Override
    public InputStream downloadAsInputStream(DiscordApi api) throws IOException {
        return new FileContainer(getUrl()).asInputStream(api);
    }

    @Override
    public CompletableFuture<BufferedImage> asBufferedImage(DiscordApi api) {
        return new FileContainer(getUrl()).asBufferedImage(api);
    }

    @Override
    public CompletableFuture<byte[]> asByteArray(DiscordApi api) {
        return new FileContainer(getUrl()).asByteArray(api);
    }

    @Override
    public InputStream asInputStream(DiscordApi api) throws IOException {
        return new FileContainer(getUrl()).asInputStream(api);
    }
}
