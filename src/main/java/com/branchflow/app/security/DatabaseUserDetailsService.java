package com.branchflow.app.security;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DatabaseUserDetailsService implements UserDetailsService {
    private final UserAccountRepository users;
    public DatabaseUserDetailsService(UserAccountRepository users) { this.users = users; }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserAccount account = users.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return User.withUsername(account.getUsername())
                .password(account.getPasswordHash())
                .roles(account.getRole().name())
                .disabled(!account.isEnabled())
                .build();
    }
}
