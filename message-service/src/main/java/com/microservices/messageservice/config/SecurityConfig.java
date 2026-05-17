package com.microservices.messageservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

//	securityFilterChain() method defines the security configuration for the application,
//	specifying which endpoints are publicly accessible and which require authentication.
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.csrf(csrf -> csrf.disable())

//				authorizeHttpRequests() method configures the authorization rules for incoming HTTP requests.
//				requestMatchers() method specifies the URL patterns that should be publicly accessible without authentication,
				.authorizeHttpRequests(auth -> auth.requestMatchers(
								"/ws/**",
								"/swagger-ui/**",
								"/swagger-ui.html",
								"/v3/api-docs/**").permitAll()
						.requestMatchers("/messages/**").permitAll()
						.requestMatchers("/internal/**").permitAll()
						.anyRequest().authenticated())
				.formLogin(form -> form.disable()).httpBasic(httpBasic -> httpBasic.disable());

		return http.build();
	}
}