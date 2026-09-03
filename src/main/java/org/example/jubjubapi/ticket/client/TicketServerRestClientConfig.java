package org.example.jubjubapi.ticket.client;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

import java.time.Duration;

/**
 * HTTP 타임아웃 설정 (Redis TTL 3초 설정해놓음)
 * 추후에 HttpClient 직접 구성해서 연결 타임아웃 잡아보기 (후순위)
 */
@Configuration
public class TicketServerRestClientConfig {

    @Bean
    public ClientHttpRequestFactory ticketServerRequestFactory() {
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setConnectionRequestTimeout(Duration.ofSeconds(1)); //커넥션 풀에서 연결대기
        factory.setReadTimeout(Duration.ofSeconds(2)); //응답대기
        return factory;
    }
}
