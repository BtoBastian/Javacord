package org.javacord.api.entity.message.embed;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.Nameable;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * This interface represents an embed author.
 */
public interface EmbedAuthor extends Nameable {

    /**
     * Gets the url of the author.
     *
     * @return The url of the author.
     */
    Optional<URL> getUrl();

    /**
     * Gets the url of the author icon.
     *
     * @return The url of the author icon.
     */
    Optional<URL> getIconUrl();

    /**
     * Gets the proxy url of the author icon.
     *
     * @return The proxy url of the author icon.
     */
    Optional<URL> getProxyIconUrl();

    /**
     * Downloads the author icon as a {@code BufferedImage}.
     *
     * @param api The discord api instance used to download the author icon.
     * @return The thumbnail as a {@code BufferedImage}.
     * @deprecated Use {@link EmbedAuthor#iconAsBufferedImage(DiscordApi)} instead.
     */
    @Deprecated
    Optional<CompletableFuture<BufferedImage>> downloadIconAsBufferedImage(DiscordApi api);

    /**
     * Downloads the author icon as a byte array.
     *
     * @param api The discord api instance used to download the author icon.
     * @return The thumbnail as a byte array.
     * @deprecated Use {@link EmbedAuthor#iconAsByteArray(DiscordApi)} instead.
     */
    @Deprecated
    Optional<CompletableFuture<byte[]>> downloadIconAsByteArray(DiscordApi api);

    /**
     * Downloads the author icon as an input stream.
     *
     * @param api The discord api instance used to download the author icon.
     * @return The thumbnail as a input stream.
     * @throws IOException If an IO error occurs.
     * @deprecated Use {@link EmbedAuthor#iconAsInputStream(DiscordApi)} instead.
     */
    @Deprecated
    Optional<InputStream> downloadIconAsInputStream(DiscordApi api) throws IOException;

    /**
     * Downloads the author icon as a {@code BufferedImage}.
     *
     * @param api The discord api instance used to download the author icon.
     * @return The thumbnail as a {@code BufferedImage}.
     */
    Optional<CompletableFuture<BufferedImage>> iconAsBufferedImage(DiscordApi api);

    /**
     * Downloads the author icon as a byte array.
     *
     * @param api The discord api instance used to download the author icon.
     * @return The thumbnail as a byte array.
     */
    Optional<CompletableFuture<byte[]>> iconAsByteArray(DiscordApi api);

    /**
     * Downloads the author icon as an input stream.
     *
     * @param api The discord api instance used to download the author icon.
     * @return The thumbnail as a input stream.
     * @throws IOException If an IO error occurs.
     */
    Optional<InputStream> iconAsInputStream(DiscordApi api) throws IOException;

}
