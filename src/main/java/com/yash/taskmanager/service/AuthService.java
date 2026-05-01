package com.yash.taskmanager.service;

import com.yash.taskmanager.dto.AuthResponse;
import com.yash.taskmanager.dto.LoginRequest;
import com.yash.taskmanager.dto.RegisterRequest;
import com.yash.taskmanager.entity.Role;
import com.yash.taskmanager.entity.User;
import com.yash.taskmanager.exception.NotFoundException;
import com.yash.taskmanager.repository.UserRepository;
import com.yash.taskmanager.security.JwtTokenProvider;
import com.yash.taskmanager.security.UserDetailsImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider tokenProvider,
                       AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.authenticationManager = authenticationManager;
    }

    public void register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already in use");
        }

        Role role = userRepository.count() == 0 ? Role.ADMIN : Role.MEMBER;

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .build();

        userRepository.save(user);
    }

    public AuthResponse login(LoginRequest request) {
        userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new NotFoundException("User not found"));

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail()).get();
        String token = tokenProvider.generateToken(new UserDetailsImpl(user));
        return new AuthResponse(token, user.getRole().name());
    }
}
