package com.smarttrip.smarttrip.controller;

import com.smarttrip.smarttrip.entity.User;
import com.smarttrip.smarttrip.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:5173")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User savedUser = userService.saveUser(user);
        return ResponseEntity.ok(savedUser);
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody User user) {
        User loggedInUser =
                userService.loginUser(user.getEmail(), user.getPassword());

        if (loggedInUser != null) {
            return ResponseEntity.ok(loggedInUser);
        }

        return ResponseEntity
                .status(401)
                .body("Invalid email or password");
    }
}