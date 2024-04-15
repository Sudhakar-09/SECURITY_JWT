package com.thbs.security;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import com.thbs.security.auth.AuthenticationService;
import com.thbs.security.auth.RegisterRequest;
import com.thbs.security.user.Role;
import com.thbs.security.user.UserRepository;

@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class SecurityApplication {

    public static void main(String[] args) {
        SpringApplication.run(SecurityApplication.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(
            AuthenticationService service,
            UserRepository userRepository // Inject UserRepository
    ) {
        return args -> {
            // Register an admin user
            var admin = RegisterRequest.builder()
                    .firstname("Admin")
                    .lastname("Admin")
                    .email("admin@mail.com")
                    .password("password") // Note: Password will be hashed to bcrypt format by the service
                    .role(Role.ADMIN)
                    .businessUnit("NBU-TRAINING")
                    .employeeId(7000)
                    .build();
    
            // Check if admin user already exists
            if (userRepository.findByEmail(admin.getEmail()).isEmpty()) {
                // Save and register admin user
                System.out.println("Admin token: " + service.register(admin).getAccessToken());
            } else {
                System.out.println("Admin user already exists.");
            }
    
            // Register a trainer user
            var trainer = RegisterRequest.builder()
                    .firstname("Trainer")
                    .lastname("Trainer")
                    .email("trainer@mail.com")
                    .password("password") // Note: Password will be hashed to bcrypt format by the service
                    .role(Role.TRAINER)
                    .businessUnit("NBU-TRAINING")
                    .employeeId(7001)
                    .build();
    
            // Check if trainer user already exists
            if (userRepository.findByEmail(trainer.getEmail()).isEmpty()) {
                // Save and register trainer user
                System.out.println("Trainer token: " + service.register(trainer).getAccessToken());
            } else {
                System.out.println("Trainer user already exists.");
            }
        };
    }
    
    }
    

