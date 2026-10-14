
package com.smarttrip.smarttrip.service;

import com.smarttrip.smarttrip.entity.PasswordResetToken;
import com.smarttrip.smarttrip.entity.User;
import com.smarttrip.smarttrip.repository.PasswordResetTokenRepository;
import com.smarttrip.smarttrip.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final RestClient restClient;

    @Value("${RESEND_API_KEY}")
    private String resendApiKey;

    public UserService(
            UserRepository userRepository,
            PasswordResetTokenRepository passwordResetTokenRepository
    ) {
        this.userRepository = userRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.restClient = RestClient.create();
    }

    private void sendEmail(String to, String subject, String text) {

        Map<String, Object> body = Map.of(
                "from", "SmartTrip Planner <onboarding@resend.dev>",
                "to", List.of(to),
                "subject", subject,
                "text", text
        );

        restClient.post()
                .uri("https://api.resend.com/emails")
                .header("Authorization", "Bearer " + resendApiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .toBodilessEntity();
    }

    public User saveUser(User user) {

        if (user.getPassword() != null
                && !user.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        User savedUser = userRepository.save(user);

        try {
            sendEmail(
                    savedUser.getEmail(),
                    "Welcome to SmartTrip Planner!",
                    "Hello " + savedUser.getName()
                            + ",\n\nWelcome to SmartTrip Planner!\n\n"
                            + "Your account has been created.\n\n"
                            + "Regards,\nSmartTrip Planner Team"
            );
        } catch (Exception e) {
            System.err.println("Welcome email failed: " + e.getMessage());
        }

        return savedUser;
    }

    public boolean sendRegistrationOtp(User user) {

        if (user.getEmail() == null || user.getEmail().isBlank()) {
            return false;
        }

        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            System.err.println("Registration failed: email already exists.");
            return false;
        }

        String otp = String.valueOf(
                100000 + new Random().nextInt(900000)
        );

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setEmailVerified(false);
        user.setRegistrationOtp(otp);
        user.setRegistrationOtpExpiry(LocalDateTime.now().plusMinutes(5));

        User savedUser = userRepository.save(user);

        try {
            sendEmail(
                    savedUser.getEmail(),
                    "SmartTrip - Registration OTP",
                    "Hello " + savedUser.getName() + ",\n\n"
                            + "Welcome to SmartTrip Planner!\n\n"
                            + "Your registration OTP is: " + otp + "\n\n"
                            + "This OTP is valid for 5 minutes.\n\n"
                            + "Regards,\nSmartTrip Planner Team"
            );

            System.out.println(
                    "Registration OTP sent to " + savedUser.getEmail()
            );

            return true;

        } catch (Exception e) {
            System.err.println(
                    "Registration OTP email failed: " + e.getMessage()
            );

            userRepository.delete(savedUser);
            return false;
        }
    }

    public User verifyRegistrationOtp(String email, String otp) {

        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            return null;
        }

        if (user.isEmailVerified()) {
            return user;
        }

        if (user.getRegistrationOtp() == null
                || user.getRegistrationOtpExpiry() == null) {
            return null;
        }

        if (user.getRegistrationOtpExpiry().isBefore(LocalDateTime.now())) {
            return null;
        }

        if (!user.getRegistrationOtp().equals(otp)) {
            return null;
        }

        user.setEmailVerified(true);
        user.setRegistrationOtp(null);
        user.setRegistrationOtpExpiry(null);

        User verifiedUser = userRepository.save(user);

        try {
            sendEmail(
                    verifiedUser.getEmail(),
                    "Welcome to SmartTrip Planner!",
                    "Hello " + verifiedUser.getName() + ",\n\n"
                            + "Your account has been successfully created "
                            + "and your email has been verified.\n\n"
                            + "You can now log in to SmartTrip Planner.\n\n"
                            + "Happy Travelling!\n\n"
                            + "Regards,\nSmartTrip Planner Team"
            );
        } catch (Exception e) {
            System.err.println("Welcome email failed: " + e.getMessage());
        }

        return verifiedUser;
    }

    public User loginUser(String email, String password) {

        User user = userRepository.findByEmail(email).orElse(null);

        if (user != null
                && user.getPassword() != null
                && passwordEncoder.matches(password, user.getPassword())) {
            return user;
        }

        return null;
    }

    public boolean sendPasswordResetOtp(String email) {

        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            return false;
        }

        String otp = String.valueOf(
                100000 + new Random().nextInt(900000)
        );

        passwordResetTokenRepository.deleteByEmail(email);

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setEmail(email);
        resetToken.setOtp(otp);
        resetToken.setExpiryTime(LocalDateTime.now().plusMinutes(5));

        passwordResetTokenRepository.save(resetToken);

        try {
            sendEmail(
                    email,
                    "SmartTrip - Password Reset OTP",
                    "Hello " + user.getName() + ",\n\n"
                            + "Your SmartTrip password reset OTP is: " + otp
                            + "\n\nThis OTP is valid for 5 minutes.\n\n"
                            + "If you did not request a password reset, "
                            + "please ignore this email.\n\n"
                            + "Regards,\nSmartTrip Team"
            );

            return true;

        } catch (Exception e) {
            System.err.println(
                    "Password reset OTP email failed: " + e.getMessage()
            );

            passwordResetTokenRepository.deleteByEmail(email);
            return false;
        }
    }

    public boolean verifyPasswordResetOtp(String email, String otp) {

        PasswordResetToken resetToken =
                passwordResetTokenRepository.findByEmail(email).orElse(null);

        if (resetToken == null) {
            return false;
        }

        if (resetToken.getExpiryTime().isBefore(LocalDateTime.now())) {
            passwordResetTokenRepository.deleteByEmail(email);
            return false;
        }

        return resetToken.getOtp().equals(otp);
    }

    public boolean resetPassword(
            String email,
            String otp,
            String newPassword
    ) {

        if (!verifyPasswordResetOtp(email, otp)) {
            return false;
        }

        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            return false;
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        passwordResetTokenRepository.deleteByEmail(email);

        return true;
    }
}
