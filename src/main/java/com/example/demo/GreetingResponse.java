package com.example.demo;

/**
 * ============================================================================
 *  SHARED DTO — the JSON contract passed between PROVIDER and CONSUMER
 * ============================================================================
 *
 * This is the "wire format": whatever the provider returns gets serialized
 * to JSON by Jackson, sent over HTTP, and deserialized back into an instance
 * of this SAME class on the consumer side - regardless of WHICH of the 4
 * HTTP clients (RestTemplate / Feign / WebClient / RestClient) is used to
 * fetch it. This proves the DTO/contract is independent of the transport
 * mechanism.
 *
 * Example JSON on the wire:
 *   {"message":"Hello from PROVIDER service","from":"provider-service"}
 *
 * WHY A JAVA `record` (not a regular class)?
 *   - Records (Java 16+) auto-generate a canonical constructor, accessors
 *     (message(), from() - note: no "get" prefix), equals(), hashCode(), and
 *     toString() - perfect for small, immutable data-carrier objects like
 *     this one, with zero boilerplate.
 *   - Jackson (Spring Boot's default JSON library) supports records out of
 *     the box: it matches JSON field names to record component names for
 *     both serialization (provider side) and deserialization (consumer
 *     side) automatically - no extra annotations needed.
 */
public record GreetingResponse(String message, String from) {
}
