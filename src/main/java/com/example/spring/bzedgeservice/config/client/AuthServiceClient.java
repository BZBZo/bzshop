package com.example.spring.bzedgeservice.config.client;

import com.example.spring.bzedgeservice.dto.ValidTokenRequestDTO;
import com.example.spring.bzedgeservice.dto.ValidTokenResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class AuthServiceClient {
    // PreGatewayFilter에서 Bean으로 등록할때 지정한 이름으로(authClient)
    private final WebClient authClient;
    /*
    * 토큰 검증 후 상태 코드를 반환(1:유효, 2:무효, -1:오류)
     */

    public Mono<Integer> validToken(String token) {
        token = token.replaceFirst("(?i)^Bearer ", "");
        return  authClient.post()
                .uri("/token/validToken")
                .bodyValue(
                        ValidTokenRequestDTO.builder()
                                .token(token)
                                .build()
                )
                .retrieve()
                .bodyToMono(ValidTokenResponseDTO.class)
                .map(ValidTokenResponseDTO::getStatusNum)
                .onErrorResume(e->{
                    // 예외 처리
                    return Mono.just(-1); //오류 발생시 -1 반환
                });
    }
}
