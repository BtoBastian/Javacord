package org.javacord.core.entity.team;

import com.fasterxml.jackson.databind.JsonNode;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.team.TeamMember;
import org.javacord.api.entity.team.TeamMembershipState;
import org.javacord.api.entity.user.User;
import org.javacord.core.DiscordApiImpl;

import java.util.concurrent.CompletableFuture;

public class TeamMemberImpl implements TeamMember  {

    /**
     * The discord api instance.
     */
    private final DiscordApiImpl api;

    /**
     * The id of the team member.
     */
    private final long id;

    /**
     * The state of the membership of the team member.
     */
    private final TeamMembershipState membershipState;

    /**
     * Creates a new TeamMember.
     *
     * @param api The discord api instance.
     * @param data The json data of the team member.
     */
    TeamMemberImpl(DiscordApiImpl api, JsonNode data) {
        this.api = api;

        this.id = data.get("user").get("id").asLong();

        this.membershipState = TeamMembershipState.fromId(data.get("membership_state").asInt());
    }

    @Override
    public TeamMembershipState getMembershipState() {
        return membershipState;
    }

    @Override
    public CompletableFuture<User> getUser() {
        return api.getUserById(id);
    }

    @Override
    public DiscordApi getApi() {
        return api;
    }

    @Override
    public long getId() {
        return id;
    }
}
