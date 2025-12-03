package hexlet.code.config;

import hexlet.code.model.User;
import hexlet.code.repository.UserRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminInitializer {

    @Bean
    ApplicationRunner initAdmin(UserRepository userRepository,
                                PasswordEncoder passwordEncoder) {
        return args -> {
            String email = "admin123@local";
            String rawPassword = "admin123";

            User admin = userRepository.findByEmail(email).orElseGet(() -> {
                User u = new User();
                u.setEmail(email);
                u.setFirstName("Admin");
                u.setLastName("User");
                return u;
            });

            admin.setPasswordHash(passwordEncoder.encode(rawPassword));

            if (admin.getId() == null) {
                userRepository.save(admin);
            } else {
                userRepository.save(admin);
            }
        };
    }
}
