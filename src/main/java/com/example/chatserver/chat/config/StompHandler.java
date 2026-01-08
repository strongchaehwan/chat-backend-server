package com.example.chatserver.chat.config;


import com.example.chatserver.chat.service.ChatService;
import com.example.chatserver.common.auth.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
@Slf4j
public class StompHandler implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;
    private final ChatService chatService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        final StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message); // 토큰 꺼낼수있음


        if (StompCommand.CONNECT == accessor.getCommand()) {
            log.info("connect 요청시 토큰 유효성 검증");
            String token = this.getToken(accessor);

            // 토큰 검증
            jwtTokenProvider.validateToken(token);
            log.info("토큰 검증 완료");
        }

        if (StompCommand.SUBSCRIBE == accessor.getCommand()) {
            log.info("subscribe 토큰 유효성 검증");
            String token = this.getToken(accessor);

            // 토큰 검증
            jwtTokenProvider.validateToken(token);
            Claims claim = jwtTokenProvider.getClaim(token);
            String email = claim.getSubject();
            String roomId = Objects.requireNonNull(accessor.getDestination()).split("/")[2];

            if (!chatService.isRoomParicipant(email,Long.parseLong(roomId))) {
                throw new AuthenticationServiceException("해당 room에 권한이 없습니다.");
            }
            log.info("토큰 검증 완료");
        }

        return message;
    }

    private String getToken(StompHeaderAccessor accessor) {
        String bearerToken = accessor.getFirstNativeHeader("Authorization");
        String token = bearerToken.substring(7);
        return token;
    }
}
