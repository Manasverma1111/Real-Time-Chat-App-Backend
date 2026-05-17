package com.microservices.notificationservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

//	securityFilterChain() method configures the security settings for the application using the HttpSecurity object.
//	It disables CSRF protection, allows all requests to the "/notifications/**" endpoint
//	without authentication, and disables form login and HTTP Basic authentication.
//	Finally, it builds and returns the configured SecurityFilterChain.
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.csrf(csrf -> csrf.disable())
				.authorizeHttpRequests(
						auth -> auth.requestMatchers("/notifications/**").permitAll().anyRequest().permitAll())
				.formLogin(form -> form.disable()).httpBasic(httpBasic -> httpBasic.disable());

		return http.build();
	}
}