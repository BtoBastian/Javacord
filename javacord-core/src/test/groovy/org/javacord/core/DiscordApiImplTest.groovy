package org.javacord.core

import io.netty.handler.codec.http.HttpHeaderNames
import okhttp3.Credentials
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.test.appender.ListAppender
import org.javacord.api.entity.server.Server
import org.javacord.api.exception.NotFoundException
import org.javacord.test.MockProxyManager
import org.mockserver.configuration.ConfigurationProperties
import org.mockserver.model.HttpRequest
import org.mockserver.model.HttpResponse
import org.mockserver.verify.VerificationTimes
import spock.lang.*
import spock.util.environment.RestoreSystemProperties

import javax.net.ssl.SSLHandshakeException
import java.util.concurrent.CompletionException

@Subject(DiscordApiImpl)
class DiscordApiImplTest extends Specification {

    @Subject
    def api = new DiscordApiImpl(null, null, null, null, null, false)

    def 'getAllServers returns all servers'() {
        given:
            Server readyServer = Stub()
            Server nonReadyServer = Stub()
            api.@servers << [0: readyServer]
            api.@nonReadyServers << [1: nonReadyServer]

        expect:
            with(api.allServers) {
                it.size() == 2
                it.containsAll readyServer, nonReadyServer
            }
    }

    @Unroll
    def '#collectionGetter returns unmodifiable collection'() {
        when:
            api."$collectionGetter"(*arguments).clear()

        then:
            thrown UnsupportedOperationException

        where:
            collectionGetter                         | arguments
            'getUnavailableServers'                  | []
            'getCachedUsers'                         | []
            'getCachedUsersByName'                   | [null]
            'getCachedUsersByNameIgnoreCase'         | [null]
            'getCachedUsersByNickname'               | [null, null]
            'getCachedUsersByNicknameIgnoreCase'     | [null, null]
            'getCachedUsersByDisplayName'            | [null, null]
            'getCachedUsersByDisplayNameIgnoreCase'  | [null, null]
            'getServers'                             | []
            'getServersByName'                       | [null]
            'getServersByNameIgnoreCase'             | [null]
            'getCustomEmojis'                        | []
            'getCustomEmojisByName'                  | [null]
            'getCustomEmojisByNameIgnoreCase'        | [null]
            'getRoles'                               | []
            'getRolesByName'                         | [null]
            'getRolesByNameIgnoreCase'               | [null]
            'getChannels'                            | []
            'getPrivateChannels'                     | []
            'getServerChannels'                      | []
            'getChannelCategories'                   | []
            'getServerTextChannels'                  | []
            'getServerVoiceChannels'                 | []
            'getTextChannels'                        | []
            'getVoiceChannels'                       | []
            'getChannelsByName'                      | [null]
            'getChannelsByNameIgnoreCase'            | [null]
            'getTextChannelsByName'                  | [null]
            'getTextChannelsByNameIgnoreCase'        | [null]
            'getVoiceChannelsByName'                 | [null]
            'getVoiceChannelsByNameIgnoreCase'       | [null]
            'getServerChannelsByName'                | [null]
            'getServerChannelsByNameIgnoreCase'      | [null]
            'getChannelCategoriesByName'             | [null]
            'getChannelCategoriesByNameIgnoreCase'   | [null]
            'getServerTextChannelsByName'            | [null]
            'getServerTextChannelsByNameIgnoreCase'  | [null]
            'getServerVoiceChannelsByName'           | [null]
            'getServerVoiceChannelsByNameIgnoreCase' | [null]
            'getAllServers'                          | []
    }

    @RestoreSystemProperties
    def 'REST calls with a man-in-the-middle attack fail'() {
        given:
            MockProxyManager.mockProxy.when(
                    HttpRequest.request()
            ) respond HttpResponse.response().withStatusCode(HttpURLConnection.HTTP_NOT_FOUND)
            MockProxyManager.setHttpSystemProperties()
            def api = new DiscordApiImpl('fakeBotToken', null, null, null, null, false)

        when:
            api.applicationInfo.join()

        then:
            CompletionException ce = thrown()
            ce.cause instanceof SSLHandshakeException
    }

    @RestoreSystemProperties
    def 'REST calls with man-in-the-middle attack allowed do not fail with handshake exception'() {
        given:
            MockProxyManager.mockProxy.when(
                    HttpRequest.request()
            ) respond HttpResponse.response().withStatusCode(HttpURLConnection.HTTP_NOT_FOUND)
            MockProxyManager.setHttpSystemProperties()
            def api = new DiscordApiImpl('fakeBotToken', null, null, null, null, true)

        when:
            api.applicationInfo.join()

        then:
            CompletionException ce = thrown()
            ce.cause instanceof NotFoundException
            ce.cause.message == 'Received a 404 response from Discord with body !'

        and:
            MockProxyManager.mockProxy.verify HttpRequest.request(), VerificationTimes.atLeast(1)
    }

    def 'allowing man-in-the-middle attacks logs a warning on api instantiation'() {
        when:
            new DiscordApiImpl('fakeBotToken', null, null, null, null, true)

        then:
            def expectedWarning = 'All SSL certificates are trusted when connecting to the Discord API and websocket.' +
                    ' This increases the risk of man-in-the-middle attacks!'
            new ArrayList<>(ListAppender.getListAppender('Test Appender').events)
                    .findAll { it.level == Level.WARN }
                    .any { it.message.formattedMessage == expectedWarning }
    }

    @RestoreSystemProperties
    def 'REST calls are done via system properties configured HTTP proxy'() {
        given:
            MockProxyManager.mockProxy.when(
                    HttpRequest.request()
            ) respond HttpResponse.response().withStatusCode(HttpURLConnection.HTTP_NOT_FOUND)
            MockProxyManager.setHttpSystemProperties()
            def api = new DiscordApiImpl('fakeBotToken', null, null, null, null, true)

        when:
            api.applicationInfo.join()

        then:
            CompletionException ce = thrown()
            ce.cause instanceof NotFoundException
            ce.cause.message == 'Received a 404 response from Discord with body !'

        and:
            MockProxyManager.mockProxy.verify HttpRequest.request(), VerificationTimes.atLeast(1)
    }

    def 'REST calls are done via system default proxy selector configured proxy'() {
        given:
            MockProxyManager.mockProxy.when(
                    HttpRequest.request()
            ) respond HttpResponse.response().withStatusCode(HttpURLConnection.HTTP_NOT_FOUND)
            def defaultProxySelector = ProxySelector.default
            ProxySelector.default = MockProxyManager.proxySelector
            def api = new DiscordApiImpl('fakeBotToken', null, null, null, null, true)

        when:
            api.applicationInfo.join()

        then:
            CompletionException ce = thrown()
            ce.cause instanceof NotFoundException
            ce.cause.message == 'Received a 404 response from Discord with body !'

        and:
            MockProxyManager.mockProxy.verify HttpRequest.request(), VerificationTimes.atLeast(1)

        cleanup:
            ProxySelector.default = defaultProxySelector
    }

    def 'REST calls are done via explicitly configured proxy'() {
        given:
            MockProxyManager.mockProxy.when(
                    HttpRequest.request()
            ) respond HttpResponse.response().withStatusCode(HttpURLConnection.HTTP_NOT_FOUND)
            def api = new DiscordApiImpl('fakeBotToken', null, null, MockProxyManager.httpProxy, null, true)

        when:
            api.applicationInfo.join()

        then:
            CompletionException ce = thrown()
            ce.cause instanceof NotFoundException
            ce.cause.message == 'Received a 404 response from Discord with body !'

        and:
            MockProxyManager.mockProxy.verify HttpRequest.request(), VerificationTimes.atLeast(1)
    }

    def 'configuring proxy and proxySelector throws an IllegalStateException'() {
        when:
            new DiscordApiImpl('fakeBotToken', null, Stub(ProxySelector), Proxy.NO_PROXY, null, true)

        then:
            IllegalStateException ise = thrown()
            ise.message == 'proxy and proxySelector must not be configured both'
    }

    def 'REST calls are done via explicitly configured proxy selector'() {
        given:
            MockProxyManager.mockProxy.when(
                    HttpRequest.request()
            ) respond HttpResponse.response().withStatusCode(HttpURLConnection.HTTP_NOT_FOUND)
            def api = new DiscordApiImpl('fakeBotToken', null, MockProxyManager.proxySelector, null, null, true)

        when:
            api.applicationInfo.join()

        then:
            CompletionException ce = thrown()
            ce.cause instanceof NotFoundException
            ce.cause.message == 'Received a 404 response from Discord with body !'

        and:
            MockProxyManager.mockProxy.verify HttpRequest.request(), VerificationTimes.atLeast(1)
    }

    @RestoreSystemProperties
    def 'REST calls through authenticated HTTP proxy use the system default authenticator'() {
        given:
            def username = UUID.randomUUID().toString()
            def password = UUID.randomUUID().toString()
            ConfigurationProperties.httpProxyServerUsername username
            ConfigurationProperties.httpProxyServerPassword password

        and:
            MockProxyManager.mockProxy.when(
                    HttpRequest.request()
            ) respond HttpResponse.response().withStatusCode(HttpURLConnection.HTTP_NOT_FOUND)

        and:
            def defaultAuthenticator = Authenticator.theAuthenticator
            Authenticator.default = Mock(Authenticator) {
                (1.._) * getPasswordAuthentication() >> new PasswordAuthentication(username, password as char[])
            }
            def api = new DiscordApiImpl('fakeBotToken', null, null, MockProxyManager.httpProxy, null, true)

        when:
            api.applicationInfo.join()

        then:
            CompletionException ce = thrown()
            ce.cause instanceof NotFoundException
            ce.cause.message == 'Received a 404 response from Discord with body !'

        and:
            MockProxyManager.mockProxy.verify HttpRequest.request(), VerificationTimes.atLeast(1)

        cleanup:
            Authenticator.default = defaultAuthenticator
    }

    @RestoreSystemProperties
    def 'REST calls through authenticated HTTP proxy use an explicit authenticator'() {
        given:
            def username = UUID.randomUUID().toString()
            def password = UUID.randomUUID().toString()
            String credentials = Credentials.basic username, password
            ConfigurationProperties.httpProxyServerUsername username
            ConfigurationProperties.httpProxyServerPassword password

        and:
            MockProxyManager.mockProxy.when(
                    HttpRequest.request()
            ) respond HttpResponse.response().withStatusCode(HttpURLConnection.HTTP_NOT_FOUND)

        and:
            org.javacord.api.util.auth.Authenticator authenticator = Mock {
                (1.._) * authenticate(_, _, _) >> [(HttpHeaderNames.PROXY_AUTHORIZATION as String): [null, credentials]]
            }
            def api = new DiscordApiImpl('fakeBotToken', null, null, MockProxyManager.httpProxy, authenticator, true)

        when:
            api.applicationInfo.join()

        then:
            CompletionException ce = thrown()
            ce.cause instanceof NotFoundException
            ce.cause.message == 'Received a 404 response from Discord with body !'

        and:
            MockProxyManager.mockProxy.verify HttpRequest.request(), VerificationTimes.atLeast(1)
    }

    @IgnoreIf({
        // SOCKS4 implementation in Java 8 is broken and never used
        // but always SOCKS5, so do not execute this test on Java 8
        def javaVersion = System.properties.'java.version'
        javaVersion.startsWith('1.8.') || (javaVersion == '1.8') ||
                javaVersion.startsWith('8.') || (javaVersion == '8')
    })
    @PendingFeature(exceptions = MissingMethodException,
            reason = 'OkHttp does not resolve the hostname currently, so SOCKS4 cannot work')
    @RestoreSystemProperties
    def 'REST calls are done via system properties configured SOCKS4 proxy'() {
        given:
            MockProxyManager.mockProxy.when(
                    HttpRequest.request()
            ) respond HttpResponse.response().withStatusCode(HttpURLConnection.HTTP_NOT_FOUND)
            MockProxyManager.setSocks4SystemProperties()

        and:
            def api = new DiscordApiImpl('fakeBotToken', 0, 1, Collections.emptySet(), false, false, null,
                    null, null, null, true,
                    null, { [InetAddress.getLoopbackAddress()] })

        when:
            api.applicationInfo.join()

        then:
            CompletionException ce = thrown()
            ce.cause instanceof NotFoundException
            ce.cause.message == 'Received a 404 response from Discord with body !'

        and:
            MockProxyManager.mockProxy.verify HttpRequest.request(), VerificationTimes.atLeast(1)
    }

    @IgnoreIf({
        // SOCKS5 implementation in Java 8 is broken when the answer
        // contains a domain type address which MockProxy supplies,
        // so do not execute this test on Java 8
        def javaVersion = System.properties.'java.version'
        javaVersion.startsWith('1.8.') || (javaVersion == '1.8') ||
                javaVersion.startsWith('8.') || (javaVersion == '8')
    })
    @RestoreSystemProperties
    def 'REST calls are done via system properties configured SOCKS5 proxy'() {
        given:
            MockProxyManager.mockProxy.when(
                    HttpRequest.request()
            ) respond HttpResponse.response().withStatusCode(HttpURLConnection.HTTP_NOT_FOUND)
            MockProxyManager.setSocks5SystemProperties()
            def api = new DiscordApiImpl('fakeBotToken', null, null, null, null, true)

        when:
            api.applicationInfo.join()

        then:
            CompletionException ce = thrown()
            ce.cause instanceof NotFoundException
            ce.cause.message == 'Received a 404 response from Discord with body !'

        and:
            MockProxyManager.mockProxy.verify HttpRequest.request(), VerificationTimes.atLeast(1)
    }

    @IgnoreIf({
        // SOCKS5 implementation in Java 8 is broken when the answer
        // contains a domain type address which MockProxy supplies,
        // so do not execute this test on Java 8
        def javaVersion = System.properties.'java.version'
        javaVersion.startsWith('1.8.') || (javaVersion == '1.8') ||
                javaVersion.startsWith('8.') || (javaVersion == '8')
    })
    @RestoreSystemProperties
    def 'REST calls through authenticated SOCKS5 proxy use the system default authenticator'() {
        given:
            MockProxyManager.mockProxy.when(
                    HttpRequest.request()
            ) respond HttpResponse.response().withStatusCode(HttpURLConnection.HTTP_NOT_FOUND)
            def api = new DiscordApiImpl('fakeBotToken', null, null, MockProxyManager.socksProxy, null, true)

        and:
            def username = UUID.randomUUID().toString()
            def password = UUID.randomUUID().toString()
            ConfigurationProperties.socksProxyServerUsername username
            ConfigurationProperties.socksProxyServerPassword password

        and:
            def defaultAuthenticator = Authenticator.theAuthenticator
            Authenticator.default = Mock(Authenticator) {
                (1.._) * getPasswordAuthentication() >> new PasswordAuthentication(username, password as char[])
            }

        when:
            api.applicationInfo.join()

        then:
            CompletionException ce = thrown()
            ce.cause instanceof NotFoundException
            ce.cause.message == 'Received a 404 response from Discord with body !'

        and:
            MockProxyManager.mockProxy.verify HttpRequest.request(), VerificationTimes.atLeast(1)

        cleanup:
            Authenticator.default = defaultAuthenticator
    }

    @RestoreSystemProperties
    def 'Explicitly configured proxy for REST calls takes precedence'() {
        given:
            MockProxyManager.mockProxy.when(
                    HttpRequest.request()
            ) respond HttpResponse.response().withStatusCode(HttpURLConnection.HTTP_NOT_FOUND)
            System.properties.'https.proxyHost' = '0.0.0.1'
            System.properties.'https.proxyPort' = '1'
            def defaultProxySelector = ProxySelector.default
            ProxySelector.default = new ProxySelector() {
                @Override
                List<Proxy> select(URI uri) {
                    [new Proxy(
                            Proxy.Type.HTTP,
                            new InetSocketAddress(InetAddress.getByAddress([0, 0, 0, 1] as byte[]), 1))]
                }

                @Override
                void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
                }
            }
            def api = new DiscordApiImpl('fakeBotToken', null, null, MockProxyManager.httpProxy, null, true)

        when:
            api.applicationInfo.join()

        then:
            CompletionException ce = thrown()
            ce.cause instanceof NotFoundException
            ce.cause.message == 'Received a 404 response from Discord with body !'

        and:
            MockProxyManager.mockProxy.verify HttpRequest.request(), VerificationTimes.atLeast(1)

        cleanup:
            ProxySelector.default = defaultProxySelector
    }

    @RestoreSystemProperties
    def 'Explicitly configured proxy selector for REST calls takes precedence'() {
        given:
            MockProxyManager.mockProxy.when(
                    HttpRequest.request()
            ) respond HttpResponse.response().withStatusCode(HttpURLConnection.HTTP_NOT_FOUND)
            System.properties.'https.proxyHost' = '0.0.0.1'
            System.properties.'https.proxyPort' = '1'
            def defaultProxySelector = ProxySelector.default
            ProxySelector.default = new ProxySelector() {
                @Override
                List<Proxy> select(URI uri) {
                    [new Proxy(
                            Proxy.Type.HTTP,
                            new InetSocketAddress(InetAddress.getByAddress([0, 0, 0, 1] as byte[]), 1))]
                }

                @Override
                void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
                }
            }
            def api = new DiscordApiImpl('fakeBotToken', null, MockProxyManager.proxySelector, null, null, true)

        when:
            api.applicationInfo.join()

        then:
            CompletionException ce = thrown()
            ce.cause instanceof NotFoundException
            ce.cause.message == 'Received a 404 response from Discord with body !'

        and:
            MockProxyManager.mockProxy.verify HttpRequest.request(), VerificationTimes.atLeast(1)

        cleanup:
            ProxySelector.default = defaultProxySelector
    }

    @RestoreSystemProperties
    def 'System default proxy selector for REST calls takes precedence over system properties'() {
        given:
            MockProxyManager.mockProxy.when(
                    HttpRequest.request()
            ) respond HttpResponse.response().withStatusCode(HttpURLConnection.HTTP_NOT_FOUND)
            System.properties.'https.proxyHost' = '0.0.0.1'
            System.properties.'https.proxyPort' = '1'
            def defaultProxySelector = ProxySelector.default
            ProxySelector.default = MockProxyManager.proxySelector
            def api = new DiscordApiImpl('fakeBotToken', null, null, null, null, true)

        when:
            api.applicationInfo.join()

        then:
            CompletionException ce = thrown()
            ce.cause instanceof NotFoundException
            ce.cause.message == 'Received a 404 response from Discord with body !'

        and:
            MockProxyManager.mockProxy.verify HttpRequest.request(), VerificationTimes.atLeast(1)

        cleanup:
            ProxySelector.default = defaultProxySelector
    }

    @RestoreSystemProperties
    def 'Explicitly configured authenticator for REST calls takes precedence'() {
        given:
            def username = UUID.randomUUID().toString()
            def password = UUID.randomUUID().toString()
            String credentials = Credentials.basic username, password
            ConfigurationProperties.httpProxyServerUsername username
            ConfigurationProperties.httpProxyServerPassword password

        and:
            MockProxyManager.mockProxy.when(
                    HttpRequest.request()
            ) respond HttpResponse.response().withStatusCode(HttpURLConnection.HTTP_NOT_FOUND)

        and:
            def defaultAuthenticator = Authenticator.theAuthenticator
            Authenticator.default = Mock(Authenticator) {
                0 * getPasswordAuthentication()
            }
            org.javacord.api.util.auth.Authenticator authenticator = Mock {
                (1.._) * authenticate(_, _, _) >> [(HttpHeaderNames.PROXY_AUTHORIZATION as String): [null, credentials]]
            }
            def api = new DiscordApiImpl('fakeBotToken', null, null, MockProxyManager.httpProxy, authenticator, true)

        when:
            api.applicationInfo.join()

        then:
            CompletionException ce = thrown()
            ce.cause instanceof NotFoundException
            ce.cause.message == 'Received a 404 response from Discord with body !'

        and:
            MockProxyManager.mockProxy.verify HttpRequest.request(), VerificationTimes.atLeast(1)

        cleanup:
            Authenticator.default = defaultAuthenticator
    }

}
