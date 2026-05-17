package com.microservices.authservice.security;

import com.microservices.authservice.entity.User;
import com.microservices.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

//    This class implements the UserDetailsService interface, which is a core interface in Spring Security.
//    It provides a method to load user-specific data during the authentication process.

    private final UserRepository userRepository;


//    The loadUserByUsername() method is overridden to fetch the user details
//    based on the email provided during authentication.
//    It uses the UserRepository to find the user by email and throws a UsernameNotFoundException
//    if the user is not found.
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        return new CustomUserDetails(user);
    }
}