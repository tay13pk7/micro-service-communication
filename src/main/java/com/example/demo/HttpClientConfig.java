package com.example.demo;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * ============================================================================
 *  BEAN CONFIGURATION — wiring up the HTTP client "tools" (WAYS 1, 3, 4)
 * ============================================================================
 *
 * RestTemplate, WebClient, and RestClient are all generic-purpose HTTP
 * clients that Spring does NOT auto-create for you by default - you must
 * explicitly register them as @Bean methods so they can be dependency-
 * injected into ConsumerController (or anywhere else in the app).
 *
 * Feign is the ODD ONE OUT: it needs NO manual bean here because
 * @EnableFeignClients (on DemoApplication) automatically scans for
 * @FeignClient interfaces and registers their generated proxy implementations
 * as beans for you. That's part of what makes Feign feel "magic".
 *
 * Each *Builder type below (RestTemplateBuilder, WebClient.Builder,
 * RestClient.Builder) is auto-configured by Spring Boot itself; we simply
 * ask for the builder, optionally customize it (timeouts, base URL,
 * default headers, interceptors, etc. - omitted here for simplicity), and
 * call .build() to get the final, ready-to-use, THREAD-SAFE client instance.
 * Because they're thread-safe, ONE shared bean instance is reused for every
 * incoming HTTP request - you don't need to create a new client per call.
 */
@Configuration
public class HttpClientConfig {

    /**
     * WAY 1 ingredient — RestTemplate.
     * Classic, SYNCHRONOUS/BLOCKING HTTP client. Internally delegates to a
     * pluggable ClientHttpRequestFactory (by default the JDK's HttpURLConnection,
     * though in Spring Boot 3.x it may auto-detect Apache HttpClient/Jetty if
     * on the classpath). Still perfectly usable, just considered "legacy" -
     * Spring Framework has stopped adding new features to it.
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

    /**
     * WAY 3 ingredient — WebClient.
     * REACTIVE / NON-BLOCKING HTTP client from Spring WebFlux, built on
     * Project Reactor (Mono/Flux) and, by default, a Netty event-loop
     * transport. Requires the spring-boot-starter-webflux dependency (see
     * pom.xml). Returns reactive types (Mono<T>/Flux<T>) instead of plain
     * objects - the caller must subscribe (or let Spring MVC/WebFlux do it
     * automatically, as in ConsumerController#callWithWebClient).
     */
    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        return builder.build();
    }

    /**
     * WAY 4 ingredient — RestClient.
     * Introduced in Spring Framework 6.1 / Spring Boot 3.2 as the modern
     * SYNCHRONOUS replacement for RestTemplate. It reuses RestTemplate's
     * underlying blocking infrastructure (ClientHttpRequestFactory) but
     * exposes it through WebClient's fluent, chainable API style. No
     * reactive/WebFlux dependency is required to use it - it works fine even
     * in a plain spring-boot-starter-web application.
     */
    @Bean
    public RestClient restClient(RestClient.Builder builder) {
        return builder.build();
    }
}
