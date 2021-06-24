package org.javacord.core.entity.user;

import com.fasterxml.jackson.databind.JsonNode;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.core.DiscordApiImpl;
import org.javacord.core.entity.server.ServerImpl;

import java.awt.Color;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Maps a member object.
 *
 * @see <a href="https://discord.com/developers/docs/resources/guild#guild-member-object">Discord Docs</a>
 */
public final class MemberImpl implements Member {

    private final DiscordApiImpl api;
    private final ServerImpl server;
    private final UserImpl user;
    private final String nickname;
    private final List<Long> roleIds;
    private final String joinedAt;
    private final String serverBoostingSince;

    /**
     * Creates a new immutable member instance.
     *
     * @param api The api instance.
     * @param server The server of the member.
     * @param data The json data of the member.
     * @param user A user object in case the json does not contain user data (e.g., for message create events).
     *             If the json contains a non-null user field, this parameter is ignored.
     */
    public MemberImpl(DiscordApiImpl api, ServerImpl server, JsonNode data, UserImpl user) {
        this.api = api;
        this.server = server;

        if (data.hasNonNull("user")) {
            this.user = new UserImpl(api, data.get("user"), this, null);
        } else {
            this.user = user;
        }

        if (data.hasNonNull("nick")) {
            nickname = data.get("nick").asText();
        } else {
            nickname = null;
        }

        roleIds = new ArrayList<>();
        for (JsonNode roleIdJson : data.get("roles")) {
            roleIds.add(roleIdJson.asLong());
        }
        roleIds.add(server.getEveryoneRole().getId());

        joinedAt = data.get("joined_at").asText();
        if (data.hasNonNull("premium_since")) {
            serverBoostingSince = data.get("premium_since").asText();
        } else {
            serverBoostingSince = null;
        }
    }

    private MemberImpl(DiscordApiImpl api, ServerImpl server, UserImpl user, String nickname, List<Long> roleIds,
                       String joinedAt, String serverBoostingSince) {
        this.api = api;
        this.server = server;
        this.user = user;
        this.nickname = nickname;
        this.roleIds = roleIds;
        this.joinedAt = joinedAt;
        this.serverBoostingSince = serverBoostingSince;
    }

    /**
     * Creates a new member object with the new user.
     *
     * @param user The new user.
     * @return The new member.
     */
    public MemberImpl setUser(UserImpl user) {
        return new MemberImpl(
                api, server, user, nickname, roleIds, joinedAt, serverBoostingSince);
    }

    /**
     * Creates a new member object with the new partial user data.
     *
     * @param partialUserJson The new partial user data.
     * @return The new member.
     */
    public MemberImpl setPartialUser(JsonNode partialUserJson) {
        return new MemberImpl(api, server, user.replacePartialUserData(partialUserJson), nickname, roleIds, joinedAt,
                serverBoostingSince);
    }

    /**
     * Creates a new member object with the new role ids.
     *
     * @param roleIds The new role ids.
     * @return The new member.
     */
    public MemberImpl setRoleIds(List<Long> roleIds) {
        roleIds.add(server.getEveryoneRole().getId());
        return new MemberImpl(
                api, server, user, nickname, roleIds, joinedAt, serverBoostingSince);
    }

    /**
     * Gets a list with the member's role ids.
     *
     * @return A list with the member's role ids.
     */
    public List<Long> getRoleIds() {
        return roleIds;
    }

    /**
     * Creates a new member object with the new nickname.
     *
     * @param nickname The new nickname.
     * @return The new member.
     */
    public MemberImpl setNickname(String nickname) {
        return new MemberImpl(
                api, server, user, nickname, roleIds, joinedAt, serverBoostingSince);
    }

    /**
     * Creates a new member object with the new nickname.
     *
     * @param serverBoostingSince The new timestamp when the user started boosting the server.
     * @return The new member.
     */
    public MemberImpl setServerBoostingSince(String serverBoostingSince) {
        return new MemberImpl(
                api, server, user, nickname, roleIds, joinedAt, serverBoostingSince);
    }

    /**
     * Gets the string value of the server boosting since field.
     *
     * @return The server boosting since field.
     */
    public String getServerBoostingSince() {
        return serverBoostingSince;
    }

    @Override
    public DiscordApi getApi() {
        return api;
    }

    @Override
    public long getId() {
        return user.getId();
    }

    @Override
    public Server getServer() {
        return server;
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public Optional<String> getNickname() {
        return Optional.ofNullable(nickname);
    }

    @Override
    public List<Role> getRoles() {
        return roleIds.stream()
                .map(server::getRoleById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .sorted()
                .collect(Collectors.toList());
    }

    @Override
    public boolean hasRole(Role role) {
        return roleIds.contains(role.getId());
    }

    @Override
    public Optional<Color> getRoleColor() {
        return getRoles().stream()
                .filter(role -> role.getColor().isPresent())
                .max(Comparator.comparingInt(Role::getRawPosition))
                .flatMap(Role::getColor);
    }

    @Override
    public Instant getJoinedAtTimestamp() {
        return OffsetDateTime.parse(joinedAt).toInstant();
    }

    @Override
    public Optional<Instant> getServerBoostingSinceTimestamp() {
        return Optional.ofNullable(serverBoostingSince)
                .map(OffsetDateTime::parse)
                .map(OffsetDateTime::toInstant);
    }

    @Override
    public String toString() {
        return String.format("Member (id: %s, display name: %s)", getIdAsString(), getDisplayName());
    }
}
