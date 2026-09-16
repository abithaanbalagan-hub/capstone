package com.smarttrip.smarttrip.service;

import com.smarttrip.smarttrip.entity.PasswordResetToken;
import com.smarttrip.smarttrip.entity.User;
import com.smarttrip.smarttrip.repository.PasswordResetTokenRepository;
import com.smarttrip.smarttrip.repository.UserRepository;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final JavaMailSender mailSender;

    public UserService(
            UserRepository userRepository,
            PasswordResetTokenRepository passwordResetTokenRepository,
            JavaMailSender mailSender
    ) {
        this.userRepository = userRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.mailSender = mailSender;
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public User loginUser(String email, String password) {
        User user = userRepository.findByEmail(email).orElse(null);

        if (user != null && user.getPassword().equals(password)) {
            return user;
        }

        return null;
    }

    public boolean sendPasswordResetOtp(String email) {

        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            return false;
        }

        // Generate 6-digit OTP
        String otp = String.valueOf(100000 + new Random().nextInt(900000));

        // Delete old OTP for this email
        passwordResetTokenRepository.deleteByEmail(email);

        // Create new reset token
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setEmail(email);
        resetToken.setOtp(otp);
        resetToken.setExpiryTime(LocalDateTime.now().plusMinutes(5));

        passwordResetTokenRepository.save(resetToken);

        // Send OTP through Gmail
        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(email);
        message.setSubject("SmartTrip - Password Reset OTP");
        message.setText(
                "Hello " + user.getName() + ",\n\n"
                        + "Your SmartTrip password reset OTP is:\n\n"
                        + otp + "\n\n"
                        + "This OTP is valid for 5 minutes.\n\n"
                        + "If you did not request a password reset, please ignore this email.\n\n"
                        + "Regards,\n"
                        + "SmartTrip Team"
        );

        mailSender.send(message);

        return true;
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

    public boolean resetPassword(String email, String otp, String newPassword) {

        boolean otpValid = verifyPasswordResetOtp(email, otp);

        if (!otpValid) {
            return false;
        }

        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            return false;
        }

        user.setPassword(newPassword);
        userRepository.save(user);

        // OTP can no longer be used after password reset
        passwordResetTokenRepository.deleteByEmail(email);

        return true;
    }
}