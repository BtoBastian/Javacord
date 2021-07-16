package org.javacord.api.entity.server;

import org.javacord.api.audio.AudioConnection;
import org.javacord.api.entity.DiscordEntity;
import org.javacord.api.entity.Icon;
import org.javacord.api.entity.Nameable;
import org.javacord.api.entity.Region;
import org.javacord.api.entity.UpdatableFromCache;
import org.javacord.api.entity.VanityUrlCode;
import org.javacord.api.entity.auditlog.AuditLog;
import org.javacord.api.entity.auditlog.AuditLogActionType;
import org.javacord.api.entity.auditlog.AuditLogEntry;
import org.javacord.api.entity.channel.ChannelCategory;
import org.javacord.api.entity.channel.ChannelCategoryBuilder;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.channel.ServerStageVoiceChannel;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.channel.ServerTextChannelBuilder;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.channel.ServerVoiceChannelBuilder;
import org.javacord.api.entity.emoji.CustomEmojiBuilder;
import org.javacord.api.entity.emoji.KnownCustomEmoji;
import org.javacord.api.entity.permission.PermissionState;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.permission.Permissions;
import org.javacord.api.entity.permission.PermissionsBuilder;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.permission.RoleBuilder;
import org.javacord.api.entity.server.invite.RichInvite;
import org.javacord.api.entity.user.User;
import org.javacord.api.entity.webhook.IncomingWebhook;
import org.javacord.api.entity.webhook.Webhook;
import org.javacord.api.interaction.SlashCommand;
import org.javacord.api.listener.server.ServerAttachableListenerManager;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * The class represents a Discord server, sometimes also called guild.
 */
public interface Server extends DiscordEntity, Nameable, UpdatableFromCache<Server>, ServerAttachableListenerManager {

    /**
     * Gets the audio connection in this server.
     *
     * @return The audio connection in this server.
     */
    Optional<AudioConnection> getAudioConnection();

    /**
     * Checks if the server has boost messages enabled.
     *
     * @return Whether the the server has boost messages enabled or not.
     */
    boolean hasBoostMessagesEnabled();

    /**
     * Checks if the server has join messages enabled.
     *
     * @return Whether the the server has join messages enabled or not.
     */
    boolean hasJoinMessagesEnabled();

    /**
     * Gets the features of the server.
     *
     * @return The server's available features.
     */
    Collection<ServerFeature> getFeatures();

    /**
     * Gets the boost level of the server.
     *
     * @return The boost level.
     */
    BoostLevel getBoostLevel();

    /**
     * Gets the boost count of the server.
     *
     * @return The boost count.
     */
    int getBoostCount();

    /**
     * Gets the rules channel.
     *
     * <p>Rule channels are only available for public servers.
     * You can check if a server is public using the {@link #getFeatures()} methods.
     *
     * @return The rules channel.
     */
    Optional<ServerTextChannel> getRulesChannel();

    /**
     * Gets the description of the server.
     *
     * @return The description.
     */
    Optional<String> getDescription();

    /**
     * Gets the NSFW level of the server.
     *
     * @return The NSFW level of the server.
     */
    NsfwLevel getNsfwLevel();

    /**
     * Gets the moderators-only channel (sometimes also called "public updates channel").
     *
     * <p>This is the channel where Discord will send announcements and updates relevant
     * to Public server admins and moderators, like new moderation features and the
     * server's eligibility in Discovery.
     *
     * <p>Moderator-only channels are only available for public servers.
     * You can check if a server is public using the {@link #getFeatures()} method.
     *
     * @return The moderators-only channel.
     */
    Optional<ServerTextChannel> getModeratorsOnlyChannel();

    /**
     * Gets the vanity url code of the server.
     *
     * @return The vanity url code.
     */
    Optional<VanityUrlCode> getVanityUrlCode();

    /**
     * Gets the discovery splash of the server.
     *
     * @return The discovery splash.
     */
    Optional<Icon> getDiscoverySplash();

    /**
     * Gets the server's preferred locale.
     *
     * <p>Discord will prioritize this server in Discovery to users who speak
     * the selected language. Updates sent from Discord in the Moderators-only
     * channel will also be in this language.
     *
     * <p>Setting a preferred locale is only available for public servers.
     * You can check if a server is public using the {@link #getFeatures()} methods.
     * @return The sever's preferred locale.
     */
    Locale getPreferredLocale();

    /**
     * Gets the region of the server.
     *
     * @return The region of the server.
     */
    Region getRegion();

    /**
     * Gets the nickname of a user.
     *
     * @param user The user to check.
     * @return The nickname of the user.
     */
    Optional<String> getNickname(User user);

    /**
     * Gets the pending state of the user.
     *
     * <p>Membership screening pending state is only available for servers that have
     * verification gate enabled, this will default to false if it isn't.
     * You can check if a server has membership verification gate enabled
     * using the {@link #getFeatures()} method if it has
     * {@link org.javacord.api.entity.server.ServerFeature#MEMBER_VERIFICATION_GATE_ENABLED}.
     *
     * @param userId The id of the user to check.
     * @return Whether the user has passed membership screening
     */
    boolean isPending(long userId);

    /**
     * Gets your self-muted state.
     *
     * @return Whether you are self-muted.
     */
    default boolean areYouSelfMuted() {
        return isSelfMuted(getApi().getYourself());
    }

    /**
     * Gets the self-muted state of the user with the given id.
     *
     * @param userId The id of the user to check.
     * @return Whether the user with the given id is self-muted.
     */
    boolean isSelfMuted(long userId);

    /**
     * Gets the self-muted state of the given user.
     *
     * @param user The user to check.
     * @return Whether the given user is self-muted.
     */
    default boolean isSelfMuted(User user) {
        return isSelfMuted(user.getId());
    }

    /**
     * Gets your self-deafened state.
     *
     * @return Whether you are self-deafened.
     */
    default boolean areYouSelfDeafened() {
        return isSelfDeafened(getApi().getYourself());
    }

    /**
     * Gets the self-deafened state of the user with the given id.
     *
     * @param userId The id of the user to check.
     * @return Whether the user with the given id is self-deafened.
     */
    boolean isSelfDeafened(long userId);

    /**
     * Gets the self-deafened state of the given user.
     *
     * @param user The user to check.
     * @return Whether the given user is self-deafened.
     */
    default boolean isSelfDeafened(User user) {
        return isSelfDeafened(user.getId());
    }

    /**
     * Gets your muted state.
     *
     * @return Whether you are muted.
     */
    default boolean areYouMuted() {
        return isMuted(getApi().getYourself());
    }

    /**
     * Gets the muted state of the user with the given id.
     *
     * @param userId The id of the user to check.
     * @return Whether the user with the given id is muted.
     */
    boolean isMuted(long userId);

    /**
     * Gets the muted state of the given user.
     *
     * @param user The user to check.
     * @return Whether the given user is muted.
     */
    default boolean isMuted(User user) {
        return isMuted(user.getId());
    }

    /**
     * Gets your deafened state.
     *
     * @return Whether you are deafened.
     */
    default boolean areYouDeafened() {
        return isDeafened(getApi().getYourself());
    }

    /**
     * Gets the deafened state of the user with the given id.
     *
     * @param userId The id of the user to check.
     * @return Whether the user with the given id is deafened.
     */
    boolean isDeafened(long userId);

    /**
     * Gets the deafened state of the given user.
     *
     * @param user The user to check.
     * @return Whether the given user is deafened.
     */
    default boolean isDeafened(User user) {
        return isDeafened(user.getId());
    }

    /**
     * Gets the display name of the user on this server.
     * If the user has a nickname, it will return the nickname, otherwise it will return the "normal" name.
     *
     * @param user The user.
     * @return The display name of the user on this server.
     */
    default String getDisplayName(User user) {
        return user.getDisplayName(this);
    }

    /**
     * Gets the timestamp of when a user joined the server.
     *
     * @param user The user to check.
     * @return The timestamp of when the user joined the server.
     */
    Optional<Instant> getJoinedAtTimestamp(User user);

    /**
     * Checks if the server is considered large.
     *
     * @return Whether the server is large or not.
     */
    boolean isLarge();

    /**
     * Gets the amount of members in this server.
     *
     * @return The amount of members in this server.
     */
    int getMemberCount();

    /**
     * Gets the owner of the server.
     *
     * @return The owner of the server.
     */
    Optional<User> getOwner();

    /**
     * Gets the owner of the server.
     *
     * <p>If the owner is in the cache, the owner is served from the cache.
     *
     * @return The owner of the server.
     */
    default CompletableFuture<User> requestOwner() {
        return getApi().getUserById(getOwnerId());
    }

    /**
     * Gets the id of the server's owner.
     *
     * @return The owner's id.
     */
    long getOwnerId();

    /**
     * Gets the application id of the server's owner.
     * The application id is only present for bot-created servers.
     *
     * @return The application id of the server's owner.
     */
    Optional<Long> getApplicationId();

    /**
     * Gets the verification level of the server.
     *
     * @return The verification level of the server.
     */
    VerificationLevel getVerificationLevel();

    /**
     * Gets the explicit content filter level of the server.
     *
     * @return The explicit content filter level of the server.
     */
    ExplicitContentFilterLevel getExplicitContentFilterLevel();

    /**
     * Gets the default message notification level of the server.
     *
     * @return The default message notification level of the server.
     */
    DefaultMessageNotificationLevel getDefaultMessageNotificationLevel();

    /**
     * Gets the multi factor authentication level of the server.
     *
     * @return The multi factor authentication level of the server.
     */
    MultiFactorAuthenticationLevel getMultiFactorAuthenticationLevel();

    /**
     * Gets the icon of the server.
     *
     * @return The icon of the server.
     */
    Optional<Icon> getIcon();

    /**
     * Gets the splash of the server.
     *
     * @return The splash of the server.
     */
    Optional<Icon> getSplash();

    /**
     * Gets the system channel of the server.
     *
     * @return The system channel of the server.
     */
    Optional<ServerTextChannel> getSystemChannel();

    /**
     * Gets the afk channel of the server.
     *
     * @return The afk channel of the server.
     */
    Optional<ServerVoiceChannel> getAfkChannel();

    /**
     * Gets the afk timeout in seconds of the server.
     *
     * @return The afk timeout in seconds of the server.
     */
    int getAfkTimeoutInSeconds();

    /**
     * Gets the amount of members without a role which were inactive at least the given amount of days.
     *
     * @param days The amount of days the member has to be inactive.
     * @return The amount of member who would get kicked.
     */
    CompletableFuture<Integer> getPruneCount(int days);

    /**
     * Kicks all members without a role which were inactive at least the given amount of days.
     *
     * @param days The amount of days the member has to be inactive.
     * @return The amount of member who got kicked.
     */
    default CompletableFuture<Integer> pruneMembers(int days) {
        return pruneMembers(days, null);
    }

    /**
     * Kicks all members without a role which were inactive at least the given amount of days.
     *
     * @param days The amount of days the member has to be inactive.
     * @param reason The audit log reason for the prune.
     * @return The amount of member who got kicked.
     */
    CompletableFuture<Integer> pruneMembers(int days, String reason);

    /**
     * Gets the invites of the server.
     *
     * @return The invites of the server.
     */
    CompletableFuture<Collection<RichInvite>> getInvites();

    /**
     * Checks if all members of the server are in the cache.
     *
     * @return Whether or not all members of the server are in the cache.
     */
    boolean hasAllMembersInCache();

    /**
     * Gets a collection with all members of the server.
     *
     * @return A collection with all members of the server.
     */
    Collection<User> getMembers();

    /**
     * Gets a member by its id.
     *
     * @param id The id of the member.
     * @return The member with the given id.
     */
    Optional<User> getMemberById(long id);

    /**
     * Gets a member by its id.
     *
     * @param id The id of the member.
     * @return The member with the given id.
     */
    default Optional<User> getMemberById(String id) {
        try {
            return getMemberById(Long.parseLong(id));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    /**
     * Gets a member by its discriminated name like e. g. {@code Bastian#8222}.
     * This method is case sensitive!
     *
     * @param discriminatedName The discriminated name of the member.
     * @return The member with the given discriminated name.
     */
    default Optional<User> getMemberByDiscriminatedName(String discriminatedName) {
        String[] nameAndDiscriminator = discriminatedName.split("#", 2);
        return (nameAndDiscriminator.length > 1)
                ? getMemberByNameAndDiscriminator(nameAndDiscriminator[0], nameAndDiscriminator[1])
                : Optional.empty();
    }

    /**
     * Gets a member by its discriminated name like e. g. {@code Bastian#8222}.
     * This method is case insensitive!
     *
     * @param discriminatedName The discriminated name of the member.
     * @return The member with the given discriminated name.
     */
    default Optional<User> getMemberByDiscriminatedNameIgnoreCase(String discriminatedName) {
        String[] nameAndDiscriminator = discriminatedName.split("#", 2);
        return (nameAndDiscriminator.length > 1)
                ? getMemberByNameAndDiscriminatorIgnoreCase(nameAndDiscriminator[0], nameAndDiscriminator[1])
                : Optional.empty();
    }

    /**
     * Gets a member by its name and discriminator.
     * This method is case sensitive!
     *
     * @param name The name of the member.
     * @param discriminator The discriminator of the member.
     * @return The member with the given name and discriminator.
     */
    default Optional<User> getMemberByNameAndDiscriminator(String name, String discriminator) {
        return getMembersByName(name).stream()
                .filter(user -> user.getDiscriminator().equals(discriminator))
                .findAny();
    }

    /**
     * Gets a member by its name and discriminator.
     * This method is case insensitive!
     *
     * @param name The name of the member.
     * @param discriminator The discriminator of the member.
     * @return The member with the given name and discriminator.
     */
    default Optional<User> getMemberByNameAndDiscriminatorIgnoreCase(String name, String discriminator) {
        return getMembersByNameIgnoreCase(name).stream()
                .filter(user -> user.getDiscriminator().equalsIgnoreCase(discriminator))
                .findAny();
    }

    /**
     * Gets a collection with all members with the given name.
     * This method is case sensitive!
     *
     * @param name The name of the members.
     * @return A collection with all members with the given name.
     */
    default Collection<User> getMembersByName(String name) {
        return Collections.unmodifiableList(
                getMembers().stream()
                        .filter(user -> user.getName().equals(name))
                        .collect(Collectors.toList()));
    }

    /**
     * Gets a collection with all members with the given name.
     * This method is case insensitive!
     *
     * @param name The name of the members.
     * @return A collection with all members with the given name.
     */
    default Collection<User> getMembersByNameIgnoreCase(String name) {
        return Collections.unmodifiableList(
                getMembers().stream()
                        .filter(user -> user.getName().equalsIgnoreCase(name))
                        .collect(Collectors.toList()));
    }

    /**
     * Gets a collection with all members with the given nickname on this server.
     * This method is case sensitive!
     *
     * @param nickname The nickname of the members.
     * @return A collection with all members with the given nickname on this server.
     */
    default Collection<User> getMembersByNickname(String nickname) {
        return Collections.unmodifiableList(
                getMembers().stream()
                        .filter(user -> user.getNickname(this).map(nickname::equals).orElse(false))
                        .collect(Collectors.toList()));
    }

    /**
     * Gets a collection with all members with the given nickname on this server.
     * This method is case insensitive!
     *
     * @param nickname The nickname of the members.
     * @return A collection with all members with the given nickname on this server.
     */
    default Collection<User> getMembersByNicknameIgnoreCase(String nickname) {
        return Collections.unmodifiableList(
                getMembers().stream()
                        .filter(user -> user.getNickname(this).map(nickname::equalsIgnoreCase).orElse(false))
                        .collect(Collectors.toList()));
    }

    /**
     * Gets a collection with all members with the given display name on this server.
     * This method is case sensitive!
     *
     * @param displayName The display name of the members.
     * @return A collection with all members with the given display name on this server.
     */
    default Collection<User> getMembersByDisplayName(String displayName) {
        return Collections.unmodifiableList(
                getMembers().stream()
                        .filter(user -> user.getDisplayName(this).equals(displayName))
                        .collect(Collectors.toList()));
    }

    /**
     * Gets a collection with all members with the given display name on this server.
     * This method is case insensitive!
     *
     * @param displayName The display name of the members.
     * @return A collection with all members with the given display name on this server.
     */
    default Collection<User> getMembersByDisplayNameIgnoreCase(String displayName) {
        return Collections.unmodifiableList(
                getMembers().stream()
                        .filter(user -> user.getDisplayName(this).equalsIgnoreCase(displayName))
                        .collect(Collectors.toList()));
    }

    /**
     * Checks if the given user is a member of this server.
     *
     * @param user The user to check.
     * @return If the user is a member of this server.
     */
    boolean isMember(User user);

    /**
     * Gets a sorted list (by position) with all roles of the server.
     *
     * @return A sorted list (by position) with all roles of the server.
     */
    List<Role> getRoles();

    /**
     * Gets a sorted list (by position) with all roles of the user in the server.
     *
     * @param user The user.
     * @return A sorted list (by position) with all roles of the user in the server.
     */
    List<Role> getRoles(User user);

    /**
     * Gets a role by its id.
     *
     * @param id The id of the role.
     * @return The role with the given id.
     */
    Optional<Role> getRoleById(long id);

    /**
     * Gets a role by its id.
     *
     * @param id The id of the role.
     * @return The role with the given id.
     */
    default Optional<Role> getRoleById(String id) {
        try {
            return getRoleById(Long.parseLong(id));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    /**
     * Gets the @everyone role.
     *
     * @return The @everyone role.
     */
    default Role getEveryoneRole() {
        return getRoleById(getId()).orElseThrow(AssertionError::new);
    }

    /**
     * Gets a sorted list (by position) with all roles with the given name.
     * This method is case sensitive!
     *
     * @param name The name of the roles.
     * @return A sorted list (by position) with all roles with the given name.
     */
    default List<Role> getRolesByName(String name) {
        return Collections.unmodifiableList(
                getRoles().stream()
                        .filter(role -> role.getName().equals(name))
                        .collect(Collectors.toList()));
    }

    /**
     * Gets a sorted list (by position) with all roles with the given name.
     * This method is case insensitive!
     *
     * @param name The name of the roles.
     * @return A sorted list (by position) with all roles with the given name.
     */
    default List<Role> getRolesByNameIgnoreCase(String name) {
        return Collections.unmodifiableList(
                getRoles().stream()
                        .filter(role -> role.getName().equalsIgnoreCase(name))
                        .collect(Collectors.toList()));
    }

    /**
     * Gets the displayed color of a user based on his roles on this server.
     *
     * @param user The user.
     * @return The color.
     */
    default Optional<Color> getRoleColor(User user) {
        return user.getRoles(this).stream()
                .filter(role -> role.getColor().isPresent())
                .max(Comparator.comparingInt(Role::getRawPosition))
                .flatMap(Role::getColor);
    }

    /**
     * Gets the permissions of a user.
     *
     * @param user The user.
     * @return The permissions of the user.
     */
    default Permissions getPermissions(User user) {
        PermissionsBuilder builder = new PermissionsBuilder();
        getAllowedPermissions(user).forEach(type -> builder.setState(type, PermissionState.ALLOWED));
        return builder.build();
    }

    /**
     * Get the allowed permissions of a given user.
     * Remember, that some permissions affect others!
     * E.g. a user who has {@link PermissionType#SEND_MESSAGES} but not {@link PermissionType#READ_MESSAGES} cannot
     * send messages, even though he has the {@link PermissionType#SEND_MESSAGES} permission.
     *
     * @param user The user.
     * @return The allowed permissions of the given user.
     */
    default Collection<PermissionType> getAllowedPermissions(User user) {
        Collection<PermissionType> allowed = new HashSet<>();
        if (isOwner(user)) {
            allowed.addAll(Arrays.asList(PermissionType.values()));
        } else {
            getRoles(user).forEach(role -> allowed.addAll(role.getAllowedPermissions()));
        }
        return Collections.unmodifiableCollection(allowed);
    }

    /**
     * Get the unset permissions of a given user.
     *
     * @param user The user.
     * @return The unset permissions of the given user.
     */
    default Collection<PermissionType> getUnsetPermissions(User user) {
        if (isOwner(user)) {
            return Collections.emptySet();
        }
        Collection<PermissionType> unset = new HashSet<>();
        getRoles(user).forEach(role -> unset.addAll(role.getUnsetPermissions()));
        return Collections.unmodifiableCollection(unset);
    }

    /**
     * Checks if the user has a given set of permissions.
     *
     * @param user The user to check.
     * @param type The permission type(s) to check.
     * @return Whether the user has all given permissions or not.
     * @see #getAllowedPermissions(User)
     */
    default boolean hasPermissions(User user, PermissionType... type) {
        return getAllowedPermissions(user).containsAll(Arrays.asList(type));
    }

    /**
     * Checks if the user has any of a given set of permissions.
     *
     * @param user The user to check.
     * @param type The permission type(s) to check.
     * @return Whether the user has any of the given permissions or not.
     * @see #getAllowedPermissions(User)
     */
    default boolean hasAnyPermission(User user, PermissionType... type) {
        return getAllowedPermissions(user).stream()
                .anyMatch(allowedPermissionType -> Arrays.stream(type).anyMatch(allowedPermissionType::equals));
    }

    /**
     * Creates a custom emoji builder to create custom emojis.
     *
     * @return A custom emoji builder to create custom emojis.
     */
    default CustomEmojiBuilder createCustomEmojiBuilder() {
        return new CustomEmojiBuilder(this);
    }

    /**
     * Creates an updater for this server.
     *
     * @return An updater for this server.
     */
    default ServerUpdater createUpdater() {
        return new ServerUpdater(this);
    }

    /**
     * Updates the name of the server.
     *
     * <p>If you want to update several settings at once, it's recommended to use the
     * {@link ServerUpdater} from {@link #createUpdater()} which provides a better performance!
     *
     * @param name The new name of the server.
     * @return A future to check if the update was successful.
     */
    default CompletableFuture<Void> updateName(String name) {
        return createUpdater().setName(name).update();
    }

    /**
     * Updates the region of the server.
     *
     * <p>If you want to update several settings at once, it's recommended to use the
     * {@link ServerUpdater} from {@link #createUpdater()} which provides a better performance!
     *
     * @param region The new region of the server.
     * @return A future to check if the update was successful.
     */
    default CompletableFuture<Void> updateRegion(Region region) {
        return createUpdater().setRegion(region).update();
    }

    /**
     * Updates the explicit content filter level of the server.
     *
     * <p>If you want to update several settings at once, it's recommended to use the
     * {@link ServerUpdater} from {@link #createUpdater()} which provides a better performance!
     *
     * @param explicitContentFilterLevel The new explicit content filter level of the server.
     * @return A future to check if the update was successful.
     */
    default CompletableFuture<Void> updateExplicitContentFilterLevel(
            ExplicitContentFilterLevel explicitContentFilterLevel) {
        return createUpdater().setExplicitContentFilterLevel(explicitContentFilterLevel).update();
    }

    /**
     * Updates the verification level of the server.
     *
     * <p>If you want to update several settings at once, it's recommended to use the
     * {@link ServerUpdater} from {@link #createUpdater()} which provides a better performance!
     *
     * @param verificationLevel The new verification level of the server.
     * @return A future to check if the update was successful.
     */
    default CompletableFuture<Void> updateVerificationLevel(VerificationLevel verificationLevel) {
        return createUpdater().setVerificationLevel(verificationLevel).update();
    }

    /**
     * Updates the default message notification level of the server.
     *
     * <p>If you want to update several settings at once, it's recommended to use the
     * {@link ServerUpdater} from {@link #createUpdater()} which provides a better performance!
     *
     * @param defaultMessageNotificationLevel The new default message notification level of the server.
     * @return A future to check if the update was successful.
     */
    default CompletableFuture<Void> updateDefaultMessageNotificationLevel(
            DefaultMessageNotificationLevel defaultMessageNotificationLevel) {
        return createUpdater().setDefaultMessageNotificationLevel(defaultMessageNotificationLevel).update();
    }

    /**
     * Updates the afk channel of the server.
     *
     * <p>If you want to update several settings at once, it's recommended to use the
     * {@link ServerUpdater} from {@link #createUpdater()} which provides a better performance!
     *
     * @param afkChannel The new afk channel of the server.
     * @return A future to check if the update was successful.
     */
    default CompletableFuture<Void> updateAfkChannel(ServerVoiceChannel afkChannel) {
        return createUpdater().setAfkChannel(afkChannel).update();
    }

    /**
     * Removes the afk channel of the server.
     *
     * <p>If you want to update several settings at once, it's recommended to use the
     * {@link ServerUpdater} from {@link #createUpdater()} which provides a better performance!
     *
     * @return A future to check if the update was successful.
     */
    default CompletableFuture<Void> removeAfkChannel() {
        return createUpdater().removeAfkChannel().update();
    }

    /**
     * Updates the afk timeout of the server.
     *
     * <p>If you want to update several settings at once, it's recommended to use the
     * {@link ServerUpdater} from {@link #createUpdater()} which provides a better performance!
     *
     * @param afkTimeout The new afk timeout of the server in seconds.
     * @return A future to check if the update was successful.
     */
    default CompletableFuture<Void> updateAfkTimeoutInSeconds(int afkTimeout) {
        return createUpdater().setAfkTimeoutInSeconds(afkTimeout).update();
    }

    /**
     * Updates the icon of the server.
     * This method assumes the file type is "png"!
     *
     * <p>If you want to update several settings at once, it's recommended to use the
     * {@link ServerUpdater} from {@link #createUpdater()} which provides a better performance!
     *
     * @param icon The new icon of the server.
     * @return A future to check if the update was successful.
     */
    default CompletableFuture<Void> updateIcon(BufferedImage icon) {
        return createUpdater().setIcon(icon).update();
    }

    /**
     * Updates the icon of the server.
     *
     * <p>If you want to update several settings at once, it's recommended to use the
     * {@link ServerUpdater} from {@link #createUpdater()} which provides a better performance!
     *
     * @param icon The new icon of the server.
     * @param fileType The type of the icon, e.g. "png" or "jpg".
     * @return A future to check if the update was successful.
     */
    default CompletableFuture<Void> updateIcon(BufferedImage icon, String fileType) {
        return createUpdater().setIcon(icon, fileType).update();
    }

    /**
     * Updates the icon of the server.
     *
     * <p>If you want to update several settings at once, it's recommended to use the
     * {@link ServerUpdater} from {@link #createUpdater()} which provides a better performance!
     *
     * @param icon The new icon of the server.
     * @return A future to check if the update was successful.
     */
    default CompletableFuture<Void> updateIcon(File icon) {
        return createUpdater().setIcon(icon).update();
    }

    /**
     * Updates the icon of the server.
     *
     * <p>If you want to update several settings at once, it's recommended to use the
     * {@link ServerUpdater} from {@link #createUpdater()} which provides a better performance!
     *
     * @param icon The new icon of the server.
     * @return A future to check if the update was successful.
     */
    default CompletableFuture<Void> updateIcon(Icon icon) {
        return createUpdater().setIcon(icon).update();
    }

    /**
     * Updates the icon of the server.
     *
     * <p>If you want to update several settings at once, it's recommended to use the
     * {@link ServerUpdater} from {@link #createUpdater()} which provides a better performance!
     *
     * @param icon The new icon of the server.
     * @return A future to check if the update was successful.
     */
    default CompletableFuture<Void> updateIcon(URL icon) {
        return createUpdater().setIcon(icon).update();
    }

    /**
     * Updates the icon of the server.
     * This method assumes the file type is "png"!
     *
     * <p>If you want to update several settings at once, it's recommended to use the
     * {@link ServerUpdater} from {@link #createUpdater()} which provides a better performance!
     *
     * @param icon The new icon of the server.
     * @return A future to check if the update was successful.
     */
    default CompletableFuture<Void> updateIcon(byte[] icon) {
        return createUpdater().setIcon(icon).update();
    }

    /**
     * Updates the icon of the server.
     *
     * <p>If you want to update several settings at once, it's recommended to use the
     * {@link ServerUpdater} from {@link #createUpdater()} which provides a better performance!
     *
     * @param icon The new icon of the server.
     * @param fileType The type of the icon, e.g. "png" or "jpg".
     * @return A future to check if the update was successful.
     */
    default CompletableFuture<Void> updateIcon(byte[] icon, String fileType) {
        return createUpdater().setIcon(icon, fileType).update();
    }

    /**
     * Updates the icon of the server.
     * This method assumes the file type is "png"!
     *
     * <p>If you want to update several settings at once, it's recommended to use the
     * {@link ServerUpdater} from {@link #createUpdater()} which provides a better performance!
     *
     * @param icon The new icon of the server.
     * @return A future to check if the update was successful.
     */
    default CompletableFuture<Void> updateIcon(InputStream icon) {
        return createUpdater().setIcon(icon).update();
    }

    /**
     * Updates the icon of the server.
     *
     * <p>If you want to update several settings at once, it's recommended to use the
     * {@link ServerUpdater} from {@link #createUpdater()} which provides a better performance!
     *
     * @param icon The new icon of the server.
     * @param fileType The type of the icon, e.g. "png" or "jpg".
     * @return A future to check if the update was successful.
     */
    default CompletableFuture<Void> updateIcon(InputStream icon, String fileType) {
        return createUpdater().setIcon(icon, fileType).update();
    }

    /**
     * Removes the icon of the server.
     *
     * <p>If you want to update several settings at once, it's recommended to use the
     * {@link ServerUpdater} from {@link #createUpdater()} which provides a better performance!
     *
     * @return A future to check if the removal was successful.
     */
    default CompletableFuture<Void> removeIcon() {
        return createUpdater().removeIcon().update();
    }

    /**
     * Updates the owner of the server.
     * You must be the owner of this server in order to transfer it!
     *
     * <p>If you want to update several settings at once, it's recommended to use the
     * {@link ServerUpdater} from {@link #createUpdater()} which provides a better performance!
     *
     * @param owner The new owner of the server.
     * @return A future to check if the update was successful.
     */
    default CompletableFuture<Void> updateOwner(User owner) {
        return createUpdater().setOwner(owner).update();
    }

    /**
     * Updates the splash of the server. Requires {@link ServerFeature#INVITE_SPLASH}.
     * This method assumes the file type is "png"!
     *
     * <p>If you want to update several settings at once, it's recommended to use the
     * {@link ServerUpdater} from {@link #createUpdater()} which provides a better performance!
     *
     * @param splash The new splash of the server.
     * @return A future to check if the update was successful.
     */
    default CompletableFuture<Void> updateSplash(BufferedImage splash) {
        return createUpdater().setSplash(splash).update();
    }

    /**
     * Updates the splash of the server. Requires {@link ServerFeature#INVITE_SPLASH}.
     *
     * <p>If you want to update several settings at once, it's recommended to use the
     * {@link ServerUpdater} from {@link #createUpdater()} which provides a better performance!
     *
     * @param splash The new splash of the server.
     * @param fileType The type of the splash, e.g. "png" or "jpg".
     * @return A future to check if the update was successful.
     */
    default CompletableFuture<Void> updateSplash(BufferedImage splash, String fileType) {
        return createUpdater().setSplash(splash, fileType).update();
    }

    /**
     * Updates the splash of the server. Requires {@link ServerFeature#INVITE_SPLASH}.
     *
     * <p>If you want to update several settings at once, it's recommended to use the
     * {@link ServerUpdater} from {@link #createUpdater()} which provides a better performance!
     *
     * @param splash The new splash of the server.
     * @return A future to check if the update was successful.
     */
    default CompletableFuture<Void> updateSplash(File splash) {
        return createUpdater().setSplash(splash).update();
    }

    /**
     * Updates the splash of the server. Requires {@link ServerFeature#INVITE_SPLASH}.
     *
     * <p>If you want to update several settings at once, it's recommended to use the
     * {@link ServerUpdater} from {@link #createUpdater()} which provides a better performance!
     *
     * @param splash The new splash of the server.
     * @return A future to check if the update was successful.
     */
    default CompletableFuture<Void> updateSplash(Icon splash) {
        return createUpdater().setSplash(splash).update();
    }

    /**
     * Updates the splash of the server. Requires {@link ServerFeature#INVITE_SPLASH}.
     *
     * <p>If you want to update several settings at once, it's recommended to use the
     * {@link ServerUpdater} from {@link #createUpdater()} which provides a better performance!
     *
     * @param splash The new splash of the server.
     * @return A future to check if the update was successful.
     */
    default CompletableFuture<Void> updateSplash(URL splash) {
        return createUpdater().setSplash(splash).update();
    }

    /**
     * Updates the splash of the server. Requires {@link ServerFeature#INVITE_SPLASH}.
     * This method assumes the file type is "png"!
     *
     * <p>If you want to update several settings at once, it's recommended to use the
     * {@link ServerUpdater} from {@link #createUpdater()} which provides a better performance!
     *
     * @param splash The new splash of the server.
     * @return A future to check if the update was successful.
     */
    default CompletableFuture<Void> updateSplash(byte[] splash) {
        return createUpdater().setSplash(splash).update();
    }

    /**
     * Updates the splash of the server. Requires {@link ServerFeature#INVITE_SPLASH}.
     *
     * <p>If you want to update several settings at once, it's recommended to use the
     * {@link ServerUpdater} from {@link #createUpdater()} which provides a better performance!
     *
     * @param splash The new splash of the server.
     * @param fileType The type of the splash, e.g. "png" or "jpg".
     * @return A future to check if the update was successful.
     */
    default CompletableFuture<Void> updateSplash(byte[] splash, String fileType) {
        return createUpdater().setSplash(splash, fileType).update();
    }

    /**
     * Updates the splash of the server. Requires {@link ServerFeature#INVITE_SPLASH}.
     * This method assumes the file type is "png"!
     *
     * <p>If you want to update several settings at once, it's recommended to use the
     * {@link ServerUpdater} from {@link #createUpdater()} which provides a better performance!
     *
     * @param splash The new splash of the server.
     * @return A future to check if the update was successful.
     */
    default CompletableFuture<Void> updateSplash(InputStream splash) {
        return createUpdater().setSplash(splash).update();
    }

    /**
     * Updates the splash of the server. Requires {@link ServerFeature#INVITE_SPLASH}.
     *
     * <p>If you want to update several settings at once, it's recommended to use the
     * {@link ServerUpdater} from {@link #createUpdater()} which provides a better performance!
     *
     * @param splash The new splash of the server.
     * @param fileType The type of the splash, e.g. "png" or "jpg".
     * @return A future to check if the update was successful.
     */
    default CompletableFuture<Void> updateSplash(InputStream splash, String fileType) {
        return createUpdater().setSplash(splash, fileType).update();
    }

    /**
     * Removes the splash of the server. Requires {@link ServerFeature#INVITE_SPLASH}.
     *
     * <p>If you want to update several settings at once, it's recommended to use the
     * {@link ServerUpdater} from {@link #createUpdater()} which provides a better performance!
     *
     * @return A future to check if the removal was successful.
     */
    default CompletableFuture<Void> removeSplash() {
        return createUpdater().removeSplash().update();
    }

    /**
     * Updates the banner of the server. Requires {@link ServerFeature#BANNER}.
     * This method assumes the file type is "png"!
     *
     * <p>If you want to update several settings at once, it's recommended to use the
     * {@link ServerUpdater} from {@link #createUpdater()} which provides a better performance!
     *
     * @param banner The new banner of the server.
     * @return A future to check if the update was successful.
     */
    default CompletableFuture<Void> updateBanner(BufferedImage banner) {
        return createUpdater().setBanner(banner).update();
    }

    /**
     * Updates the banner of the server. Requires {@link ServerFeature#BANNER}.
     *
     * <p>If you want to update several settings at once, it's recommended to use the
     * {@link ServerUpdater} from {@link #createUpdater()} which provides a better performance!
     *
     * @param banner The new banner of the server.
     * @param fileType The type of the banner, e.g. "png" or "jpg".
     * @return A future to check if the update was successful.
     */
    default CompletableFuture<Void> updateBanner(BufferedImage banner, String fileType) {
        return createUpdater().setBanner(banner, fileType).update();
    }

    /**
     * Updates the banner of the server. Requires {@link ServerFeature#BANNER}.
     *
     * <p>If you want to update several settings at once, it's recommended to use the
     * {@link ServerUpdater} from {@link #createUpdater()} which provides a better performance!
     *
     * @param banner The new banner of the server. Requires {@link ServerFeature#BANNER}.
     * @return A future to check if the update was successful.
     */
    default CompletableFuture<Void> updateBanner(File banner) {
        return createUpdater().setBanner(banner).update();
    }

    /**
     * Updates the banner of the server. Requires {@link ServerFeature#BANNER}.
     *
     * <p>If you want to update several settings at once, it's recommended to use the
     * {@link ServerUpdater} from {@link #createUpdater()} which provides a better performance!
     *
     * @param banner The new banner of the server.
     * @return A future to check if the update was successful.
     */
    default CompletableFuture<Void> updateBanner(Icon banner) {
        return createUpdater().setBanner(banner).update();
    }

    /**
     * Updates the banner of the server. Requires {@link ServerFeature#BANNER}.
     *
     * <p>If you want to update several settings at once, it's recommended to use the
     * {@link ServerUpdater} from {@link #createUpdater()} which provides a better performance!
     *
     * @param banner The new banner of the server.
     * @return A future to check if the update was successful.
     */
    default CompletableFuture<Void> updateBanner(URL banner) {
        return createUpdater().setBanner(banner).update();
    }

    /**
     * Updates the banner of the server. Requires {@link ServerFeature#BANNER}.
     * This method assumes the file type is "png"!
     *
     * <p>If you want to update several settings at once, it's recommended to use the
     * {@link ServerUpdater} from {@link #createUpdater()} which provides a better performance!
     *
     * @param banner The new banner of the server.
     * @return A future to check if the update was successful.
     */
    default CompletableFuture<Void> updateBanner(byte[] banner) {
        return createUpdater().setBanner(banner).update();
    }

    /**
     * Updates the banner of the server. Requires {@link ServerFeature#BANNER}.
     *
     * <p>If you want to update several settings at once, it's recommended to use the
     * {@link ServerUpdater} from {@link #createUpdater()} which provides a better performance!
     *
     * @param banner The new banner of the server.
     * @param fileType The type of the banner, e.g. "png" or "jpg".
     * @return A future to check if the update was successful.
     */
    default CompletableFuture<Void> updateBanner(byte[] banner, String fileType) {
        return createUpdater().setBanner(banner, fileType).update();
    }

    /**
     * Updates the banner of the server. Requires {@link ServerFeature#BANNER}.
     * This method assumes the file type is "png"!
     *
     * <p>If you want to update several settings at once, it's recommended to use the
     * {@link ServerUpdater} from {@link #createUpdater()} which provides a better performance!
     *
     * @param banner The new banner of the server.
     * @return A future to check if the update was successful.
     */
    default CompletableFuture<Void> updateBanner(InputStream banner) {
        return createUpdater().setBanner(banner).update();
    }

    /**
     * Updates the banner of the server. Requires {@link ServerFeature#BANNER}.
     *
     * <p>If you want to update several settings at once, it's recommended to use the
     * {@link ServerUpdater} from {@link #createUpdater()} which provides a better performance!
     *
     * @param banner The new banner of the server.
     * @param fileType The type of the banner, e.g. "png" or "jpg".
     * @return A future to check if the update was successful.
     */
    default CompletableFuture<Void> updateBanner(InputStream banner, String fileType) {
        return createUpdater().setBanner(banner, fileType).update();
    }

    /**
     * Removes the banner of the server. Requires {@link ServerFeature#BANNER}.
     *
     * <p>If you want to update several settings at once, it's recommended to use the
     * {@link ServerUpdater} from {@link #createUpdater()} which provides a better performance!
     *
     * @return A future to check if the removal was successful.
     */
    default CompletableFuture<Void> removeBanner() {
        return createUpdater().removeBanner().update();
    }

    /**
     * Updates the rules channel of the server. Server has to be "PUBLIC".
     *
     * <p>If you want to update several settings at once, it's recommended to use the
     * {@link ServerUpdater} from {@link #createUpdater()} which provides a better performance!
     *
     * @param rulesChannel The new rules channel of the server.
     * @return A future to check if the update was successful.
     */
    default CompletableFuture<Void> setRulesChannel(ServerTextChannel rulesChannel) {
        return createUpdater().setRulesChannel(rulesChannel).update();
    }

    /**
     * Removes the rules channel of the server. Server has to be "PUBLIC".
     *
     * <p>If you want to update several settings at once, it's recommended to use the
     * {@link ServerUpdater} from {@link #createUpdater()} which provides a better performance!
     *
     * @return A future to check if the update was successful.
     */
    default CompletableFuture<Void> removeRulesChannel() {
        return createUpdater().removeRulesChannel().update();
    }


    /**
     * Updates the moderators-only channel of the server. Server has to be "PUBLIC".
     *
     * <p>If you want to update several settings at once, it's recommended to use the
     * {@link ServerUpdater} from {@link #createUpdater()} which provides a better performance!
     *
     * @param moderatorsOnlyChannel The new moderators-only of the server.
     * @return A future to check if the update was successful.
     */
    default CompletableFuture<Void> setModeratorsOnlyChannel(ServerTextChannel moderatorsOnlyChannel) {
        return createUpdater().setModeratorsOnlyChannel(moderatorsOnlyChannel).update();
    }

    /**
     * Removes the moderators-only channel of the server. Server has to be "PUBLIC".
     *
     * <p>If you want to update several settings at once, it's recommended to use the
     * {@link ServerUpdater} from {@link #createUpdater()} which provides a better performance!
     *
     * @return A future to check if the update was successful.
     */
    default CompletableFuture<Void> removeModeratorsOnlyChannel() {
        return createUpdater().removeModeratorsOnlyChannel().update();
    }

    /**
     * Updates the locale of the server. Server has to be "PUBLIC".
     *
     * <p>If you want to update several settings at once, it's recommended to use the
     * {@link ServerUpdater} from {@link #createUpdater()} which provides a better performance!
     *
     * @param locale The new locale of the server.
     * @return A future to check if the update was successful.
     */
    default CompletableFuture<Void> updatePreferredLocale(Locale locale) {
        return createUpdater().setPreferredLocale(locale).update();
    }

    /**
     * Updates the system channel of the server.
     *
     * <p>If you want to update several settings at once, it's recommended to use the
     * {@link ServerUpdater} from {@link #createUpdater()} which provides a better performance!
     *
     * @param systemChannel The new system channel of the server.
     * @return A future to check if the update was successful.
     */
    default CompletableFuture<Void> setSystemChannel(ServerTextChannel systemChannel) {
        return createUpdater().setSystemChannel(systemChannel).update();
    }

    /**
     * Removes the system channel of the server.
     *
     * <p>If you want to update several settings at once, it's recommended to use the
     * {@link ServerUpdater} from {@link #createUpdater()} which provides a better performance!
     *
     * @return A future to check if the update was successful.
     */
    default CompletableFuture<Void> removeSystemChannel() {
        return createUpdater().removeSystemChannel().update();
    }

    /**
     * Changes the nickname of the given user.
     *
     * <p>If you want to update several settings at once, it's recommended to use the
     * {@link ServerUpdater} from {@link #createUpdater()} which provides a better performance!
     *
     * @param user The user.
     * @param nickname The new nickname of the user.
     * @return A future to check if the update was successful.
     */
    default CompletableFuture<Void> updateNickname(User user, String nickname) {
        return createUpdater().setNickname(user, nickname).update();
    }

    /**
     * Changes the nickname of the given user.
     *
     * <p>If you want to update several settings at once, it's recommended to use the
     * {@link ServerUpdater} from {@link #createUpdater()} which provides a better performance!
     *
     * @param user The user.
     * @param nickname The new nickname of the user.
     * @param reason The audit log reason for this update.
     * @return A future to check if the update was successful.
     */
    default CompletableFuture<Void> updateNickname(User user, String nickname, String reason) {
        return createUpdater().setNickname(user, nickname).setAuditLogReason(reason).update();
    }

    /**
     * Removes the nickname of the given user.
     *
     * <p>If you want to update several settings at once, it's recommended to use the
     * {@link ServerUpdater} from {@link #createUpdater()} which provides a better performance!
     *
     * @param user The user.
     * @return A future to check if the update was successful.
     */
    default CompletableFuture<Void> resetNickname(User user) {
        return createUpdater().setNickname(user, null).update();
    }

    /**
     * Removes the nickname of the given user.
     *
     * <p>If you want to update several settings at once, it's recommended to use the
     * {@link ServerUpdater} from {@link #createUpdater()} which provides a better performance!
     *
     * @param user The user.
     * @param reason The audit log reason for this update.
     * @return A future to check if the update was successful.
     */
    default CompletableFuture<Void> resetNickname(User user, String reason) {
        return createUpdater().setNickname(user, null).setAuditLogReason(reason).update();
    }

    /**
     * Deletes the server.
     *
     * @return A future to check if the deletion was successful.
     */
    CompletableFuture<Void> delete();

    /**
     * Leaves the server.
     *
     * @return A future to check if the bot successfully left the server.
     */
    CompletableFuture<Void> leave();

    /**
     * Adds the given role to the given server member.
     *
     * <p>If you want to update several settings at once, it's recommended to use the
     * {@link ServerUpdater} from {@link #createUpdater()} which provides a better performance!
     *
     * @param user The server member the role should be added to.
     * @param role The role which should be added to the server member.
     * @return A future to check if the update was successful.
     */
    default CompletableFuture<Void> addRoleToUser(User user, Role role) {
        return addRoleToUser(user, role, null);
    }

    /**
     * Adds the given role to the given server member.
     *
     * <p>If you want to update several settings at once, it's recommended to use the
     * {@link ServerUpdater} from {@link #createUpdater()} which provides a better performance!
     *
     * @param user The server member the role should be added to.
     * @param role The role which should be added to the server member.
     * @param reason The audit log reason for this update.
     * @return A future to check if the update was successful.
     */
    CompletableFuture<Void> addRoleToUser(User user, Role role, String reason);

    /**
     * Removes the given role from the given server member.
     *
     * <p>If you want to update several settings at once, it's recommended to use the
     * {@link ServerUpdater} from {@link #createUpdater()} which provides a better performance!
     *
     * @param user The server member the role should be removed from.
     * @param role The role which should be removed from the server member.
     * @return A future to check if the update was successful.
     */
    default CompletableFuture<Void> removeRoleFromUser(User user, Role role) {
        return removeRoleFromUser(user, role, null);
    }

    /**
     * Removes the given role from the given server member.
     *
     * <p>If you want to update several settings at once, it's recommended to use the
     * {@link ServerUpdater} from {@link #createUpdater()} which provides a better performance!
     *
     * @param user The server member the role should be removed from.
     * @param role The role which should be removed from the server member.
     * @param reason The audit log reason for this update.
     * @return A future to check if the update was successful.
     */
    CompletableFuture<Void> removeRoleFromUser(User user, Role role, String reason);

    /**
     * Updates the roles of a server member.
     * This will replace the roles of the server member with a provided collection.
     *
     * <p>If you want to update several settings at once, it's recommended to use the
     * {@link ServerUpdater} from {@link #createUpdater()} which provides a better performance!
     *
     * @param user The user to update the roles of.
     * @param roles The collection of roles to replace the user's roles.
     * @return A future to check if the update was successful.
     */
    default CompletableFuture<Void> updateRoles(User user, Collection<Role> roles) {
        return createUpdater()
                .removeAllRolesFromUser(user)
                .addRolesToUser(user, roles)
                .update();
    }

    /**
     * Updates the roles of a server member.
     * This will replace the roles of the server member with a provided collection.
     *
     * <p>If you want to update several settings at once, it's recommended to use the
     * {@link ServerUpdater} from {@link #createUpdater()} which provides a better performance!
     *
     * @param user The user to update the roles of.
     * @param roles The collection of roles to replace the user's roles.
     * @param reason The audit log reason for this update.
     * @return A future to check if the update was successful.
     */
    default CompletableFuture<Void> updateRoles(User user, Collection<Role> roles, String reason) {
        return createUpdater()
                .removeAllRolesFromUser(user)
                .addRolesToUser(user, roles)
                .setAuditLogReason(reason)
                .update();
    }

    /**
     * Reorders the roles of the server.
     *
     * @param roles An ordered list with the new role positions.
     * @return A future to check if the update was successful.
     */
    default CompletableFuture<Void> reorderRoles(List<Role> roles) {
        return reorderRoles(roles, null);
    }

    /**
     * Reorders the roles of the server.
     *
     * @param roles An ordered list with the new role positions.
     * @param reason The audit log reason for this update.
     * @return A future to check if the update was successful.
     */
    CompletableFuture<Void> reorderRoles(List<Role> roles, String reason);

    /**
     * Moves yourself to the given channel on the server.
     *
     * @param channel The channel to move the user to.
     * @return A future to check if the move was successful.
     */
    default CompletableFuture<Void> moveYourself(ServerVoiceChannel channel) {
        return moveUser(getApi().getYourself(), channel);
    }

    /**
     * Moves the given user to the given channel on the server.
     *
     * @param user The user to move.
     * @param channel The channel to move the user to.
     * @return A future to check if the move was successful.
     */
    default CompletableFuture<Void> moveUser(User user, ServerVoiceChannel channel) {
        return createUpdater().setVoiceChannel(user, channel).update();
    }

    /**
     * Kicks the given user from any voice channel.
     *
     * @param user The user to kick.
     * @return A future to check if the kick was successful.
     */
    default CompletableFuture<Void> kickUserFromVoiceChannel(User user) {
        return createUpdater().setVoiceChannel(user, null).update();
    }

    /**
     * Mutes yourself locally for the server.
     *
     * <p>This cannot be undone by other users. If you want to mute yourself server-sidely, so that others can unmute
     * you, use {@link #muteYourself()}, {@link #muteUser(User)} or {@link #muteUser(User, String)}.
     *
     * @see #muteYourself()
     * @see #muteUser(User)
     * @see #muteUser(User, String)
     */
    void selfMute();

    /**
     * Unmutes yourself locally for the server.
     *
     * <p>This cannot be undone by other users. If you want to unmute yourself server-sidely, so that others can
     * mute you, use {@link #unmuteYourself()}, {@link #unmuteUser(User)} or {@link #unmuteUser(User, String)}.
     *
     * @see #unmuteYourself()
     * @see #unmuteUser(User)
     * @see #unmuteUser(User, String)
     */
    void selfUnmute();

    /**
     * Mutes yourself on the server.
     *
     * @return A future to check if the mute was successful.
     */
    default CompletableFuture<Void> muteYourself() {
        return muteUser(getApi().getYourself());
    }

    /**
     * Unmutes yourself on the server.
     *
     * @return A future to check if the unmute was successful.
     */
    default CompletableFuture<Void> unmuteYourself() {
        return unmuteUser(getApi().getYourself());
    }

    /**
     * Mutes the given user on the server.
     *
     * @param user The user to mute.
     * @return A future to check if the mute was successful.
     */
    default CompletableFuture<Void> muteUser(User user) {
        return createUpdater().setMuted(user, true).update();
    }

    /**
     * Mutes the given user on the server.
     *
     * @param user The user to mute.
     * @param reason The audit log reason for this action.
     * @return A future to check if the mute was successful.
     */
    default CompletableFuture<Void> muteUser(User user, String reason) {
        return createUpdater().setMuted(user, true).setAuditLogReason(reason).update();
    }

    /**
     * Unmutes the given user on the server.
     *
     * @param user The user to unmute.
     * @return A future to check if the unmute was successful.
     */
    default CompletableFuture<Void> unmuteUser(User user) {
        return createUpdater().setMuted(user, false).update();
    }

    /**
     * Unmutes the given user on the server.
     *
     * @param user The user to unmute.
     * @param reason The audit log reason for this action.
     * @return A future to check if the unmute was successful.
     */
    default CompletableFuture<Void> unmuteUser(User user, String reason) {
        return createUpdater().setMuted(user, false).setAuditLogReason(reason).update();
    }

    /**
     * Deafens yourself locally for the server.
     *
     * <p>This cannot be undone by other users. If you want to deafen yourself server-sidely, so that others can
     * undeafen you, use {@link #deafenYourself()}, {@link #deafenUser(User)} or {@link #deafenUser(User, String)}.
     *
     * @see #deafenYourself()
     * @see #deafenUser(User)
     * @see #deafenUser(User, String)
     */
    void selfDeafen();

    /**
     * Undeafens yourself locally for the server.
     *
     * <p>This cannot be undone by other users. If you want to undeafen yourself server-sidely, so that others can
     * deafen you, use {@link #undeafenYourself()}, {@link #undeafenUser(User)} or {@link #undeafenUser(User, String)}.
     *
     * @see #undeafenYourself()
     * @see #undeafenUser(User)
     * @see #undeafenUser(User, String)
     */
    void selfUndeafen();

    /**
     * Deafens yourself on the server.
     *
     * @return A future to check if the deafen was successful.
     */
    default CompletableFuture<Void> deafenYourself() {
        return deafenUser(getApi().getYourself());
    }

    /**
     * Undeafens yourself on the server.
     *
     * @return A future to check if the undeafen was successful.
     */
    default CompletableFuture<Void> undeafenYourself() {
        return undeafenUser(getApi().getYourself());
    }

    /**
     * Deafens the given user on the server.
     *
     * @param user The user to deafen.
     * @return A future to check if the deafen was successful.
     */
    default CompletableFuture<Void> deafenUser(User user) {
        return createUpdater().setDeafened(user, true).update();
    }

    /**
     * Deafens the given user on the server.
     *
     * @param user The user to deafen.
     * @param reason The audit log reason for this action.
     * @return A future to check if the deafen was successful.
     */
    default CompletableFuture<Void> deafenUser(User user, String reason) {
        return createUpdater().setDeafened(user, true).setAuditLogReason(reason).update();
    }

    /**
     * Requests a server member.
     *
     * @param user The user to request as a member.
     * @return A future to get a server member if it exists in the server.
     */
    default CompletableFuture<User> requestMember(User user) {
        return requestMember(user.getId());
    }

    /**
     * Requests a server member.
     *
     * @param userId The user id of the member to request.
     * @return A future to get a server member if it exists in the server.
     */
    CompletableFuture<User> requestMember(long userId);

    /**
     * Undeafens the given user on the server.
     *
     * @param user The user to undeafen.
     * @return A future to check if the undeafen was successful.
     */
    default CompletableFuture<Void> undeafenUser(User user) {
        return createUpdater().setDeafened(user, false).update();
    }

    /**
     * Undeafens the given user on the server.
     *
     * @param user The user to undeafen.
     * @param reason The audit log reason for this action.
     * @return A future to check if the undeafen was successful.
     */
    default CompletableFuture<Void> undeafenUser(User user, String reason) {
        return createUpdater().setDeafened(user, false).setAuditLogReason(reason).update();
    }

    /**
     * Kicks the given user from the server.
     *
     * @param user The user to kick.
     * @return A future to check if the kick was successful.
     */
    default CompletableFuture<Void> kickUser(User user) {
        return kickUser(user, null);
    }

    /**
     * Kicks the given user from the server.
     *
     * @param user The user to kick.
     * @param reason The audit log reason for this action.
     * @return A future to check if the kick was successful.
     */
    CompletableFuture<Void> kickUser(User user, String reason);

    /**
     * Bans the given user from the server.
     *
     * @param user The user to ban.
     * @return A future to check if the ban was successful.
     */
    default CompletableFuture<Void> banUser(User user) {
        return banUser(user.getId(), 0, null);
    }

    /**
     * Bans the given user from the server.
     *
     * @param user The user to ban.
     * @param deleteMessageDays The number of days to delete messages for (0-7).
     * @return A future to check if the ban was successful.
     */
    default CompletableFuture<Void> banUser(User user, int deleteMessageDays) {
        return banUser(user.getId(), deleteMessageDays, null);
    }

    /**
     * Bans the given user from the server.
     *
     * @param user The user to ban.
     * @param deleteMessageDays The number of days to delete messages for (0-7).
     * @param reason The reason for the ban.
     * @return A future to check if the ban was successful.
     */
    default CompletableFuture<Void> banUser(User user, int deleteMessageDays, String reason) {
        return banUser(user.getId(), deleteMessageDays, reason);
    }

    /**
     * Bans the given user from the server.
     *
     * @param userId The id of the user to ban.
     * @return A future to check if the ban was successful.
     */
    default CompletableFuture<Void> banUser(String userId) {
        return banUser(userId, 0, null);
    }

    /**
     * Bans the given user from the server.
     *
     * @param userId The id of the user to ban.
     * @return A future to check if the ban was successful.
     */
    default CompletableFuture<Void> banUser(long userId) {
        return banUser(Long.toUnsignedString(userId));
    }

    /**
     * Bans the given user from the server.
     *
     * @param userId The id of the user to ban.
     * @param deleteMessageDays The number of days to delete messages for (0-7).
     * @return A future to check if the ban was successful.
     */
    default CompletableFuture<Void> banUser(String userId, int deleteMessageDays) {
        return banUser(userId, deleteMessageDays, null);
    }

    /**
     * Bans the given user from the server.
     *
     * @param userId The id of the user to ban.
     * @param deleteMessageDays The number of days to delete messages for (0-7).
     * @return A future to check if the ban was successful.
     */
    default CompletableFuture<Void> banUser(long userId, int deleteMessageDays) {
        return banUser(Long.toUnsignedString(userId), deleteMessageDays);
    }

    /**
     * Bans the given user from the server.
     *
     * @param userId The id of the user to ban.
     * @param deleteMessageDays The number of days to delete messages for (0-7).
     * @param reason The reason for the ban.
     * @return A future to check if the ban was successful.
     */
    CompletableFuture<Void> banUser(String userId, int deleteMessageDays, String reason);

    /**
     * Bans the given user from the server.
     *
     * @param userId The id of the user to ban.
     * @param deleteMessageDays The number of days to delete messages for (0-7).
     * @param reason The reason for the ban.
     * @return A future to check if the ban was successful.
     */
    default CompletableFuture<Void> banUser(long userId, int deleteMessageDays, String reason) {
        return banUser(Long.toUnsignedString(userId), deleteMessageDays, reason);
    }

    /**
     * Unbans the given user from the server.
     *
     * @param user The user to ban.
     * @return A future to check if the unban was successful.
     */
    default CompletableFuture<Void> unbanUser(User user) {
        return unbanUser(user.getId(), null);
    }

    /**
     * Unbans the given user from the server.
     *
     * @param userId The id of the user to unban.
     * @return A future to check if the unban was successful.
     */
    default CompletableFuture<Void> unbanUser(long userId) {
        return unbanUser(userId, null);
    }

    /**
     * Unbans the given user from the server.
     *
     * @param userId The id of the user to unban.
     * @return A future to check if the unban was successful.
     */
    default CompletableFuture<Void> unbanUser(String userId) {
        return unbanUser(userId, null);
    }

    /**
     * Unbans the given user from the server.
     *
     * @param user The user to ban.
     * @param reason The audit log reason for this action.
     * @return A future to check if the unban was successful.
     */
    default CompletableFuture<Void> unbanUser(User user, String reason) {
        return unbanUser(user.getId(), reason);
    }

    /**
     * Unbans the given user from the server.
     *
     * @param userId The id of the user to unban.
     * @param reason The audit log reason for this action.
     * @return A future to check if the unban was successful.
     */
    CompletableFuture<Void> unbanUser(long userId, String reason);

    /**
     * Unbans the given user from the server.
     *
     * @param userId The id of the user to unban.
     * @param reason The audit log reason for this action.
     * @return A future to check if the unban was successful.
     */
    default CompletableFuture<Void> unbanUser(String userId, String reason) {
        try {
            return unbanUser(Long.parseLong(userId), reason);
        } catch (NumberFormatException e) {
            CompletableFuture<Void> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }

    /**
     * Gets a collection with all server bans.
     *
     * @return A collection with all server bans.
     */
    CompletableFuture<Collection<Ban>> getBans();

    /**
     * Gets a list of all webhooks in this server.
     *
     * @return A list of all webhooks in this server.
     */
    CompletableFuture<List<Webhook>> getWebhooks();

    /**
     * Gets a list of all incoming webhooks in this server, they are not guaranteed to have an accessible token.
     *
     * @return A list of all incoming webhooks in this server.
     */
    CompletableFuture<List<Webhook>> getAllIncomingWebhooks();

    /**
     * Gets a list of incoming webhooks in this server.
     * This method only returns webhooks with a token, that the bot can access.
     *
     * @return A list of all incoming webhooks in this server.
     */
    CompletableFuture<List<IncomingWebhook>> getIncomingWebhooks();

    /**
     * Gets the audit log of this server.
     *
     * @param limit The maximum amount of audit log entries.
     * @return The audit log.
     */
    CompletableFuture<AuditLog> getAuditLog(int limit);

    /**
     * Gets the audit log of this server.
     *
     * @param limit The maximum amount of audit log entries.
     * @param type The action type of the audit log.
     * @return The audit log.
     */
    CompletableFuture<AuditLog> getAuditLog(int limit, AuditLogActionType type);

    /**
     * Gets the audit log of this server.
     *
     * @param limit The maximum amount of audit log entries.
     * @param before Filter the log before this entry.
     * @return The audit log.
     */
    CompletableFuture<AuditLog> getAuditLogBefore(int limit, AuditLogEntry before);

    /**
     * Gets the audit log of this server.
     *
     * @param limit The maximum amount of audit log entries.
     * @param before Filter the log before this entry.
     * @param type The action type of the audit log.
     * @return The audit log.
     */
    CompletableFuture<AuditLog> getAuditLogBefore(int limit, AuditLogEntry before, AuditLogActionType type);

    /**
     * Checks if a user has a given permission.
     * Remember, that some permissions affect others!
     * E.g. a user who has {@link PermissionType#SEND_MESSAGES} but not {@link PermissionType#READ_MESSAGES} cannot
     * send messages, even though he has the {@link PermissionType#SEND_MESSAGES} permission.
     * This method also do not take into account overwritten permissions in some channels!
     *
     * @param user The user.
     * @param permission The permission to check.
     * @return Whether the user has the permission or not.
     */
    default boolean hasPermission(User user, PermissionType permission) {
        return getAllowedPermissions(user).contains(permission);
    }

    /**
     * Gets a collection with all custom emojis of this server.
     *
     * @return A collection with all custom emojis of this server.
     */
    Collection<KnownCustomEmoji> getCustomEmojis();

    /**
     * Gets a custom emoji in this server by its id.
     *
     * @param id The id of the emoji.
     * @return The emoji with the given id.
     */
    default Optional<KnownCustomEmoji> getCustomEmojiById(long id) {
        return getCustomEmojis().stream().filter(emoji -> emoji.getId() == id).findAny();
    }

    /**
     * Gets a custom emoji in this server by its id.
     *
     * @param id The id of the emoji.
     * @return The emoji with the given id.
     */
    default Optional<KnownCustomEmoji> getCustomEmojiById(String id) {
        try {
            return getCustomEmojiById(Long.parseLong(id));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    /**
     * Gets a collection of all custom emojis with the given name in the server.
     * This method is case sensitive!
     *
     * @param name The name of the custom emojis.
     * @return A collection of all custom emojis with the given name in this server.
     */
    default Collection<KnownCustomEmoji> getCustomEmojisByName(String name) {
        return Collections.unmodifiableList(
                getCustomEmojis().stream()
                        .filter(emoji -> emoji.getName().equals(name))
                        .collect(Collectors.toList()));
    }

    /**
     * Gets a collection of all custom emojis with the given name in the server.
     * This method is case insensitive!
     *
     * @param name The name of the custom emojis.
     * @return A collection of all custom emojis with the given name in this server.
     */
    default Collection<KnownCustomEmoji> getCustomEmojisByNameIgnoreCase(String name) {
        return Collections.unmodifiableCollection(
                getCustomEmojis().stream()
                        .filter(emoji -> emoji.getName().equalsIgnoreCase(name))
                        .collect(Collectors.toList()));
    }

    /**
     * Gets a list with all slash commands for the given server.
     *
     * @return A list with all slash commands from the server.
     */
    CompletableFuture<List<SlashCommand>> getSlashCommands();

    /**
     * Gets a server slash command by its id.
     *
     * @param commandId The id of the server slash command.
     * @return The server slash command with the given id.
     */
    CompletableFuture<SlashCommand> getSlashCommandById(long commandId);

    /**
     * Creates a new channel category builder.
     *
     * @return A builder to create a new channel category.
     */
    default ChannelCategoryBuilder createChannelCategoryBuilder() {
        return new ChannelCategoryBuilder(this);
    }

    /**
     * Creates a new server text channel builder.
     *
     * @return A builder to create a new server text channel.
     */
    default ServerTextChannelBuilder createTextChannelBuilder() {
        return new ServerTextChannelBuilder(this);
    }

    /**
     * Creates a new server voice channel builder.
     *
     * @return A builder to create a new server voice channel.
     */
    default ServerVoiceChannelBuilder createVoiceChannelBuilder() {
        return new ServerVoiceChannelBuilder(this);
    }

    /**
     * Creates a new role builder.
     *
     * @return A builder to create a new role.
     */
    default RoleBuilder createRoleBuilder() {
        return new RoleBuilder(this);
    }

    /**
     * Gets a sorted list (by position) with all channels of the server.
     *
     * @return A sorted list (by position) with all channels of the server.
     */
    List<ServerChannel> getChannels();

    /**
     * Gets a sorted list (by position) with all channel categories of the server.
     *
     * @return A sorted list (by position) with all channel categories of the server.
     */
    List<ChannelCategory> getChannelCategories();

    /**
     * Gets a sorted list (by position) with all text channels of the server.
     *
     * @return A sorted list (by position) with all text channels of the server.
     */
    List<ServerTextChannel> getTextChannels();

    /**
     * Gets a sorted list (by position) with all voice channels of the server.
     *
     * @return A sorted list (by position) with all voice channels of the server.
     */
    List<ServerVoiceChannel> getVoiceChannels();

    /**
     * Gets a channel by its id.
     *
     * @param id The id of the channel.
     * @return The channel with the given id.
     */
    Optional<ServerChannel> getChannelById(long id);

    /**
     * Gets a channel by its id.
     *
     * @param id The id of the channel.
     * @return The channel with the given id.
     */
    default Optional<ServerChannel> getChannelById(String id) {
        try {
            return getChannelById(Long.parseLong(id));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    /**
     * Gets a sorted list (by position) with all channels with the given name.
     * This method is case sensitive!
     *
     * @param name The name of the channels.
     * @return A sorted list (by position) with all channels with the given name.
     */
    default List<ServerChannel> getChannelsByName(String name) {
        return Collections.unmodifiableList(
                getChannels().stream()
                        .filter(channel -> channel.getName().equals(name))
                        .collect(Collectors.toList()));
    }

    /**
     * Gets a sorted list (by position) with all channels with the given name.
     * This method is case insensitive!
     *
     * @param name The name of the channels.
     * @return A sorted list (by position) with all channels with the given name.
     */
    default List<ServerChannel> getChannelsByNameIgnoreCase(String name) {
        return Collections.unmodifiableList(
                getChannels().stream()
                        .filter(channel -> channel.getName().equalsIgnoreCase(name))
                        .collect(Collectors.toList()));
    }

    /**
     * Gets a channel category by its id.
     *
     * @param id The id of the channel category.
     * @return The channel category with the given id.
     */
    default Optional<ChannelCategory> getChannelCategoryById(long id) {
        return getChannelById(id)
                .filter(channel -> channel instanceof ChannelCategory)
                .map(channel -> (ChannelCategory) channel);
    }

    /**
     * Gets a channel category by its id.
     *
     * @param id The id of the channel category.
     * @return The channel category with the given id.
     */
    default Optional<ChannelCategory> getChannelCategoryById(String id) {
        try {
            return getChannelCategoryById(Long.parseLong(id));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    /**
     * Gets a sorted list (by position) with all channel categories with the given name.
     * This method is case sensitive!
     *
     * @param name The name of the channel categories.
     * @return A sorted list (by position) with all channel categories with the given name.
     */
    default List<ChannelCategory> getChannelCategoriesByName(String name) {
        return Collections.unmodifiableList(
                getChannelCategories().stream()
                        .filter(channel -> channel.getName().equals(name))
                        .collect(Collectors.toList()));
    }

    /**
     * Gets a sorted list (by position) with all channel categories with the given name.
     * This method is case insensitive!
     *
     * @param name The name of the channel categories.
     * @return A sorted list (by position) with all channel categories with the given name.
     */
    default List<ChannelCategory> getChannelCategoriesByNameIgnoreCase(String name) {
        return Collections.unmodifiableList(
                getChannelCategories().stream()
                        .filter(channel -> channel.getName().equalsIgnoreCase(name))
                        .collect(Collectors.toList()));
    }

    /**
     * Gets a text channel by its id.
     *
     * @param id The id of the text channel.
     * @return The text channel with the given id.
     */
    default Optional<ServerTextChannel> getTextChannelById(long id) {
        return getChannelById(id)
                .filter(channel -> channel instanceof ServerTextChannel)
                .map(channel -> (ServerTextChannel) channel);
    }

    /**
     * Gets a text channel by its id.
     *
     * @param id The id of the text channel.
     * @return The text channel with the given id.
     */
    default Optional<ServerTextChannel> getTextChannelById(String id) {
        try {
            return getTextChannelById(Long.parseLong(id));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    /**
     * Gets a sorted list (by position) with all text channels with the given name.
     * This method is case sensitive!
     *
     * @param name The name of the text channels.
     * @return A sorted list (by position) with all text channels with the given name.
     */
    default List<ServerTextChannel> getTextChannelsByName(String name) {
        return Collections.unmodifiableList(
                getTextChannels().stream()
                        .filter(channel -> channel.getName().equals(name))
                        .collect(Collectors.toList()));
    }

    /**
     * Gets a sorted list (by position) with all text channels with the given name.
     * This method is case insensitive!
     *
     * @param name The name of the text channels.
     * @return A sorted list (by position) with all text channels with the given name.
     */
    default List<ServerTextChannel> getTextChannelsByNameIgnoreCase(String name) {
        return Collections.unmodifiableList(
                getTextChannels().stream()
                        .filter(channel -> channel.getName().equalsIgnoreCase(name))
                        .collect(Collectors.toList()));
    }

    /**
     * Gets a voice channel by its id.
     *
     * @param id The id of the voice channel.
     * @return The voice channel with the given id.
     */
    default Optional<ServerVoiceChannel> getVoiceChannelById(long id) {
        return getChannelById(id)
                .filter(channel -> channel instanceof ServerVoiceChannel)
                .map(channel -> (ServerVoiceChannel) channel);
    }

    /**
     * Gets a voice channel by its id.
     *
     * @param id The id of the voice channel.
     * @return The voice channel with the given id.
     */
    default Optional<ServerVoiceChannel> getVoiceChannelById(String id) {
        try {
            return getVoiceChannelById(Long.parseLong(id));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    /**
     * Gets a stage voice channel by its id.
     *
     * @param id The id of the stage voice channel.
     * @return The stage voice channel with the given id.
     */
    default Optional<ServerStageVoiceChannel> getStageVoiceChannelById(long id) {
        return getChannelById(id)
                .filter(channel -> channel instanceof ServerStageVoiceChannel)
                .map(channel -> (ServerStageVoiceChannel) channel);
    }

    /**
     * Gets a voice channel by its id.
     *
     * @param id The id of the voice channel.
     * @return The voice channel with the given id.
     */
    default Optional<ServerStageVoiceChannel> getStageVoiceChannelById(String id) {
        try {
            return getStageVoiceChannelById(Long.parseLong(id));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    /**
     * Gets a sorted list (by position) with all voice channels with the given name.
     * This method is case sensitive!
     *
     * @param name The name of the voice channels.
     * @return A sorted list (by position) with all voice channels with the given name.
     */
    default List<ServerVoiceChannel> getVoiceChannelsByName(String name) {
        return Collections.unmodifiableList(
                getVoiceChannels().stream()
                        .filter(channel -> channel.getName().equals(name))
                        .collect(Collectors.toList()));
    }

    /**
     * Gets a sorted list (by position) with all voice channels with the given name.
     * This method is case insensitive!
     *
     * @param name The name of the voice channels.
     * @return A sorted list (by position) with all voice channels with the given name.
     */
    default List<ServerVoiceChannel> getVoiceChannelsByNameIgnoreCase(String name) {
        return Collections.unmodifiableList(
                getVoiceChannels().stream()
                        .filter(channel -> channel.getName().equalsIgnoreCase(name))
                        .collect(Collectors.toList()));
    }

    /**
     * Gets the voice channel the user with the given id is connected to on this server if any.
     *
     * @param userId The id of the user to check.
     * @return The voice channel the user is connected to.
     */
    default Optional<ServerVoiceChannel> getConnectedVoiceChannel(long userId) {
        return getVoiceChannels().stream()
                .filter(serverVoiceChannel -> serverVoiceChannel.isConnected(userId))
                .findAny();
    }

    /**
     * Gets the voice channel the given user is connected to on this server if any.
     *
     * @param user The user to check.
     * @return The voice channel the user is connected to.
     */
    default Optional<ServerVoiceChannel> getConnectedVoiceChannel(User user) {
        return getConnectedVoiceChannel(user.getId());
    }

    /**
     * Gets a sorted (by position) list with all channels of this server the given user can see.
     * Returns an empty list, if the user is not a member of this server.
     *
     * @param user The user to check.
     * @return The visible channels of this server.
     */
    default List<ServerChannel> getVisibleChannels(User user) {
        return Collections.unmodifiableList(getChannels().stream()
                .filter(channel -> channel.canSee(user))
                .collect(Collectors.toList()));
    }

    /**
     * Gets the highest role of the given user in this server.
     * The optional is empty, if the user is not a member of this server.
     *
     * @param user The user.
     * @return The highest role of the given user.
     */
    default Optional<Role> getHighestRole(User user) {
        List<Role> roles = getRoles(user);
        if (roles.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(roles.get(roles.size() - 1));
    }

    /**
     * Checks if the given user is the owner of the server.
     *
     * @param user The user to check.
     * @return Whether the given user is the owner of the server or not.
     */
    default boolean isOwner(User user) {
        return getOwnerId() == user.getId();
    }

    /**
     * Checks if the given user is an administrator of the server.
     *
     * @param user The user to check.
     * @return Whether the given user is an administrator of the server or not.
     */
    default boolean isAdmin(User user) {
        return hasPermission(user, PermissionType.ADMINISTRATOR);
    }

    /**
     * Checks if the given user can create new channels.
     *
     * @param user The user to check.
     * @return Whether the given user can create channels or not.
     */
    default boolean canCreateChannels(User user) {
        return hasAnyPermission(user,
                PermissionType.ADMINISTRATOR,
                PermissionType.MANAGE_CHANNELS);
    }

    /**
     * Checks if the user of the connected account can create new channels.
     *
     * @return Whether the user of the connected account can create channels or not.
     */
    default boolean canYouCreateChannels() {
        return canCreateChannels(getApi().getYourself());
    }

    /**
     * Checks if the given user can view the audit log of the server.
     *
     * @param user The user to check.
     * @return Whether the given user can view the audit log or not.
     */
    default boolean canViewAuditLog(User user) {
        return hasAnyPermission(user,
                PermissionType.ADMINISTRATOR,
                PermissionType.VIEW_AUDIT_LOG);
    }

    /**
     * Checks if the user of the connected account can view the audit log of the server.
     *
     * @return Whether the user of the connected account can view the audit log or not.
     */
    default boolean canYouViewAuditLog() {
        return canViewAuditLog(getApi().getYourself());
    }

    /**
     * Checks if the given user can change its own nickname in the server.
     *
     * @param user The user to check.
     * @return Whether the given user can change its own nickname or not.
     */
    default boolean canChangeOwnNickname(User user) {
        return hasAnyPermission(user,
                PermissionType.ADMINISTRATOR,
                PermissionType.CHANGE_NICKNAME,
                PermissionType.MANAGE_NICKNAMES);
    }

    /**
     * Checks if the user of the connected account can change its own nickname in the server.
     *
     * @return Whether the user of the connected account can change its own nickname or not.
     */
    default boolean canYouChangeOwnNickname() {
        return canChangeOwnNickname(getApi().getYourself());
    }

    /**
     * Checks if the given user can manage nicknames on the server.
     *
     * @param user The user to check.
     * @return Whether the given user can manage nicknames or not.
     */
    default boolean canManageNicknames(User user) {
        return hasAnyPermission(user,
                PermissionType.ADMINISTRATOR,
                PermissionType.MANAGE_NICKNAMES);
    }

    /**
     * Checks if the user of the connected account can manage nicknames on the server.
     *
     * @return Whether the user of the connected account can manage nicknames or not.
     */
    default boolean canYouManageNicknames() {
        return canManageNicknames(getApi().getYourself());
    }

    /**
     * Checks if the given user can mute members on the server.
     *
     * @param user The user to check.
     * @return Whether the given user can mute members or not.
     */
    default boolean canMuteMembers(User user) {
        return hasAnyPermission(user,
                PermissionType.ADMINISTRATOR,
                PermissionType.MUTE_MEMBERS);
    }

    /**
     * Checks if the user of the connected account can mute members on the server.
     *
     * @return Whether the user of the connected account can mute members or not.
     */
    default boolean canYouMuteMembers() {
        return canMuteMembers(getApi().getYourself());
    }

    /**
     * Checks if the given user can deafen members on the server.
     *
     * @param user The user to check.
     * @return Whether the given user can deafen members or not.
     */
    default boolean canDeafenMembers(User user) {
        return hasAnyPermission(user,
                PermissionType.ADMINISTRATOR,
                PermissionType.DEAFEN_MEMBERS);
    }

    /**
     * Checks if the user of the connected account can deafen members on the server.
     *
     * @return Whether the user of the connected account can deafen members or not.
     */
    default boolean canYouDeafenMembers() {
        return canDeafenMembers(getApi().getYourself());
    }

    /**
     * Checks if the given user can move members on the server.
     *
     * @param user The user to check.
     * @return Whether the given user can move members or not.
     */
    default boolean canMoveMembers(User user) {
        return hasAnyPermission(user,
                PermissionType.ADMINISTRATOR,
                PermissionType.MOVE_MEMBERS);
    }

    /**
     * Checks if the user of the connected account can move members on the server.
     *
     * @return Whether the user of the connected account can move members or not.
     */
    default boolean canYouMoveMembers() {
        return canMoveMembers(getApi().getYourself());
    }

    /**
     * Checks if the given user can manage emojis on the server.
     *
     * @param user The user to check.
     * @return Whether the given user can manage emojis or not.
     */
    default boolean canManageEmojis(User user) {
        return hasAnyPermission(user,
                PermissionType.ADMINISTRATOR,
                PermissionType.MANAGE_EMOJIS);
    }

    /**
     * Checks if the user of the connected account can manage emojis on the server.
     *
     * @return Whether the user of the connected account can manage emojis or not.
     */
    default boolean canYouManageEmojis() {
        return canManageEmojis(getApi().getYourself());
    }

    /**
     * Checks if the given user can use slash commands on the server.
     *
     * @param user The user to check.
     * @return Whether the given user can use slash commands or not.
     */
    default boolean canUseSlashCommands(User user) {
        return hasAnyPermission(user,
                PermissionType.ADMINISTRATOR,
                PermissionType.USE_SLASH_COMMANDS);
    }

    /**
     * Checks if the user of the connected account can use slash commands on the server.
     *
     * @return Whether the user of the connected account can use slash commands or not.
     */
    default boolean canYouUseSlashCommands() {
        return canUseSlashCommands(getApi().getYourself());
    }

    /**
     * Checks if the given user can manage roles on the server.
     *
     * @param user The user to check.
     * @return Whether the given user can manage roles or not.
     */
    default boolean canManageRoles(User user) {
        return hasAnyPermission(user,
                PermissionType.ADMINISTRATOR,
                PermissionType.MANAGE_ROLES);
    }

    /**
     * Checks if the user of the connected account can manage roles on the server.
     *
     * @return Whether the user of the connected account can manage roles or not.
     */
    default boolean canYouManageRoles() {
        return canManageRoles(getApi().getYourself());
    }

    /**
     * Checks if the user can manage the roles of the target user.
     *
     * @param user The user that tries to manage roles.
     * @param target The user whose roles are to be managed.
     * @return Whether the user can manage the target's roles.
     */
    default boolean canManageRolesOf(User user, User target) {
        return canManageRole(user, getHighestRole(target).orElseGet(this::getEveryoneRole));
    }

    /**
     * Checks if the user can manage the target role.
     *
     * @param user The user that tries to manage the role.
     * @param target The role that is to be managed.
     * @return Whether the user can manage the role.
     */
    default boolean canManageRole(User user, Role target) {
        return target.getServer() == this
                && (isOwner(user)
                || (canManageRoles(user)
                && getHighestRole(user).orElseGet(this::getEveryoneRole).compareTo(target) > 0));
    }

    /**
     * Checks if the given user can manage the server.
     *
     * @param user The user to check.
     * @return Whether the given user can manage the server or not.
     */
    default boolean canManage(User user) {
        return hasAnyPermission(user,
                PermissionType.ADMINISTRATOR,
                PermissionType.MANAGE_SERVER);
    }

    /**
     * Checks if the user of the connected account can manage the server.
     *
     * @return Whether the user of the connected account can manage the server or not.
     */
    default boolean canYouManage() {
        return canManage(getApi().getYourself());
    }

    /**
     * Checks if the given user can kick users from the server.
     *
     * @param user The user to check.
     * @return Whether the given user can kick users or not.
     */
    default boolean canKickUsers(User user) {
        return hasAnyPermission(user,
                PermissionType.ADMINISTRATOR,
                PermissionType.KICK_MEMBERS);
    }

    /**
     * Checks if the user of the connected account can kick users from the server.
     *
     * @return Whether the user of the connected account can kick users or not.
     */
    default boolean canYouKickUsers() {
        return canKickUsers(getApi().getYourself());
    }

    /**
     * Checks if the given user can kick the other user.
     * This method also considers the position of the user roles and the owner.
     * If the to-be-kicked user is not part of this server, still {@code true} is returned as Discord allows this.
     *
     * @param user The user which "want" to kick the other user.
     * @param userToKick The user which should be kicked.
     * @return Whether the given user can kick the other user or not.
     */
    default boolean canKickUser(User user, User userToKick) {
        // owner can always kick, regardless of role for example
        if (isOwner(user)) {
            return true;
        }
        // user cannot kick at all
        if (!canKickUsers(user)) {
            return false;
        }
        // only returns empty optional if user is not member of server
        // but then canKickUsers should already have kicked in
        Role ownRole = getHighestRole(user).orElseThrow(AssertionError::new);
        Optional<Role> otherRole = getHighestRole(userToKick);
        // otherRole empty => userToKick is not on the server => kick is allowed as Discord allows it
        boolean userToKickOnServer = otherRole.isPresent();
        return !userToKickOnServer || (ownRole.compareTo(otherRole.get()) > 0);
    }

    /**
     * Checks if the user of the connected account can kick the user.
     * This method also considers the position of the user roles and the owner.
     *
     * @param userToKick The user which should be kicked.
     * @return Whether the user of the connected account can kick the user or not.
     */
    default boolean canYouKickUser(User userToKick) {
        return canKickUser(getApi().getYourself(), userToKick);
    }

    /**
     * Checks if the given user can ban users from the server.
     *
     * @param user The user to check.
     * @return Whether the given user can ban users or not.
     */
    default boolean canBanUsers(User user) {
        return hasAnyPermission(user,
                PermissionType.ADMINISTRATOR,
                PermissionType.BAN_MEMBERS);
    }

    /**
     * Checks if the user of the connected account can ban users from the server.
     *
     * @return Whether the user of the connected account can ban users or not.
     */
    default boolean canYouBanUsers() {
        return canBanUsers(getApi().getYourself());
    }

    /**
     * Checks if the given user can ban the other user.
     * This method also considers the position of the user roles and the owner.
     *
     * @param user The user which "want" to ban the other user.
     * @param userToBan The user which should be banned.
     * @return Whether the given user can ban the other user or not.
     */
    default boolean canBanUser(User user, User userToBan) {
        // owner can always ban, regardless of role for example
        if (isOwner(user)) {
            return true;
        }
        // user cannot ban at all
        if (!canBanUsers(user)) {
            return false;
        }
        // only returns empty optional if user is not member of server
        // but then canBanUsers should already have kicked in
        Role ownRole = getHighestRole(user).orElseThrow(AssertionError::new);
        Optional<Role> otherRole = getHighestRole(userToBan);
        // otherRole empty => userToBan is not on the server => ban is allowed
        boolean userToBanOnServer = otherRole.isPresent();
        return !userToBanOnServer || (ownRole.compareTo(otherRole.get()) > 0);
    }

    /**
     * Checks if the user of the connected account can ban the user.
     * This method also considers the position of the user roles and the owner.
     *
     * @param userToBan The user which should be banned.
     * @return Whether the user of the connected account can ban the user or not.
     */
    default boolean canYouBanUser(User userToBan) {
        return canBanUser(getApi().getYourself(), userToBan);
    }

    @Override
    default Optional<Server> getCurrentCachedInstance() {
        return getApi().getServerById(getId());
    }

}