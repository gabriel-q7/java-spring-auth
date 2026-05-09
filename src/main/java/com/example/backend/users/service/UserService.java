package com.example.backend.users.service;

import com.example.backend.shared.exception.ApiException;
import com.example.backend.shared.exception.ResourceNotFoundException;
import com.example.backend.shared.util.UserMapper;
import com.example.backend.users.dto.UpdateUserRequest;
import com.example.backend.users.dto.UserResponse;
import com.example.backend.users.entity.User;
import com.example.backend.users.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return UserMapper.toResponse(user);
    }

    @Transactional
    public UserResponse updateCurrentUser(String username, UpdateUserRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!user.getUsername().equals(request.getUsername()) && userRepository.existsByUsername(request.getUsername())) {
            throw new ApiException(HttpStatus.CONFLICT, "Username is already taken");
        }
        if (!user.getEmail().equals(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new ApiException(HttpStatus.CONFLICT, "Email is already registered");
        }

        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());

        return UserMapper.toResponse(userRepository.save(user));
    }
}
