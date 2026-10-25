package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * ============================================================================
 *  APPLICATION ENTRY POINT — the runnable Spring Boot main class
 * ============================================================================
 *
 * This is the file you RUN (either via `mvn spring-boot:run`, `java -jar`,
 * or your IDE's "Run" button on main()) to start the entire demo.
 *
 * A single Spring Boot process plays BOTH microservice roles here, purely to
 * make the demo runnable with one command:
 *   - PROVIDER : exposes GET /provider/greeting      (see ProviderController)
 *                 - this simulates "the OTHER microservice" you'd normally
 *                   call over the network in a real distributed system.
 *   - CONSUMER : calls that same endpoint 4 different ways (see
 *                ConsumerController) - this simulates "YOUR microservice"
 *                reaching out to a dependency.
 *
 * In a REAL deployment these would be two separate Spring Boot applications,
 * each with their own DemoApplication-style entry point, running on
 * different hosts/ports/containers, communicating purely over the network.
 *
 * ANNOTATIONS:
 *   @SpringBootApplication  = combines @Configuration + @EnableAutoConfiguration
 *                             + @ComponentScan. This is what bootstraps the
 *                             entire Spring context (beans, controllers,
 *                             embedded Tomcat server on the port configured
 *                             in application.properties, etc.).
 *   @EnableFeignClients     = activates Spring Cloud OpenFeign's component
 *                             scanning for @FeignClient interfaces (like
 *                             ProviderFeignClient) and auto-generates their
 *                             proxy implementations at startup. WITHOUT this
 *                             annotation, injecting ProviderFeignClient into
 *                             ConsumerController would fail with
 *                             "NoSuchBeanDefinitionException".
 *
 * main(String[] args):
 *   SpringApplication.run(...) does all the heavy lifting: creates the
 *   ApplicationContext, scans for components/configurations, starts the
 *   embedded web server (Tomcat, on port 8089 per application.properties),
 *   and keeps the JVM alive to serve HTTP requests until the process is
 *   stopped.
 */
@SpringBootApplication
@EnableFeignClients
public class DemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
