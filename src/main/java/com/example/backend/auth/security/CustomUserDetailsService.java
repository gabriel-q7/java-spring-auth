package com.example.backend.auth.security;

import com.example.backend.auth.repository.CredentialRepository;
import com.example.backend.users.entity.User;
import com.example.backend.users.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final CredentialRepository credentialRepository;

    public CustomUserDetailsService(UserRepository userRepository, CredentialRepository credentialRepository) {
        this.userRepository = userRepository;
        this.credentialRepository = credentialRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String passwordHash = credentialRepository.findByUserId(user.getId())
                .map(c -> c.getPasswordHash())
                .orElseThrow(() -> new UsernameNotFoundException("Credentials not found"));

        return new CustomUserDetails(user.getId(), user.getUsername(), passwordHash);
    }
}
