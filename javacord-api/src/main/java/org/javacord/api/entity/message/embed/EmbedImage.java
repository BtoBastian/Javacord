package org.javacord.api.entity.message.embed;

import org.javacord.api.DiscordApi;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

/**
 * This interface represents an embed image.
 */
public interface EmbedImage {

    /**
     * Gets the url of the image.
     *
     * @return The url of the image.
     */
    URL getUrl();

    /**
     * Gets the proxy url of the image.
     *
     * @return The proxy url of the image.
     */
    URL getProxyUrl();

    /**
     * Gets the height of the image.
     *
     * @return The height of the image.
     */
    int getHeight();

    /**
     * Gets the width of the image.
     *
     * @return The width of the image.
     */
    int getWidth();

    /**
     * Downloads the image as a {@code BufferedImage}.
     *
     * @param api The discord api instance used to download the image.
     * @return The thumbnail as a {@code BufferedImage}.
     * @deprecated Use {@link EmbedImage#asBufferedImage(DiscordApi)} instead.
     */
    @Deprecated
    CompletableFuture<BufferedImage> downloadAsBufferedImage(DiscordApi api);

    /**
     * Downloads the image as a byte array.
     *
     * @param api The discord api instance used to download the image.
     * @return The thumbnail as a byte array.
     * @deprecated Use {@link EmbedImage#asByteArray(DiscordApi)} instead.
     */
    @Deprecated
    CompletableFuture<byte[]> downloadAsByteArray(DiscordApi api);

    /**
     * Downloads the image as an input stream.
     *
     * @param api The discord api instance used to download the image.
     * @return The thumbnail as a input stream.
     * @throws IOException If an IO error occurs.
     * @deprecated Use {@link EmbedImage#asInputStream(DiscordApi)} instead.
     */
    @Deprecated
    InputStream downloadAsInputStream(DiscordApi api) throws IOException;

    /**
     * Downloads the image as a {@code BufferedImage}.
     *
     * @param api The discord api instance used to download the image.
     * @return The thumbnail as a {@code BufferedImage}.
     */
    CompletableFuture<BufferedImage> asBufferedImage(DiscordApi api);

    /**
     * Downloads the image as a byte array.
     *
     * @param api The discord api instance used to download the image.
     * @return The thumbnail as a byte array.
     */
    CompletableFuture<byte[]> asByteArray(DiscordApi api);

    /**
     * Downloads the image as an input stream.
     *
     * @param api The discord api instance used to download the image.
     * @return The thumbnail as a input stream.
     * @throws IOException If an IO error occurs.
     */
    InputStream asInputStream(DiscordApi api) throws IOException;

}