package com.opao.pp_api.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 1. DISABLE CSRF (Cross-Site Request Forgery) 
            // Crucial for testing REST APIs (POST/PUT/DELETE) without authentication tokens
            .csrf(AbstractHttpConfigurer::disable)
            
            // 2. DISABLE AUTHORIZATION STRATEGIES
            // Sets every path (including root, actuators, and swagger) to be completely open and public
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            )
            
            // 3. DISABLE FORM LOGIN & HTTP BASIC
            // Prevents Spring Security from showing the default login popup window or redirect page
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable);

        return http.build();
    }

    /**
     * 💡 HELPER FUNCTION: Defines a PasswordEncoder bean.
     * You can inject this right away into your UserService to safely run password hashing.
     */
    // @Bean
    // public PasswordEncoder passwordEncoder() {
    //     return new BCryptPasswordEncoder();
    // }
}
