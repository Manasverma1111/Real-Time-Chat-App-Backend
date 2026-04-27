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

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                // Disable CSRF for REST APIs
                .csrf(csrf -> csrf.disable())

                // Enable CORS
                .cors(Customizer.withDefaults())

                // Stateless session (JWT-style microservice)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Authorization rules
                .authorizeHttpRequests(auth -> auth
                        // allow browser preflight requests
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // allow room APIs (JWT manually handled inside controller)
                        .requestMatchers("/rooms", "/rooms/**").permitAll()

                        // Eureka / actuator safe access if needed
                        .requestMatchers("/actuator/**").permitAll()

                        // everything else secured
                        .anyRequest().authenticated()
                )

                // VERY IMPORTANT:
                // fully disable default Spring Security login screen
                .formLogin(form -> form.disable())

                // disable HTTP Basic generated password auth
                .httpBasic(basic -> basic.disable())

                // disable default logout endpoint
                .logout(logout -> logout.disable());

        return http.build();
    }
}

//package com.microservices.roomservice.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.http.HttpMethod;
//import org.springframework.security.config.Customizer;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.security.web.SecurityFilterChain;
//
//@Configuration
//public class SecurityConfig {
//
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//
//        http
//                // Disable CSRF for REST APIs
//                .csrf(csrf -> csrf.disable())
//
//                // Enable CORS
//                .cors(Customizer.withDefaults())
//
//                // Stateless session (JWT style)
//                .sessionManagement(session ->
//                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
//                )
//
//                // Authorization rules
//                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
//                        .requestMatchers("/rooms/**").permitAll()
//                        .anyRequest().authenticated()
//                )
//
//                // Disable form login completely
//                .formLogin(form -> form.disable())
//
//                // Disable HTTP basic auth
//                .httpBasic(basic -> basic.disable());
//
//        return http.build();
//    }
//}