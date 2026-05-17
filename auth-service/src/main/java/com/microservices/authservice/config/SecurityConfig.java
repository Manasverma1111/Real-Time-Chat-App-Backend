package com.microservices.authservice.config;

import com.microservices.authservice.security.JwtAuthenticationFilter;
import com.microservices.authservice.security.OAuth2LoginSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final JwtAuthenticationFilter jwtAuthenticationFilter;
	private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

//	Configures the security filter chain for the application,
//	defining which endpoints are publicly accessible and which require authentication,
//	as well as setting up session management and integrating JWT authentication and OAuth2 login handling.
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

		http.csrf(csrf -> csrf.disable())
				.authorizeHttpRequests(auth -> auth
						.requestMatchers(
								"/auth/**",
								"/oauth2/**",
								"/login/**",
								"/swagger-ui/**",
								"/swagger-ui.html",
								"/v3/api-docs/**"
						).permitAll()
						.anyRequest().authenticated()
				)
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.httpBasic(httpBasic -> httpBasic.disable())
				.formLogin(form -> form.disable())
				.oauth2Login(oauth -> oauth.successHandler(oAuth2LoginSuccessHandler))
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

//	Defines a PasswordEncoder bean using BCrypt hashing algorithm for securely encoding user passwords.
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

//	Defines an AuthenticationManager bean that retrieves the authentication manager
//	from the provided AuthenticationConfiguration, allowing it to be used for authentication processes
//	in the application.
	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}
}