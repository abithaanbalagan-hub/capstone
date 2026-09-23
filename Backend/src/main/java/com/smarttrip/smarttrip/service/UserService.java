package com.smarttrip.smarttrip.service;

import com.smarttrip.smarttrip.entity.PasswordResetToken;
import com.smarttrip.smarttrip.entity.User;
import com.smarttrip.smarttrip.repository.PasswordResetTokenRepository;
import com.smarttrip.smarttrip.repository.UserRepository;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final JavaMailSender mailSender;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            PasswordResetTokenRepository passwordResetTokenRepository,
            JavaMailSender mailSender
    ) {
        this.userRepository = userRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.mailSender = mailSender;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    /*
     * Existing user save method.
     *
     * Kept for existing UserService tests and backward compatibility.
     */
    public User saveUser(User user) {

        if (user.getPassword() != null
                && !user.getPassword().isBlank()) {

            user.setPassword(
                    passwordEncoder.encode(user.getPassword())
            );
        }

        User savedUser =
                userRepository.save(user);

        sendWelcomeEmail(savedUser);

        return savedUser;
    }

    // Registration - Send OTP
    public boolean sendRegistrationOtp(User user) {

        if (user.getEmail() == null
                || user.getEmail().isBlank()) {

            return false;
        }

        if (userRepository
                .findByEmail(user.getEmail())
                .isPresent()) {

            return false;
        }

        String otp =
                String.valueOf(
                        100000 + new Random().nextInt(900000)
                );

        user.setPassword(
                passwordEncoder.encode(
                        user.getPassword()
                )
        );

        user.setEmailVerified(false);

        user.setRegistrationOtp(otp);

        user.setRegistrationOtpExpiry(
                LocalDateTime.now().plusMinutes(5)
        );

        userRepository.save(user);

        try {

            SimpleMailMessage message =
                    new SimpleMailMessage();

            message.setTo(user.getEmail());

            message.setSubject(
                    "SmartTrip - Registration OTP"
            );

            message.setText(
                    "Hello " + user.getName() + ",\n\n"
                            + "Welcome to SmartTrip Planner! 🌍\n\n"
                            + "Your registration OTP is:\n\n"
                            + otp + "\n\n"
                            + "This OTP is valid for 5 minutes.\n\n"
                            + "Please enter this OTP in SmartTrip Planner "
                            + "to complete your registration.\n\n"
                            + "If you did not create this account, "
                            + "please ignore this email.\n\n"
                            + "Regards,\n"
                            + "SmartTrip Planner Team"
            );

            mailSender.send(message);

            System.out.println(
                    "Registration OTP sent successfully to "
                            + user.getEmail()
            );

            return true;

        } catch (Exception e) {

            System.out.println(
                    "Registration OTP could not be sent to "
                            + user.getEmail()
            );

            System.out.println(
                    "Email error: " + e.getMessage()
            );

            return false;
        }
    }

    // Registration - Verify OTP
    public User verifyRegistrationOtp(
            String email,
            String otp
    ) {

        User user =
                userRepository
                        .findByEmail(email)
                        .orElse(null);

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

        if (user.getRegistrationOtpExpiry()
                .isBefore(LocalDateTime.now())) {

            return null;
        }

        if (!user.getRegistrationOtp().equals(otp)) {
            return null;
        }

        user.setEmailVerified(true);

        user.setRegistrationOtp(null);

        user.setRegistrationOtpExpiry(null);

        User verifiedUser =
                userRepository.save(user);

        sendWelcomeEmail(verifiedUser);

        return verifiedUser;
    }

    // Welcome Email
    private void sendWelcomeEmail(User user) {

        try {

            SimpleMailMessage message =
                    new SimpleMailMessage();

            message.setTo(user.getEmail());

            message.setSubject(
                    "Welcome to SmartTrip Planner! ✈️"
            );

            message.setText(
                    "Hello " + user.getName() + ",\n\n"
                            + "Welcome to SmartTrip Planner! 🌍\n\n"
                            + "Your account has been successfully created "
                            + "and your email has been verified.\n\n"
                            + "Registered Email: " + user.getEmail() + "\n\n"
                            + "You can now login to SmartTrip Planner and "
                            + "start planning your trips, discovering "
                            + "destinations and creating personalized "
                            + "travel plans.\n\n"
                            + "Happy Travelling! ✈️\n\n"
                            + "Regards,\n"
                            + "SmartTrip Planner Team"
            );

            mailSender.send(message);

            System.out.println(
                    "Welcome email sent successfully to "
                            + user.getEmail()
            );

        } catch (Exception e) {

            System.out.println(
                    "Welcome email could not be sent to "
                            + user.getEmail()
            );

            System.out.println(
                    "Email error: " + e.getMessage()
            );
        }
    }

    // Login
    public User loginUser(
            String email,
            String password
    ) {

        User user =
                userRepository
                        .findByEmail(email)
                        .orElse(null);

        if (user != null
                && user.getPassword() != null
                && passwordEncoder.matches(
                        password,
                        user.getPassword()
                )) {

            return user;
        }

        return null;
    }

    // Forgot Password - Send OTP
    public boolean sendPasswordResetOtp(
            String email
    ) {

        User user =
                userRepository
                        .findByEmail(email)
                        .orElse(null);

        if (user == null) {
            return false;
        }

        String otp =
                String.valueOf(
                        100000 + new Random().nextInt(900000)
                );

        passwordResetTokenRepository
                .deleteByEmail(email);

        PasswordResetToken resetToken =
                new PasswordResetToken();

        resetToken.setEmail(email);

        resetToken.setOtp(otp);

        resetToken.setExpiryTime(
                LocalDateTime.now().plusMinutes(5)
        );

        passwordResetTokenRepository
                .save(resetToken);

        SimpleMailMessage message =
                new SimpleMailMessage();

        message.setTo(email);

        message.setSubject(
                "SmartTrip - Password Reset OTP"
        );

        message.setText(
                "Hello " + user.getName() + ",\n\n"
                        + "Your SmartTrip password reset OTP is:\n\n"
                        + otp + "\n\n"
                        + "This OTP is valid for 5 minutes.\n\n"
                        + "If you did not request a password reset, "
                        + "please ignore this email.\n\n"
                        + "Regards,\n"
                        + "SmartTrip Team"
        );

        mailSender.send(message);

        return true;
    }

    // Forgot Password - Verify OTP
    public boolean verifyPasswordResetOtp(
            String email,
            String otp
    ) {

        PasswordResetToken resetToken =
                passwordResetTokenRepository
                        .findByEmail(email)
                        .orElse(null);

        if (resetToken == null) {
            return false;
        }

        if (resetToken.getExpiryTime()
                .isBefore(LocalDateTime.now())) {

            passwordResetTokenRepository
                    .deleteByEmail(email);

            return false;
        }

        return resetToken.getOtp().equals(otp);
    }

    // Forgot Password - Reset Password
    public boolean resetPassword(
            String email,
            String otp,
            String newPassword
    ) {

        boolean otpValid =
                verifyPasswordResetOtp(
                        email,
                        otp
                );

        if (!otpValid) {
            return false;
        }

        User user =
                userRepository
                        .findByEmail(email)
                        .orElse(null);

        if (user == null) {
            return false;
        }

        user.setPassword(
                passwordEncoder.encode(newPassword)
        );

        userRepository.save(user);

        passwordResetTokenRepository
                .deleteByEmail(email);

        return true;
    }
}