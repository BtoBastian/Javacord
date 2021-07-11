package org.javacord.api;

import org.javacord.api.entity.intent.Intent;
import org.javacord.api.event.server.ServerBecomesAvailableEvent;
import org.javacord.api.internal.DiscordApiBuilderDelegate;
import org.javacord.api.listener.ChainableGloballyAttachableListenerManager;
import org.javacord.api.listener.GloballyAttachableListener;
import org.javacord.api.util.auth.Authenticator;
import org.javacord.api.util.internal.DelegateFactory;
import org.javacord.api.util.ratelimit.LocalRatelimiter;
import org.javacord.api.util.ratelimit.Ratelimiter;

import java.net.Proxy;
import java.net.ProxySelector;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.IntStream;

/**
 * This class is used to login to a Discord account.
 */
public class DiscordApiBuilder implements ChainableGloballyAttachableListenerManager {

    /**
     * The delegate used to create a {@link DiscordApi} instance.
     */
    private final DiscordApiBuilderDelegate delegate = DelegateFactory.createDiscordApiBuilderDelegate();

    /**
     * Login to the account with the given token.
     *
     * @return A {@link CompletableFuture} which contains the DiscordApi.
     */
    public CompletableFuture<DiscordApi> login() {
        return delegate.login();
    }

    /**
     * Login all shards to the account with the given token.
     * It is invalid to call {@link #setCurrentShard(int)} with
     * anything but {@code 0} before calling this method.
     *
     * @return A collection of {@link CompletableFuture}s which contain the {@code DiscordApi}s for the shards.
     */
    public Collection<CompletableFuture<DiscordApi>> loginAllShards() {
        return loginShards(shard -> true);
    }

    /**
     * Login shards adhering to the given predicate to the account with the given token.
     * It is invalid to call {@link #setCurrentShard(int)} with
     * anything but {@code 0} before calling this method.
     *
     * @param shardsCondition The predicate for identifying shards to connect, starting with {@code 0}!
     * @return A collection of {@link CompletableFuture}s which contain the {@code DiscordApi}s for the shards.
     */
    public Collection<CompletableFuture<DiscordApi>> loginShards(IntPredicate shardsCondition) {
        return loginShards(IntStream.range(0, delegate.getTotalShards()).filter(shardsCondition).toArray());
    }

    /**
     * Login given shards to the account with the given token.
     * It is invalid to call {@link #setCurrentShard(int)} with
     * anything but {@code 0} before calling this method.
     *
     * @param shards The shards to connect, starting with {@code 0}!
     * @return A collection of {@link CompletableFuture}s which contain the {@code DiscordApi}s for the shards.
     */
    public Collection<CompletableFuture<DiscordApi>> loginShards(int... shards) {
        return delegate.loginShards(shards);
    }

    /**
     * Sets a ratelimiter that can be used to control global ratelimits.
     *
     * <p>If you don't provide a ratelimiter by yourself, Javacord will use a default ratelimiter
     * which is set to {@code 5} requests per {@code 112 ms}, resulting in about 45 requests per second.
     * The default ratelimiter will be shared by every bot with the same token in the same Java program.
     *
     * <p>If you provide a custom ratelimiter, you have to make sure to use the exact same ratelimiter instance
     * with all Javacord instances, or otherwise synchronize the global ratelimit across shards.
     *
     * <p>An easy implementation is available with the {@link LocalRatelimiter}.
     *
     * @param ratelimiter The ratelimiter used to control global ratelimits.
     * @return The current instance in order to chain call methods.
     */
    public DiscordApiBuilder setGlobalRatelimiter(Ratelimiter ratelimiter) {
        delegate.setGlobalRatelimiter(ratelimiter);
        return this;
    }

    /**
     * Sets a ratelimiter that can be used to control the 5 seconds gateway identify ratelimit.
     *
     * <p>By default, Javacord automatically provides a default {@link LocalRatelimiter}
     * which is set to allow one gateway identify request per 5500ms and is shared with every bot with the same token
     * in the same Java program.
     *
     * <p>**DO NOT** set a custom gateway identify ratelimiter unless you have to synchronize the ratelimit across
     * multiple Java programs (running on different JVMs, VMs, phyiscal servers etc.) that run Javacord on the same
     * bot token. The default ratelimiter will handle the ratelimit for you as long as your whole bot runs in the same
     * Java program.
     *
     * @param ratelimiter The ratelimiter used to control the 5 seconds gateway identify ratelimit.
     * @return The current instance in order to chain call methods.
     */
    public DiscordApiBuilder setGatewayIdentifyRatelimiter(Ratelimiter ratelimiter) {
        delegate.setGatewayIdentifyRatelimiter(ratelimiter);
        return this;
    }

    /**
     * Sets the proxy selector which should be used to determine the proxies that should be used to connect to the
     * Discord REST API and web socket.
     * If no explicit proxy is configured using {@link #setProxy(Proxy)} and no proxy selector is configured using this
     * method, {@link ProxySelector#getDefault()} is used to select appropriate proxies.
     * If {@link ProxySelector#setDefault(ProxySelector)} was not used to set a custom proxy selector,
     * the default one evaluates the system properties {@code https.proxyHost}, {@code https.proxyPort},
     * {@code http.nonProxyHosts}, {@code socksProxyHost}, {@code socksProxyPort} and {@code socksProxyVersion} as
     * documented on the <a href=https://docs.oracle.com/javase/8/docs/technotes/guides/net/properties.html>Networking
     * Properties</a> page.
     *
     * <p><b>Note:</b> It is an error to configure an explicit proxy via {@link #setProxy(Proxy)} and a proxy selector
     * using this method.
     *
     * @param proxySelector The proxy selector to set.
     * @return The current instance in order to chain call methods.
     * @see #setProxy(Proxy)
     * @see #setProxyAuthenticator(Authenticator)
     * @see ProxySelector#getDefault()
     * @see ProxySelector#setDefault(ProxySelector)
     * @see <a href=https://docs.oracle.com/javase/8/docs/technotes/guides/net/properties.html>Networking Properties</a>
     */
    public DiscordApiBuilder setProxySelector(ProxySelector proxySelector) {
        delegate.setProxySelector(proxySelector);
        return this;
    }

    /**
     * Sets the proxy which should be used to connect to the Discord REST API and web socket.
     * If this is not set explicitly, the proxy selector configured with {@link #setProxySelector(ProxySelector)} is
     * used to select appropriate proxies. If both are not set, {@link ProxySelector#getDefault()} is used to select
     * appropriate proxies. If {@link ProxySelector#setDefault(ProxySelector)} was not used to set a custom proxy
     * selector, the default one evaluates the system properties {@code https.proxyHost}, {@code https.proxyPort},
     * {@code http.nonProxyHosts}, {@code socksProxyHost}, {@code socksProxyPort} and {@code socksProxyVersion} as
     * documented on the <a href=https://docs.oracle.com/javase/8/docs/technotes/guides/net/properties.html>Networking
     * Properties</a> page.
     *
     * <p><b>Note:</b> It is an error to configure an explicit proxy using this method and a proxy selector using
     * {@link #setProxySelector(ProxySelector)}.
     *
     * @param proxy The proxy to set.
     * @return The current instance in order to chain call methods.
     * @see #setProxyAuthenticator(Authenticator)
     * @see #setProxySelector(ProxySelector)
     * @see ProxySelector#getDefault()
     * @see ProxySelector#setDefault(ProxySelector)
     * @see <a href=https://docs.oracle.com/javase/8/docs/technotes/guides/net/properties.html>Networking Properties</a>
     */
    public DiscordApiBuilder setProxy(Proxy proxy) {
        delegate.setProxy(proxy);
        return this;
    }

    /**
     * Sets the authenticator that should be used to authenticate against proxies that require it.
     * If this is not set explicitly, the authenticator configured with
     * {@link java.net.Authenticator#setDefault(java.net.Authenticator)}, if any, is used to get credentials for
     * {@code Basic} auth if the proxy supports it. If you need to support a more sophisticated authentication algorithm
     * or scheme, use this method to set an own authenticator.
     *
     * @param authenticator The proxy authenticator to set.
     * @return The current instance in order to chain call methods.
     * @see #setProxy(Proxy)
     * @see #setProxySelector(ProxySelector)
     * @see java.net.Authenticator#setDefault(java.net.Authenticator)
     */
    public DiscordApiBuilder setProxyAuthenticator(Authenticator authenticator) {
        delegate.setProxyAuthenticator(authenticator);
        return this;
    }

    /**
     * Sets whether all SSL certificates should be trusted when connecting to the Discord API and web socket.
     * This might for example be necessary when connecting through a decrypting proxy.
     * Be aware that this also increases the risk of man-in-the-middle attacks, which basically is,
     * what a decrypting proxy does. Due to this risk, a warning is logged when connecting with this property
     * set to {@code true}. If you do not care about this risk, you can suppress this warning using your logging
     * configuration.
     *
     * @param trustAllCertificates Whether to trust all SSL certificates.
     * @return The current instance in order to chain call methods.
     */
    public DiscordApiBuilder setTrustAllCertificates(boolean trustAllCertificates) {
        delegate.setTrustAllCertificates(trustAllCertificates);
        return this;
    }

    /**
     * Sets the token which is required for the login process.
     * A tutorial on how to get the token can be found in the
     * <a href="https://github.com/Javacord/Javacord/wiki">Javacord wiki</a>.
     *
     * @param token The token to set.
     * @return The current instance in order to chain call methods.
     */
    public DiscordApiBuilder setToken(String token) {
        delegate.setToken(token);
        return this;
    }

    /**
     * Gets the token that will be used to login.
     *
     * @return The token.
     * @see #setToken(String)
     */
    public Optional<String> getToken() {
        return delegate.getToken();
    }

    /**
     * Sets the account type.
     * By default the builder assumes that you want to login to a bot account.
     * Please notice, that public client bots are not allowed by Discord!
     *
     * @param type The account type.
     * @return The current instance in order to chain call methods.
     */
    public DiscordApiBuilder setAccountType(AccountType type) {
        delegate.setAccountType(type);
        return this;
    }

    /**
     * Gets the account type.
     *
     * @return The account type.
     * @see #setAccountType(AccountType)
     */
    public AccountType getAccountType() {
        return delegate.getAccountType();
    }

    /**
     * Sets total shards for server sharding.
     * Sharding allows you to split your bot into several independent instances.
     * A shard only handles a subset of a bot's servers.
     *
     * @param totalShards The total amount of shards. Sharding will be disabled if set to <code>1</code>.
     * @return The current instance in order to chain call methods.
     * @see <a href="https://discord.com/developers/docs/topics/gateway#sharding">API docs</a>
     */
    public DiscordApiBuilder setTotalShards(int totalShards) {
        delegate.setTotalShards(totalShards);
        return this;
    }

    /**
     * Gets the total amount of shards.
     * Sharding is disabled if set to {@code 1}.
     *
     * @return The total amount of shards.
     * @see #setTotalShards(int)
     */
    public int getTotalShards() {
        return delegate.getTotalShards();
    }

    /**
     * Sets shard for server sharding.
     * Sharding allows you to split your bot into several independent instances.
     * A shard only handles a subset of a bot's servers.
     *
     * @param currentShard The shard of this connection starting with <code>0</code>!
     * @return The current instance in order to chain call methods.
     * @see <a href="https://discord.com/developers/docs/topics/gateway#sharding">API docs</a>
     */
    public DiscordApiBuilder setCurrentShard(int currentShard) {
        delegate.setCurrentShard(currentShard);
        return this;
    }

    /**
     * Gets the current shard.
     *
     * @return The current shard.
     * @see #setCurrentShard(int)
     */
    public int getCurrentShard() {
        return delegate.getCurrentShard();
    }

    /**
     * Sets if Javacord should wait for all servers to become available on startup.
     * If this is disabled the {@link DiscordApi#getServers()} method will return an empty collection directly after
     * logging in and fire {@link ServerBecomesAvailableEvent} events once they
     * become available. You can check the ids of unavailable servers using the
     * {@link DiscordApi#getUnavailableServers()} method.
     *
     * @param waitForServersOnStartup Whether Javacord should wait for all servers
     *                                to become available on startup or not.
     * @return The current instance in order to chain call methods.
     */
    public DiscordApiBuilder setWaitForServersOnStartup(boolean waitForServersOnStartup) {
        delegate.setWaitForServersOnStartup(waitForServersOnStartup);
        return this;
    }

    /**
     * Checks if Javacord should wait for all servers to become available on startup.
     *
     * @return If Javacord should wait.
     * @see #setWaitForServersOnStartup(boolean)
     */
    public boolean isWaitingForServersOnStartup() {
        return delegate.isWaitingForServersOnStartup();
    }

    /**
     * Sets if Javacord should wait for all users to be cached.
     * If this is set to {@code true}, Javacord will consider Servers with uncached users as unavailable.
     *
     * <p>Requires the {@link Intent#GUILD_MEMBERS} intent to be set.
     *
     * @param waitForUsersOnStartup Whether Javacord should wait for all users to be cached or not.
     * @return The current instance in order to chain call methods.
     */
    public DiscordApiBuilder setWaitForUsersOnStartup(boolean waitForUsersOnStartup) {
        delegate.setWaitForUsersOnStartup(waitForUsersOnStartup);
        return this;
    }

    /**
     * Checks if Javacord should wait for all users to be cached.
     *
     * @return If Javacord should wait.
     * @see #setWaitForUsersOnStartup(boolean)
     */
    public boolean isWaitingForUsersOnStartup() {
        return delegate.isWaitingForUsersOnStartup();
    }

    /**
     * Sets if Javacord should register a shutdown hook that disconnects the {@link DiscordApi} instance.
     *
     * <p>By default, Javacord registers a shutdown hook using {@link Runtime#addShutdownHook(Thread)} that calls
     * the {@link DiscordApi#disconnect()} method. Setting this flag to {@code false} will disable this behavior.
     *
     * @param registerShutdownHook Whether the shutdown hook should be registered or not.
     * @return The current instance in order to chain call methods.
     */
    public DiscordApiBuilder setShutdownHookRegistrationEnabled(boolean registerShutdownHook) {
        delegate.setShutdownHookRegistrationEnabled(registerShutdownHook);
        return this;
    }

    /**
     * Checks if newly created {@link DiscordApi} instances should register a shutdown hook to disconnect the
     * instance.
     *
     * @return Whether the shutdown hook will be registered or not.
     * @see #setShutdownHookRegistrationEnabled(boolean)
     */
    public boolean isShutdownHookRegistrationEnabled() {
        return delegate.isShutdownHookRegistrationEnabled();
    }

    /**
     * Sets intent for the events which should be received.
     *
     * @param intents One or more intents from {@link Intent}.
     * @return The current instance in order to chain call methods.
     */
    public DiscordApiBuilder setIntents(Intent... intents) {
        setAllIntentsWhere(intent -> Arrays.asList(intents).contains(intent));
        return this;
    }

    /**
     * Sets all intents.
     *
     * @return The current instance in order to chain call methods.
     */
    public DiscordApiBuilder setAllIntents() {
        setAllIntentsWhere(intent -> true);
        return this;
    }

    /**
     * Sets all non privileged intents.
     *
     * <p>This is the default behavior if no intents are set in the builder.
     *
     * @return The current instance in order to chain call methods.
     */
    public DiscordApiBuilder setAllNonPrivilegedIntents() {
        setAllIntentsWhere(intent -> !intent.isPrivileged());
        return this;
    }

    /**
     * Sets all intents except the given intents.
     *
     * @param intentsToOmit One or more {@code Intent}s which should be omitted.
     * @return The current instance in order to chain call methods.
     */
    public DiscordApiBuilder setAllIntentsExcept(Intent... intentsToOmit) {
        setAllIntentsWhere(intent -> !Arrays.asList(intentsToOmit).contains(intent));
        return this;
    }

    /**
     * Sets all non privileged intents except the given intents.
     *
     * @param intentsToOmit One or more {@code Intent}s which should be omitted.
     * @return The current instance in order to chain call methods.
     */
    public DiscordApiBuilder setAllNonPrivilegedIntentsExcept(Intent... intentsToOmit) {
        setAllIntentsWhere(intent -> !intent.isPrivileged() && !Arrays.asList(intentsToOmit).contains(intent));
        return this;
    }

    /**
     * Sets all non privileged intents and the given intents.
     *
     * @param intentsToInclude One or more {@code Intent}s which should be included.
     * @return The current instance in order to chain call methods.
     */
    public DiscordApiBuilder setAllNonPrivilegedIntentsAnd(Intent... intentsToInclude) {
        setAllIntentsWhere(intent -> !intent.isPrivileged());
        addIntents(intentsToInclude);
        return this;
    }

    /**
     * Sets the intents where the given predicate matches.
     *
     * @param condition Whether the intent should be added or not.
     * @return The current instance in order to chain call methods.
     */
    public DiscordApiBuilder setAllIntentsWhere(Predicate<Intent> condition) {
        delegate.setAllIntentsWhere(condition);
        return this;
    }

    /**
     * Adds the intents specified.
     *
     * @param intents The intents to add.
     * @return The current instance in order to chain call methods.
     */
    public DiscordApiBuilder addIntents(Intent... intents) {
        delegate.addIntents(intents);
        return this;
    }

    /**
     * Retrieves the recommended shards count from the Discord API and sets it in this builder.
     * Sharding allows you to split your bot into several independent instances.
     * A shard only handles a subset of a bot's servers.
     *
     * @return A future with the current api builder.
     * @see <a href="https://discord.com/developers/docs/topics/gateway#sharding">API docs</a>
     */
    public CompletableFuture<DiscordApiBuilder> setRecommendedTotalShards() {
        return delegate.setRecommendedTotalShards().thenCompose(nothing -> CompletableFuture.completedFuture(this));
    }

    @Override
    public <T extends GloballyAttachableListener> DiscordApiBuilder addListener(Class<T> listenerClass, T listener) {
        delegate.addListener(listenerClass, listener);
        return this;
    }

    @Override
    public DiscordApiBuilder addListener(GloballyAttachableListener listener) {
        delegate.addListener(listener);
        return this;
    }

    @Override
    public <T extends GloballyAttachableListener> DiscordApiBuilder addListener(
                                            Class<T> listenerClass, Supplier<T> listenerSupplier) {
        delegate.addListener(listenerClass, listenerSupplier);
        return this;
    }

    @Override
    public DiscordApiBuilder addListener(Supplier<GloballyAttachableListener> listenerSupplier) {
        delegate.addListener(listenerSupplier);
        return this;
    }

    @Override
    public <T extends GloballyAttachableListener> DiscordApiBuilder addListener(
                                Class<T> listenerClass, Function<DiscordApi, T> listenerFunction) {
        delegate.addListener(listenerClass, listenerFunction);
        return this;
    }

    @Override
    public DiscordApiBuilder addListener(Function<DiscordApi, GloballyAttachableListener> listenerFunction) {
        delegate.addListener(listenerFunction);
        return this;
    }

    @Override
    public DiscordApiBuilder removeListener(GloballyAttachableListener listener) {
        delegate.removeListener(listener);
        return this;
    }

    @Override
    public <T extends GloballyAttachableListener> DiscordApiBuilder removeListener(Class<T> listenerClass, T listener) {
        delegate.removeListener(listenerClass, listener);
        return this;
    }

    @Override
    public DiscordApiBuilder removeListenerSupplier(Supplier<GloballyAttachableListener> listenerSupplier) {
        delegate.removeListenerSupplier(listenerSupplier);
        return this;
    }

    @Override
    public <T extends GloballyAttachableListener> DiscordApiBuilder removeListenerSupplier(
                                                            Class<T> listenerClass, Supplier<T> listenerSupplier) {
        delegate.removeListenerSupplier(listenerClass, listenerSupplier);
        return this;
    }

    @Override
    public DiscordApiBuilder removeListenerFunction(Function<DiscordApi, GloballyAttachableListener> listenerFunction) {
        delegate.removeListenerFunction(listenerFunction);
        return this;
    }

    @Override
    public <T extends GloballyAttachableListener> DiscordApiBuilder removeListenerFunction(
                                                    Class<T> listenerClass, Function<DiscordApi, T> listenerFunction) {
        delegate.removeListenerFunction(listenerClass, listenerFunction);
        return this;
    }
}
