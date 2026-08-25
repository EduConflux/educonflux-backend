package com.projects.EduConflux.common.config;

import com.projects.EduConflux.common.security.AuthenticatedUser;
import com.projects.EduConflux.identity.service.JwtService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtStompInterceptor implements ChannelInterceptor {

    public static final String AUTHENTICATED_USER =
            "AUTHENTICATED_USER";

    private final JwtService jwtService;

    @Override
    public Message<?> preSend(
            Message<?> message,
            MessageChannel channel) {

        StompHeaderAccessor accessor =
                StompHeaderAccessor.wrap(message);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {

            String authHeader =
                    accessor.getFirstNativeHeader(
                            "Authorization"
                    );

            if (authHeader == null ||
                    !authHeader.startsWith("Bearer ")) {

                throw new IllegalArgumentException(
                        "Missing WebSocket Authorization token"
                );
            }

            String token =
                    authHeader.substring(7);

            try {

                Claims claims =
                        jwtService.extractClaims(token);

                String email =
                        claims.getSubject();

                List<String> roles =
                        claims.get("roles", List.class);

                if (roles == null || roles.isEmpty()) {
                    throw new IllegalArgumentException(
                            "No roles found in JWT"
                    );
                }

                List<SimpleGrantedAuthority> authorities =
                        roles.stream()
                                .map(role ->
                                        new SimpleGrantedAuthority(
                                                "ROLE_" + role
                                        )
                                )
                                .toList();

                AuthenticatedUser authenticatedUser =
                        new AuthenticatedUser(
                                claims.get(
                                        "userId",
                                        Long.class
                                ),
                                email,
                                claims.get(
                                        "institutionId",
                                        Long.class
                                ),
                                roles
                        );

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                authenticatedUser,
                                null,
                                authorities
                        );

                /*
                 * Store the authenticated user in the
                 * WebSocket/STOMP session.
                 *
                 * This survives subsequent SEND messages.
                 */
                if (accessor.getSessionAttributes() != null) {

                    accessor.getSessionAttributes().put(
                            AUTHENTICATED_USER,
                            authenticatedUser
                    );
                }

                /*
                 * Also attach Authentication as Principal.
                 * This is useful for Spring Security and
                 * gives us both mechanisms.
                 */
                accessor.setUser(authentication);

                System.out.println(
                        "WEBSOCKET JWT AUTHENTICATED: "
                                + email
                );

                System.out.println(
                        "WEBSOCKET ROLES: "
                                + roles
                );

                System.out.println(
                        "WEBSOCKET SESSION USER STORED"
                );

                /*
                 * Return the modified message.
                 */
                return MessageBuilder
                        .createMessage(
                                message.getPayload(),
                                accessor.getMessageHeaders()
                        );

            } catch (Exception e) {

                throw new IllegalArgumentException(
                        "Invalid WebSocket JWT",
                        e
                );
            }
        }

        return message;
    }
}