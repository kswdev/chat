package net.study.messageuser.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class SecurityConfig {

    @Bean
    @Profile("!load-test")
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Profile("load-test")
    @SuppressWarnings("deprecation")
    public PasswordEncoder loadTestPasswordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }
}
