package org.javacord.core.entity.activity;

import com.fasterxml.jackson.databind.JsonNode;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.ApplicationInfo;
import org.javacord.api.entity.team.Team;
import org.javacord.api.entity.user.User;
import org.javacord.core.DiscordApiImpl;
import org.javacord.core.entity.team.TeamImpl;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * The implementation of {@link ApplicationInfo}.
 */
public class ApplicationInfoImpl implements ApplicationInfo {

    private final DiscordApi api;

    private final long clientId;
    private final String name;
    private final String description;
    private final boolean publicBot;
    private final boolean botRequiresCodeGrant;
    private final long ownerId;
    private final String ownerName;
    private final String ownerDiscriminator;
    private final Team team;

    /**
     * Creates a new application info object.
     *
     * @param api The discord api.
     * @param data The json data of the application.
     */
    public ApplicationInfoImpl(DiscordApi api, JsonNode data) {
        this.api = api;

        clientId = data.get("id").asLong();
        name = data.get("name").asText();
        description = data.get("description").asText();
        publicBot = data.get("bot_public").asBoolean();
        botRequiresCodeGrant = data.get("bot_require_code_grant").asBoolean();
        ownerId = data.get("owner").get("id").asLong();
        ownerName = data.get("owner").get("username").asText();
        ownerDiscriminator = data.get("owner").get("discriminator").asText();
        team = data.hasNonNull("team") ? new TeamImpl((DiscordApiImpl) api, data.get("team")) : null;
    }

    @Override
    public long getClientId() {
        return clientId;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public boolean isPublicBot() {
        return publicBot;
    }

    @Override
    public boolean botRequiresCodeGrant() {
        return botRequiresCodeGrant;
    }

    @Override
    public String getOwnerName() {
        return ownerName;
    }

    @Override
    public long getOwnerId() {
        return ownerId;
    }

    @Override
    public String getOwnerDiscriminator() {
        return ownerDiscriminator;
    }

    @Override
    public CompletableFuture<User> getOwner() {
        return api.getUserById(getOwnerId());
    }

    @Override
    public Optional<Team> getTeam() {
        return Optional.ofNullable(team);
    }
}
