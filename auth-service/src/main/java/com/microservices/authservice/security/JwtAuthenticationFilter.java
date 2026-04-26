// JwtAuthenticationFilter.java

package com.microservices.authservice.security;

import com.microservices.authservice.service.TokenBlacklistService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtService jwtService;
	private final CustomUserDetailsService userDetailsService;
	private final TokenBlacklistService tokenBlacklistService;

	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain
	) throws ServletException, IOException {

		String requestPath = request.getRequestURI();

		// Skip JWT validation for public endpoints
		if (
				requestPath.startsWith("/auth/login") ||
						requestPath.startsWith("/auth/register") ||
						requestPath.startsWith("/auth/logout") ||
						requestPath.startsWith("/oauth2") ||
						requestPath.startsWith("/login")
		) {
			filterChain.doFilter(request, response);
			return;
		}

		String authHeader = request.getHeader("Authorization");

		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			filterChain.doFilter(request, response);
			return;
		}

		String token = authHeader.substring(7);

		// If token is blacklisted, continue without authentication
		// instead of blocking login flow
		if (tokenBlacklistService.isBlacklisted(token)) {
			filterChain.doFilter(request, response);
			return;
		}

		String email;

		try {
			email = jwtService.extractEmail(token);
		} catch (Exception e) {
			filterChain.doFilter(request, response);
			return;
		}

		if (
				email != null &&
						SecurityContextHolder.getContext().getAuthentication() == null
		) {
			UserDetails userDetails =
					userDetailsService.loadUserByUsername(email);

			if (jwtService.isTokenValid(token)) {
				UsernamePasswordAuthenticationToken authenticationToken =
						new UsernamePasswordAuthenticationToken(
								userDetails,
								null,
								userDetails.getAuthorities()
						);

				authenticationToken.setDetails(
						new WebAuthenticationDetailsSource()
								.buildDetails(request)
				);

				SecurityContextHolder.getContext()
						.setAuthentication(authenticationToken);
			}
		}

		filterChain.doFilter(request, response);
	}
}


//package com.microservices.authservice.security;
//
//import com.microservices.authservice.service.TokenBlacklistService;
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
//import org.springframework.stereotype.Component;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import java.io.IOException;
//
//@Component
//@RequiredArgsConstructor
//public class JwtAuthenticationFilter extends OncePerRequestFilter {
//
//	private final JwtService jwtService;
//	private final CustomUserDetailsService userDetailsService;
//	private final TokenBlacklistService tokenBlacklistService;
//
//	@Override
//	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
//			throws ServletException, IOException {
//
//		String authHeader = request.getHeader("Authorization");
//
//		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
//			filterChain.doFilter(request, response);
//			return;
//		}
//
//		String token = authHeader.substring(7);
//		if (tokenBlacklistService.isBlacklisted(token)) {
//			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//			return;
//		}
//		String email;
//
//		try {
//			email = jwtService.extractEmail(token);
//		} catch (Exception e) {
//			filterChain.doFilter(request, response);
//			return;
//		}
//
//		if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
//			UserDetails userDetails = userDetailsService.loadUserByUsername(email);
//
//			if (jwtService.isTokenValid(token)) {
//				UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
//						userDetails, null, userDetails.getAuthorities());
//
//				authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
//
//				SecurityContextHolder.getContext().setAuthentication(authenticationToken);
//			}
//		}
//
//		filterChain.doFilter(request, response);
//	}
//}