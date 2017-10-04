package de.btobastian.javacord.entities.message.embed;

import java.net.URL;

/**
 * This interface represents an embed provider.
 */
public interface EmbedProvider {

    /**
     * Gets the name of the provider.
     *
     * @return The name of the provider.
     */
    String getName();

    /**
     * Gets the url of the provider.
     *
     * @return The url of the provider.
     */
    URL getUrl();

}