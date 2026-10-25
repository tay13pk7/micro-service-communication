package com.example.demo;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * ============================================================================
 *  WAY 2 — FEIGN CLIENT (the "declarative" HTTP client)
 * ============================================================================
 *
 * This is NOT a class you implement yourself - it's just an INTERFACE that
 * DESCRIBES a remote endpoint using familiar Spring MVC annotations
 * (@GetMapping, @PostMapping, @RequestParam, etc., same ones you'd use on a
 * @RestController). You never write the method body.
 *
 * HOW THE MAGIC HAPPENS:
 *   1. @EnableFeignClients on DemoApplication tells Spring Cloud OpenFeign to
 *      scan for interfaces annotated with @FeignClient.
 *   2. At application startup, Spring generates a dynamic proxy class that
 *      IMPLEMENTS this interface. You never see or write that generated code.
 *   3. When your controller calls feignClient.getGreeting(), it's really
 *      calling into the generated proxy, which:
 *        a. Reads the @GetMapping("/provider/greeting") annotation to know
 *           the HTTP method + path.
 *        b. Combines it with the "url" attribute below to build the full
 *           request URL.
 *        c. Executes the HTTP GET (blocking, like RestTemplate, under the
 *           default configuration).
 *        d. Deserializes the JSON response body into a GreetingResponse.
 *
 * ATTRIBUTES EXPLAINED:
 *   - name : a LOGICAL name for this client. Used internally by Spring Cloud
 *            for configuration keys, logging, metrics, and (in a real
 *            microservices setup with Eureka/Consul) for SERVICE DISCOVERY -
 *            i.e. "find whatever instance is currently registered as
 *            provider-client" instead of a hard-coded host:port.
 *   - url  : here we hard-code the base URL from application.properties
 *            because this demo has no service registry. In production with
 *            service discovery, you would DROP the "url" attribute entirely
 *            and just use "name" - Spring Cloud LoadBalancer would resolve
 *            it to a real instance address and even round-robin between
 *            multiple running instances of the provider service.
 *
 * WHY USE THIS OVER RestTemplate/WebClient DIRECTLY?
 *   - No manual URL building, no boilerplate HTTP client code in your
 *     business logic - the interface IS the contract.
 *   - Centralizes everything about "how to call the provider service" in one
 *     file, making it a single source of truth other developers can read.
 *   - Trivially supports retries, circuit breakers (Resilience4j), and
 *     load balancing via configuration/annotations, without touching the
 *     call sites in ConsumerController.
 */
@FeignClient(name = "provider-client", url = "${provider.base-url}")
public interface ProviderFeignClient {

    /**
     * Just like a @RestController method signature, but with NO body -
     * Feign generates the implementation that performs the actual HTTP call.
     */
    @GetMapping("/provider/greeting")
    GreetingResponse getGreeting();
}
