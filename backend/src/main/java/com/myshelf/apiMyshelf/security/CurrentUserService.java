package com.myshelf.apiMyshelf.security;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.myshelf.apiMyshelf.model.User;
import com.myshelf.apiMyshelf.repository.UserRepository;

@Service
public class CurrentUserService {

private final UserRepository userRepository;

  public CurrentUserService(UserRepository userRepository) { this.userRepository = userRepository; }

  public User getCurrentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }
}
