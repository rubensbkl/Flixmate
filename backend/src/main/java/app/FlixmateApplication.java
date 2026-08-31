package app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FlixmateApplication {
    public static void main(String[] args) {
        SpringApplication.run(FlixmateApplication.class, args);
        System.out.println("✅ Flixmate Spring Boot Application Started!");
    }
}
