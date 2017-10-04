package de.btobastian.javacord.entities.message.emoji.impl;

import de.btobastian.javacord.DiscordApi;
import de.btobastian.javacord.ImplDiscordApi;
import de.btobastian.javacord.entities.Server;
import de.btobastian.javacord.entities.message.emoji.CustomEmoji;
import org.json.JSONObject;

import java.util.Optional;

/**
 * The implementation of {@link CustomEmoji}.
 */
public class ImplCustomEmoji implements CustomEmoji {

    /**
     * The discord api instance.
     */
    private final ImplDiscordApi api;

    /**
     * The id of the emoji.
     */
    private final long id;

    /**
     * The name of the emoji.
     */
    private String name;

    /**
     * The server of the emoji. Might be <code>null</code>!
     */
    private Server server;

    /**
     * Creates a new custom emoji.
     *
     * @param api The discord api instance.
     * @param data The json data of the emoji.
     */
    public ImplCustomEmoji(ImplDiscordApi api, JSONObject data) {
        this(api, null, data);
    }

    /**
     * Creates a new custom emoji.
     *
     * @param api The discord api instance.
     * @param server The server of the emoji.
     * @param data The json data of the emoji.
     */
    public ImplCustomEmoji(ImplDiscordApi api, Server server, JSONObject data) {
        this.api = api;
        this.server = server;
        id = Long.parseLong(data.getString("id"));
        name = data.getString("name");
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
    public Optional<Server> getServer() {
        return Optional.ofNullable(server);
    }

    @Override
    public String toString() {
        return String.format("CustomEmoji (id: %s, name: %s)", getId(), getName());
    }
}
