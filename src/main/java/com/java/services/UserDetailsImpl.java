package com.java.services;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.java.Entity.User;

public class UserDetailsImpl implements UserDetails {

    private String username;
    private String password;

    // Constructor
    public UserDetailsImpl(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // Static method to build UserDetailsImpl from User entity
    public static UserDetailsImpl build(User user) {
        return new UserDetailsImpl(user.getUsername(), user.getPassword());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null; // No authorities
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // Account is non-expired
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Account is non-locked
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Credentials are non-expired
    }

    @Override
    public boolean isEnabled() {
        return true; // Account is enabled
    }
}
