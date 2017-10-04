package de.btobastian.javacord.entities;

import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

/**
 * All entities which have an avatar.
 */
public interface AvatarHolder extends DiscordEntity {

    /**
     * Gets the url of the avatar.
     *
     * @return The url of the avatar.
     */
    default URL getAvatarUrl() {
        return ((IconHolder) this).getIconUrl().orElseThrow(() -> new IllegalStateException(
                "Something happened which should never happen. Please contact the developer!"));
    }

    /**
     * Gets the avatar of the entity as byte array.
     *
     * @return The avatar of the entity as byte array.
     */
    default CompletableFuture<byte[]> getAvatarAsByteArray() {
        CompletableFuture<byte[]> future = new CompletableFuture<>();
        ((IconHolder) this).getIconAsByteArray().whenComplete((bytes, throwable) -> {
            if (throwable != null) {
                future.completeExceptionally(throwable);
                return;
            }
            future.complete(bytes.orElseThrow(() -> new IllegalStateException(
                            "Something happened which should never happen. Please contact the developer!")));
        });
        return future;
    }

    /**
     * Gets the avatar of the entity as {@link BufferedImage}.
     *
     * @return The avatar of the entity.
     */
    default CompletableFuture<BufferedImage> getAvatar() {
        CompletableFuture<BufferedImage> future = new CompletableFuture<>();
        ((IconHolder) this).getIcon().whenComplete((icon, throwable) -> {
            if (throwable != null) {
                future.completeExceptionally(throwable);
                return;
            }
            future.complete(icon.orElseThrow(() -> new IllegalStateException(
                    "Something happened which should never happen. Please contact the developer!")));
        });
        return future;
    }

}
