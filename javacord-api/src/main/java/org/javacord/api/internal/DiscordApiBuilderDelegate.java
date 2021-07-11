package org.javacord.api.internal;

import org.javacord.api.AccountType;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.intent.Intent;
import org.javacord.api.listener.GloballyAttachableListener;
import org.javacord.api.util.auth.Authenticator;
import org.javacord.api.util.ratelimit.Ratelimiter;

import java.net.Proxy;
import java.net.ProxySelector;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * This class is internally used by the {@link DiscordApiBuilder} to create discord api instances.
 * You usually don't want to interact with this object.
 */
public interface DiscordApiBuilderDelegate {

    /**
     * Sets a ratelimiter that can be used to control global ratelimits.
     *
     * @param ratelimiter The ratelimiter used to control global ratelimits.
     */
    void setGlobalRatelimiter(Ratelimiter ratelimiter);

    /**
     * Sets a ratelimiter that can be used to respect the 5 second gateway identify ratelimit.
     *
     * @param ratelimiter The ratelimiter used to respect the 5 second gateway identify ratelimit.
     */
    void setGatewayIdentifyRatelimiter(Ratelimiter ratelimiter);

    /**
     * Sets the proxy selector which should be used to determine the proxies that should be used to connect to the
     * Discord REST API and websocket.
     *
     * @param proxySelector The proxy selector to set.
     */
    void setProxySelector(ProxySelector proxySelector);

    /**
     * Sets the proxy which should be used to connect to the Discord REST API and websocket.
     *
     * @param proxy The poxy to set.
     */
    void setProxy(Proxy proxy);

    /**
     * Sets the authenticator that should be used to authenticate against proxies that require it.
     *
     * @param authenticator The proxy authenticator to set.
     */
    void setProxyAuthenticator(Authenticator authenticator);

    /**
     * Sets whether all SSL certificates should be trusted when connecting to the Discord API and websocket.
     *
     * @param trustAllCertificates Whether to trust all SSL certificates.
     */
    void setTrustAllCertificates(boolean trustAllCertificates);

    /**
     * Sets the token.
     *
     * @param token The token to set.
     */
    void setToken(String token);

    /**
     * Gets the token.
     *
     * @return The token.
     */
    Optional<String> getToken();

    /**
     * Sets the account type.
     *
     * @param accountType The account type to set.
     */
    void setAccountType(AccountType accountType);

    /**
     * Gets the account type.
     *
     * @return The account type.
     */
    AccountType getAccountType();

    /**
     * Sets the total shards.
     *
     * @param totalShards The total shards to set.
     * @see DiscordApiBuilder#setTotalShards(int)
     */
    void setTotalShards(int totalShards);

    /**
     * Gets the total shards.
     *
     * @return The total shards.
     */
    int getTotalShards();

    /**
     * Sets the current shards.
     *
     * @param currentShard The current shards to set.
     * @see DiscordApiBuilder#setCurrentShard(int)
     */
    void setCurrentShard(int currentShard);

    /**
     * Gets the current shard.
     *
     * @return The current shard.
     */
    int getCurrentShard();

    /**
     * Sets the wait for servers on startup flag.
     *
     * @param waitForServersOnStartup The wait for servers on startup flag to set.
     */
    void setWaitForServersOnStartup(boolean waitForServersOnStartup);

    /**
     * Checks if Javacord should wait for all servers to become available on startup.
     *
     * @return If Javacord should wait.
     */
    boolean isWaitingForServersOnStartup();

    /**
     * Sets the wait for users on startup flag.
     *
     * @param waitForUsersOnStartup The wait for users on startup flag to set.
     */
    void setWaitForUsersOnStartup(boolean waitForUsersOnStartup);

    /**
     * Checks if Javacord should wait for all users to get cached on startup.
     *
     * @return If Javacord should wait.
     */
    boolean isWaitingForUsersOnStartup();

    /**
     * Sets if Javacord should register a shutdown hook that disconnects the {@link DiscordApi} instance.
     *
     * <p>By default, Javacord registers a shutdown hook using {@link Runtime#addShutdownHook(Thread)} that calls
     * the {@link DiscordApi#disconnect()} method. Setting this flag to {@code false} will disable this behavior.
     *
     * @param registerShutdownHook Whether the shutdown hook should be registered or not.
     */
    void setShutdownHookRegistrationEnabled(boolean registerShutdownHook);

    /**
     * Checks if newly created {@link DiscordApi} instances should register a shutdown hook to disconnect the
     * instance.
     *
     * @return Whether the shutdown hook will be registered or not.
     * @see #setShutdownHookRegistrationEnabled(boolean)
     */
    boolean isShutdownHookRegistrationEnabled();

    /**
     * Sets the intents where the given predicate matches.
     *
     * @param condition Whether the intent should be added or not.
     */
    void setAllIntentsWhere(Predicate<Intent> condition);

    /**
     * Adds the intents specified to the current list of
     * intents.
     *
     * @param intents The intents to add.
     */
    void addIntents(Intent... intents);

    /**
     * Logs the bot in.
     *
     * @return The discord api instance.
     */
    CompletableFuture<DiscordApi> login();

    /**
     * Login given shards to the account with the given token.
     * It is invalid to call {@link #setCurrentShard(int)} with
     * anything but {@code 0} before calling this method.
     *
     * @param shards The shards to connect, starting with {@code 0}!
     * @return A collection of {@link CompletableFuture}s which contain the {@code DiscordApi}s for the shards.
     */
    Collection<CompletableFuture<DiscordApi>> loginShards(int... shards);

    /**
     * Sets the recommended total shards.
     *
     * @return A future to check if the action was successful.
     */
    CompletableFuture<Void> setRecommendedTotalShards();

    /**
     * Adds a {@code GloballyAttachableListener} to all created {@code DiscordApi} instances.
     * Adding a listener multiple times will only add it once.
     * The order of invocation is according to first addition.
     *
     * @param listenerClass The listener class.
     * @param listener The listener to add.
     * @param <T> The type of the listener.
     */
    <T extends GloballyAttachableListener> void addListener(Class<T> listenerClass, T listener);

    /**
     * Adds a listener that implements one or more {@code GloballyAttachableListener}s to all created
     * {@code DiscordApi} instances.
     * Adding a listener multiple times will only add it once.
     * The order of invocation is according to first addition.
     *
     * @param listener The listener to add.
     */
    void addListener(GloballyAttachableListener listener);

    /**
     * Adds a {@code GloballyAttachableListener} to all created {@code DiscordApi} instances. The supplier is
     * called for every created {@code DiscordApi} instance, so either the same or different instances can be
     * registered. One example would be a method reference to a default constructor like
     * {@code MyListener::new}.
     * Adding a listener multiple times will only add it once.
     * The order of invocation is according to first addition.
     *
     * @param listenerClass The listener class.
     * @param listenerSupplier The supplier of listeners to add.
     * @param <T> The type of the listener.
     */
    <T extends GloballyAttachableListener> void addListener(Class<T> listenerClass, Supplier<T> listenerSupplier);

    /**
     * Adds a listener that implements one or more {@code GloballyAttachableListener}s to all created
     * {@code DiscordApi} instances. The supplier is called for every created {@code DiscordApi} instance,
     * so either the same or different instances can be registered. One example would be a method reference
     * to a default constructor like {@code MyListener::new}.
     * Adding a listener multiple times will only add it once.
     * The order of invocation is according to first addition.
     *
     * @param listenerSupplier The supplier of listeners to add.
     */
    void addListener(Supplier<GloballyAttachableListener> listenerSupplier);

    /**
     * Adds a {@code GloballyAttachableListener} to all created {@code DiscordApi} instances. The function
     * is called for every created {@code DiscordApi} instance, so either the same or different instances
     * can be registered. One example would be a method reference to a constructor with a {@code DiscordApi}
     * parameter like {@code MyListener::new}.
     * Adding a listener multiple times will only add it once.
     * The order of invocation is according to first addition.
     *
     * @param listenerClass The listener class.
     * @param listenerFunction The function to provide listeners to add.
     * @param <T> The type of the listener.
     */
    <T extends GloballyAttachableListener> void addListener(Class<T> listenerClass,
                                                            Function<DiscordApi, T> listenerFunction);

    /**
     * Adds a listener that implements one or more {@code GloballyAttachableListener}s to all created
     * {@code DiscordApi} instances. The function is called for every created {@code DiscordApi} instance,
     * so either the same or different instances can be registered. One example would be a method reference
     * to a constructor with a {@code DiscordApi} parameter like {@code MyListener::new}.
     * Adding a listener multiple times will only add it once.
     * The order of invocation is according to first addition.
     *
     * @param listenerFunction The function to provide listeners to add.
     */
    void addListener(Function<DiscordApi, GloballyAttachableListener> listenerFunction);

    /**
     * Removes a listener that implements one or more {@code GloballyAttachableListener}s from the list of
     * listeners to be added to {@code DiscordApi} instances. If the listener was already added to a
     * {@code DiscordApi} instance, it will not get removed by calling this method.
     * This method should normally only be used before calling one of the login methods.
     *
     * @param listener The listener to remove.
     */
    void removeListener(GloballyAttachableListener listener);

    /**
     * Removes a {@code GloballyAttachableListener} from the list of listeners to be added to
     * {@code DiscordApi} instances. If the listener was already added to a {@code DiscordApi} instance,
     * it will not get removed by calling this method.
     * This method should normally only be used before calling one of the login methods.
     *
     * @param listenerClass The listener class.
     * @param listener The listener to remove.
     * @param <T> The type of the listener.
     */
    <T extends GloballyAttachableListener> void removeListener(Class<T> listenerClass, T listener);

    /**
     * Removes a supplier of listeners that implements one or more {@code GloballyAttachableListener}s
     * from the list of listeners to be added to {@code DiscordApi} instances. If the listener was already
     * added to a {@code DiscordApi} instance, it will not get removed by calling this method.
     * This method should normally only be used before calling one of the login methods.
     *
     * @param listenerSupplier The supplier of listeners to remove.
     */
    void removeListenerSupplier(Supplier<GloballyAttachableListener> listenerSupplier);

    /**
     * Removes a supplier of {@code GloballyAttachableListener}s from the list of listeners to be added to
     * {@code DiscordApi} instances. If the listener was already added to a {@code DiscordApi} instance,
     * it will not get removed by calling this method.
     * This method should normally only be used before calling one of the login methods.
     *
     * @param listenerClass The listener class.
     * @param listenerSupplier The supplier of listeners to remove.
     * @param <T> The type of the listener.
     */
    <T extends GloballyAttachableListener> void removeListenerSupplier(Class<T> listenerClass,
                                                                       Supplier<T> listenerSupplier);

    /**
     * Removes a function that provides listeners that implements one or more
     * {@code GloballyAttachableListener}s from the list of listeners to be added to {@code DiscordApi}
     * instances. If the listener was already added to a {@code DiscordApi} instance, it will not get
     * removed by calling this method.
     * This method should normally only be used before calling one of the login methods.
     *
     * @param listenerFunction The function to provide listeners to remove.
     */
    void removeListenerFunction(Function<DiscordApi, GloballyAttachableListener> listenerFunction);

    /**
     * Removes a function that provides {@code GloballyAttachableListener}s from the list of listeners to be
     * added to {@code DiscordApi} instances. If the listener was already added to a {@code DiscordApi}
     * instance, it will not get removed by calling this method.
     * This method should normally only be used before calling one of the login methods.
     *
     * @param listenerClass The listener class.
     * @param listenerFunction The function to provide listeners to remove.
     * @param <T> The type of the listener.
     */
    <T extends GloballyAttachableListener> void removeListenerFunction(Class<T> listenerClass,
                                                                       Function<DiscordApi, T> listenerFunction);
}
