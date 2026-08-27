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
import java.util.Map;

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


        /*
         * =====================================================
         * CONNECT
         * =====================================================
         *
         * Authenticate the JWT when the client connects.
         */
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
                        claims.get(
                                "roles",
                                List.class
                        );

                if (roles == null || roles.isEmpty()) {

                    throw new IllegalArgumentException(
                            "No roles found in JWT"
                    );
                }


                Long userId =
                        claims.get(
                                "userId",
                                Long.class
                        );

                Long institutionId =
                        claims.get(
                                "institutionId",
                                Long.class
                        );


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
                                userId,
                                email,
                                institutionId,
                                roles
                        );


                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                authenticatedUser,
                                null,
                                authorities
                        );


                /*
                 * IMPORTANT:
                 *
                 * Attach Authentication to the CONNECT
                 * message.
                 */
                accessor.setUser(authentication);


                /*
                 * Store the AuthenticatedUser in the
                 * WebSocket session.
                 */
                Map<String, Object> sessionAttributes =
                        accessor.getSessionAttributes();

                if (sessionAttributes != null) {

                    sessionAttributes.put(
                            AUTHENTICATED_USER,
                            authenticatedUser
                    );
                }


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


        /*
         * =====================================================
         * SUBSEQUENT STOMP MESSAGES
         * =====================================================
         *
         * SEND / SUBSCRIBE / etc.
         *
         * Recover the authenticated user from the
         * WebSocket session.
         */
        Map<String, Object> sessionAttributes =
                accessor.getSessionAttributes();


        if (sessionAttributes != null) {

            Object storedUser =
                    sessionAttributes.get(
                            AUTHENTICATED_USER
                    );


            if (storedUser instanceof AuthenticatedUser authenticatedUser) {

                List<String> roles =
                        authenticatedUser.getRoles();


                List<SimpleGrantedAuthority> authorities =
                        roles.stream()
                                .map(role ->
                                        new SimpleGrantedAuthority(
                                                "ROLE_" + role
                                        )
                                )
                                .toList();


                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                authenticatedUser,
                                null,
                                authorities
                        );


                /*
                 * THIS IS THE IMPORTANT PART.
                 *
                 * Attach the authentication to every
                 * subsequent STOMP message.
                 */
                accessor.setUser(authentication);


                return MessageBuilder
                        .createMessage(
                                message.getPayload(),
                                accessor.getMessageHeaders()
                        );
            }
        }


        return message;
    }
}