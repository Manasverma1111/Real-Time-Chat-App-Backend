package com.microservices.mediaservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

	// Configure application security rules
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

		http

				// Disable CSRF protection for REST APIs
				.csrf(csrf -> csrf.disable())

				// Configure endpoint access rules
				.authorizeHttpRequests(auth -> auth

						// Allow public access to media APIs and Swagger documentation
						.requestMatchers(
								"/media/**",
								"/swagger-ui/**",
								"/swagger-ui.html",
								"/v3/api-docs/**"
						).permitAll()

						// Allow access to all remaining requests
						.anyRequest().permitAll()
				)

				// Disable default login form
				.formLogin(form -> form.disable())

				// Disable HTTP Basic authentication
				.httpBasic(httpBasic -> httpBasic.disable());

		return http.build();
	}
}