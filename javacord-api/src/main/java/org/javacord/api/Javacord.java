package org.javacord.api;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.Properties;

/**
 * This class contains some static information about Javacord.
 */
public class Javacord {

    /**
     * The current Javacord version.
     *
     * @see #DISPLAY_VERSION
     */
    public static final String VERSION;

    /**
     * The commit ID from which the current Javacord version was built.
     *
     * @see #DISPLAY_VERSION
     */
    public static final String COMMIT_ID;

    /**
     * The build timestamp at which the current Javacord version was built.
     *
     * @see #DISPLAY_VERSION
     */
    public static final Instant BUILD_TIMESTAMP;

    /**
     * The domain of Discord.
     */
    public static final String DISCORD_DOMAIN = "discord.com";

    /**
     * The domain of Discord's CDN.
     */
    public static final String DISCORD_CDN_DOMAIN = "cdn.discordapp.com";

    /**
     * The display version of the current Javacord version.
     * If the current Javacord version is a release version, it is equal to {@link #VERSION}.
     * If the current Javacord version is a snapshot version, it consists of the {@link #VERSION}, the
     * {@link #COMMIT_ID}, and the {@link #BUILD_TIMESTAMP}.
     * For displaying the version somewhere it is best to use this constant.
     *
     * @see #VERSION
     * @see #COMMIT_ID
     * @see #BUILD_TIMESTAMP
     */
    public static final String DISPLAY_VERSION;

    static {
        Properties versionProperties = new Properties();
        try (InputStream versionPropertiesStream = Javacord.class.getResourceAsStream("/git.properties")) {
            versionProperties.load(versionPropertiesStream);
        } catch (IOException ignored) { }

        VERSION = versionProperties.getProperty("version", "<unknown>");
        COMMIT_ID = versionProperties.getProperty("git.commit.id.abbrev", "<unknown>");

        String buildTimestamp = versionProperties.getProperty("buildTimestamp", null);
        if (buildTimestamp == null) {
            BUILD_TIMESTAMP = null;
        } else {
            BUILD_TIMESTAMP = Instant.parse(buildTimestamp);
        }

        DISPLAY_VERSION = VERSION.endsWith("-SNAPSHOT")
                ? String.format("%s [%s]", VERSION, BUILD_TIMESTAMP)
                : VERSION;
    }

    /**
     * The github url of javacord.
     */
    public static final String GITHUB_URL = "https://github.com/Javacord/Javacord";

    /**
     * The user agent used for requests.
     */
    public static final String USER_AGENT = "DiscordBot (" + GITHUB_URL + ", v" + DISPLAY_VERSION + ")";

    /**
     * The gateway version from Discord which we are using.
     * A list with all gateway versions can be found
     * <a href="https://discord.com/developers/docs/topics/gateway#gateways-gateway-versions">here</a>.
     */
    public static final String DISCORD_GATEWAY_VERSION = "8";

    /**
     * The voice gateway version from Discord which we are using.
     * A list with all voice gateway versions can be found
     * <a href="https://discordapp.com/developers/docs/topics/voice-connections#voice-gateway-versioning">here</a>.
     */
    public static final String DISCORD_VOICE_GATEWAY_VERSION = "4";

    /**
     * The API version from Discord which we are using.
     * A list with all API versions can be found
     * <a href="https://discord.com/developers/docs/reference#api-versioning-api-versions">here</a>.
     */
    public static final String DISCORD_API_VERSION = "8";

    private Javacord() {
        throw new UnsupportedOperationException();
    }
}
