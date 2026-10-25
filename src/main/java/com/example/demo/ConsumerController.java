package com.example.demo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * ============================================================================
 *  MICROSERVICE COMMUNICATION — CONSUMER SIDE (the "caller" service)
 * ============================================================================
 *
 * In a real microservices architecture, services almost never run in the same
 * process. Service A (consumer) needs data owned by Service B (provider), so
 * A makes a network call to B over HTTP (or another protocol). This class
 * demonstrates FOUR different Java/Spring tools you can use to make that
 * SAME kind of call: "synchronous request/response over REST".
 *
 * Even though the business outcome is identical (fetch a greeting from the
 * provider), the THREADING MODEL, CODE STYLE, and MATURITY of each client is
 * different — which is exactly what interviewers ask about.
 *
 * Try each one after starting the app (see DemoApplication):
 *   GET http://localhost:8089/consumer/rest-template   -> WAY 1 (old, blocking)
 *   GET http://localhost:8089/consumer/feign           -> WAY 2 (declarative)
 *   GET http://localhost:8089/consumer/web-client       -> WAY 3 (reactive)
 *   GET http://localhost:8089/consumer/rest-client      -> WAY 4 (new, blocking)
 *
 * All four hit the SAME provider endpoint (see ProviderController) and return
 * the SAME JSON body - only the HTTP client mechanics differ underneath.
 *
 * NOTE: This demo is "synchronous HTTP" communication (request -> wait/async
 * -> response). The OTHER major style of microservice communication is
 * ASYNCHRONOUS / EVENT-DRIVEN messaging (e.g. Kafka, RabbitMQ), where the
 * caller publishes a message and does not wait for an immediate reply. That
 * style is not shown in this project, but it's worth remembering:
 *      1. Synchronous (REST/HTTP, gRPC)      <- this whole class
 *      2. Asynchronous messaging (Kafka/MQ)  <- fire-and-forget / pub-sub
 *      3. Service discovery + client-side load balancing (Eureka + Feign/RestTemplate)
 */
@RestController
@RequestMapping("/consumer")
public class ConsumerController {

    // --- The 4 HTTP client "tools" injected by Spring (beans defined in HttpClientConfig / Feign auto-config) ---
    private final RestTemplate restTemplate;   // WAY 1: classic blocking client
    private final ProviderFeignClient feignClient; // WAY 2: declarative interface-based client
    private final WebClient webClient;         // WAY 3: reactive non-blocking client
    private final RestClient restClient;       // WAY 4: modern blocking client
    private final String providerBaseUrl;      // e.g. "http://localhost:8089" (from application.properties)

    /**
     * Constructor injection: Spring hands us the already-built client beans.
     * providerBaseUrl is injected from application.properties via @Value, so
     * the consumer doesn't hard-code the other service's address - in a real
     * system this would come from service discovery (Eureka/Consul) or a
     * gateway/load-balancer URL instead of a literal host:port.
     */
    public ConsumerController(RestTemplate restTemplate,
                              ProviderFeignClient feignClient,
                              WebClient webClient,
                              RestClient restClient,
                              @Value("${provider.base-url}") String providerBaseUrl) {
        this.restTemplate = restTemplate;
        this.feignClient = feignClient;
        this.webClient = webClient;
        this.restClient = restClient;
        this.providerBaseUrl = providerBaseUrl;
    }

    /**
     * ------------------------------------------------------------------
     * WAY 1 — RestTemplate  (SYNCHRONOUS / BLOCKING, legacy but still common)
     * ------------------------------------------------------------------
     * HOW IT WORKS:
     *   1. You manually build the full URL string yourself.
     *   2. restTemplate.getForObject(...) opens an HTTP connection, sends the
     *      GET request, and then BLOCKS the current thread - it literally
     *      stops and waits - until the provider responds.
     *   3. Once the response arrives, Jackson converts the JSON body into a
     *      GreetingResponse object and the method returns it.
     *
     * THREADING: one servlet thread is occupied for the ENTIRE round trip.
     * If the provider is slow, that thread (and the Tomcat thread pool slot)
     * is wasted doing nothing but waiting. Under high load with many slow
     * downstream calls, this can exhaust the thread pool ("thread starvation").
     *
     * STATUS: Not deprecated, but Spring has put it in "maintenance mode" -
     * no new features are added. Spring recommends RestClient or WebClient
     * for new code (see WAY 3 and WAY 4 below).
     *
     * WHEN YOU'D STILL SEE IT: older Spring Boot codebases (pre Spring 6.1),
     * simple internal tools, or teams that haven't migrated yet.
     */
    @GetMapping("/rest-template")
    public GreetingResponse callWithRestTemplate() {
        String url = providerBaseUrl + "/provider/greeting";
        return restTemplate.getForObject(url, GreetingResponse.class);
    }

    /**
     * ------------------------------------------------------------------
     * WAY 2 — Feign  (DECLARATIVE, SYNCHRONOUS / BLOCKING under the hood)
     * ------------------------------------------------------------------
     * HOW IT WORKS:
     *   You never write the HTTP call yourself. Instead you declare a plain
     *   Java INTERFACE (see ProviderFeignClient) annotated with @FeignClient
     *   and @GetMapping, describing what the remote endpoint looks like.
     *   At startup, Spring Cloud OpenFeign generates a dynamic proxy
     *   implementation of that interface. When you call
     *   feignClient.getGreeting(), that generated proxy:
     *     1. Builds the HTTP request based on the interface's annotations.
     *     2. Sends it (still synchronously/blocking by default).
     *     3. Deserializes the JSON response into GreetingResponse.
     *   So "calling a method" literally IS "making an HTTP call" - no
     *   RestTemplate/WebClient boilerplate needed in your business code.
     *
     * WHY TEAMS LIKE IT:
     *   - Looks like a normal local method call -> very readable call sites.
     *   - Centralizes the remote contract in ONE interface file.
     *   - Plays nicely with Spring Cloud features: client-side load
     *     balancing (via Spring Cloud LoadBalancer), retries, and circuit
     *     breakers (Resilience4j) can be bolted on with configuration only.
     *
     * TRADE-OFF: a bit more "magic" (proxy generation, annotation
     * processing) which can be harder to debug than an explicit client call.
     *
     * BEST FOR: large systems with MANY inter-service calls, especially
     * when combined with service discovery (e.g. Eureka) so the "url"
     * attribute is replaced by a logical service name.
     */
    @GetMapping("/feign")
    public GreetingResponse callWithFeign() {
        return feignClient.getGreeting();
    }

    /**
     * ------------------------------------------------------------------
     * WAY 3 — WebClient  (REACTIVE / NON-BLOCKING, from Spring WebFlux)
     * ------------------------------------------------------------------
     * HOW IT WORKS:
     *   Instead of returning the GreetingResponse directly, this method
     *   returns a Mono<GreetingResponse> - a "promise" of a value that will
     *   arrive LATER, not immediately.
     *     1. webClient.get().uri(...) describes the request (nothing is sent
     *        yet - WebClient is lazy, like a recipe).
     *     2. .retrieve() marks "I want the response body".
     *     3. .bodyToMono(GreetingResponse.class) says "when it eventually
     *        arrives, convert the JSON into this type and wrap it in a Mono".
     *   The actual network call only fires when something SUBSCRIBES to the
     *   Mono. Here, Spring MVC/WebFlux subscribes for you automatically
     *   because the controller method's return type is Mono<...> - it knows
     *   to wait for the async result and write it to the HTTP response when
     *   it's ready, WITHOUT blocking a thread while waiting.
     *
     * THREADING: the calling thread is released immediately after kicking
     * off the request; a small pool of event-loop threads (Netty by default)
     * handles the I/O and resumes your code when data arrives. This is why
     * WebClient can handle THOUSANDS of concurrent slow downstream calls
     * using only a handful of threads, unlike RestTemplate.
     *
     * BEST FOR: high-concurrency systems, streaming responses, reactive
     * pipelines (e.g. combining multiple calls with .zip()/.flatMap()),
     * or when calling multiple services in parallel without blocking.
     *
     * COST: steeper learning curve (reactive operators, error handling via
     * onErrorResume, etc.) and requires the reactive mindset end-to-end to
     * get the full benefit.
     */
    @GetMapping("/web-client")
    public Mono<GreetingResponse> callWithWebClient() {
        return webClient.get()
                .uri(providerBaseUrl + "/provider/greeting")
                .retrieve()
                .bodyToMono(GreetingResponse.class);
    }

    /**
     * ------------------------------------------------------------------
     * WAY 4 — RestClient  (SYNCHRONOUS / BLOCKING, modern - Spring 6.1+ / Boot 3.2+)
     * ------------------------------------------------------------------
     * HOW IT WORKS:
     *   Same fluent, chained method style as WebClient
     *   (get().uri().retrieve()...), but notice the LAST call is
     *   `.body(GreetingResponse.class)` instead of `.bodyToMono(...)`.
     *   That single difference means: "don't give me a Mono - BLOCK this
     *   thread right now and hand me the real object directly." There is no
     *   reactive stack involved at all; RestClient is built on the same
     *   blocking HttpClient machinery RestTemplate uses internally, just
     *   wrapped in a much nicer, fluent API.
     *
     * WHY IT EXISTS:
     *   Before Spring 6.1, you had to choose between:
     *     - RestTemplate: blocking, but an old/clunky API (many overloaded
     *       methods like getForObject/postForEntity/exchange...).
     *     - WebClient: modern fluent API, but drags in the reactive/WebFlux
     *       stack even if you never needed non-blocking behavior.
     *   RestClient fills that gap: WebClient's ergonomics + RestTemplate's
     *   simple blocking execution model, no reactive dependency required.
     *
     * THREADING: identical to RestTemplate - the thread blocks and waits.
     * The improvement here is purely DEVELOPER EXPERIENCE (readability,
     * consistent fluent API across your whole codebase), not performance.
     *
     * RECOMMENDATION: for NEW synchronous/blocking code, prefer RestClient
     * over RestTemplate. Use WebClient only when you actually need
     * non-blocking/reactive behavior (WAY 3).
     */
    @GetMapping("/rest-client")
    public GreetingResponse callWithRestClient() {
        return restClient.get()
                .uri(providerBaseUrl + "/provider/greeting")
                .retrieve()
                .body(GreetingResponse.class);
    }
}
