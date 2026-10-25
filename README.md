# spring-comm-demo

A tiny, single-app Spring Boot demo showing **three ways one microservice calls
another over REST**:

| Way | Client | Style | Endpoint to try |
|-----|--------|-------|-----------------|
| 1 | `RestTemplate` | Synchronous / blocking | `GET /consumer/rest-template` |
| 2 | `Feign` client | Declarative (interface) | `GET /consumer/feign` |
| 3 | `WebClient` (WebFlux) | Reactive / non-blocking | `GET /consumer/web-client` |

To keep it runnable as ONE process, the same app also plays the "other"
service (the **provider**): `GET /provider/greeting`.

## Run

```bash
cd spring-comm-demo
mvn spring-boot:run
```

Then open in a browser or curl:

```bash
curl http://localhost:8080/provider/greeting        # the target service
curl http://localhost:8080/consumer/rest-template    # Way 1
curl http://localhost:8080/consumer/feign            # Way 2
curl http://localhost:8080/consumer/web-client       # Way 3
```

All three return the same JSON:
`{"message":"Hello from PROVIDER service","from":"provider-service"}`

## Files

- `ProviderController`  – the remote endpoint being called.
- `ConsumerController`  – calls it 3 ways.
- `HttpClientConfig`    – registers `RestTemplate` and `WebClient` beans.
- `ProviderFeignClient` – declarative Feign interface.
- `GreetingResponse`    – shared DTO (JSON body).


GET http://localhost:8089/consumer/rest-template   # WAY 1 RestTemplate (blocking, legacy)
GET http://localhost:8089/consumer/feign           # WAY 2 Feign (declarative)
GET http://localhost:8089/consumer/web-client      # WAY 3 WebClient (reactive)
GET http://localhost:8089/consumer/rest-client     # WAY 4 RestClient (blocking, modern)
