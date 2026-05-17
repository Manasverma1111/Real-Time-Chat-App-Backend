package com.microservices.roomservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

//    SecurityConfig is a configuration class that defines the security settings for the Room Service API using Spring Security.
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

//       securityFilterChain() method configures the security settings for the application,
//       including disabling CSRF protection, enabling CORS, setting the session management policy to stateless,
//       and defining the authorization rules for different API endpoints.
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth

                        // Allow browser preflight requests
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // EXPLICIT: internal member lookup by message-service
                        .requestMatchers(HttpMethod.GET, "/rooms/*/members").permitAll()

                        // EXPLICIT: public groups listing
                        .requestMatchers(HttpMethod.GET, "/rooms/public").permitAll()

                        // Allow all room APIs (covers POST, DELETE, PUT, GET)
                        .requestMatchers("/rooms", "/rooms/**").permitAll()

                        // Actuator
                        .requestMatchers(
                                "/actuator/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**").permitAll()

                        // Everything else secured
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .logout(logout -> logout.disable());

        return http.build();
    }
}