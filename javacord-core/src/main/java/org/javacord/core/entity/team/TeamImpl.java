package org.javacord.core.entity.team;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.logging.log4j.Logger;
import org.javacord.api.DiscordApi;
import org.javacord.api.Javacord;
import org.javacord.api.entity.Icon;
import org.javacord.api.entity.team.Team;
import org.javacord.api.entity.team.TeamMember;
import org.javacord.core.DiscordApiImpl;
import org.javacord.core.entity.IconImpl;
import org.javacord.core.util.logging.LoggerUtil;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class TeamImpl implements Team {

    /**
     * The logger of this class.
     */
    private static final Logger logger = LoggerUtil.getLogger(TeamImpl.class);

    /**
     * The discord api instance.
     */
    private final DiscordApiImpl api;

    /**
     * The id of the team.
     */
    private final long id;

    /**
     * The id of the owner.
     */
    private final long ownerId;

    /**
     * The avatar hash of the team.
     */
    private final String iconHash;

    /**
     * The members of the team.
     */
    private final List<TeamMember> members = new LinkedList<TeamMember>();

    /**
     * Creates a new user.
     *
     * @param api The discord api instance.
     * @param data The json data of the user.
     */
    public TeamImpl(DiscordApiImpl api, JsonNode data) {
        this.api = api;

        id = Long.parseLong(data.get("id").asText());


        iconHash = data.has("icon") ? data.get("icon").asText() : null;


        ownerId = data.get("owner_user_id").asLong();

        for (JsonNode member : data.get("members")) {
            members.add(new TeamMemberImpl(api, member));
        }
    }

    @Override
    public Optional<Icon> getIcon() {
        if (iconHash == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(new IconImpl(
                    getApi(),
                    new URL("https://" + Javacord.DISCORD_CDN_DOMAIN
                            + "/team-icons/" + getIdAsString() + "/" + iconHash + ".png")));
        } catch (MalformedURLException e) {
            logger.warn("Seems like the url of the icon is malformed! Please contact the developer!", e);
            return Optional.empty();
        }
    }

    @Override
    public Collection<TeamMember> getTeamMembers() {
        return new ArrayList<>(members);
    }

    @Override
    public long getOwnerId() {
        return ownerId;
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
