package com.pmi.tpd;

import java.security.Principal;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import org.elasticsearch.common.Strings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.HandshakeInterceptor;

import com.google.common.collect.Lists;
import com.pmi.tpd.api.ApplicationConstants;
import com.pmi.tpd.api.event.publisher.IEventPublisher;
import com.pmi.tpd.web.security.jwt.JwtConfigurer;
import com.pmi.tpd.web.security.jwt.JwtTokenProvider;
import com.pmi.tpd.web.websocket.WebSocketController;

/**
 * @author Christophe Friederich
 * @since 1.0
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebsocketConfig implements WebSocketMessageBrokerConfigurer {

    /** */
    public static final String IP_ADDRESS = "IP_ADDRESS";

    @Inject
    private JwtTokenProvider provider;

    @Override
    public void configureMessageBroker(final MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/ws");
    }

    @Bean
    public WebSocketController webSocketController(@Nonnull final SimpMessageSendingOperations messagingTemplate,
        @Nonnull final IEventPublisher eventPublisher) {
        return new WebSocketController(messagingTemplate, eventPublisher);
    }

    @Override
    public void registerStompEndpoints(final StompEndpointRegistry registry) {
        registry.addEndpoint("/activity")
                .setAllowedOriginPatterns("*")
                .withSockJS()
                .setInterceptors(httpSessionHandshakeInterceptor())
                .setSessionCookieNeeded(false);

        registry.addEndpoint("/submissions").setAllowedOriginPatterns("*").withSockJS();
        registry.addEndpoint("/progress").setAllowedOriginPatterns("*").withSockJS();
        registry.addEndpoint("/notification").setAllowedOriginPatterns("*").withSockJS();
    }

    @Override
    public void configureClientInboundChannel(final ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {

            @Override
            public Message<?> preSend(final Message<?> message, final MessageChannel channel) {
                final StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
                final List<String> tokenList = accessor.getNativeHeader(JwtConfigurer.AUTHORIZATION_HEADER);
                String token = null;
                if (tokenList == null || tokenList.size() < 1 && Strings.isNullOrEmpty(tokenList.get(0))) {
                    final Collection<SimpleGrantedAuthority> authorities = Lists.newArrayList();
                    authorities.add(new SimpleGrantedAuthority(ApplicationConstants.Authorities.ANONYMOUS));
                    final Principal principal = new AnonymousAuthenticationToken("WebsocketConfiguration", "anonymous",
                            authorities);
                    accessor.setUser(principal);
                } else {
                    token = tokenList.get(0);
                    // validate and convert to a Principal based on your own requirements e.g.
                    try {
                        final Authentication authentication = provider.getAuthentication(resolveToken(token), null);
                        accessor.setUser(authentication);
                    } catch (final Exception e) {
                        throw new IllegalArgumentException(e.getMessage());
                    }

                }
                // not documented anywhere but necessary otherwise NPE in StompSubProtocolHandler!
                accessor.setLeaveMutable(true);
                return MessageBuilder.createMessage(message.getPayload(), accessor.getMessageHeaders());
            }
        });
    }

    @Bean
    public HandshakeInterceptor httpSessionHandshakeInterceptor() {
        return new HandshakeInterceptor() {

            @Override
            public boolean beforeHandshake(final ServerHttpRequest request,
                final ServerHttpResponse response,
                final WebSocketHandler wsHandler,
                final Map<String, Object> attributes) throws Exception {
                if (request instanceof ServletServerHttpRequest) {
                    final ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
                    attributes.put(IP_ADDRESS, servletRequest.getRemoteAddress());
                }
                return true;
            }

            @Override
            public void afterHandshake(final ServerHttpRequest request,
                final ServerHttpResponse response,
                final WebSocketHandler wsHandler,
                final Exception exception) {

            }
        };
    }

    private static String resolveToken(final String bearerToken) {
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7, bearerToken.length());
        }
        return null;
    }
}
