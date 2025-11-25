package com.myshelf.apiMyshelf.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.myshelf.apiMyshelf.dto.auth.AuthResponse;
import com.myshelf.apiMyshelf.dto.auth.DisplayNameResponse;
import com.myshelf.apiMyshelf.dto.auth.LoginRequest;
import com.myshelf.apiMyshelf.dto.auth.RegisterRequest;
import com.myshelf.apiMyshelf.dto.common.MessageResponse;
import com.myshelf.apiMyshelf.model.User;
import com.myshelf.apiMyshelf.repository.UserRepository;
import com.myshelf.apiMyshelf.service.AuthService;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserRepository userRepository;

    private final AuthService authService;

    public AuthController(AuthService authService, UserRepository userRepository) {
        this.authService = authService;
        this.userRepository = userRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<MessageResponse> register(@RequestBody RegisterRequest request) {
        MessageResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
    
    @GetMapping("/me")
public ResponseEntity<DisplayNameResponse> me() {
    String email = SecurityContextHolder.getContext().getAuthentication().getName();

    User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));

    return ResponseEntity.ok(new DisplayNameResponse(user.getDisplayName()));
}
}
