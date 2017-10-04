package de.btobastian.javacord.entities.impl;

import de.btobastian.javacord.DiscordApi;
import de.btobastian.javacord.ImplDiscordApi;
import de.btobastian.javacord.entities.*;
import de.btobastian.javacord.entities.channels.ChannelCategory;
import de.btobastian.javacord.entities.channels.ServerChannel;
import de.btobastian.javacord.entities.channels.ServerTextChannel;
import de.btobastian.javacord.entities.channels.ServerVoiceChannel;
import de.btobastian.javacord.entities.channels.impl.ImplChannelCategory;
import de.btobastian.javacord.entities.channels.impl.ImplServerTextChannel;
import de.btobastian.javacord.entities.channels.impl.ImplServerVoiceChannel;
import de.btobastian.javacord.entities.message.emoji.CustomEmoji;
import de.btobastian.javacord.entities.permissions.Role;
import de.btobastian.javacord.entities.permissions.impl.ImplRole;
import de.btobastian.javacord.listeners.message.MessageCreateListener;
import de.btobastian.javacord.listeners.message.MessageDeleteListener;
import de.btobastian.javacord.listeners.message.MessageEditListener;
import de.btobastian.javacord.listeners.message.reaction.ReactionAddListener;
import de.btobastian.javacord.listeners.message.reaction.ReactionRemoveListener;
import de.btobastian.javacord.listeners.server.*;
import de.btobastian.javacord.listeners.server.channel.ServerChannelChangeNameListener;
import de.btobastian.javacord.listeners.server.channel.ServerChannelChangePositionListener;
import de.btobastian.javacord.listeners.server.channel.ServerChannelCreateListener;
import de.btobastian.javacord.listeners.server.channel.ServerChannelDeleteListener;
import de.btobastian.javacord.listeners.server.emoji.CustomEmojiCreateListener;
import de.btobastian.javacord.listeners.user.UserChangeGameListener;
import de.btobastian.javacord.listeners.user.UserChangeStatusListener;
import de.btobastian.javacord.listeners.user.UserStartTypingListener;
import de.btobastian.javacord.utils.logging.LoggerUtil;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * The implementation of {@link de.btobastian.javacord.entities.Server}.
 */
public class ImplServer implements Server {

    /**
     * The logger of this class.
     */
    private static final Logger logger = LoggerUtil.getLogger(ImplServer.class);

    /**
     * The discord api instance.
     */
    private final ImplDiscordApi api;

    /**
     * The id of the server.
     */
    private final long id;

    /**
     * The name of the server.
     */
    private String name;

    /**
     * The region of the server.
     */
    private Region region;

    /**
     * Whether the server is considered as large or not.
     */
    private boolean large;

    /**
     * The id of the owner.
     */
    private long ownerId;

    /**
     * The amount of members in this server.
     */
    private int memberCount = -1;

    /**
     * The icon id of the server. Might be <code>null</code>.
     */
    private String iconId;

    /**
     * A map with all roles of the server.
     */
    private final ConcurrentHashMap<Long, Role> roles = new ConcurrentHashMap<>();

    /**
     * A map with all channels of the server.
     */
    private final ConcurrentHashMap<Long, ServerChannel> channels = new ConcurrentHashMap<>();

    /**
     * A map with all members of the server.
     */
    private final ConcurrentHashMap<Long, User> members = new ConcurrentHashMap<>();

    /**
     * A map with all nicknames. The key is the user id.
     */
    private final ConcurrentHashMap<Long, String> nicknames = new ConcurrentHashMap<>();

    /**
     * A list with all custom emojis from this server.
     */
    private final Collection<CustomEmoji> customEmojis = new ArrayList<>();

    /**
     * A map which contains all listeners.
     * The key is the class of the listener.
     */
    private final ConcurrentHashMap<Class<?>, List<Object>> listeners = new ConcurrentHashMap<>();

    /**
     * Creates a new server object.
     *
     * @param api The discord api instance.
     * @param data The json data of the server.
     */
    public ImplServer(ImplDiscordApi api, JSONObject data) {
        this.api = api;

        id = Long.parseLong(data.getString("id"));
        name = data.getString("name");
        region = Region.getRegionByKey(data.getString("region"));
        large = data.getBoolean("large");
        memberCount = data.getInt("member_count");
        ownerId = Long.parseLong(data.getString("owner_id"));
        if (data.has("icon") && !data.isNull("icon")) {
            iconId = data.getString("icon");
        }

        if (data.has("channels")) {
            JSONArray channels = data.getJSONArray("channels");
            for (int i = 0; i < channels.length(); i++) {
                JSONObject channel = channels.getJSONObject(i);
                switch (channel.getInt("type")) {
                    case 0:
                        getOrCreateServerTextChannel(channel);
                        break;
                    case 2:
                        getOrCreateServerVoiceChannel(channel);
                        break;
                    case 4:
                        getOrCreateChannelCategory(channel);
                        break;
                }
            }
        }

        JSONArray roles = data.has("roles") ? data.getJSONArray("roles") : new JSONArray();
        for (int i = 0; i < roles.length(); i++) {
            Role role = new ImplRole(api, this, roles.getJSONObject(i));
            this.roles.put(role.getId(), role);
        }

        JSONArray members = new JSONArray();
        if (data.has("members")) {
            members = data.getJSONArray("members");
        }
        addMembers(members);

        if (isLarge() && getMembers().size() < getMemberCount()) {
            JSONObject requestGuildMembersPacket = new JSONObject()
                    .put("op", 8)
                    .put("d", new JSONObject()
                            .put("guild_id", String.valueOf(getId()))
                            .put("query","")
                            .put("limit", 0));
            logger.debug("Sending request guild members packet for server {}", this);
            this.api.getWebSocketAdapter().getWebSocket().sendText(requestGuildMembersPacket.toString());
        }

        JSONArray emojis = data.has("emojis") ? data.getJSONArray("emojis") : new JSONArray();
        for (int i = 0; i < emojis.length(); i++) {
            CustomEmoji emoji = api.getOrCreateCustomEmoji(this, emojis.getJSONObject(i));
            addCustomEmoji(emoji);
        }

        JSONArray presences = data.has("presences") ? data.getJSONArray("presences") : new JSONArray();
        for (int i = 0; i < presences.length(); i++) {
            JSONObject presence = presences.getJSONObject(i);
            long userId = Long.parseLong(presence.getJSONObject("user").getString("id"));
            api.getUserById(userId).map(user -> ((ImplUser) user)).ifPresent(user -> {
                if (presence.has("game")) {
                    Game game = null;
                    if (!presence.isNull("game")) {
                        int gameType = presence.getJSONObject("game").getInt("type");
                        String name = presence.getJSONObject("game").getString("name");
                        String streamingUrl =
                                presence.getJSONObject("game").has("url") &&
                                !presence.getJSONObject("game").isNull("url") ?
                                presence.getJSONObject("game").getString("url") : null;
                        game = new ImplGame(GameType.getGameTypeById(gameType), name, streamingUrl);
                    }
                    user.setGame(game);
                }
                if (presence.has("status")) {
                    UserStatus status = UserStatus.fromString(presence.optString("status"));
                    user.setStatus(status);
                }
            });
        }

        api.addServerToCache(this);
    }

    /**
     * Adds a channel to the cache.
     *
     * @param channel The channel to add.
     */
    public void addChannelToCache(ServerChannel channel) {
        channels.put(channel.getId(), channel);
    }

    /**
     * Removes a channel from the cache.
     *
     * @param channelId The if of the channel to remove.
     */
    public void removeChannelFromCache(long channelId) {
        channels.remove(channelId);
    }

    /**
     * Adds a custom emoji.
     *
     * @param emoji The emoji to add.
     */
    public void addCustomEmoji(CustomEmoji emoji) {
        customEmojis.add(emoji);
    }

    /**
     * Removes a custom emoji.
     *
     * @param emoji The emoji to remove.
     */
    public void removeCustomEmoji(CustomEmoji emoji) {
        customEmojis.remove(emoji);
    }

    /**
     * Gets or creates a channel category.
     *
     * @param data The json data of the channel.
     * @return The server text channel.
     */
    public ChannelCategory getOrCreateChannelCategory(JSONObject data) {
        long id = Long.parseLong(data.getString("id"));
        int type = data.getInt("type");
        synchronized (this) {
            if (type == 4) {
                return getChannelCategoryById(id).orElse(new ImplChannelCategory(api, this, data));
            }
        }
        // Invalid channel type
        return null;
    }

    /**
     * Gets or creates a server text channel.
     *
     * @param data The json data of the channel.
     * @return The server text channel.
     */
    public ServerTextChannel getOrCreateServerTextChannel(JSONObject data) {
        long id = Long.parseLong(data.getString("id"));
        int type = data.getInt("type");
        synchronized (this) {
            if (type == 0) {
                return getTextChannelById(id).orElse(new ImplServerTextChannel(api, this, data));
            }
        }
        // Invalid channel type
        return null;
    }

    /**
     * Gets or creates a server voice channel.
     *
     * @param data The json data of the channel.
     * @return The server voice channel.
     */
    public ServerVoiceChannel getOrCreateServerVoiceChannel(JSONObject data) {
        long id = Long.parseLong(data.getString("id"));
        int type = data.getInt("type");
        synchronized (this) {
            if (type == 2) {
                return getVoiceChannelById(id).orElse(new ImplServerVoiceChannel(api, this, data));
            }
        }
        // Invalid channel type
        return null;
    }

    /**
     * Removes a member from the server.
     *
     * @param user The user to remove.
     */
    public void removeMember(User user) {
        members.remove(user.getId());
        nicknames.remove(user.getId());
        getRoles().forEach(role -> ((ImplRole) role).removeUserFromCache(user));
    }

    /**
     * Adds a member to the server.
     *
     * @param member The user to add.
     */
    public void addMember(JSONObject member) {
        User user = api.getOrCreateUser(member.getJSONObject("user"));
        members.put(user.getId(), user);
        if (member.has("nick") && !member.isNull("nick")) {
            nicknames.put(user.getId(), member.getString("nick"));
        }

        JSONArray memberRoles = member.getJSONArray("roles");
        for (int i = 0; i < memberRoles.length(); i++) {
            long roleId = Long.parseLong(memberRoles.getString(i));
            getRoleById(roleId).map(role -> ((ImplRole) role)).ifPresent(role -> role.addUserToCache(user));
        }
    }

    /**
     * Adds members to the server.
     *
     * @param members An array of guild member objects.
     */
    public void addMembers(JSONArray members) {
        for (int i = 0; i < members.length(); i++) {
            addMember(members.getJSONObject(i));
        }
    }

    /**
     * Sets the name of the server.
     *
     * @param name The name of the server.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Adds a listener.
     *
     * @param clazz The listener class.
     * @param listener The listener to add.
     */
    private void addListener(Class<?> clazz, Object listener) {
        List<Object> classListeners = listeners.computeIfAbsent(clazz, c -> new ArrayList<>());
        classListeners.add(listener);
    }

    /**
     * Gets all listeners of the given class.
     *
     * @param clazz The class of the listener.
     * @param <T> The class of the listener.
     * @return A list with all listeners of the given type.
     */
    @SuppressWarnings("unchecked") // We make sure it's the right type when adding elements
    private <T> List<T> getListeners(Class<?> clazz) {
        List<Object> classListeners = listeners.getOrDefault(clazz, new ArrayList<>());
        return classListeners.stream().map(o -> (T) o).collect(Collectors.toCollection(ArrayList::new));
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
    public Region getRegion() {
        return region;
    }

    @Override
    public Optional<String> getNickname(User user) {
        return Optional.ofNullable(nicknames.get(user.getId()));
    }

    @Override
    public Collection<User> getMembers() {
        return members.values();
    }

    @Override
    public boolean isLarge() {
        return large;
    }

    @Override
    public int getMemberCount() {
        return memberCount;
    }

    @Override
    public User getOwner() {
        return api.getUserById(ownerId)
                .orElseThrow(() -> new IllegalStateException("Owner of server " + toString() + " is not cached!"));
    }

    @Override
    public List<Role> getRoles() {
        return roles.values().stream()
                .sorted(Comparator.comparingInt(Role::getPosition))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Role> getRoleById(long id) {
        return Optional.ofNullable(roles.get(id));
    }

    @Override
    public Optional<URL> getIconUrl() {
        if (iconId == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(new URL("https://cdn.discordapp.com/icons/" + getId() + "/" + iconId + ".png"));
        } catch (MalformedURLException e) {
            logger.warn("Seems like the url of the icon is malformed! Please contact the developer!", e);
            return Optional.empty();
        }
    }

    @Override
    public Collection<CustomEmoji> getCustomEmojis() {
        return Collections.unmodifiableCollection(customEmojis);
    }

    @Override
    public Collection<ServerChannel> getChannels() {
        return channels.values();
    }

    @Override
    public Optional<ServerChannel> getChannelById(long id) {
        return Optional.ofNullable(channels.get(id));
    }

    @Override
    public String toString() {
        return String.format("Server (id: %s, name: %s)", getId(), getName());
    }

    @Override
    public void addMessageCreateListener(MessageCreateListener listener) {
        addListener(MessageCreateListener.class, listener);
    }

    @Override
    public List<MessageCreateListener> getMessageCreateListeners() {
        return getListeners(MessageCreateListener.class);
    }

    @Override
    public void addServerLeaveListener(ServerLeaveListener listener) {
        addListener(ServerLeaveListener.class, listener);
    }

    @Override
    public List<ServerLeaveListener> getServerLeaveListeners() {
        return getListeners(ServerLeaveListener.class);
    }

    @Override
    public void addServerBecomesUnavailableListener(ServerBecomesUnavailableListener listener) {
        addListener(ServerBecomesUnavailableListener.class, listener);
    }

    @Override
    public List<ServerBecomesUnavailableListener> getServerBecomesUnavailableListeners() {
        return getListeners(ServerBecomesUnavailableListener.class);
    }

    @Override
    public void addUserStartTypingListener(UserStartTypingListener listener) {
        addListener(UserStartTypingListener.class, listener);
    }

    @Override
    public List<UserStartTypingListener> getUserStartTypingListeners() {
        return getListeners(UserStartTypingListener.class);
    }

    @Override
    public void addServerChannelCreateListener(ServerChannelCreateListener listener) {
        addListener(ServerChannelCreateListener.class, listener);
    }

    @Override
    public List<ServerChannelCreateListener> getServerChannelCreateListeners() {
        return getListeners(ServerChannelCreateListener.class);
    }

    @Override
    public void addServerChannelDeleteListener(ServerChannelDeleteListener listener) {
        addListener(ServerChannelDeleteListener.class, listener);
    }

    @Override
    public List<ServerChannelDeleteListener> getServerChannelDeleteListeners() {
        return getListeners(ServerChannelDeleteListener.class);
    }

    @Override
    public void addMessageDeleteListener(MessageDeleteListener listener) {
        addListener(MessageDeleteListener.class, listener);
    }

    @Override
    public List<MessageDeleteListener> getMessageDeleteListeners() {
        return getListeners(MessageDeleteListener.class);
    }

    @Override
    public void addMessageEditListener(MessageEditListener listener) {
        addListener(MessageEditListener.class, listener);
    }

    @Override
    public List<MessageEditListener> getMessageEditListeners() {
        return getListeners(MessageEditListener.class);
    }

    @Override
    public void addReactionAddListener(ReactionAddListener listener) {
        addListener(ReactionAddListener.class, listener);
    }

    @Override
    public List<ReactionAddListener> getReactionAddListeners() {
        return getListeners(ReactionAddListener.class);
    }

    @Override
    public void addReactionRemoveListener(ReactionRemoveListener listener) {
        addListener(ReactionRemoveListener.class, listener);
    }

    @Override
    public List<ReactionRemoveListener> getReactionRemoveListeners() {
        return getListeners(ReactionRemoveListener.class);
    }

    @Override
    public void addServerMemberAddListener(ServerMemberAddListener listener) {
        addListener(ServerMemberAddListener.class, listener);
    }

    @Override
    public List<ServerMemberAddListener> getServerMemberAddListeners() {
        return getListeners(ServerMemberAddListener.class);
    }

    @Override
    public void addServerMemberRemoveListener(ServerMemberRemoveListener listener) {
        addListener(ServerMemberRemoveListener.class, listener);
    }

    @Override
    public List<ServerMemberRemoveListener> getServerMemberRemoveListeners() {
        return getListeners(ServerMemberRemoveListener.class);
    }

    @Override
    public void addServerChangeNameListener(ServerChangeNameListener listener) {
        addListener(ServerChangeNameListener.class, listener);
    }

    @Override
    public List<ServerChangeNameListener> getServerChangeNameListeners() {
        return getListeners(ServerChangeNameListener.class);
    }

    @Override
    public void addServerChannelChangeNameListener(ServerChannelChangeNameListener listener) {
        addListener(ServerChannelChangeNameListener.class, listener);
    }

    @Override
    public List<ServerChannelChangeNameListener> getServerChannelChangeNameListeners() {
        return getListeners(ServerChannelChangeNameListener.class);
    }

    @Override
    public void addServerChannelChangePositionListener(ServerChannelChangePositionListener listener) {
        addListener(ServerChannelChangePositionListener.class, listener);
    }

    @Override
    public List<ServerChannelChangePositionListener> getServerChannelChangePositionListeners() {
        return getListeners(ServerChannelChangePositionListener.class);
    }

    @Override
    public void addCustomEmojiCreateListener(CustomEmojiCreateListener listener) {
        addListener(CustomEmojiCreateListener.class, listener);
    }

    @Override
    public List<CustomEmojiCreateListener> getCustomEmojiCreateListeners() {
        return getListeners(CustomEmojiCreateListener.class);
    }

    @Override
    public void addUserChangeGameListener(UserChangeGameListener listener) {
        addListener(UserChangeGameListener.class, listener);
    }

    @Override
    public List<UserChangeGameListener> getUserChangeGameListeners() {
        return getListeners(UserChangeGameListener.class);
    }

    @Override
    public void addUserChangeStatusListener(UserChangeStatusListener listener) {
        addListener(UserChangeStatusListener.class, listener);
    }

    @Override
    public List<UserChangeStatusListener> getUserChangeStatusListeners() {
        return getListeners(UserChangeStatusListener.class);
    }
}
