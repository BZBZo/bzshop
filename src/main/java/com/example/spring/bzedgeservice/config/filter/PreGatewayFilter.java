package com.example.spring.bzedgeservice.config.filter;

import com.example.spring.bzedgeservice.config.client.AuthServiceClient;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;

//필터 순서 지정(가장 처음 실행돼야할 필터)
@Order(0)
@Component
@Slf4j
public class PreGatewayFilter extends AbstractGatewayFilterFactory<PreGatewayFilter.Config> {

    // @RequiredArgsConstructor 못씀.
    public PreGatewayFilter(AuthServiceClient authServiceClient) {
        super(Config.class); // 얘를 수동으로 넣어줘야함
        this.authServiceClient = authServiceClient;
    }

    private final AuthServiceClient authServiceClient;

    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {
            String token = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            log.info("token: {}", token);

            // 토큰 유효성 검사
            if (token != null && !token.startsWith(config.getTokenPrefix())) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
            return authServiceClient.validToken(token)
                    .flatMap(statusNum -> {
                        if (statusNum == 2) {
                            exchange.getResponse().setStatusCode(HttpStatusCode.valueOf(config.getAuthenticationTimeoutCode()));
                            return exchange.getResponse().setComplete();
                        } else if (statusNum == 3 || statusNum == -1) {
                            exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
                            return exchange.getResponse().setComplete();
                        }
                        return chain.filter(exchange);
                    })
                    .onErrorResume(e -> {
                        log.error("token filter error: {}", e.getMessage());
                        exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
                        return exchange.getResponse().setComplete();
                    });
            });
    }

    // 여기서 설정한 config 값이 위의 apply로 들어감
    @Getter
    @Setter
    public static class Config {
        private String tokenPrefix = "Bearer";
        private int authenticationTimeoutCode = 419;
    }

}
