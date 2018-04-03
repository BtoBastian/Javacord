package de.btobastian.javacord.entities;

import de.btobastian.javacord.AccountType;
import de.btobastian.javacord.DiscordApi;
import de.btobastian.javacord.ImplDiscordApi;
import de.btobastian.javacord.entities.channels.GroupChannel;
import de.btobastian.javacord.entities.channels.PrivateChannel;
import de.btobastian.javacord.entities.channels.ServerVoiceChannel;
import de.btobastian.javacord.entities.message.Messageable;
import de.btobastian.javacord.entities.permissions.Role;
import de.btobastian.javacord.listeners.ObjectAttachableListener;
import de.btobastian.javacord.listeners.group.channel.GroupChannelChangeNameListener;
import de.btobastian.javacord.listeners.group.channel.GroupChannelCreateListener;
import de.btobastian.javacord.listeners.group.channel.GroupChannelDeleteListener;
import de.btobastian.javacord.listeners.message.MessageCreateListener;
import de.btobastian.javacord.listeners.message.reaction.ReactionAddListener;
import de.btobastian.javacord.listeners.message.reaction.ReactionRemoveListener;
import de.btobastian.javacord.listeners.server.channel.ServerChannelChangeOverwrittenPermissionsListener;
import de.btobastian.javacord.listeners.server.channel.ServerVoiceChannelMemberJoinListener;
import de.btobastian.javacord.listeners.server.channel.ServerVoiceChannelMemberLeaveListener;
import de.btobastian.javacord.listeners.server.member.ServerMemberBanListener;
import de.btobastian.javacord.listeners.server.member.ServerMemberJoinListener;
import de.btobastian.javacord.listeners.server.member.ServerMemberLeaveListener;
import de.btobastian.javacord.listeners.server.member.ServerMemberUnbanListener;
import de.btobastian.javacord.listeners.server.role.UserRoleAddListener;
import de.btobastian.javacord.listeners.server.role.UserRoleRemoveListener;
import de.btobastian.javacord.listeners.user.UserAttachableListener;
import de.btobastian.javacord.listeners.user.UserChangeActivityListener;
import de.btobastian.javacord.listeners.user.UserChangeAvatarListener;
import de.btobastian.javacord.listeners.user.UserChangeNameListener;
import de.btobastian.javacord.listeners.user.UserChangeNicknameListener;
import de.btobastian.javacord.listeners.user.UserChangeStatusListener;
import de.btobastian.javacord.listeners.user.UserStartTypingListener;
import de.btobastian.javacord.listeners.user.channel.PrivateChannelCreateListener;
import de.btobastian.javacord.listeners.user.channel.PrivateChannelDeleteListener;
import de.btobastian.javacord.utils.ClassHelper;
import de.btobastian.javacord.utils.ListenerManager;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * This class represents a user.
 */
public interface User extends DiscordEntity, Messageable, Mentionable {

    @Override
    default String getMentionTag() {
        return "<@" + getId() + ">";
    }

    /**
     * Gets the mention tag, to mention the user with its nickname, instead of its normal name.
     *
     * @return The mention tag, to mention the user with its nickname.
     */
    default String getNicknameMentionTag() {
        return "<@!" + getId() + ">";
    }

    /**
     * Gets the name of the user.
     *
     * @return The name of the user.
     */
    String getName();

    /**
     * Gets the discriminator of the user.
     *
     * @return The discriminator of the user.
     */
    String getDiscriminator();

    /**
     * Checks if the user is a bot account.
     *
     * @return Whether the user is a bot account or not.
     */
    boolean isBot();

    /**
     * Checks if this user is the owner of the current account.
     * Always returns <code>false</code> if logged in to a user account.
     *
     * @return Whether this user is the owner of the current account.
     */
    default boolean isBotOwner() {
        return getApi().getAccountType() == AccountType.BOT && getApi().getOwnerId() == getId();
    }

    /**
     * Gets the activity of the user.
     *
     * @return The activity of the user.
     */
    Optional<Activity> getActivity();

    /**
     * Gets the server voice channels the user is connected to.
     *
     * @return The server voice channels the user is connected to.
     */
    Collection<ServerVoiceChannel> getConnectedVoiceChannels();

    /**
     * Gets the voice channel this user is connected to on the given server if any.
     *
     * @param server The server to check.
     * @return The server voice channel the user is connected to.
     */
    default Optional<ServerVoiceChannel> getConnectedVoiceChannel(Server server) {
        return server.getConnectedVoiceChannel(this);
    }

    /**
     * Gets the status of the user.
     *
     * @return The status of the user.
     */
    UserStatus getStatus();

    /**
     * Gets the avatar of the user.
     *
     * @return The avatar of the user.
     */
    Icon getAvatar();

    /**
     * Gets if the user has a default Discord avatar.
     *
     * @return Whether this user has a default avatar or not.
     */
    boolean hasDefaultAvatar();

    /**
     * Returns if this User is the same as the given User, going after ID
     *
     * @param user The User to Compare to
     * @return Whether the Users are the same.
     */
    default boolean equals(User user) {
        return (this.getId() == user.getId());
    }

    /**
     * Gets all mutual servers with this user.
     *
     * @return All mutual servers with this user.
     */
    default Collection<Server> getMutualServers() {
        // TODO This is probably not the most efficient way to do it
        return getApi().getServers().stream()
                .filter(server -> server.getMembers().contains(this))
                .collect(Collectors.toList());
    }

    /**
     * Gets the display name of the user.
     * If the user has a nickname, it will return the nickname, otherwise it will return the "normal" name.
     *
     * @param server The server.
     * @return The display name of the user.
     */
    default String getDisplayName(Server server) {
        return server.getNickname(this).orElseGet(this::getName);
    }

    /**
     * Gets the discriminated name of the user, e. g. {@code Bastian#8222}.
     *
     * @return The discriminated name of the user.
     */
    default String getDiscriminatedName() {
        return getName() + "#" + getDiscriminator();
    }

    /**
     * Changes the nickname of the user in the given server.
     *
     * @param server The server.
     * @param nickname The new nickname of the user.
     * @return A future to check if the update was successful.
     */
    default CompletableFuture<Void> updateNickname(Server server, String nickname) {
        return server.updateNickname(this, nickname);
    }

    /**
     * Removes the nickname of the user in the given server.
     *
     * @param server The server.
     * @return A future to check if the update was successful.
     */
    default CompletableFuture<Void> resetNickname(Server server) {
        return server.resetNickname(this);
    }

    /**
     * Gets the nickname of the user in the given server.
     *
     * @param server The server to check.
     * @return The nickname of the user.
     */
    default Optional<String> getNickname(Server server) {
        return server.getNickname(this);
    }

    /**
     * Gets the timestamp of when the user joined the given server.
     *
     * @param server The server to check.
     * @return The timestamp of when the user joined the server.
     */
    default Optional<Instant> getJoinedAtTimestamp(Server server) {
        return server.getJoinedAtTimestamp(this);
    }

    /**
     * Gets a sorted list (by position) with all roles of the user in the given server.
     *
     * @param server The server.
     * @return A sorted list (by position) with all roles of the user in the given server.
     * @see Server#getRolesOf(User)
     */
    default List<Role> getRoles(Server server) {
        return server.getRolesOf(this);
    }

    /**
     * Gets if this user is the user of the connected account.
     *
     * @return Whether this user is the user of the connected account or not.
     * @see DiscordApi#getYourself()
     */
    default boolean isYourself() {
        return getId() == getApi().getYourself().getId();
    }

    /**
     * Gets the private channel with the user.
     * This will only be present, if there was an conversation with the user in the past or you manually opened a
     * private channel with the given user, using {@link #openPrivateChannel()}.
     *
     * @return The private channel with the user.
     */
    Optional<PrivateChannel> getPrivateChannel();

    /**
     * Opens a new private channel with the given user.
     * If there's already a private channel with the user, it will just return the one which already exists.
     *
     * @return The new (or old) private channel with the user.
     */
    CompletableFuture<PrivateChannel> openPrivateChannel();

    /**
     * Gets the currently existing group channels with the user.
     *
     * @return The group channels with the user.
     */
    default Collection<GroupChannel> getGroupChannels() {
        return getApi().getGroupChannels().stream()
                .filter(groupChannel -> groupChannel.getMembers().contains(this))
                .collect(Collectors.toList());
    }

    /**
     * Adds a listener, which listens to private channel creations for this user.
     *
     * @param listener The listener to add.
     * @return The manager of the listener.
     */
    default ListenerManager<PrivateChannelCreateListener> addPrivateChannelCreateListener(
            PrivateChannelCreateListener listener) {
        return ((ImplDiscordApi) getApi()).addObjectListener(
                User.class, getId(), PrivateChannelCreateListener.class, listener);
    }

    /**
     * Gets a list with all registered private channel create listeners.
     *
     * @return A list with all registered private channel create listeners.
     */
    default List<PrivateChannelCreateListener> getPrivateChannelCreateListeners() {
        return ((ImplDiscordApi) getApi()).getObjectListeners(User.class, getId(), PrivateChannelCreateListener.class);
    }

    /**
     * Adds a listener, which listens to private channel deletions for this user.
     *
     * @param listener The listener to add.
     * @return The manager of the listener.
     */
    default ListenerManager<PrivateChannelDeleteListener> addPrivateChannelDeleteListener(
            PrivateChannelDeleteListener listener) {
        return ((ImplDiscordApi) getApi()).addObjectListener(
                User.class, getId(), PrivateChannelDeleteListener.class, listener);
    }

    /**
     * Gets a list with all registered private channel delete listeners.
     *
     * @return A list with all registered private channel delete listeners.
     */
    default List<PrivateChannelDeleteListener> getPrivateChannelDeleteListeners() {
        return ((ImplDiscordApi) getApi()).getObjectListeners(User.class, getId(), PrivateChannelDeleteListener.class);
    }

    /**
     * Adds a listener, which listens to group channel creations for this user.
     *
     * @param listener The listener to add.
     * @return The manager of the listener.
     */
    default ListenerManager<GroupChannelCreateListener> addGroupChannelCreateListener(
            GroupChannelCreateListener listener) {
        return ((ImplDiscordApi) getApi()).addObjectListener(
                User.class, getId(), GroupChannelCreateListener.class, listener);
    }

    /**
     * Gets a list with all registered group channel create listeners.
     *
     * @return A list with all registered group channel create listeners.
     */
    default List<GroupChannelCreateListener> getGroupChannelCreateListeners() {
        return ((ImplDiscordApi) getApi()).getObjectListeners(User.class, getId(), GroupChannelCreateListener.class);
    }

    /**
     * Adds a listener, which listens to group channel name changes for this user.
     *
     * @param listener The listener to add.
     * @return The manager of the listener.
     */
    default ListenerManager<GroupChannelChangeNameListener> addGroupChannelChangeNameListener(
            GroupChannelChangeNameListener listener) {
        return ((ImplDiscordApi) getApi()).addObjectListener(
                User.class, getId(), GroupChannelChangeNameListener.class, listener);
    }

    /**
     * Gets a list with all registered group channel change name listeners.
     *
     * @return A list with all registered group channel change name listeners.
     */
    default List<GroupChannelChangeNameListener> getGroupChannelChangeNameListeners() {
        return ((ImplDiscordApi) getApi()).getObjectListeners(
                User.class, getId(), GroupChannelChangeNameListener.class);
    }

    /**
     * Adds a listener, which listens to group channel deletions for this user.
     *
     * @param listener The listener to add.
     * @return The manager of the listener.
     */
    default ListenerManager<GroupChannelDeleteListener> addGroupChannelDeleteListener(
            GroupChannelDeleteListener listener) {
        return ((ImplDiscordApi) getApi()).addObjectListener(
                User.class, getId(), GroupChannelDeleteListener.class, listener);
    }

    /**
     * Gets a list with all registered group channel delete listeners.
     *
     * @return A list with all registered group channel delete listeners.
     */
    default List<GroupChannelDeleteListener> getGroupChannelDeleteListeners() {
        return ((ImplDiscordApi) getApi()).getObjectListeners(User.class, getId(), GroupChannelDeleteListener.class);
    }

    /**
     * Adds a listener, which listens to message creates from this user.
     *
     * @param listener The listener to add.
     * @return The manager of the listener.
     */
    default ListenerManager<MessageCreateListener> addMessageCreateListener(MessageCreateListener listener) {
        return ((ImplDiscordApi) getApi())
                .addObjectListener(User.class, getId(), MessageCreateListener.class, listener);
    }

    /**
     * Gets a list with all registered message create listeners.
     *
     * @return A list with all registered message create listeners.
     */
    default List<MessageCreateListener> getMessageCreateListeners() {
        return ((ImplDiscordApi) getApi()).getObjectListeners(User.class, getId(), MessageCreateListener.class);
    }

    /**
     * Adds a listener, which listens to this user starting to type.
     *
     * @param listener The listener to add.
     * @return The manager of the listener.
     */
    default ListenerManager<UserStartTypingListener> addUserStartTypingListener(UserStartTypingListener listener) {
        return ((ImplDiscordApi) getApi())
                .addObjectListener(User.class, getId(), UserStartTypingListener.class, listener);
    }

    /**
     * Gets a list with all registered user starts typing listeners.
     *
     * @return A list with all registered user starts typing listeners.
     */
    default List<UserStartTypingListener> getUserStartTypingListeners() {
        return ((ImplDiscordApi) getApi()).getObjectListeners(User.class, getId(), UserStartTypingListener.class);
    }

    /**
     * Adds a listener, which listens to reactions being added by this user.
     *
     * @param listener The listener to add.
     * @return The manager of the listener.
     */
    default ListenerManager<ReactionAddListener> addReactionAddListener(ReactionAddListener listener) {
        return ((ImplDiscordApi) getApi()).addObjectListener(User.class, getId(), ReactionAddListener.class, listener);
    }

    /**
     * Gets a list with all registered reaction add listeners.
     *
     * @return A list with all registered reaction add listeners.
     */
    default List<ReactionAddListener> getReactionAddListeners() {
        return ((ImplDiscordApi) getApi()).getObjectListeners(User.class, getId(), ReactionAddListener.class);
    }

    /**
     * Adds a listener, which listens to reactions being removed by this user.
     *
     * @param listener The listener to add.
     * @return The manager of the listener.
     */
    default ListenerManager<ReactionRemoveListener> addReactionRemoveListener(ReactionRemoveListener listener) {
        return ((ImplDiscordApi) getApi())
                .addObjectListener(User.class, getId(), ReactionRemoveListener.class, listener);
    }

    /**
     * Gets a list with all registered reaction remove listeners.
     *
     * @return A list with all registered reaction remove listeners.
     */
    default List<ReactionRemoveListener> getReactionRemoveListeners() {
        return ((ImplDiscordApi) getApi()).getObjectListeners(User.class, getId(), ReactionRemoveListener.class);
    }

    /**
     * Adds a listener, which listens to this user joining known servers.
     *
     * @param listener The listener to add.
     * @return The manager of the listener.
     */
    default ListenerManager<ServerMemberJoinListener> addServerMemberJoinListener(ServerMemberJoinListener listener) {
        return ((ImplDiscordApi) getApi())
                .addObjectListener(User.class, getId(), ServerMemberJoinListener.class, listener);
    }

    /**
     * Gets a list with all registered server member join listeners.
     *
     * @return A list with all registered server member join listeners.
     */
    default List<ServerMemberJoinListener> getServerMemberJoinListeners() {
        return ((ImplDiscordApi) getApi()).getObjectListeners(User.class, getId(), ServerMemberJoinListener.class);
    }

    /**
     * Adds a listener, which listens to this user leaving known servers.
     *
     * @param listener The listener to add.
     * @return The manager of the listener.
     */
    default ListenerManager<ServerMemberLeaveListener> addServerMemberLeaveListener(
            ServerMemberLeaveListener listener) {
        return ((ImplDiscordApi) getApi())
                .addObjectListener(User.class, getId(), ServerMemberLeaveListener.class, listener);
    }

    /**
     * Gets a list with all registered server member leave listeners.
     *
     * @return A list with all registered server member leave listeners.
     */
    default List<ServerMemberLeaveListener> getServerMemberLeaveListeners() {
        return ((ImplDiscordApi) getApi()).getObjectListeners(User.class, getId(), ServerMemberLeaveListener.class);
    }

    /**
     * Adds a listener, which listens to this user getting banned from known servers.
     *
     * @param listener The listener to add.
     * @return The manager of the listener.
     */
    default ListenerManager<ServerMemberBanListener> addServerMemberBanListener(ServerMemberBanListener listener) {
        return ((ImplDiscordApi) getApi())
                .addObjectListener(User.class, getId(), ServerMemberBanListener.class, listener);
    }

    /**
     * Gets a list with all registered server member ban listeners.
     *
     * @return A list with all registered server member ban listeners.
     */
    default List<ServerMemberBanListener> getServerMemberBanListeners() {
        return ((ImplDiscordApi) getApi()).getObjectListeners(User.class, getId(), ServerMemberBanListener.class);
    }

    /**
     * Adds a listener, which listens to this user getting unbanned from known servers.
     *
     * @param listener The listener to add.
     * @return The manager of the listener.
     */
    default ListenerManager<ServerMemberUnbanListener> addServerMemberUnbanListener(ServerMemberUnbanListener listener) {
        return ((ImplDiscordApi) getApi())
                .addObjectListener(User.class, getId(), ServerMemberUnbanListener.class, listener);
    }

    /**
     * Gets a list with all registered server member unban listeners.
     *
     * @return A list with all registered server member unban listeners.
     */
    default List<ServerMemberUnbanListener> getServerMemberUnbanListeners() {
        return ((ImplDiscordApi) getApi()).getObjectListeners(User.class, getId(), ServerMemberUnbanListener.class);
    }

    /**
     * Adds a listener, which listens to this user's activity changes.
     *
     * @param listener The listener to add.
     * @return The manager of the listener.
     */
    default ListenerManager<UserChangeActivityListener> addUserChangeActivityListener(UserChangeActivityListener listener) {
        return ((ImplDiscordApi) getApi())
                .addObjectListener(User.class, getId(), UserChangeActivityListener.class, listener);
    }

    /**
     * Gets a list with all registered user change activity listeners.
     *
     * @return A list with all registered user change activity listeners.
     */
    default List<UserChangeActivityListener> getUserChangeActivityListeners() {
        return ((ImplDiscordApi) getApi()).getObjectListeners(User.class, getId(), UserChangeActivityListener.class);
    }

    /**
     * Adds a listener, which listens to this user's status changes.
     *
     * @param listener The listener to add.
     * @return The manager of the listener.
     */
    default ListenerManager<UserChangeStatusListener> addUserChangeStatusListener(UserChangeStatusListener listener) {
        return ((ImplDiscordApi) getApi())
                .addObjectListener(User.class, getId(), UserChangeStatusListener.class, listener);
    }

    /**
     * Gets a list with all registered user change status listeners.
     *
     * @return A list with all registered user change status listeners.
     */
    default List<UserChangeStatusListener> getUserChangeStatusListeners() {
        return ((ImplDiscordApi) getApi()).getObjectListeners(User.class, getId(), UserChangeStatusListener.class);
    }

    /**
     * Adds a listener, which listens to overwritten permission changes of this user.
     *
     * @param listener The listener to add.
     * @return The manager of the listener.
     */
    default ListenerManager<ServerChannelChangeOverwrittenPermissionsListener>
    addServerChannelChangeOverwrittenPermissionsListener(ServerChannelChangeOverwrittenPermissionsListener listener) {
        return ((ImplDiscordApi) getApi()).addObjectListener(
                User.class, getId(), ServerChannelChangeOverwrittenPermissionsListener.class, listener);
    }

    /**
     * Gets a list with all registered server channel change overwritten permissions listeners.
     *
     * @return A list with all registered server channel change overwritten permissions listeners.
     */
    default List<ServerChannelChangeOverwrittenPermissionsListener>
            getServerChannelChangeOverwrittenPermissionsListeners() {
        return ((ImplDiscordApi) getApi()).getObjectListeners(
                User.class, getId(), ServerChannelChangeOverwrittenPermissionsListener.class);
    }

    /**
     * Adds a listener, which listens to nickname changes of this user.
     *
     * @param listener The listener to add.
     * @return The manager of the listener.
     */
    default ListenerManager<UserChangeNicknameListener> addUserChangeNicknameListener(
            UserChangeNicknameListener listener) {
        return ((ImplDiscordApi) getApi())
                .addObjectListener(User.class, getId(), UserChangeNicknameListener.class, listener);
    }

    /**
     * Gets a list with all registered user change nickname listeners.
     *
     * @return A list with all registered user change nickname listeners.
     */
    default List<UserChangeNicknameListener> getUserChangeNicknameListeners() {
        return ((ImplDiscordApi) getApi()).getObjectListeners(User.class, getId(), UserChangeNicknameListener.class);
    }

    /**
     * Adds a listener, which listens to this user being added to roles.
     *
     * @param listener The listener to add.
     * @return The manager of the listener.
     */
    default ListenerManager<UserRoleAddListener> addUserRoleAddListener(UserRoleAddListener listener) {
        return ((ImplDiscordApi) getApi()).addObjectListener(User.class, getId(), UserRoleAddListener.class, listener);
    }

    /**
     * Gets a list with all registered user role add listeners.
     *
     * @return A list with all registered user role add listeners.
     */
    default List<UserRoleAddListener> getUserRoleAddListeners() {
        return ((ImplDiscordApi) getApi()).getObjectListeners(User.class, getId(), UserRoleAddListener.class);
    }

    /**
     * Adds a listener, which listens to this user being removed from roles in this server.
     *
     * @param listener The listener to add.
     * @return The manager of the listener.
     */
    default ListenerManager<UserRoleRemoveListener> addUserRoleRemoveListener(UserRoleRemoveListener listener) {
        return ((ImplDiscordApi) getApi())
                .addObjectListener(User.class, getId(), UserRoleRemoveListener.class, listener);
    }

    /**
     * Gets a list with all registered user role remove listeners.
     *
     * @return A list with all registered user role remove listeners.
     */
    default List<UserRoleRemoveListener> getUserRoleRemoveListeners() {
        return ((ImplDiscordApi) getApi()).getObjectListeners(User.class, getId(), UserRoleRemoveListener.class);
    }

    /**
     * Adds a listener, which listens to name changes of this user.
     *
     * @param listener The listener to add.
     * @return The manager of the listener.
     */
    default ListenerManager<UserChangeNameListener> addUserChangeNameListener(UserChangeNameListener listener) {
        return ((ImplDiscordApi) getApi())
                .addObjectListener(User.class, getId(), UserChangeNameListener.class, listener);
    }

    /**
     * Gets a list with all registered user change name listeners.
     *
     * @return A list with all registered user change name listeners.
     */
    default List<UserChangeNameListener> getUserChangeNameListeners() {
        return ((ImplDiscordApi) getApi()).getObjectListeners(User.class, getId(), UserChangeNameListener.class);
    }

    /**
     * Adds a listener, which listens to avatar changes of this user.
     *
     * @param listener The listener to add.
     * @return The manager of the listener.
     */
    default ListenerManager<UserChangeAvatarListener> addUserChangeAvatarListener(UserChangeAvatarListener listener) {
        return ((ImplDiscordApi) getApi())
                .addObjectListener(User.class, getId(), UserChangeAvatarListener.class, listener);
    }

    /**
     * Gets a list with all registered user change avatar listeners.
     *
     * @return A list with all registered user change avatar listeners.
     */
    default List<UserChangeAvatarListener> getUserChangeAvatarListeners() {
        return ((ImplDiscordApi) getApi()).getObjectListeners(User.class, getId(), UserChangeAvatarListener.class);
    }

    /**
     * Adds a listener, which listens to this user joining a server voice channel.
     *
     * @param listener The listener to add.
     * @return The manager of the listener.
     */
    default ListenerManager<ServerVoiceChannelMemberJoinListener> addServerVoiceChannelMemberJoinListener(
            ServerVoiceChannelMemberJoinListener listener) {
        return ((ImplDiscordApi) getApi())
                .addObjectListener(User.class, getId(), ServerVoiceChannelMemberJoinListener.class, listener);
    }

    /**
     * Gets a list with all registered server voice channel member join listeners.
     *
     * @return A list with all registered server voice channel member join listeners.
     */
    default List<ServerVoiceChannelMemberJoinListener> getServerVoiceChannelMemberJoinListeners() {
        return ((ImplDiscordApi) getApi()).getObjectListeners(
                User.class, getId(), ServerVoiceChannelMemberJoinListener.class);
    }

    /**
     * Adds a listener, which listens to this user leaving a server voice channel.
     *
     * @param listener The listener to add.
     * @return The manager of the listener.
     */
    default ListenerManager<ServerVoiceChannelMemberLeaveListener> addServerVoiceChannelMemberLeaveListener(
            ServerVoiceChannelMemberLeaveListener listener) {
        return ((ImplDiscordApi) getApi())
                .addObjectListener(User.class, getId(), ServerVoiceChannelMemberLeaveListener.class, listener);
    }

    /**
     * Gets a list with all registered server voice channel member leave listeners.
     *
     * @return A list with all registered server voice channel member leave listeners.
     */
    default List<ServerVoiceChannelMemberLeaveListener> getServerVoiceChannelMemberLeaveListeners() {
        return ((ImplDiscordApi) getApi()).getObjectListeners(
                User.class, getId(), ServerVoiceChannelMemberLeaveListener.class);
    }

    /**
     * Adds a listener that implements one or more {@code UserAttachableListener}s.
     * Adding a listener multiple times will only add it once
     * and return the same listener managers on each invocation.
     * The order of invocation is according to first addition.
     *
     * @param listener The listener to add.
     * @param <T> The type of the listener.
     * @return The managers for the added listener.
     */
    @SuppressWarnings("unchecked")
    default <T extends UserAttachableListener & ObjectAttachableListener> Collection<ListenerManager<T>>
    addUserAttachableListener(T listener) {
        return ClassHelper.getInterfacesAsStream(listener.getClass())
                .filter(UserAttachableListener.class::isAssignableFrom)
                .filter(ObjectAttachableListener.class::isAssignableFrom)
                .map(listenerClass -> (Class<T>) listenerClass)
                .map(listenerClass -> ((ImplDiscordApi) getApi()).addObjectListener(User.class, getId(),
                                                                                    listenerClass, listener))
                .collect(Collectors.toList());
    }

    /**
     * Removes a listener that implements one or more {@code UserAttachableListener}s.
     *
     * @param listener The listener to remove.
     * @param <T> The type of the listener.
     */
    @SuppressWarnings("unchecked")
    default <T extends UserAttachableListener & ObjectAttachableListener> void removeUserAttachableListener(
            T listener) {
        ClassHelper.getInterfacesAsStream(listener.getClass())
                .filter(UserAttachableListener.class::isAssignableFrom)
                .filter(ObjectAttachableListener.class::isAssignableFrom)
                .map(listenerClass -> (Class<T>) listenerClass)
                .forEach(listenerClass -> ((ImplDiscordApi) getApi()).removeObjectListener(User.class, getId(),
                                                                                           listenerClass, listener));
    }

    /**
     * Gets a map with all registered listeners that implement one or more {@code UserAttachableListener}s and their
     * assigned listener classes they listen to.
     *
     * @param <T> The type of the listeners.
     * @return A map with all registered listeners that implement one or more {@code UserAttachableListener}s and their
     * assigned listener classes they listen to.
     */
    default <T extends UserAttachableListener & ObjectAttachableListener> Map<T, List<Class<T>>>
    getUserAttachableListeners() {
        return ((ImplDiscordApi) getApi()).getObjectListeners(User.class, getId());
    }

    /**
     * Removes a listener from this user.
     *
     * @param listenerClass The listener class.
     * @param listener The listener to remove.
     * @param <T> The type of the listener.
     */
    default <T extends UserAttachableListener & ObjectAttachableListener> void removeListener(
            Class<T> listenerClass, T listener) {
        ((ImplDiscordApi) getApi()).removeObjectListener(User.class, getId(), listenerClass, listener);
    }

}
