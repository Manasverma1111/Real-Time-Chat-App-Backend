package com.microservices.presenceservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

//	securityFilterChain() method configures the security settings for the application using Spring Security.
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

//		The configuration disables Cross-Site Request Forgery (CSRF) protection,
//		allows all requests to the "/presence/**" endpoint without authentication,
//		and permits all other requests as well. Additionally,
//		it disables form-based login and HTTP Basic authentication.
		http.csrf(csrf -> csrf.disable())
				.authorizeHttpRequests(
						auth -> auth.requestMatchers("/presence/**").permitAll().anyRequest().permitAll())
				.formLogin(form -> form.disable()).httpBasic(httpBasic -> httpBasic.disable());

		return http.build();
	}
}