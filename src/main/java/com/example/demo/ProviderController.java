package com.example.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ============================================================================
 *  MICROSERVICE COMMUNICATION — PROVIDER SIDE (the "callee" / remote service)
 * ============================================================================
 *
 * In real life, this class would live in a COMPLETELY SEPARATE microservice -
 * its own JVM, its own deployment, possibly its own database and team. In
 * this single-process demo we run it in the SAME application as the consumer
 * purely for convenience, so you can learn all four communication styles
 * without spinning up two Spring Boot apps.
 *
 * All four consumer methods (RestTemplate, Feign, WebClient, RestClient) call
 * THIS exact same endpoint. That's the whole point: the wire contract (the
 * URL + JSON shape) never changes - only the client-side tool used to invoke
 * it changes. This proves that the choice between RestTemplate / Feign /
 * WebClient / RestClient is purely a CONSUMER-side decision; the provider
 * does not know or care which client called it.
 *
 * Exposes: GET http://localhost:8089/provider/greeting
 * Returns JSON: {"message":"Hello from PROVIDER service","from":"provider-service"}
 *
 * @RestController = @Controller + @ResponseBody, so the returned
 * GreetingResponse object is automatically serialized to JSON by Jackson and
 * written straight into the HTTP response body (no view/template involved).
 */
@RestController
@RequestMapping("/provider")
public class ProviderController {

    /**
     * A plain, ordinary REST endpoint - nothing special is needed here just
     * because it will be called by Feign/WebClient/etc. Any standard
     * Spring MVC @GetMapping works as a microservice "provider" endpoint;
     * the magic lives entirely on the consumer side.
     */
    @GetMapping("/greeting")
    public GreetingResponse greeting() {
        return new GreetingResponse("Hello from PROVIDER service", "provider-service");
    }
}
