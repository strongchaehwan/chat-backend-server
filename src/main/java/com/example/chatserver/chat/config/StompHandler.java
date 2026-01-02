package com.example.chatserver.chat.config;


import com.example.chatserver.common.auth.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class StompHandler implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;

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

        return message;
    }

    private String getToken(StompHeaderAccessor accessor) {
        String bearerToken = accessor.getFirstNativeHeader("Authorization");
        String token = bearerToken.substring(7);
        return token;
    }
}
