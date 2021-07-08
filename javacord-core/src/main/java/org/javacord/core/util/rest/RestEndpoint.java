package org.javacord.core.util.rest;

import okhttp3.HttpUrl;
import org.javacord.api.Javacord;

import java.util.Optional;

/**
 * This enum contains all endpoints which we may use.
 */
public enum RestEndpoint {

    GATEWAY("/gateway"),
    GATEWAY_BOT("/gateway/bot"),
    MESSAGE("/channels/%s/messages", 0),
    /**
     * This is the same endpoint as {@link RestEndpoint#MESSAGE}, but it has an different ratelimit bucket.
     */
    MESSAGE_DELETE("/channels/%s/messages", 0),
    MESSAGES_BULK_DELETE("/channels/%s/messages/bulk-delete", 0),
    CHANNEL_TYPING("/channels/%s/typing", 0),
    CHANNEL_INVITE("/channels/%s/invites", 0),
    USER("/users/%s"),
    USER_CHANNEL("/users/@me/channels"),
    CHANNEL("/channels/%s", 0),
    ROLE("/guilds/%s/roles", 0),
    SERVER("/guilds", 0),
    SERVER_PRUNE("/guilds/%s/prune", 0),
    SERVER_SELF("/users/@me/guilds/%s", 0),
    SERVER_CHANNEL("/guilds/%s/channels", 0),
    // hardcoded reactions ratelimit due to https://github.com/discordapp/discord-api-docs/issues/182
    REACTION("/channels/%s/messages/%s/reactions", 0, 250),
    PINS("/channels/%s/pins", 0),
    SERVER_MEMBER("/guilds/%s/members/%s", 0),
    SERVER_MEMBER_ROLE("/guilds/%s/members/%s/roles/%s", 0),
    OWN_NICKNAME("/guilds/%s/members/@me/nick", 0),
    SELF_INFO("/oauth2/applications/@me"),
    CHANNEL_WEBHOOK("/channels/%s/webhooks", 0),
    SERVER_WEBHOOK("/guilds/%s/webhooks", 0),
    SERVER_INVITE("/guilds/%s/invites", 0),
    WEBHOOK("/webhooks/%s", 0),
    WEBHOOK_SEND("/webhooks/%s/%s", 0),
    WEBHOOK_MESSAGE("/webhooks/%s/%s/messages/%s", 0),
    INVITE("/invites/%s"),
    BAN("/guilds/%s/bans", 0),
    CURRENT_USER("/users/@me"),
    AUDIT_LOG("/guilds/%s/audit-logs", 0),
    CUSTOM_EMOJI("/guilds/%s/emojis", 0),
    INTERACTION_RESPONSE("/interactions/%s/%s/callback", 0),
    ORIGINAL_INTERACTION_RESPONSE("/webhooks/%s/%s/messages/@original",0),
    SLASH_COMMANDS("/applications/%s/commands", 0),
    SERVER_SLASH_COMMANDS("/applications/%s/guilds/%s/commands",0),
    SERVER_SLASH_COMMAND_PERMISSIONS("/applications/%s/guilds/%s/commands/permissions",0),
    SLASH_COMMAND_PERMISSIONS("/applications/%s/guilds/%s/commands/%s/permissions",0);

    /**
     * The endpoint url (only including the base, not the https://discord.com/api/vXYZ/ "prefix".
     */
    private final String endpointUrl;

    /**
     * The position of the major parameter starting with <code>0</code> or <code>-1</code> if no major parameter exists.
     */
    private final int majorParameterPosition;

    /**
     * Whether the endpoint is global or not.
     */
    private boolean global;

    /**
     * The position of the major parameter starting with <code>0</code> or <code>-1</code> if no major parameter exists.
     */
    private final int hardcodedRatelimit;

    RestEndpoint(String endpointUrl) {
        this(endpointUrl, -1, false, -1);
    }

    RestEndpoint(String endpointUrl, boolean global) {
        this(endpointUrl, -1, global, -1);
    }

    RestEndpoint(String endpointUrl, int majorParameterPosition) {
        this(endpointUrl, majorParameterPosition, false, -1);
    }

    RestEndpoint(String endpointUrl, int majorParameterPosition, int hardcodedRatelimit) {
        this(endpointUrl, majorParameterPosition, false, hardcodedRatelimit);
    }

    RestEndpoint(String endpointUrl, int majorParameterPosition, boolean global) {
        this(endpointUrl, majorParameterPosition, false, -1);
    }

    RestEndpoint(String endpointUrl, int majorParameterPosition, boolean global, int hardcodedRatelimit) {
        this.endpointUrl = endpointUrl;
        this.majorParameterPosition = majorParameterPosition;
        this.global = global;
        this.hardcodedRatelimit = hardcodedRatelimit;
    }

    /**
     * Gets the major parameter position of the endpoint.
     * If an endpoint has a major parameter, it means that the ratelimits for this endpoint are based on this parameter.
     * The position starts counting at <code>0</code>!
     *
     * @return An optional which is present, if the endpoint has a major parameter.
     */
    public Optional<Integer> getMajorParameterPosition() {
        if (majorParameterPosition >= 0) {
            return Optional.of(majorParameterPosition);
        }
        return Optional.empty();
    }

    /**
     * Gets the endpoint url (only including the base, not the https://discord.com/api/vXYZ/ "prefix".
     *
     * @return The gateway url.
     */
    public String getEndpointUrl() {
        return endpointUrl;
    }

    /**
     * Checks if the endpoint is global.
     *
     * @return Whether the endpoint is global or not.
     */
    public boolean isGlobal() {
        return global;
    }

    /**
     * Sets whether this endpoint is global or not.
     *
     * @param global If the endpoint is global.
     */
    public void setGlobal(boolean global) {
        this.global = global;
    }

    /**
     * Gets the hardcoded ratelimit if one is set.
     *
     * @return An optional which is present, if the endpoint has a hardcoded ratelimit.
     */
    public Optional<Integer> getHardcodedRatelimit() {
        if (hardcodedRatelimit >= 0) {
            return Optional.of(hardcodedRatelimit);
        }
        return Optional.empty();
    }

    /**
     * Gets the full url of the endpoint.
     * Parameters which are "too much" are added to the end.
     *
     * @param parameters The parameters of the url. E.g. for channel ids.
     * @return The full url of the endpoint.
     */
    public String getFullUrl(String... parameters) {
        StringBuilder url = new StringBuilder(
                "https://" + Javacord.DISCORD_DOMAIN + "/api/v" + Javacord.DISCORD_API_VERSION + getEndpointUrl());
        url = new StringBuilder(String.format(url.toString(), (Object[]) parameters));
        int parameterAmount = getEndpointUrl().split("%s").length - (getEndpointUrl().endsWith("%s") ? 0 : 1);
        if (parameters.length > parameterAmount) {
            for (int i = parameterAmount; i < parameters.length; i++) {
                url.append("/").append(parameters[i]);
            }
        }
        return url.toString();
    }

    /**
     * Gets the full {@link HttpUrl http url} of the endpoint.
     * Parameters which are "too much" are added to the end.
     *
     * @param parameters The parameters of the url. E.g. for channel ids.
     * @return The full http url of the endpoint.
     */
    public HttpUrl getOkHttpUrl(String... parameters) {
        return HttpUrl.parse(getFullUrl(parameters));
    }

}
