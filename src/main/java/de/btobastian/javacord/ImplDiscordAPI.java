/*
 * Copyright (C) 2016 Bastian Oppermann
 * 
 * This file is part of Javacord.
 * 
 * Javacord is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser general Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 * 
 * Javacord is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.btobastian.javacord;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import de.btobastian.javacord.entities.*;
import de.btobastian.javacord.entities.impl.ImplApplication;
import de.btobastian.javacord.entities.impl.ImplInvite;
import de.btobastian.javacord.entities.impl.ImplServer;
import de.btobastian.javacord.entities.impl.ImplUser;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.javacord.entities.message.MessageHistory;
import de.btobastian.javacord.entities.message.impl.ImplMessageHistory;
import de.btobastian.javacord.entities.permissions.impl.ImplRole;
import de.btobastian.javacord.exceptions.BadResponseException;
import de.btobastian.javacord.exceptions.PermissionsException;
import de.btobastian.javacord.exceptions.RateLimitedException;
import de.btobastian.javacord.listener.Listener;
import de.btobastian.javacord.listener.server.ServerJoinListener;
import de.btobastian.javacord.listener.user.UserChangeNameListener;
import de.btobastian.javacord.utils.DiscordWebsocket;
import de.btobastian.javacord.utils.LoggerUtil;
import de.btobastian.javacord.utils.ThreadPool;
import org.java_websocket.client.DefaultSSLWebSocketClientFactory;
import org.java_websocket.util.Base64;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;

import javax.imageio.ImageIO;
import javax.net.ssl.SSLContext;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * The implementation of {@link DiscordAPI}.
 */
public class ImplDiscordAPI implements DiscordAPI {

    /**
     * The logger of this class.
     */
    private static final Logger logger = LoggerUtil.getLogger(ImplDiscordAPI.class);

    private final ThreadPool pool;

    private String email = null;
    private String password = null;
    private String token = null;
    private String game = null;
    private boolean idle = false;

    private boolean autoReconnect = true;

    private User you = null;

    private volatile int messageCacheSize = 200;

    private DiscordWebsocket socket = null;

    private RateLimitedException lastRateLimitedException = null;

    private final ConcurrentHashMap<String, Server> servers = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, User> users = new ConcurrentHashMap<>();

    private final ArrayList<Message> messages = new ArrayList<>();

    private final ConcurrentHashMap<Class<?>, List<Listener>> listeners = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, SettableFuture<Server>> waitingForListener = new ConcurrentHashMap<>();

    // let the garbage collector delete old histories
    private final Set<MessageHistory> messageHistories =
            Collections.newSetFromMap(new WeakHashMap<MessageHistory, Boolean>());

    private final Object listenerLock = new Object();
    private final ServerJoinListener listener = new ServerJoinListener() {
        @Override
        public void onServerJoin(DiscordAPI api, Server server) {
            synchronized (listenerLock) { // be sure to read the guild id before trying to check for waiting listeners
                SettableFuture<Server> future = waitingForListener.get(server.getId());
                if (future != null) {
                    logger.debug("Joined or created server {}. We were waiting for this server!", server);
                    waitingForListener.remove(server.getId());
                    future.set(server);
                }
            }
        }
    };

    /**
     * Creates a new instance of this class.
     *
     * @param pool The used pool of the library.
     */
    public ImplDiscordAPI(ThreadPool pool) {
        this.pool = pool;
    }

    @Override
    public void connect(FutureCallback<DiscordAPI> callback) {
        final DiscordAPI api = this;
        Futures.addCallback(pool.getListeningExecutorService().submit(new Callable<DiscordAPI>() {
            @Override
            public DiscordAPI call() throws Exception {
                connectBlocking();
                return api;
            }
        }), callback);
    }

    @Override
    public void connectBlocking() {
        if (token == null || !checkTokenBlocking(token)) {
            if (email == null || password == null) {
                logger.warn("No valid token provided AND missing email or password. Connecting not possible!");
                return;
            }
            token = requestTokenBlocking();
        }
        String gateway = requestGatewayBlocking();
        try {
            socket = new DiscordWebsocket(new URI(gateway), this, false);
            socket.setWebSocketFactory(new DefaultSSLWebSocketClientFactory(SSLContext.getDefault()));
            socket.connect();
        } catch (URISyntaxException | NoSuchAlgorithmException e) {
            logger.warn("Something went wrong while connecting. Please contact the developer!", e);
            return;
        }
        try {
            if (!socket.isReady().get()) {
                throw new IllegalStateException("Socket closed before ready packet was received!");
            }
        } catch (InterruptedException | ExecutionException e) {
            logger.warn("Something went wrong while connecting. Please contact the developer!", e);
        }
    }

    @Override
    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public void setGame(String game) {
        this.game = game;
        try {
            if (socket != null && socket.isReady().isDone() && socket.isReady().get()) {
                socket.updateStatus();
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getGame() {
        return game;
    }

    @Override
    public Server getServerById(String id) {
        return servers.get(id);
    }

    @Override
    public Collection<Server> getServers() {
        return Collections.unmodifiableCollection(servers.values());
    }

    @Override
    public Collection<Channel> getChannels() {
        Collection<Channel> channels = new ArrayList<>();
        for (Server server : getServers()) {
            channels.addAll(server.getChannels());
        }
        return Collections.unmodifiableCollection(channels);
    }

    @Override
    public Channel getChannelById(String id) {
        Iterator<Server> serverIterator = getServers().iterator();
        while (serverIterator.hasNext()) {
            Channel channel = serverIterator.next().getChannelById(id);
            if (channel != null) {
                return channel;
            }
        }
        return null;
    }

    @Override
    public Collection<VoiceChannel> getVoiceChannels() {
        Collection<VoiceChannel> channels = new ArrayList<>();
        for (Server server : getServers()) {
            channels.addAll(server.getVoiceChannels());
        }
        return Collections.unmodifiableCollection(channels);
    }

    @Override
    public VoiceChannel getVoiceChannelById(String id) {
        Iterator<Server> serverIterator = getServers().iterator();
        while (serverIterator.hasNext()) {
            VoiceChannel channel = serverIterator.next().getVoiceChannelById(id);
            if (channel != null) {
                return channel;
            }
        }
        return null;
    }

    @Override
    public Future<User> getUserById(final String id) {
        User user = users.get(id);
        if (user != null) {
            return Futures.immediateFuture(user);
        }
        return getThreadPool().getListeningExecutorService().submit(new Callable<User>() {
            @Override
            public User call() throws Exception {
                logger.debug("Trying request/find user with id {} who isn't cached", id);
                User user = null;
                Iterator<Server> serverIterator = getServers().iterator();
                while (serverIterator.hasNext()) {
                    Server server = serverIterator.next();
                    HttpResponse<JsonNode> response = Unirest
                            .get("https://discordapp.com/api/guilds/" + server.getId() + "/members/" + id)
                            .header("authorization", token)
                            .asJson();
                    // user does not exist
                    if (response.getStatus() < 200 || response.getStatus() > 299) {
                        continue;
                    }
                    user = getOrCreateUser(response.getBody().getObject().getJSONObject("user"));
                    // add user to server
                    ((ImplServer) server).addMember(user);
                    // assign user roles
                    if (response.getBody().getObject().has("roles")) {
                        JSONArray roleIds = response.getBody().getObject().getJSONArray("roles");
                        for (int i = 0; i < roleIds.length(); i++) {
                            // add user to the role
                            ((ImplRole) server.getRoleById(roleIds.getString(i))).addUserNoUpdate(user);
                        }
                    }
                }
                if (user != null) {
                    logger.debug("Found user {} with id {}", user, id);
                } else {
                    logger.debug("No user with id {} was found", id);
                }
                return user;
            }
        });
    }

    @Override
    public User getCachedUserById(String id) {
        return users.get(id);
    }

    @Override
    public Collection<User> getUsers() {
        return Collections.unmodifiableCollection(users.values());
    }

    @Override
    public void registerListener(Listener listener) {
        for (Class<?> implementedInterface : listener.getClass().getInterfaces()) {
            if (Listener.class.isAssignableFrom(implementedInterface)) {
                List<Listener> listenersList = listeners.get(implementedInterface);
                if (listenersList == null) {
                    listenersList = new ArrayList<>();
                    listeners.put(implementedInterface, listenersList);
                }
                synchronized (listenersList) {
                    listenersList.add(listener);
                }
            }
        }
    }

    @Override
    public Message getMessageById(String id) {
        synchronized (messages) {
            for (Message message : messages) {
                if (message.getId().equals(id)) {
                    return message;
                }
            }
        }
        synchronized (messageHistories) {
            for (MessageHistory history : messageHistories) {
                history.getMessageById(id);
            }
        }
        return null;
    }

    @Override
    public ThreadPool getThreadPool() {
        return pool;
    }

    @Override
    public void setIdle(boolean idle) {
        this.idle = idle;
        try {
            if (socket != null && socket.isReady().isDone() && socket.isReady().get()) {
                socket.updateStatus();
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isIdle() {
        return idle;
    }

    @Override
    public String getToken() {
        return token;
    }

    @Override
    public void setToken(String token, boolean bot) {
        this.token = bot ? "Bot " + token : token;
    }

    @Override
    public boolean checkTokenBlocking(String token) {
        try {
            // only the last 0-9 digits of the token should be visible.
            // We don't want someone being able to login to an account by reading the logs.
            logger.debug("Checking token {}", token.replaceAll(".{10}", "**********"));
            HttpResponse<JsonNode> response = Unirest.get("https://discordapp.com/api/users/@me/guilds")
                    .header("authorization", token)
                    .asJson();
            if (response.getStatus() < 200 || response.getStatus() > 299) {
                logger.debug("Checked token {} (valid: {})", token.replaceAll(".{10}", "**********"), false);
                return false;
            }
            logger.debug("Checked token {} (valid: {})", token.replaceAll(".{10}", "**********"), true);
            return true;
        } catch (UnirestException e) {
            return false;
        }
    }

    @Override
    public Future<Server> acceptInvite(String inviteCode) {
        return acceptInvite(inviteCode, null);
    }

    @Override
    public Future<Server> acceptInvite(final String inviteCode, FutureCallback<Server> callback) {
        ListenableFuture<Server> future = getThreadPool().getListeningExecutorService().submit(new Callable<Server>() {
            @Override
            public Server call() throws Exception {
                logger.debug("Trying to accept invite (code: {})", inviteCode);
                final SettableFuture<Server> settableFuture;
                synchronized (listenerLock) {
                    HttpResponse<JsonNode> response = Unirest.post("https://discordapp.com/api/invite/" + inviteCode)
                            .header("authorization", token)
                            .asJson();
                    checkResponse(response);
                    String guildId = response.getBody().getObject().getJSONObject("guild").getString("id");
                    if (getServerById(guildId) != null) {
                        throw new IllegalStateException("Already member of this server!");
                    }
                    logger.info("Accepted invite and waiting for listener to be called (code: {}, server id: {})"
                            , inviteCode, guildId);
                    settableFuture = SettableFuture.create();
                    waitingForListener.put(guildId, settableFuture);
                }
                return settableFuture.get();
            }
        });
        if (callback != null) {
            Futures.addCallback(future, callback);
        }
        return future;
    }

    @Override
    public Future<Server> createServer(String name) {
        return createServer(name, Region.US_WEST, null, null);
    }

    @Override
    public Future<Server> createServer(String name, FutureCallback<Server> callback) {
        return createServer(name, Region.US_WEST, null, callback);
    }

    @Override
    public Future<Server> createServer(String name, Region region) {
        return createServer(name, region, null, null);
    }

    @Override
    public Future<Server> createServer(String name, Region region, FutureCallback<Server> callback) {
        return createServer(name, region, null, callback);
    }

    @Override
    public Future<Server> createServer(String name, BufferedImage icon) {
        return createServer(name, Region.US_WEST, icon, null);
    }

    @Override
    public Future<Server> createServer(String name, BufferedImage icon, FutureCallback<Server> callback) {
        return createServer(name, Region.US_WEST, icon, callback);
    }

    @Override
    public Future<Server> createServer(String name, Region region, BufferedImage icon) {
        return createServer(name, region, icon, null);
    }

    @Override
    public Future<Server> createServer(
            final String name, final Region region, final BufferedImage icon, FutureCallback<Server> callback) {
        ListenableFuture<Server> future = getThreadPool().getListeningExecutorService().submit(new Callable<Server>() {
            @Override
            public Server call() throws Exception {
                logger.debug("Trying to create server (name: {}, region: {}, icon: {}",
                        name, region == null ? "null" : region.getKey(), icon != null);
                if (name == null || name.length() < 2 || name.length() > 100) {
                    throw new IllegalArgumentException("Name must be 2-100 characters long!");
                }
                JSONObject params = new JSONObject();
                if (icon != null) {
                    if (icon.getHeight() != 128 || icon.getWidth() != 128) {
                        throw new IllegalArgumentException("Icon must be 128*128px!");
                    }
                    ByteArrayOutputStream os = new ByteArrayOutputStream();
                    ImageIO.write(icon, "jpg", os);
                    params.put("icon", "data:image/jpg;base64," + Base64.encodeBytes(os.toByteArray()));
                }
                params.put("name", name);
                params.put("region", region == null ? Region.US_WEST.getKey() : region.getKey());
                final SettableFuture<Server> settableFuture;
                synchronized (listenerLock) {
                    HttpResponse<JsonNode> response = Unirest.post("https://discordapp.com/api/guilds")
                            .header("authorization", token)
                            .header("Content-Type", "application/json")
                            .body(params.toString())
                            .asJson();
                    checkResponse(response);
                    String guildId = response.getBody().getObject().getString("id");
                    logger.info("Created server and waiting for listener to be called " +
                            "(name: {}, region: {}, icon: {}, server id: {})"
                            , name, region == null ? "null" : region.getKey(), icon != null, guildId);
                    settableFuture = SettableFuture.create();
                    waitingForListener.put(guildId, settableFuture);
                }
                return settableFuture.get();
            }
        });
        if (callback != null) {
            Futures.addCallback(future, callback);
        }
        return future;
    }

    @Override
    public User getYourself() {
        return you;
    }

    @Override
    public Future<Exception> updateUsername(String newUsername) {
        return updateProfile(newUsername, null, null, null);
    }

    @Override
    public Future<Exception> updateEmail(String newEmail) {
        return updateProfile(null, newEmail, null, null);
    }

    @Override
    public Future<Exception> updatePassword(String newPassword) {
        return updateProfile(null, null, newPassword, null);
    }

    @Override
    public Future<Exception> updateAvatar(BufferedImage newAvatar) {
        return updateProfile(null, null, null, newAvatar);
    }

    @Override
    public Future<Exception> updateProfile(
            final String newUsername, String newEmail, final String newPassword, final BufferedImage newAvatar) {
        logger.debug("Trying to update profile (username: {}, email: {}, password: {}, change avatar: {}",
                newUsername, email, newPassword == null ? "null" : newPassword.replaceAll(".", "*"), newAvatar != null);
        String avatarString = getYourself().getAvatarId();
        if (newAvatar != null) {
            try {
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                ImageIO.write(newAvatar, "jpg", os);
                avatarString = "data:image/jpg;base64," + Base64.encodeBytes(os.toByteArray());
            } catch (IOException ignored) { }
        }
        final JSONObject params = new JSONObject()
                .put("username", newUsername == null ? getYourself().getName() : newUsername)
                .put("email", newEmail == null ? email : newEmail)
                .put("avatar", avatarString == null ? JSONObject.NULL : avatarString)
                .put("password", password);
        if (newPassword != null) {
            params.put("new_password", newPassword);
        }
        return getThreadPool().getExecutorService().submit(new Callable<Exception>() {
            @Override
            public Exception call() throws Exception {
                try {
                    HttpResponse<JsonNode> response = Unirest
                            .patch("https://discordapp.com/api/users/@me")
                            .header("authorization", token)
                            .header("Content-Type", "application/json")
                            .body(params.toString())
                            .asJson();
                    checkResponse(response);
                    logger.info("Updated profile (username: {}, email: {}, password: {}, change avatar: {}",
                            newUsername, email, newPassword == null ? "null" : newPassword.replaceAll(".", "*"),
                            newAvatar != null);
                    ((ImplUser) getYourself()).setAvatarId(response.getBody().getObject().getString("avatar"));
                    setEmail(response.getBody().getObject().getString("email"));
                    setToken(response.getBody().getObject().getString("token"), token.startsWith("Bot "));
                    final String oldName = getYourself().getName();
                    ((ImplUser) getYourself()).setName(response.getBody().getObject().getString("username"));
                    if (newPassword != null) {
                        password = newPassword;
                    }

                    if (!getYourself().getName().equals(oldName)) {
                        getThreadPool().getSingleThreadExecutorService("listeners").submit(new Runnable() {
                            @Override
                            public void run() {
                                List<Listener> listeners = getListeners(UserChangeNameListener.class);
                                synchronized (listeners) {
                                    for (Listener listener : listeners) {
                                        ((UserChangeNameListener) listener)
                                                .onUserChangeName(ImplDiscordAPI.this, getYourself(), oldName);
                                    }
                                }
                            }
                        });
                    }
                } catch (Exception e) {
                    return e;
                }
                return null;
            }
        });
    }

    @Override
    public Future<Invite> parseInvite(String invite) {
        return parseInvite(invite, null);
    }

    @Override
    public Future<Invite> parseInvite(final String invite, FutureCallback<Invite> callback) {
        final String inviteCode = invite.replace("https://discord.gg/", "").replace("http://discord.gg/", "");
        ListenableFuture<Invite> future = getThreadPool().getListeningExecutorService().submit(new Callable<Invite>() {
            @Override
            public Invite call() throws Exception {
                logger.debug("Trying to parse invite {} (parsed code: {})", invite, inviteCode);
                HttpResponse<JsonNode> response = Unirest
                        .get("https://discordapp.com/api/invite/" + inviteCode)
                        .header("authorization", token)
                        .asJson();
                checkResponse(response);
                logger.debug("Parsed invite {} (parsed code: {})", invite, inviteCode);
                return new ImplInvite(ImplDiscordAPI.this, response.getBody().getObject());
            }
        });
        if (callback != null) {
            Futures.addCallback(future, callback);
        }
        return future;
    }

    @Override
    public Future<Exception> deleteInvite(final String inviteCode) {
        return getThreadPool().getExecutorService().submit(new Callable<Exception>() {
            @Override
            public Exception call() throws Exception {
                logger.debug("Trying to delete invite {}", inviteCode);
                try {
                    HttpResponse<JsonNode> response = Unirest
                            .delete("https://discordapp.com/api/invite/" + inviteCode)
                            .header("authorization", token)
                            .asJson();
                    checkResponse(response);
                    logger.info("Deleted invite {}", inviteCode);
                } catch (Exception e) {
                    return e;
                }
                return null;
            }
        });
    }

    @Override
    public void setMessageCacheSize(int size) {
        this.messageCacheSize = size < 0 ? 0 : size;
        synchronized (messages) {
            while (messages.size() > messageCacheSize) {
                messages.remove(0);
            }
        }
    }

    @Override
    public int getMessageCacheSize() {
        return messageCacheSize;
    }

    @Override
    public void reconnect(FutureCallback<DiscordAPI> callback) {
        Futures.addCallback(getThreadPool().getListeningExecutorService().submit(new Callable<DiscordAPI>() {
            @Override
            public DiscordAPI call() throws Exception {
                reconnectBlocking();
                return ImplDiscordAPI.this;
            }
        }), callback);
    }

    @Override
    public void reconnectBlocking() {
        reconnectBlocking(requestGatewayBlocking());
    }

    @Override
    public void setAutoReconnect(boolean autoReconnect) {
        this.autoReconnect = autoReconnect;
    }

    @Override
    public boolean isAutoReconnectEnabled() {
        return autoReconnect;
    }

    @Override
    public Future<String> convertToBotAccount(String ownerToken) {
        return convertToBotAccount(null, ownerToken);
    }

    @Override
    public Future<String> convertToBotAccount(final String applicationId, final String ownerToken) {
        return getThreadPool().getExecutorService().submit(new Callable<String>() {
            @Override
            public String call() throws Exception {
                String id = applicationId;
                logger.debug("Trying to convert account to bot account (application id: {}, owner token: {}",
                        id, ownerToken.replaceAll(".{10}", "**********"));
                if (applicationId == null) {
                    logger.debug("Trying to create application for owner");
                    HttpResponse<JsonNode> response = Unirest.post("https://discordapp.com/api/oauth2/applications")
                            .header("content-type", "application/json")
                            .header("authorization", ownerToken)
                            .body(new JSONObject()
                                    .put("name", getYourself().getName())
                                    .toString())
                            .asJson();
                    checkResponse(response);
                    Application application = new ImplApplication(ImplDiscordAPI.this, response.getBody().getObject());
                    logger.debug("Created application for owner (application: {})", application);
                    id = application.getId();
                }
                HttpResponse<JsonNode> response = Unirest
                        .post("https://discordapp.com/api/oauth2/applications/" + id + "/bot")
                        .header("content-type", "application/json")
                        .header("authorization", ownerToken)
                        .body(new JSONObject().put("token", getToken()).toString())
                        .asJson();
                setToken(response.getBody().getObject().getString("token"), true);
                logger.info("Converted account into bot account (id: {}, new token: {})",
                        id, getToken().replaceAll(".{10}", "**********"));
                return id;
            }
        });
    }

    @Override
    public Future<Collection<Application>> getApplications() {
        return getApplications(null);
    }

    @Override
    public Future<Collection<Application>> getApplications(FutureCallback<Collection<Application>> callback) {
        ListenableFuture<Collection<Application>> future =
                getThreadPool().getListeningExecutorService().submit(new Callable<Collection<Application>>() {
                    @Override
                    public Collection<Application> call() throws Exception {
                        logger.debug("Trying to get applications");
                        HttpResponse<JsonNode> response = Unirest.get("https://discordapp.com/api/oauth2/applications")
                                .header("authorization", token)
                                .asJson();
                        checkResponse(response);
                        JSONArray jsonApplications = response.getBody().getArray();
                        Set<Application> applications = new HashSet<>();
                        for (int i = 0; i < jsonApplications.length(); i++) {
                            applications.add(
                                    new ImplApplication(ImplDiscordAPI.this, jsonApplications.getJSONObject(i)));
                        }
                        logger.debug("Got applications (amount: {})", applications.size());
                        return applications;
                    }
                });
        if (callback != null) {
            Futures.addCallback(future, callback);
        }
        return future;
    }

    @Override
    public Future<Application> createApplication(String name) {
        return createApplication(name, null);
    }

    @Override
    public Future<Application> createApplication(final String name, FutureCallback<Application> callback) {
        ListenableFuture<Application> future =
                getThreadPool().getListeningExecutorService().submit(new Callable<Application>() {
                    @Override
                    public Application call() throws Exception {
                        logger.debug("Trying to create application with name {}", name);
                        HttpResponse<JsonNode> response = Unirest.post("https://discordapp.com/api/oauth2/applications")
                                .header("content-type", "application/json")
                                .header("authorization", getToken())
                                .body(new JSONObject()
                                        .put("name", name)
                                        .toString())
                                .asJson();
                        checkResponse(response);
                        Application application =
                                new ImplApplication(ImplDiscordAPI.this, response.getBody().getObject());
                        logger.debug("Created application {}", application);
                        return application;
                    }
                });
        if (callback != null) {
            Futures.addCallback(future, callback);
        }
        return future;
    }

    @Override
    public Future<Application> getApplication(String id) {
        return getApplication(id, null);
    }

    @Override
    public Future<Application> getApplication(final String id, FutureCallback<Application> callback) {
        ListenableFuture<Application> future =
                getThreadPool().getListeningExecutorService().submit(new Callable<Application>() {
                    @Override
                    public Application call() throws Exception {
                        logger.debug("Trying to get application with id {}", id);
                        HttpResponse<JsonNode> response = Unirest
                                .get("https://discordapp.com/api/oauth2/applications/" + id)
                                .header("Authorization", token)
                                .asJson();
                        checkResponse(response);
                        Application application =
                                new ImplApplication(ImplDiscordAPI.this, response.getBody().getObject());
                        logger.debug("Got application {}", application);
                        return application;

                    }
                });
        if (callback != null) {
            Futures.addCallback(future, callback);
        }
        return future;
    }

    @Override
    public Future<Exception> deleteApplication(final String id) {
        return getThreadPool().getExecutorService().submit(new Callable<Exception>() {
            @Override
            public Exception call() throws Exception {
                try {
                    logger.debug("Trying to delete application with id {}", id);
                    HttpResponse<JsonNode> response = Unirest
                            .delete("https://discordapp.com/api/oauth2/applications/" + id)
                            .header("Authorization", getToken())
                            .asJson();
                    checkResponse(response);
                    logger.debug("Deleted application with id {}", id);
                } catch (Exception e) {
                    return e;
                }
                return null;
            }
        });
    }

    /**
     * Tries to reconnect to the given gateway.
     *
     * @param gateway The gateway to reconnect to.
     */
    public void reconnectBlocking(String gateway) {
        logger.debug("Trying to reconnect to gateway {}", gateway);
        try {
            socket.closeBlocking();
        } catch (InterruptedException e) {
            logger.warn(
                    "We were interrupted when we tried to close the current socket. Please contact the developer!", e);
        }
        if (token == null || !checkTokenBlocking(token)) {
            token = requestTokenBlocking();
        }
        try {
            socket = new DiscordWebsocket(new URI(gateway), this, true);
            socket.setWebSocketFactory(new DefaultSSLWebSocketClientFactory(SSLContext.getDefault()));
            socket.connect();
        } catch (URISyntaxException | NoSuchAlgorithmException e) {
            logger.warn("Reconnect failed. Please contact the developer!", e);
            return;
        }
        try {
            if (!socket.isReady().get()) {
                throw new IllegalStateException("Socket closed before ready packet was received!");
            }
        } catch (InterruptedException | ExecutionException e) {
            logger.warn("Reconnect failed. Please contact the developer!", e);
        }
    }

    /**
     * Checks if we are still rate limited.
     *
     * @throws RateLimitedException If we are rate limited.
     */
    public void checkRateLimit() throws RateLimitedException {
        long retryAt = lastRateLimitedException == null ? 0L : lastRateLimitedException.getRetryAfter();
        long retryAfter = retryAt - System.currentTimeMillis();
        if (retryAfter > 0) {
            throw new RateLimitedException("We are still rate limited for " + retryAfter + " ms!", retryAfter);
        }
    }

    /**
     * Sets yourself.
     *
     * @param user You.
     */
    public void setYourself(User user) {
        this.you = user;
    }

    /**
     * Gets or creates a user based on the given data.
     *
     * @param data The JSONObject containing the data.
     * @return The user.
     */
    public User getOrCreateUser(JSONObject data) {
        String id = data.getString("id");
        User user = users.get(id);
        if (user == null) {
            user = new ImplUser(data, this);
        }
        return user;
    }

    /**
     * Gets the map which contains all known servers.
     *
     * @return The map which contains all known servers.
     */
    public ConcurrentHashMap<String, Server> getServerMap() {
        return servers;
    }

    /**
     * Gets the map which contains all known users.
     *
     * @return The map which contains all known users.
     */
    public ConcurrentHashMap<String, User> getUserMap() {
        return users;
    }

    /**
     * Gets the used websocket.
     *
     * @return The websocket.
     */
    public DiscordWebsocket getSocket() {
        return socket;
    }

    /**
     * Requests a new token.
     *
     * @return The requested token.
     */
    public String requestTokenBlocking() {
        try {
            logger.debug("Trying to request token (email: {}, password: {})", email, password.replaceAll(".", "*"));
            HttpResponse<JsonNode> response = Unirest.post("https://discordapp.com/api/auth/login")
                    .field("email", email)
                    .field("password", password)
                    .asJson();
            JSONObject jsonResponse = response.getBody().getObject();
            if (response.getStatus() == 400) {
                throw new IllegalArgumentException("400 Bad request! Maybe wrong email or password?");
            }
            if (response.getStatus() < 200 || response.getStatus() > 299) {
                throw new IllegalStateException("Received http status code " + response.getStatus()
                        + " with message " + response.getStatusText() + " and body " + response.getBody());
            }
            if (jsonResponse.has("password") || jsonResponse.has("email")) {
                throw new IllegalArgumentException("Wrong email or password!");
            }
            String token = jsonResponse.getString("token");
            logger.debug("Requested token {} (email: {}, password: {})",
                    token.replaceAll(".{10}", "**********"), email, password.replaceAll(".", "*"));
            return token;
        } catch (UnirestException e) {
            logger.warn("Couldn't request token (email: {}, password: {}). Please contact the developer!",
                    email, password.replaceAll(".", "*"), e);
            return null;
        }
    }

    /**
     * Requests the gateway.
     *
     * @return The requested gateway.
     */
    public String requestGatewayBlocking() {
        try {
            logger.debug("Requesting gateway (token: {})", token.replaceAll(".{10}", "**********"));
            HttpResponse<JsonNode> response = Unirest.get("https://discordapp.com/api/gateway")
                    .header("authorization", token)
                    .asJson();
            if (response.getStatus() == 401) {
                throw new IllegalStateException("Cannot request gateway! Invalid token?");
            }
            if (response.getStatus() < 200 || response.getStatus() > 299) {
                throw new IllegalStateException("Received http status code " + response.getStatus()
                        + " with message " + response.getStatusText() + " and body " + response.getBody());
            }
            String gateway = response.getBody().getObject().getString("url");
            logger.debug("Requested gateway {} (token: {})", gateway, token.replaceAll(".{10}", "**********"));
            return gateway;
        } catch (UnirestException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Gets a list with all registers listeners of the given class.
     *
     * @param listenerClass The type of the listener.
     * @return A list with all registers listeners of the given class.
     */
    public List<Listener> getListeners(Class<?> listenerClass) {
        List<Listener> listenersList = listeners.get(listenerClass);
        return listenersList == null ? new ArrayList<Listener>() : listenersList;
    }

    /**
     * Adds a message to the message cache.
     *
     * @param message The message to add.
     */
    public void addMessage(Message message) {
        synchronized (messages) {
            if (messages.size() > messageCacheSize) { // only cache the last 200 messages
                messages.remove(0);
            }
            messages.add(message);
        }
    }

    /**
     * Removes a message from the cache.
     *
     * @param message The message to remove.
     */
    public void removeMessage(Message message) {
        synchronized (messages) {
            messages.remove(message);
        }
        synchronized (messageHistories) {
            for (MessageHistory history : messageHistories) {
                ((ImplMessageHistory) history).removeMessage(message.getId());
            }
        }
    }

    /**
     * Adds a history to the history list.
     *
     * @param history The history to add.
     */
    public void addHistory(MessageHistory history) {
        synchronized (messageHistories) {
            messageHistories.add(history);
        }
    }

    /**
     * Checks the response.
     *
     * @param response The response to check.
     * @throws Exception If the response has problems (status code not between 200 and 300).
     */
    public void checkResponse(HttpResponse<JsonNode> response) throws Exception {
        String message = "";
        if (response.getBody() != null && !response.getBody().isArray() && response.getBody().getObject().has("message")) {
            message = " " + response.getBody().getObject().getString("message");
        }
        if (response.getStatus() == 403) {
            throw new PermissionsException("Missing permissions!" + message);
        }
        if (response.getBody() != null && !response.getBody().isArray() && response.getBody().getObject().has("retry_after")) {
            long retryAfter = response.getBody().getObject().getLong("retry_after");
            RateLimitedException exception =
                    new RateLimitedException("We got rate limited for " + retryAfter + " ms!", retryAfter);
            lastRateLimitedException = exception;
            throw exception;
        }
        if (response.getStatus() < 200 || response.getStatus() > 299) {
            throw new BadResponseException("Received http status code " + response.getStatus() + " with message "
                    + response.getStatusText() + " and body " + response.getBody(), response.getStatus(),
                    response.getStatusText(), response);
        }
    }

    /**
     * Gets a set with all message histories.
     *
     * @return A set with all message histories.
     */
    public Set<MessageHistory> getMessageHistories() {
        return messageHistories;
    }

    /**
     * Gets the internal used server join listener (for server creations and invite accepts).
     *
     * @return The internal used server join listener.
     */
    public ServerJoinListener getInternalServerJoinListener() {
        return listener;
    }

    /**
     * Sets the socket.
     *
     * @param socket The socket to set.
     */
    public void setSocket(DiscordWebsocket socket) {
        this.socket = socket;
    }

}
