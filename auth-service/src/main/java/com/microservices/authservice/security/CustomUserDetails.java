package com.microservices.authservice.security;

import com.microservices.authservice.entity.User;
import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
public class CustomUserDetails implements UserDetails {

//	This class implements the UserDetails interface from Spring Security,
//	providing a custom implementation that wraps around the User entity.
//	It includes methods to retrieve user information and authorities for authentication and authorization purposes.

	private final User user;

//	The getUserId() method returns the unique identifier of the user,
//	which can be used for various purposes such as fetching user details or performing operations related to the user.
	public UUID getUserId() {
		return user.getUserId();
	}

//	The getFullName() method returns the full name of the user,
//	which can be used for display purposes in the application,
	public String getFullName() {
		return user.getFullName();
	}

//	The getUser() method returns the entire User entity,
	public User getUser() {
		return user;
	}

//	The getAuthorities() method returns a collection of GrantedAuthority objects,
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of(
				new SimpleGrantedAuthority("ROLE_" + user.getRole().name()),
				new SimpleGrantedAuthority("ROLE_USER"));
	}

//	The getPassword() method returns the password hash of the user,
	@Override
	public String getPassword() {
		return user.getPasswordHash();
	}

//	The getUsername() method returns the email of the user,
//	which is used as the username for authentication purposes.
	@Override
	public String getUsername() {
		return user.getEmail();
	}

//	The isAccountNonExpired(), isAccountNonLocked(), and isCredentialsNonExpired() methods all return true,
	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

//	The isEnabled() method checks if the user's account is active
//	by returning the value of the isActive field from the User entity.
	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

//	The isCredentialsNonExpired() method returns true,
//	indicating that the user's credentials are not expired and are valid for authentication.
	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

//	The isEnabled() method checks if the user's account is active
//	by returning the value of the isActive field from the User entity.
	@Override
	public boolean isEnabled() {
		return Boolean.TRUE.equals(user.getIsActive());
	}
}