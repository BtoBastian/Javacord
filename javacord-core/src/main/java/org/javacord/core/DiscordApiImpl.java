package org.javacord.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.Dns;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.logging.HttpLoggingInterceptor.Level;
import org.apache.logging.log4j.Logger;
import org.javacord.api.DiscordApi;
import org.javacord.api.Javacord;
import org.javacord.api.entity.ApplicationInfo;
import org.javacord.api.entity.activity.Activity;
import org.javacord.api.entity.activity.ActivityType;
import org.javacord.api.entity.channel.Channel;
import org.javacord.api.entity.channel.ChannelCategory;
import org.javacord.api.entity.channel.ChannelType;
import org.javacord.api.entity.channel.PrivateChannel;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.channel.VoiceChannel;
import org.javacord.api.entity.emoji.CustomEmoji;
import org.javacord.api.entity.emoji.KnownCustomEmoji;
import org.javacord.api.entity.intent.Intent;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageSet;
import org.javacord.api.entity.message.UncachedMessageUtil;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.server.invite.Invite;
import org.javacord.api.entity.user.User;
import org.javacord.api.entity.user.UserStatus;
import org.javacord.api.entity.webhook.Webhook;
import org.javacord.api.listener.GloballyAttachableListener;
import org.javacord.api.listener.ObjectAttachableListener;
import org.javacord.api.util.auth.Authenticator;
import org.javacord.api.util.concurrent.ThreadPool;
import org.javacord.api.util.event.ListenerManager;
import org.javacord.api.util.ratelimit.LocalRatelimiter;
import org.javacord.api.util.ratelimit.Ratelimiter;
import org.javacord.core.audio.AudioConnectionImpl;
import org.javacord.core.entity.activity.ActivityImpl;
import org.javacord.core.entity.activity.ApplicationInfoImpl;
import org.javacord.core.entity.emoji.CustomEmojiImpl;
import org.javacord.core.entity.emoji.KnownCustomEmojiImpl;
import org.javacord.core.entity.message.MessageImpl;
import org.javacord.core.entity.message.MessageSetImpl;
import org.javacord.core.entity.message.UncachedMessageUtilImpl;
import org.javacord.core.entity.server.ServerImpl;
import org.javacord.core.entity.server.invite.InviteImpl;
import org.javacord.core.entity.user.Member;
import org.javacord.core.entity.user.MemberImpl;
import org.javacord.core.entity.user.UserImpl;
import org.javacord.core.entity.user.UserPresence;
import org.javacord.core.entity.webhook.WebhookImpl;
import org.javacord.core.util.ClassHelper;
import org.javacord.core.util.Cleanupable;
import org.javacord.core.util.cache.JavacordEntityCache;
import org.javacord.core.util.concurrent.ThreadPoolImpl;
import org.javacord.core.util.event.DispatchQueueSelector;
import org.javacord.core.util.event.EventDispatcher;
import org.javacord.core.util.event.ListenerManagerImpl;
import org.javacord.core.util.gateway.DiscordWebSocketAdapter;
import org.javacord.core.util.http.ProxyAuthenticator;
import org.javacord.core.util.http.TrustAllTrustManager;
import org.javacord.core.util.logging.LoggerUtil;
import org.javacord.core.util.ratelimit.RatelimitManager;
import org.javacord.core.util.rest.RestEndpoint;
import org.javacord.core.util.rest.RestMethod;
import org.javacord.core.util.rest.RestRequest;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.net.Proxy;
import java.net.ProxySelector;
import java.time.Duration;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

/**
 * The implementation of {@link DiscordApi}.
 */
public class DiscordApiImpl implements DiscordApi, DispatchQueueSelector {

    /**
     * The logger of this class.
     */
    private static final Logger logger = LoggerUtil.getLogger(DiscordApiImpl.class);

    /**
     * A map with the default global ratelimiter.
     *
     * <p>The key is the bot's token (because ratelimits are per account) and the key is the ratelimiter for this token.
     */
    private static final Map<String, Ratelimiter> defaultGlobalRatelimiter = new ConcurrentHashMap<>();

    /**
     * The key prefix for bot accounts.
     */
    private static final String BOT_TOKEN_PREFIX = "Bot ";

    /**
     * The thread pool which is used internally.
     */
    private final ThreadPoolImpl threadPool = new ThreadPoolImpl();

    /**
     * The http client for this instance.
     */
    private final OkHttpClient httpClient;

    /**
     * The event dispatcher.
     */
    private final EventDispatcher eventDispatcher;

    /**
     * The object mapper for this instance.
     */
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * The ratelimit manager for this bot.
     */
    private final RatelimitManager ratelimitManager = new RatelimitManager(this);

    /**
     * The utility class to interact with uncached messages.
     */
    private final UncachedMessageUtil uncachedMessageUtil = new UncachedMessageUtilImpl(this);

    /**
     * The websocket adapter used to connect to Discord.
     */
    private volatile DiscordWebSocketAdapter websocketAdapter = null;

    /**
     * The token used for authentication.
     */
    private final String token;

    /**
     * Whether the {@link #disconnect()} method has been called before or not.
     */
    private volatile boolean disconnectCalled = false;

    /**
     * A lock to synchronize on {@link DiscordApiImpl#disconnectCalled}.
     */
    private final Object disconnectCalledLock = new Object();

    /**
     * The status which should be displayed for the bot.
     */
    private volatile UserStatus status = UserStatus.ONLINE;

    /**
     * The activity which should be displayed. May be <code>null</code>.
     */
    private volatile Activity activity;

    /**
     * The default message cache capacity which is applied for every newly created channel.
     */
    private volatile int defaultMessageCacheCapacity = 50;

    /**
     * The default maximum age of cached messages.
     */
    private volatile int defaultMessageCacheStorageTimeInSeconds = 60 * 60 * 12;

    /**
     * Whether automatic message cache cleanup is enabled by default.
     */
    private boolean defaultAutomaticMessageCacheCleanupEnabled = true;

    /**
     * The function to calculate the reconnect delay.
     */
    private volatile Function<Integer, Integer> reconnectDelayProvider;

    /**
     * The current shard of the bot.
     */
    private final int currentShard;

    /**
     * The total amount of shards.
     */
    private final int totalShards;

    /**
     * The intents to be set.
     */
    private final Set<Intent> intents;

    /**
     * Whether Javacord should wait for all servers to become available on startup or not.
     */
    private final boolean waitForServersOnStartup;

    /**
     * Whether Javacord should wait for all users to get cached on startup or not.
     */
    private final boolean waitForUsersOnStartup;

    /**
     * The latest gateway latency.
     */
    private volatile long latestGatewayLatencyNanos = -1;

    /**
     * A lock that makes sure that there are not more than one ping attempt at the same time.
     * This ensures that the ping does not get affected by other requests that use the same bucket.
     */
    private final Lock restLatencyLock = new ReentrantLock();

    /**
     * A ratelimiter that is used for global ratelimits.
     */
    private final Ratelimiter globalRatelimiter;

    /**
     * The proxy selector which should be used to determine the proxies that should be used to connect to the Discord
     * REST API and websocket.
     */
    private final ProxySelector proxySelector;

    /**
     * The proxy which should be used to connect to the Discord REST API and websocket.
     */
    private final Proxy proxy;

    /**
     * The authenticator that should be used to authenticate against proxies that require it.
     */
    private final Authenticator proxyAuthenticator;

    /**
     * Whether to trust all SSL certificates.
     */
    private final boolean trustAllCertificates;

    /**
     * The user of the connected account.
     */
    private volatile User you;

    /**
     * The client id of the application.
     */
    private volatile long clientId = -1;

    /**
     * The id of the application's owner.
     */
    private volatile long ownerId = -1;

    /**
     * The time offset between the Discord time and our local time.
     */
    private volatile Long timeOffset = null;

    /**
     * An immutable cache with all Javacord entities.
     */
    private final AtomicReference<JavacordEntityCache> entityCache = new AtomicReference<>(JavacordEntityCache.empty());

    /**
     * Whether the user cache is enabled or not.
     */
    private final boolean userCacheEnabled;

    /**
     * A map which contains all servers that are ready.
     */
    private final ConcurrentHashMap<Long, Server> servers = new ConcurrentHashMap<>();

    /**
     * A map with audio connections. The key is the server id.
     */
    private final ConcurrentHashMap<Long, AudioConnectionImpl> audioConnections = new ConcurrentHashMap<>();

    /**
     * A map with pending audio connections. The key is the server id.
     * A pending connection is a connect that is currently trying to connect to a websocket and establish an udp
     * connection but has not finished.
     * The field might still be set, even though the connection is no longer pending!
     */
    private final ConcurrentHashMap<Long, AudioConnectionImpl> pendingAudioConnections = new ConcurrentHashMap<>();

    /**
     * A map which contains all servers that are not ready.
     */
    private final ConcurrentHashMap<Long, Server> nonReadyServers = new ConcurrentHashMap<>();

    /**
     * A set with all unavailable servers.
     */
    private final HashSet<Long> unavailableServers = new HashSet<>();

    /**
     * A map with all known custom emoji.
     */
    private final ConcurrentHashMap<Long, KnownCustomEmoji> customEmojis = new ConcurrentHashMap<>();

    /**
     * A map with all cached messages.
     */
    private final Map<Long, WeakReference<Message>> messages = Collections.synchronizedMap(new ConcurrentHashMap<>());

    /**
     * A map to retrieve message IDs by the weak ref that point to the respective
     * message or used to point to it for usage in the messages cleanup,
     * as at cleanup time the weak ref is already emptied.
     */
    private final Map<Reference<? extends Message>, Long> messageIdByRef
            = Collections.synchronizedMap(new WeakHashMap<>());

    /**
     * The queue that is notified if a message became weakly-reachable.
     */
    private final ReferenceQueue<Message> messagesCleanupQueue = new ReferenceQueue<>();

    /**
     * A map which contains all globally attachable listeners.
     * The key is the class of the listener.
     */
    private final Map<Class<? extends GloballyAttachableListener>,
            Map<GloballyAttachableListener, ListenerManagerImpl<? extends GloballyAttachableListener>>>
            listeners = Collections.synchronizedMap(new ConcurrentHashMap<>());

    /**
     * A map which contains all listeners which are assigned to a specific object instead of being global.
     * The key of the outer map is the class which the listener was registered to (e.g. Message.class).
     * The key of the first inner map is the id of the object.
     * The key of the second inner map is the class of the listener.
     * The key of the third inner map is the listener itself.
     * The final value is the listener manager.
     */
    private final Map<Class<?>, Map<Long, Map<Class<? extends ObjectAttachableListener>,
            Map<ObjectAttachableListener, ListenerManagerImpl<? extends ObjectAttachableListener>>>>>
            objectListeners = Collections.synchronizedMap(new ConcurrentHashMap<>());

    /**
     * Creates a new discord api instance that can be used for auto-ratelimited REST calls,
     * but does not connect to the Discord WebSocket.
     *
     * @param token                The token used to connect without any account type specific prefix.
     * @param globalRatelimiter    The ratelimiter used for global ratelimits.
     * @param proxySelector        The proxy selector which should be used to determine the proxies that should be used
     *                             to connect to the Discord REST API and websocket.
     * @param proxy                The proxy which should be used to connect to the Discord REST API and websocket.
     * @param proxyAuthenticator   The authenticator that should be used to authenticate against proxies that require
     *                             it.
     * @param trustAllCertificates Whether to trust all SSL certificates.
     */
    public DiscordApiImpl(String token, Ratelimiter globalRatelimiter, ProxySelector proxySelector, Proxy proxy,
                          Authenticator proxyAuthenticator, boolean trustAllCertificates) {
        this(token, 0, 1, Collections.emptySet(), true, false, globalRatelimiter,
                proxySelector, proxy, proxyAuthenticator, trustAllCertificates, null);
    }

    /**
     * Creates a new discord api instance.
     *
     * @param token                   The token used to connect without any account type specific prefix.
     * @param currentShard            The current shard the bot should connect to.
     * @param totalShards             The total amount of shards.
     * @param intents                  The intents for the events which should be received.
     * @param waitForServersOnStartup Whether Javacord should wait for all servers
     *                                to become available on startup or not.
     * @param waitForUsersOnStartup   Whether Javacord should wait for all users
     *                                to become available on startup or not.
     * @param globalRatelimiter       The ratelimiter used for global ratelimits.
     * @param proxySelector           The proxy selector which should be used to determine the proxies that should be
     *                                used to connect to the Discord REST API and websocket.
     * @param proxy                   The proxy which should be used to connect to the Discord REST API and websocket.
     * @param proxyAuthenticator      The authenticator that should be used to authenticate against proxies that require
     *                                it.
     * @param trustAllCertificates    Whether to trust all SSL certificates.
     * @param ready                   The future which will be completed when the connection to Discord was successful.
     */
    public DiscordApiImpl(
            String token,
            int currentShard,
            int totalShards,
            Set<Intent> intents,
            boolean waitForServersOnStartup,
            boolean waitForUsersOnStartup,
            Ratelimiter globalRatelimiter,
            ProxySelector proxySelector,
            Proxy proxy,
            Authenticator proxyAuthenticator,
            boolean trustAllCertificates,
            CompletableFuture<DiscordApi> ready
    ) {
        this(token, currentShard, totalShards, intents, waitForServersOnStartup, waitForUsersOnStartup,
                true, globalRatelimiter,  proxySelector, proxy, proxyAuthenticator, trustAllCertificates, ready, null,
                Collections.emptyMap(), Collections.emptyList());
    }

    /**
     * Creates a new discord api instance.
     *
     * @param token                   The token used to connect without any account type specific prefix.
     * @param currentShard            The current shard the bot should connect to.
     * @param totalShards             The total amount of shards.
     * @param intents                  The intents for the events which should be received.
     * @param waitForServersOnStartup Whether Javacord should wait for all servers
     *                                to become available on startup or not.
     * @param waitForUsersOnStartup   Whether Javacord should wait for all users
     *                                to become available on startup or not.
     * @param globalRatelimiter       The ratelimiter used for global ratelimits.
     * @param proxySelector           The proxy selector which should be used to determine the proxies that should be
     *                                used to connect to the Discord REST API and websocket.
     * @param proxy                   The proxy which should be used to connect to the Discord REST API and websocket.
     * @param proxyAuthenticator      The authenticator that should be used to authenticate against proxies that require
     *                                it.
     * @param trustAllCertificates    Whether to trust all SSL certificates.
     * @param ready                   The future which will be completed when the connection to Discord was successful.
     * @param dns                     The DNS instance to use in the OkHttp client. This should only be used in testing.
     */
    private DiscordApiImpl(
            String token,
            int currentShard,
            int totalShards,
            Set<Intent> intents,
            boolean waitForServersOnStartup,
            boolean waitForUsersOnStartup,
            Ratelimiter globalRatelimiter,
            ProxySelector proxySelector,
            Proxy proxy,
            Authenticator proxyAuthenticator,
            boolean trustAllCertificates,
            CompletableFuture<DiscordApi> ready,
            Dns dns) {
        this(token, currentShard, totalShards, intents, waitForServersOnStartup, waitForUsersOnStartup,
                true, globalRatelimiter, proxySelector, proxy, proxyAuthenticator, trustAllCertificates, ready, dns,
                Collections.emptyMap(), Collections.emptyList());
    }

    /**
     * Creates a new discord api instance.
     * @param token                   The token used to connect without any account type specific prefix.
     * @param currentShard            The current shard the bot should connect to.
     * @param totalShards             The total amount of shards.
     * @param intents                 The intents for the events which should be received.
     * @param waitForServersOnStartup Whether Javacord should wait for all servers
     *                                to become available on startup or not.
     * @param waitForUsersOnStartup   Whether Javacord should wait for all users
     *                                to become available on startup or not.
     * @param registerShutdownHook    Whether the shutdown hook should be registered or not.
     * @param globalRatelimiter       The ratelimiter used for global ratelimits.
     * @param proxySelector           The proxy selector which should be used to determine the proxies that should be
     *                                used to connect to the Discord REST API and websocket.
     * @param proxy                   The proxy which should be used to connect to the Discord REST API and websocket.
     * @param proxyAuthenticator      The authenticator that should be used to authenticate against proxies that require
     *                                it.
     * @param trustAllCertificates    Whether to trust all SSL certificates.
     * @param ready                   The future which will be completed when the connection to Discord was successful.
     * @param dns                     The DNS instance to use in the OkHttp client. This should only be used in testing.
     * @param listenerSourceMap       The functions to create listeners for pre-registration.
     * @param unspecifiedListeners    The listeners of unspecified types to pre-register.
     */
    @SuppressWarnings("unchecked")
    public DiscordApiImpl(
            String token,
            int currentShard,
            int totalShards,
            Set<Intent> intents,
            boolean waitForServersOnStartup,
            boolean waitForUsersOnStartup,
            boolean registerShutdownHook,
            Ratelimiter globalRatelimiter,
            ProxySelector proxySelector,
            Proxy proxy,
            Authenticator proxyAuthenticator,
            boolean trustAllCertificates,
            CompletableFuture<DiscordApi> ready,
            Dns dns,
            Map<Class<? extends GloballyAttachableListener>,
                    List<Function<DiscordApi,GloballyAttachableListener>>
                    > listenerSourceMap,
            List<Function<DiscordApi, GloballyAttachableListener>> unspecifiedListeners) {
        this.token = token;
        this.currentShard = currentShard;
        this.totalShards = totalShards;
        this.waitForServersOnStartup = waitForServersOnStartup;
        this.waitForUsersOnStartup = waitForUsersOnStartup;
        this.globalRatelimiter = globalRatelimiter;
        this.proxySelector = proxySelector;
        this.proxy = proxy;
        this.proxyAuthenticator = proxyAuthenticator;
        this.trustAllCertificates = trustAllCertificates;
        this.intents = intents;
        userCacheEnabled = intents.contains(Intent.GUILD_MEMBERS);
        this.reconnectDelayProvider = x ->
                (int) Math.round(Math.pow(x, 1.5) - (1 / (1 / (0.1 * x) + 1)) * Math.pow(x, 1.5));

        if ((proxySelector != null) && (proxy != null)) {
            throw new IllegalStateException("proxy and proxySelector must not be configured both");
        }

        if (!intents.contains(Intent.GUILD_MEMBERS) && isWaitingForUsersOnStartup()) {
            throw new IllegalArgumentException("Cannot wait for users when GUILD_MEMBERS intent is not set!");
        }

        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder()
                .addInterceptor(chain -> chain.proceed(chain.request()
                        .newBuilder()
                        .addHeader("User-Agent", Javacord.USER_AGENT)
                        .build()))
                .addInterceptor(
                        new HttpLoggingInterceptor(LoggerUtil.getLogger(OkHttpClient.class)::trace).setLevel(Level.BODY)
                )
                .proxyAuthenticator(new ProxyAuthenticator(proxyAuthenticator))
                .proxy(proxy);
        if (proxySelector != null) {
            httpClientBuilder.proxySelector(proxySelector);
        }
        if (dns != null) {
            httpClientBuilder.dns(dns);
        }
        if (trustAllCertificates) {
            logger.warn("All SSL certificates are trusted when connecting to the Discord API and websocket. "
                    + "This increases the risk of man-in-the-middle attacks!");
            TrustAllTrustManager trustManager = new TrustAllTrustManager();
            httpClientBuilder.sslSocketFactory(trustManager.createSslSocketFactory(), trustManager);
        }
        this.httpClient = httpClientBuilder.build();
        this.eventDispatcher = new EventDispatcher(this);

        if (ready != null) {
            getThreadPool().getExecutorService().submit(() -> {
                try {
                    this.websocketAdapter = new DiscordWebSocketAdapter(this);
                    this.websocketAdapter.isReady().whenComplete((readyReceived, throwable) -> {
                        if (readyReceived) {
                            // Register listeners
                            listenerSourceMap.forEach((clazz, listenerSources) ->
                                    listenerSources.forEach(listenerSource -> {
                                        Class<GloballyAttachableListener> type
                                                = (Class<GloballyAttachableListener>) clazz;
                                        GloballyAttachableListener listener = listenerSource.apply(this);
                                        addListener(type, type.cast(listener));
                                    }
                            ));
                            unspecifiedListeners.stream()
                                    .map(source -> source.apply(this))
                                    .forEach(this::addListener);
                            // Application information
                            getApplicationInfo().whenComplete((applicationInfo, exception) -> {
                                if (exception != null) {
                                    logger.error("Could not access self application info on startup!", exception);
                                } else {
                                    clientId = applicationInfo.getClientId();
                                    ownerId = applicationInfo.getOwnerId();
                                }
                                ready.complete(this);
                            });
                        } else {
                            threadPool.shutdown();
                            ready.completeExceptionally(
                                    new IllegalStateException("Websocket closed before READY packet was received!"));
                        }
                    });
                } catch (Throwable t) {
                    if (websocketAdapter != null) {
                        websocketAdapter.disconnect();
                    }
                    ready.completeExceptionally(t);
                }
            });

            // After minimum JDK 9 is required this can be switched to use a Cleaner
            getThreadPool().getScheduler().scheduleWithFixedDelay(() -> {
                try {
                    for (Reference<? extends Message> messageRef = messagesCleanupQueue.poll();
                            messageRef != null;
                            messageRef = messagesCleanupQueue.poll()) {
                        Long messageId = messageIdByRef.remove(messageRef);
                        if (messageId != null) {
                            messages.remove(messageId, messageRef);
                        }
                    }
                } catch (Throwable t) {
                    logger.error("Failed to process messages cleanup queue!", t);
                }
            }, 30, 30, TimeUnit.SECONDS);

            if (registerShutdownHook) {
                // Add shutdown hook
                ready.thenAccept(api -> {
                    WeakReference<DiscordApi> discordApiReference = new WeakReference<>(api);
                    Runtime.getRuntime().addShutdownHook(new Thread(() -> Optional.ofNullable(discordApiReference.get())
                            .ifPresent(DiscordApi::disconnect),
                            String.format("Javacord - Shutdown Disconnector (%s)", api)));
                });
            }
        } else {
            if (registerShutdownHook) {
                WeakReference<DiscordApi> discordApiReference = new WeakReference<>(this);
                Runtime.getRuntime().addShutdownHook(new Thread(() -> Optional.ofNullable(discordApiReference.get())
                        .ifPresent(DiscordApi::disconnect),
                        String.format("Javacord - Shutdown Disconnector (%s)", this)));
            }
        }
    }

    /**
     * Gets the entity cache.
     *
     * @return The entity cache.
     */
    public AtomicReference<JavacordEntityCache> getEntityCache() {
        return entityCache;
    }

    /**
     * Checks if the user cache is enabled.
     *
     * @return Whetehr or not teh user cache is enabled.
     */
    public boolean hasUserCacheEnabled() {
        return userCacheEnabled;
    }

    /**
     * Gets the used {@link OkHttpClient http client} for this api instance.
     *
     * @return The used http client.
     */
    public OkHttpClient getHttpClient() {
        if (disconnectCalled) {
            throw new IllegalStateException("disconnect was called already");
        }
        return httpClient;
    }

    /**
     * Gets the event dispatcher which is used to dispatch events.
     *
     * @return The used event dispatcher.
     */
    public EventDispatcher getEventDispatcher() {
        return eventDispatcher;
    }

    /**
     * Gets the ratelimit manager for this bot.
     *
     * @return The ratelimit manager for this bot.
     */
    public RatelimitManager getRatelimitManager() {
        return ratelimitManager;
    }

    /**
     * Gets the object mapper used by this api instance.
     *
     * @return The object mapper used by this api instance.
     */
    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    /**
     * Purges all cached entities.
     * This method is only meant to be called after receiving a READY packet.
     */
    public void purgeCache() {
        servers.values().stream()
                .map(Cleanupable.class::cast)
                .forEach(Cleanupable::cleanup);
        servers.clear();
        entityCache.get().getChannelCache().getChannels().stream()
                .filter(Cleanupable.class::isInstance)
                .map(Cleanupable.class::cast)
                .forEach(Cleanupable::cleanup);
        entityCache.set(JavacordEntityCache.empty());
        unavailableServers.clear();
        customEmojis.clear();
        messages.clear();
        messageIdByRef.clear();
        timeOffset = null;
    }

    /**
     * Gets the audio connection for the server with the given id.
     *
     * @param serverId The server id.
     * @return The audio connection.
     */
    public AudioConnectionImpl getAudioConnectionByServerId(long serverId) {
        return audioConnections.get(serverId);
    }

    /**
     * Sets the audio connection for the server with the given id.
     *
     * @param serverId The server id.
     * @param connection The audio connection.
     */
    public void setAudioConnection(long serverId, AudioConnectionImpl connection) {
        audioConnections.put(serverId, connection);
    }

    /**
     * Removes the audio connection for the server with the given id.
     *
     * @param serverId The server id.
     */
    public void removeAudioConnection(long serverId) {
        audioConnections.remove(serverId);
    }

    /**
     * Gets the pending audio connection for the server with the given id.
     *
     * @param serverId The server id.
     * @return The pending audio connection.
     */
    public AudioConnectionImpl getPendingAudioConnectionByServerId(long serverId) {
        return pendingAudioConnections.get(serverId);
    }

    /**
     * Sets the audio pending connection for the server with the given id.
     *
     * @param serverId The server id.
     * @param connection The pending audio connection.
     */
    public void setPendingAudioConnection(long serverId, AudioConnectionImpl connection) {
        pendingAudioConnections.put(serverId, connection);
    }

    /**
     * Removes the pending audio connection for the server with the given id.
     *
     * @param serverId The server id.
     */
    public void removePendingAudioConnection(long serverId) {
        pendingAudioConnections.remove(serverId);
    }

    /**
     * Gets a collection with all servers, including ready and not ready ones.
     *
     * @return A collection with all servers.
     */
    public Collection<Server> getAllServers() {
        ArrayList<Server> allServers = new ArrayList<>(nonReadyServers.values());
        allServers.addAll(servers.values());
        return Collections.unmodifiableList(allServers);
    }

    /**
     * Gets a server by it's id, including ready and not ready ones.
     *
     * @param id The of the server.
     * @return The server with the given id.
     */
    public Optional<Server> getPossiblyUnreadyServerById(long id) {
        if (nonReadyServers.containsKey(id)) {
            return Optional.ofNullable(nonReadyServers.get(id));
        }
        return Optional.ofNullable(servers.get(id));
    }

    /**
     * Adds the given server to the cache.
     *
     * @param server The server to add.
     */
    public void addServerToCache(ServerImpl server) {
        // Remove in case, there's an old instance in cache
        removeServerFromCache(server.getId());

        nonReadyServers.put(server.getId(), server);
        server.addServerReadyConsumer(s -> {
            nonReadyServers.remove(s.getId());
            removeUnavailableServerFromCache(s.getId());
            servers.put(s.getId(), s);
        });
    }

    /**
     * Removes the given server from the cache.
     *
     * @param serverId The id of the server to remove.
     */
    public void removeServerFromCache(long serverId) {
        servers.computeIfPresent(serverId, (key, server) -> {
            ((Cleanupable) server).cleanup();
            return null;
        });
        nonReadyServers.computeIfPresent(serverId, (key, server) -> {
            ((Cleanupable) server).cleanup();
            return null;
        });
    }

    /**
     * Adds a channel to the cache.
     *
     * @param channel The channel to add.
     */
    public void addChannelToCache(Channel channel) {
        entityCache.getAndUpdate(cache -> {
            Channel oldChannel = cache.getChannelCache().getChannelById(channel.getId()).orElse(null);
            if (oldChannel != channel && oldChannel instanceof Cleanupable) {
                ((Cleanupable) oldChannel).cleanup();
            }
            return cache.updateChannelCache(channelCache -> channelCache.addChannel(channel));
        });
    }

    /**
     * Updates a user presence in the cache.
     *
     * @param userId The id of the user.
     * @param mapper A function that takes the old user presence (or null) and returns the new user presence.
     */
    public void updateUserPresence(long userId, UnaryOperator<UserPresence> mapper) {
        entityCache.getAndUpdate(cache -> {
            UserPresence presence = cache.getUserPresenceCache().getPresenceByUserId(userId)
                    .orElseGet(() -> new UserPresence(userId, null, null, io.vavr.collection.HashMap.empty()));
            return cache.updateUserPresenceCache(userPresenceCache ->
                    userPresenceCache.removeUserPresence(presence).addUserPresence(mapper.apply(presence)));
        });
    }

    /**
     * Removes a channel from the cache.
     *
     * @param channelId The id of the channel to remove.
     */
    public void removeChannelFromCache(long channelId) {
        entityCache.getAndUpdate(cache -> {
            Channel channel = cache.getChannelCache().getChannelById(channelId).orElse(null);
            if (channel == null) {
                return cache;
            }
            if (channel instanceof Cleanupable) {
                ((Cleanupable) channel).cleanup();
            }
            return cache.updateChannelCache(channelCache -> channelCache.removeChannel(channel));
        });
    }

    /**
     * Adds a member to the cache.
     *
     * @param member The member to add.
     */
    public void addMemberToCacheOrReplaceExisting(Member member) {
        entityCache.getAndUpdate(cache -> {
            Member oldMember = cache.getMemberCache()
                    .getMemberByIdAndServer(member.getId(), member.getServer().getId())
                    .orElse(null);
            return cache.updateMemberCache(memberCache -> memberCache.removeMember(oldMember).addMember(member));
        });
    }

    /**
     * Updates the user object for all members in the cache.
     *
     * @param user The new user object.
     */
    public void updateUserOfAllMembers(User user) {
        entityCache.getAndUpdate(cache ->  {
            JavacordEntityCache newCache = cache;
            for (Member member : cache.getMemberCache().getMembersById(user.getId())) {
                newCache = newCache.updateMemberCache(memberCache -> memberCache
                        .removeMember(member)
                        .addMember(((MemberImpl) member).setUser((UserImpl) user))
                );
            }
            return newCache;
        });
    }

    /**
     * Removes a member from the cache.
     *
     * @param memberId The id of the member to remove.
     * @param serverId The id of the member's server.
     */
    public void removeMemberFromCache(long memberId, long serverId) {
        entityCache.getAndUpdate(cache -> {
            Member member = cache.getMemberCache().getMemberByIdAndServer(memberId, serverId).orElse(null);
            if (member == null) {
                return cache;
            }
            return cache.updateMemberCache(memberCache -> memberCache.removeMember(member));
        });
    }

    /**
     * Adds a server id to the list with unavailable servers.
     *
     * @param serverId The id of the server.
     */
    public void addUnavailableServerToCache(long serverId) {
        unavailableServers.add(serverId);
    }

    /**
     * Removes a server id from the list with unavailable servers.
     *
     * @param serverId The id of the server.
     */
    private void removeUnavailableServerFromCache(long serverId) {
        unavailableServers.remove(serverId);
    }

    /**
     * Gets the latest gateway latency in nano seconds.
     *
     * @return The latest gateway latency.
     */
    public long getLatestGatewayLatencyNanos() {
        return latestGatewayLatencyNanos;
    }

    /**
     * Sets the latest gateway latency in nano seconds.
     *
     * @param latestGatewayLatencyNanos The latest gateway latency.
     */
    public void setLatestGatewayLatencyNanos(long latestGatewayLatencyNanos) {
        this.latestGatewayLatencyNanos = latestGatewayLatencyNanos;
    }

    /**
     * Sets the user of the connected account.
     *
     * @param yourself The user of the connected account.
     */
    public void setYourself(User yourself) {
        you = yourself;
    }

    /**
     * Gets the time offset between the Discord time and our local time.
     * Might be <code>null</code> if it hasn't been calculated yet.
     *
     * @return The time offset between the Discord time and our local time.
     */
    public Long getTimeOffset() {
        return timeOffset;
    }

    /**
     * Sets the time offset between the Discord time and our local time.
     *
     * @param timeOffset The time offset to set.
     */
    public void setTimeOffset(Long timeOffset) {
        this.timeOffset = timeOffset;
    }

    /**
     * Gets or creates a new known custom emoji object.
     *
     * @param server The server of the emoji.
     * @param data The data of the emoji.
     * @return The emoji for the given json object.
     */
    public KnownCustomEmoji getOrCreateKnownCustomEmoji(Server server, JsonNode data) {
        long id = Long.parseLong(data.get("id").asText());
        return customEmojis.computeIfAbsent(id, key -> new KnownCustomEmojiImpl(this, server, data));
    }

    /**
     * Gets a known custom emoji or creates a new (unknown) custom emoji object.
     *
     * @param data The data of the emoji.
     * @return The emoji for the given json object.
     */
    public CustomEmoji getKnownCustomEmojiOrCreateCustomEmoji(JsonNode data) {
        long id = Long.parseLong(data.get("id").asText());
        CustomEmoji emoji = customEmojis.get(id);
        return emoji == null ? new CustomEmojiImpl(this, data) : emoji;
    }

    /**
     * Gets a known custom emoji or creates a new (unknown) custom emoji object.
     *
     * @param id The id of the emoji.
     * @param name The name of the emoji.
     * @param animated Whether the emoji is animated or not.
     * @return The emoji for the given json object.
     */
    public CustomEmoji getKnownCustomEmojiOrCreateCustomEmoji(long id, String name, boolean animated) {
        CustomEmoji emoji = customEmojis.get(id);
        return emoji == null ? new CustomEmojiImpl(this, id, name, animated) : emoji;
    }

    /**
     * Removes a known custom emoji object.
     *
     * @param emoji The emoji to remove.
     */
    public void removeCustomEmoji(KnownCustomEmoji emoji) {
        customEmojis.remove(emoji.getId());
    }

    /**
     * Gets or creates a new message object.
     *
     * @param channel The channel of the message.
     * @param data The data of the message.
     * @return The message for the given json object.
     */
    public Message getOrCreateMessage(TextChannel channel, JsonNode data) {
        long id = Long.parseLong(data.get("id").asText());
        synchronized (messages) {
            return getCachedMessageById(id).orElseGet(() -> new MessageImpl(this, channel, data));
        }
    }

    /**
     * Adds a message to the cache.
     *
     * @param message The message to add.
     */
    public void addMessageToCache(Message message) {
        messages.compute(message.getId(), (key, value) -> {
            if ((value == null) || (value.get() == null)) {
                WeakReference<Message> result = new WeakReference<>(message, messagesCleanupQueue);
                messageIdByRef.put(result, key);
                return result;
            }
            return value;
        });
    }

    /**
     * Removes a message from the cache.
     *
     * @param messageId The id of the message to remove.
     */
    public void removeMessageFromCache(long messageId) {
        WeakReference<Message> messageRef = messages.remove(messageId);
        if (messageRef != null) {
            messageIdByRef.remove(messageRef, messageId);
        }
    }

    /**
     * Adds an object listener.
     * Adding a listener multiple times to the same object will only add it once
     * and return the same listener manager on each invocation.
     * The order of invocation is according to first addition.
     *
     * @param objectClass The class of the object.
     * @param objectId The id of the object.
     * @param listenerClass The listener class.
     * @param listener The listener to add.
     * @param <T> The type of the listener.
     * @return The manager for the added listener.
     */
    @SuppressWarnings("unchecked")
    public <T extends ObjectAttachableListener> ListenerManager<T> addObjectListener(
            Class<?> objectClass, long objectId, Class<T> listenerClass, T listener) {
        Map<ObjectAttachableListener, ListenerManagerImpl<? extends ObjectAttachableListener>> listeners =
                objectListeners
                        .computeIfAbsent(objectClass, key -> new ConcurrentHashMap<>())
                        .computeIfAbsent(objectId, key -> new ConcurrentHashMap<>())
                        .computeIfAbsent(listenerClass, c -> Collections.synchronizedMap(new LinkedHashMap<>()));
        return (ListenerManager<T>) listeners.computeIfAbsent(
                listener, key -> new ListenerManagerImpl<>(this, listener, listenerClass, objectClass, objectId));
    }

    /**
     * Removes an object listener.
     *
     * @param objectClass The class of the object.
     * @param objectId The id of the object.
     * @param listenerClass The listener class.
     * @param listener The listener to remove.
     * @param <T> The type of the listener.
     */
    public <T extends ObjectAttachableListener> void removeObjectListener(
            Class<?> objectClass, long objectId, Class<T> listenerClass, T listener) {
        synchronized (objectListeners) {
            if (objectClass == null) {
                return;
            }
            Map<Long, Map<Class<? extends ObjectAttachableListener>, Map<ObjectAttachableListener,
                    ListenerManagerImpl<? extends ObjectAttachableListener>>>> objectListener =
                    objectListeners.get(objectClass);
            if (objectListener == null) {
                return;
            }
            Map<Class<? extends ObjectAttachableListener>, Map<ObjectAttachableListener,
                    ListenerManagerImpl<? extends ObjectAttachableListener>>> listeners = objectListener.get(objectId);
            if (listeners == null) {
                return;
            }
            Map<ObjectAttachableListener, ListenerManagerImpl<? extends ObjectAttachableListener>> classListeners =
                    listeners.get(listenerClass);
            if (classListeners == null) {
                return;
            }
            ListenerManagerImpl<? extends ObjectAttachableListener> listenerManager = classListeners.get(listener);
            if (listenerManager == null) {
                return;
            }
            classListeners.remove(listener);
            listenerManager.removed();
            // Clean it up
            if (classListeners.isEmpty()) {
                listeners.remove(listenerClass);
                if (listeners.isEmpty()) {
                    objectListener.remove(objectId);
                    if (objectListener.isEmpty()) {
                        objectListeners.remove(objectClass);
                    }
                }
            }
        }
    }

    /**
     * Remove all listeners attached to an object.
     *
     * @param objectClass The class of the object.
     * @param objectId The id of the object.
     */
    public void removeObjectListeners(Class<?> objectClass, long objectId) {
        if (objectClass == null) {
            return;
        }
        synchronized (objectListeners) {
            Map<Long, Map<Class<? extends ObjectAttachableListener>,
                    Map<ObjectAttachableListener, ListenerManagerImpl<? extends ObjectAttachableListener>>>>
                    objects = objectListeners.get(objectClass);
            if (objects == null) {
                return;
            }
            // Remove all listeners
            objects.computeIfPresent(objectId, (id, listeners) -> {
                listeners.values().stream()
                        .flatMap(map -> map.values().stream())
                        .forEach(ListenerManagerImpl::removed);
                listeners.clear();
                return null;
            });
            // Cleanup
            if (objects.isEmpty()) {
                objectListeners.remove(objectClass);
            }
        }
    }

    /**
     * Gets a map with all registered listeners that implement one or more {@code ObjectAttachableListener}s and their
     * assigned listener classes they listen to.
     *
     * @param objectClass The class of the object.
     * @param objectId The id of the object.
     * @param <T> The type of the listeners.
     * @return A map with all registered listeners that implement one or more {@code ObjectAttachableListener}s and
     *     their assigned listener classes they listen to.
     */
    @SuppressWarnings("unchecked")
    public <T extends ObjectAttachableListener> Map<T, List<Class<T>>> getObjectListeners(
            Class<?> objectClass, long objectId) {
        return Collections.unmodifiableMap(Optional.ofNullable(objectClass)
                .map(objectListeners::get)
                .map(objectListener -> objectListener.get(objectId))
                .map(Map::entrySet)
                .map(Set::stream)
                .map(entryStream -> entryStream
                        .flatMap(entry -> entry
                                .getValue()
                                .keySet()
                                .stream()
                                .map(listener -> new SimpleEntry<>((T) listener, (Class<T>) entry.getKey()))))
                .map(entryStream -> entryStream
                        .collect(Collectors.groupingBy(Entry::getKey,
                                                       Collectors.mapping(Entry::getValue, Collectors.toList()))))
                .orElseGet(HashMap::new));
    }

    /**
     * Gets all object listeners of the given class.
     *
     * @param objectClass The class of the object.
     * @param objectId The id of the object.
     * @param listenerClass The listener class.
     * @param <T> The type of the listener.
     * @return A list with all object listeners of the given type.
     */
    @SuppressWarnings("unchecked")
    public <T extends ObjectAttachableListener> List<T> getObjectListeners(
            Class<?> objectClass, long objectId, Class<T> listenerClass) {
        return Collections.unmodifiableList((List<T>) Optional.ofNullable(objectClass)
                .map(objectListeners::get)
                .map(objectListener -> objectListener.get(objectId))
                .map(listeners -> listeners.get(listenerClass))
                .map(Map::keySet)
                .map(ArrayList::new)
                .orElseGet(ArrayList::new));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends GloballyAttachableListener> Map<T, List<Class<T>>> getListeners() {
        return Collections.unmodifiableMap(listeners.entrySet().stream()
                .flatMap(entry -> entry
                        .getValue()
                        .keySet()
                        .stream()
                        .map(listener -> new SimpleEntry<>((T) listener, (Class<T>) entry.getKey())))
                .collect(Collectors.groupingBy(Entry::getKey,
                                               Collectors.mapping(Entry::getValue, Collectors.toList()))));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends GloballyAttachableListener> List<T> getListeners(Class<T> listenerClass) {
        return Collections.unmodifiableList((List<T>) Optional.ofNullable(listenerClass)
                .map(listeners::get)
                .map(Map::keySet)
                .map(ArrayList::new)
                .orElseGet(ArrayList::new));
    }

    @Override
    public String getPrefixedToken() {
        return BOT_TOKEN_PREFIX + token;
    }

    @Override
    public Set<Intent> getIntents() {
        return Collections.unmodifiableSet(intents);
    }

    @Override
    public String getToken() {
        return token;
    }

    @Override
    public ThreadPool getThreadPool() {
        return threadPool;
    }

    @Override
    public UncachedMessageUtil getUncachedMessageUtil() {
        return uncachedMessageUtil;
    }

    /*
     * Note: You might think the return type should be Optional<WebsocketAdapter>, because it's null till we receive
     *       the gateway from Discord. However the DiscordApi instance is only passed to the user, AFTER we connect
     *       so for the end user it is in fact never null.
     */
    /**
     * Gets the websocket adapter which is used to connect to Discord.
     *
     * @return The websocket adapter.
     */
    public DiscordWebSocketAdapter getWebSocketAdapter() {
        return websocketAdapter;
    }

    @Override
    public Optional<Ratelimiter> getGlobalRatelimiter() {
        if (globalRatelimiter == null) {
            Ratelimiter ratelimiter = defaultGlobalRatelimiter.computeIfAbsent(
                    getToken(),
                    (token) -> new LocalRatelimiter(5, Duration.ofMillis((long) Math.ceil(5D / 45D))));
            return Optional.of(ratelimiter);
        }
        return Optional.of(globalRatelimiter);
    }

    @Override
    public Duration getLatestGatewayLatency() {
        return Duration.ofNanos(latestGatewayLatencyNanos);
    }

    @Override
    public CompletableFuture<Duration> measureRestLatency() {
        return CompletableFuture.supplyAsync(() -> {
            restLatencyLock.lock();
            try {
                RestRequest<Duration> request = new RestRequest<>(this, RestMethod.GET, RestEndpoint.CURRENT_USER);
                long nanoStart = System.nanoTime();
                return request.execute(result -> Duration.ofNanos(System.nanoTime() - nanoStart)).join();
            } finally {
                restLatencyLock.unlock();
            }
        }, getThreadPool().getExecutorService());
    }

    @Override
    public void setMessageCacheSize(int capacity, int storageTimeInSeconds) {
        this.defaultMessageCacheCapacity = capacity;
        this.defaultMessageCacheStorageTimeInSeconds = storageTimeInSeconds;
        getChannels().stream()
                .filter(channel -> channel instanceof TextChannel)
                .map(channel -> (TextChannel) channel)
                .forEach(channel -> {
                    channel.getMessageCache().setCapacity(capacity);
                    channel.getMessageCache().setStorageTimeInSeconds(storageTimeInSeconds);
                });
    }

    @Override
    public int getDefaultMessageCacheCapacity() {
        return defaultMessageCacheCapacity;
    }

    @Override
    public int getDefaultMessageCacheStorageTimeInSeconds() {
        return defaultMessageCacheStorageTimeInSeconds;
    }

    @Override
    public void setAutomaticMessageCacheCleanupEnabled(boolean automaticMessageCacheCleanupEnabled) {
        this.defaultAutomaticMessageCacheCleanupEnabled = automaticMessageCacheCleanupEnabled;
        getChannels().stream()
                .filter(TextChannel.class::isInstance)
                .map(TextChannel.class::cast)
                .forEach(channel -> channel.getMessageCache()
                        .setAutomaticCleanupEnabled(automaticMessageCacheCleanupEnabled));
    }

    @Override
    public boolean isDefaultAutomaticMessageCacheCleanupEnabled() {
        return defaultAutomaticMessageCacheCleanupEnabled;
    }

    @Override
    public int getCurrentShard() {
        return currentShard;
    }

    @Override
    public int getTotalShards() {
        return totalShards;
    }

    @Override
    public boolean isWaitingForServersOnStartup() {
        return waitForServersOnStartup;
    }

    @Override
    public boolean isWaitingForUsersOnStartup() {
        return waitForUsersOnStartup;
    }

    /**
     * The proxy selector which should be used to determine the proxies that should be used to connect to the Discord
     * REST API and websocket.
     *
     * @return the proxy selector which should be used to determine the proxies that should be used to connect to the
     *     Discord REST API and websocket.
     */
    public Optional<ProxySelector> getProxySelector() {
        return Optional.ofNullable(proxySelector);
    }

    /**
     * The proxy which should be used to connect to the Discord REST API and websocket.
     *
     * @return the proxy which should be used to connect to the Discord REST API and websocket.
     */
    public Optional<Proxy> getProxy() {
        return Optional.ofNullable(proxy);
    }

    /**
     * The authenticator that should be used to authenticate against proxies that require it.
     *
     * @return the authenticator that should be used to authenticate against proxies that require it.
     */
    public Optional<Authenticator> getProxyAuthenticator() {
        return Optional.ofNullable(proxyAuthenticator);
    }

    /**
     * Whether to trust all SSL certificates.
     *
     * @return whether to trust all SSL certificates.
     */
    public boolean isTrustAllCertificates() {
        return trustAllCertificates;
    }

    @Override
    public void updateStatus(UserStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("The status cannot be null");
        }
        this.status = status;
        websocketAdapter.updateStatus();
    }

    @Override
    public UserStatus getStatus() {
        return status;
    }


    /**
     * Sets the current activity, along with type and streaming Url.
     *
     * @param type The activity's type.
     * @param name The name of the activity.
     * @param streamingUrl The Url used for streaming.
     */
    private void updateActivity(ActivityType type, String name, String streamingUrl) {
        if (name == null) {
            activity = null;
        } else if (streamingUrl == null) {
            activity = new ActivityImpl(type, name, null);
        } else {
            activity = new ActivityImpl(type, name, streamingUrl);
        }
        websocketAdapter.updateStatus();
    }


    @Override
    public void updateActivity(String name) {
        updateActivity(ActivityType.PLAYING, name, null);
    }

    @Override
    public void updateActivity(ActivityType type, String name) {
        updateActivity(type, name, null);
    }

    @Override
    public void updateActivity(String name, String streamingUrl) {
        updateActivity(ActivityType.STREAMING, name, streamingUrl);
    }

    @Override
    public void unsetActivity() {
        updateActivity(null);
    }

    @Override
    public Optional<Activity> getActivity() {
        return Optional.ofNullable(activity);
    }

    @Override
    public User getYourself() {
        return you;
    }

    @Override
    public long getOwnerId() {
        return ownerId;
    }

    @Override
    public long getClientId() {
        return clientId;
    }

    @Override
    public void disconnect() {
        synchronized (disconnectCalledLock) {
            if (!disconnectCalled) {
                if (websocketAdapter == null) {
                    // if no web socket is connected, immediately shutdown thread pool
                    threadPool.shutdown();
                } else {
                    // shutdown thread pool after web socket disconnected event was dispatched
                    addLostConnectionListener(event -> threadPool.shutdown());
                    // disconnect web socket
                    websocketAdapter.disconnect();
                    // shutdown thread pool if within one minute no disconnect event was dispatched
                    threadPool.getDaemonScheduler().schedule(threadPool::shutdown, 1, TimeUnit.MINUTES);
                }
                disconnectCalled = true;
                httpClient.dispatcher().executorService().shutdown();
                httpClient.connectionPool().evictAll();
            }
        }
    }

    @Override
    public void setReconnectDelay(Function<Integer, Integer> reconnectDelayProvider) {
        this.reconnectDelayProvider = reconnectDelayProvider;
    }

    @Override
    public int getReconnectDelay(int attempt) {
        if (attempt < 0) {
            throw new IllegalArgumentException("attempt must be 1 or greater");
        }
        return reconnectDelayProvider.apply(attempt);
    }

    @Override
    public CompletableFuture<ApplicationInfo> getApplicationInfo() {
        return new RestRequest<ApplicationInfo>(this, RestMethod.GET, RestEndpoint.SELF_INFO)
                .execute(result -> new ApplicationInfoImpl(this, result.getJsonBody()));
    }

    @Override
    public CompletableFuture<Webhook> getWebhookById(long id) {
        return new RestRequest<Webhook>(this, RestMethod.GET, RestEndpoint.WEBHOOK)
                .setUrlParameters(Long.toUnsignedString(id))
                .execute(result -> new WebhookImpl(this, result.getJsonBody()));
    }

    @Override
    public Collection<Long> getUnavailableServers() {
        return Collections.unmodifiableCollection(unavailableServers);
    }

    @Override
    public CompletableFuture<Invite> getInviteByCode(String code) {
        return new RestRequest<Invite>(this, RestMethod.GET, RestEndpoint.INVITE)
                .setUrlParameters(code)
                .addQueryParameter("with_counts", "false")
                .execute(result -> new InviteImpl(this, result.getJsonBody()));
    }

    @Override
    public CompletableFuture<Invite> getInviteWithMemberCountsByCode(String code) {
        return new RestRequest<Invite>(this, RestMethod.GET, RestEndpoint.INVITE)
                .setUrlParameters(code)
                .addQueryParameter("with_counts", "true")
                .execute(result -> new InviteImpl(this, result.getJsonBody()));
    }

    @Override
    public boolean isUserCacheEnabled() {
        return userCacheEnabled;
    }

    @Override
    public Collection<User> getCachedUsers() {
        return getEntityCache().get().getMemberCache().getUserCache().getUsers();
    }

    @Override
    public Optional<User> getCachedUserById(long id) {
        return getEntityCache().get().getMemberCache().getUserCache().getUserById(id);
    }

    @Override
    public CompletableFuture<User> getUserById(long id) {
        return getCachedUserById(id)
                .map(CompletableFuture::completedFuture)
                .orElseGet(() -> new RestRequest<User>(this, RestMethod.GET, RestEndpoint.USER)
                .setUrlParameters(Long.toUnsignedString(id))
                .execute(result -> new UserImpl(this, result.getJsonBody(), (MemberImpl) null, null)));
    }

    @Override
    public MessageSet getCachedMessages() {
        synchronized (messages) {
            return new MessageSetImpl(messages.values().stream()
                    .map(Reference::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList()));
        }
    }

    /**
     * Get messages from the cache that satisfy a given condition.
     *
     * @param filter The filter for messages to be included.
     * @return A set of cached messages satisfying the condition.
     */
    public MessageSet getCachedMessagesWhere(Predicate<Message> filter) {
        synchronized (messages) {
            return new MessageSetImpl(messages.values().stream()
                    .map(Reference::get)
                    .filter(Objects::nonNull)
                    .filter(filter)
                    .collect(Collectors.toList()));
        }
    }

    /**
     * Execute a task for every message in cache that satisfied a given condition.
     *
     * @param filter The condition on which to execute the code.
     * @param action The action to be applied to the messages.
     */
    public void forEachCachedMessageWhere(Predicate<Message> filter, Consumer<Message> action) {
        synchronized (messages) {
            messages.values().stream()
                    .map(Reference::get)
                    .filter(Objects::nonNull)
                    .filter(filter)
                    .forEach(action);
        }
    }

    @Override
    public Optional<Message> getCachedMessageById(long id) {
        return Optional.ofNullable(messages.get(id)).map(Reference::get);
    }

    @Override
    public Collection<Server> getServers() {
        return Collections.unmodifiableList(new ArrayList<>(servers.values()));
    }

    @Override
    public Optional<Server> getServerById(long id) {
        return Optional.ofNullable(servers.get(id));
    }

    @Override
    public Collection<KnownCustomEmoji> getCustomEmojis() {
        return Collections.unmodifiableCollection(customEmojis.values());
    }

    @Override
    public Optional<KnownCustomEmoji> getCustomEmojiById(long id) {
        return Optional.ofNullable(customEmojis.get(id));
    }

    @Override
    public Collection<Channel> getChannels() {
        return entityCache.get().getChannelCache().getChannels();
    }

    @Override
    public Collection<PrivateChannel> getPrivateChannels() {
        return entityCache.get().getChannelCache().getChannelsWithTypes(ChannelType.PRIVATE_CHANNEL);
    }

    @Override
    public Collection<ServerChannel> getServerChannels() {
        return entityCache.get().getChannelCache().getChannelsWithTypes(ChannelType.getServerChannelTypes());
    }

    @Override
    public Collection<ChannelCategory> getChannelCategories() {
        return entityCache.get().getChannelCache().getChannelsWithTypes(ChannelType.CHANNEL_CATEGORY);
    }

    @Override
    public Collection<ServerTextChannel> getServerTextChannels() {
        return entityCache.get().getChannelCache().getChannelsWithTypes(ChannelType.SERVER_TEXT_CHANNEL);
    }

    @Override
    public Collection<ServerVoiceChannel> getServerVoiceChannels() {
        return entityCache.get().getChannelCache().getChannelsWithTypes(ChannelType.SERVER_VOICE_CHANNEL);
    }

    @Override
    public Collection<TextChannel> getTextChannels() {
        return entityCache.get().getChannelCache().getChannelsWithTypes(ChannelType.getTextChannelTypes());
    }

    @Override
    public Collection<VoiceChannel> getVoiceChannels() {
        return entityCache.get().getChannelCache().getChannelsWithTypes(ChannelType.getVoiceChannelTypes());
    }

    @Override
    public Optional<Channel> getChannelById(long id) {
        return entityCache.get().getChannelCache().getChannelById(id);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Collection<ListenerManager<? extends GloballyAttachableListener>> addListener(
            GloballyAttachableListener listener) {
        return ClassHelper.getInterfacesAsStream(listener.getClass())
                .filter(GloballyAttachableListener.class::isAssignableFrom)
                .filter(listenerClass -> listenerClass != GloballyAttachableListener.class)
                .map(listenerClass -> (Class<GloballyAttachableListener>) listenerClass)
                .map(listenerClass -> addListener(listenerClass, listener))
                .collect(Collectors.toList());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends GloballyAttachableListener> ListenerManager<T> addListener(Class<T> listenerClass, T listener) {
        return (ListenerManager<T>) listeners
                .computeIfAbsent(listenerClass, key -> Collections.synchronizedMap(new LinkedHashMap<>()))
                .computeIfAbsent(listener, key -> new ListenerManagerImpl<>(this, listener, listenerClass));
    }

    @Override
    public <T extends GloballyAttachableListener> void removeListener(Class<T> listenerClass, T listener) {
        synchronized (listeners) {
            Map<GloballyAttachableListener, ListenerManagerImpl<? extends GloballyAttachableListener>> classListeners =
                    listeners.get(listenerClass);
            if (classListeners == null) {
                return;
            }
            ListenerManagerImpl<? extends GloballyAttachableListener> listenerManager = classListeners.get(listener);
            if (listenerManager == null) {
                return;
            }
            classListeners.remove(listener);
            listenerManager.removed();
            // Clean it up
            if (classListeners.isEmpty()) {
                listeners.remove(listenerClass);
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void removeListener(GloballyAttachableListener listener) {
        ClassHelper.getInterfacesAsStream(listener.getClass())
                .filter(GloballyAttachableListener.class::isAssignableFrom)
                .filter(listenerClass -> listenerClass != GloballyAttachableListener.class)
                .map(listenerClass -> (Class<GloballyAttachableListener>) listenerClass)
                .forEach(listenerClass -> removeListener(listenerClass, listener));
    }

    @Override
    @SuppressWarnings("deprecation")
    protected void finalize() throws Throwable {
        disconnect();
        super.finalize();
    }
}
