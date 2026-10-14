package com.mansi.wallet_system.controller;

import com.mansi.wallet_system.dto.JwtResponse;
import com.mansi.wallet_system.entity.User;
import com.mansi.wallet_system.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final com.mansi.wallet_system.security.JwtUtil jwtUtil;

    public UserController(UserService userService, com.mansi.wallet_system.security.JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping
    public org.springframework.http.ResponseEntity<?> createUser(@jakarta.validation.Valid @RequestBody User user) {
        try {
            User savedUser = userService.saveUser(user);
            String token = jwtUtil.generateToken(savedUser.getId(), savedUser.getEmail(), savedUser.getRole());
            
            // Return JwtResponse so the frontend can log in immediately upon registration
            return org.springframework.http.ResponseEntity.ok(new JwtResponse(
                    token,
                    savedUser.getId(),
                    savedUser.getName(),
                    savedUser.getEmail(),
                    savedUser.getRole()
            ));
        } catch (IllegalArgumentException e) {
            java.util.Map<String, String> errorResponse = new java.util.HashMap<>();
            errorResponse.put("message", e.getMessage());
            return org.springframework.http.ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }
}
