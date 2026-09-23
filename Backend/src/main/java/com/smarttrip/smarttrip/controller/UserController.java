package com.smarttrip.smarttrip.controller;

import com.smarttrip.smarttrip.entity.User;
import com.smarttrip.smarttrip.service.UserService;
import com.smarttrip.smarttrip.util.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:5173")
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    public UserController(
            UserService userService,
            JwtUtil jwtUtil
    ) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    // Registration - Send OTP
    @PostMapping
    public ResponseEntity<?> createUser(
            @RequestBody User user
    ) {

        if (user.getName() == null
                || user.getName().trim().isEmpty()) {

            return ResponseEntity
                    .badRequest()
                    .body("Name is required");
        }

        if (user.getEmail() == null
                || user.getEmail().trim().isEmpty()) {

            return ResponseEntity
                    .badRequest()
                    .body("Email is required");
        }

        if (user.getPassword() == null
                || user.getPassword().length() < 6) {

            return ResponseEntity
                    .badRequest()
                    .body("Password must be at least 6 characters");
        }

        boolean sent =
                userService.sendRegistrationOtp(user);

        if (sent) {

            Map<String, String> response =
                    new HashMap<>();

            response.put(
                    "message",
                    "Registration OTP sent successfully"
            );

            response.put(
                    "email",
                    user.getEmail()
            );

            return ResponseEntity.ok(response);
        }

        return ResponseEntity
                .status(400)
                .body(
                        "Registration failed. " +
                        "Email may already be registered " +
                        "or OTP email could not be sent."
                );
    }

    // Verify Registration OTP
    @PostMapping("/verify-registration-otp")
    public ResponseEntity<?> verifyRegistrationOtp(
            @RequestBody Map<String, String> request
    ) {

        String email = request.get("email");
        String otp = request.get("otp");

        if (email == null
                || email.trim().isEmpty()
                || otp == null
                || otp.trim().isEmpty()) {

            return ResponseEntity
                    .badRequest()
                    .body("Email and OTP are required");
        }

        User verifiedUser =
                userService.verifyRegistrationOtp(
                        email,
                        otp
                );

        if (verifiedUser != null) {

            return ResponseEntity.ok(
                    "Registration successful"
            );
        }

        return ResponseEntity
                .status(400)
                .body("Invalid or expired registration OTP");
    }

    // Login
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(
            @RequestBody User user
    ) {

        User loggedInUser =
                userService.loginUser(
                        user.getEmail(),
                        user.getPassword()
                );

        if (loggedInUser != null) {

            if (!loggedInUser.isEmailVerified()) {

                return ResponseEntity
                        .status(403)
                        .body(
                                "Please verify your email before login"
                        );
            }

            String token =
                    jwtUtil.generateToken(
                            loggedInUser.getEmail()
                    );

            Map<String, Object> response =
                    new HashMap<>();

            response.put("token", token);
            response.put("user", loggedInUser);

            return ResponseEntity.ok(response);
        }

        return ResponseEntity
                .status(401)
                .body("Invalid email or password");
    }

    // Forgot Password - Send OTP
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(
            @RequestBody Map<String, String> request
    ) {

        String email = request.get("email");

        if (email == null
                || email.trim().isEmpty()) {

            return ResponseEntity
                    .badRequest()
                    .body("Email is required");
        }

        boolean sent =
                userService.sendPasswordResetOtp(email);

        if (sent) {

            return ResponseEntity.ok(
                    "OTP sent successfully to your email"
            );
        }

        return ResponseEntity
                .status(404)
                .body("Email not found");
    }

    // Forgot Password - Verify OTP
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(
            @RequestBody Map<String, String> request
    ) {

        String email = request.get("email");
        String otp = request.get("otp");

        if (email == null || otp == null) {

            return ResponseEntity
                    .badRequest()
                    .body("Email and OTP are required");
        }

        boolean valid =
                userService.verifyPasswordResetOtp(
                        email,
                        otp
                );

        if (valid) {

            return ResponseEntity.ok(
                    "OTP verified successfully"
            );
        }

        return ResponseEntity
                .status(400)
                .body("Invalid or expired OTP");
    }

    // Forgot Password - Reset Password
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(
            @RequestBody Map<String, String> request
    ) {

        String email = request.get("email");
        String otp = request.get("otp");
        String newPassword = request.get("newPassword");

        if (email == null
                || otp == null
                || newPassword == null) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            "Email, OTP and new password are required"
                    );
        }

        if (newPassword.length() < 6) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            "Password must be at least 6 characters"
                    );
        }

        boolean reset =
                userService.resetPassword(
                        email,
                        otp,
                        newPassword
                );

        if (reset) {

            return ResponseEntity.ok(
                    "Password reset successfully"
            );
        }

        return ResponseEntity
                .status(400)
                .body("Invalid or expired OTP");
    }
}