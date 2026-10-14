package com.smarttrip.smarttrip;

import com.smarttrip.smarttrip.entity.User;
import com.smarttrip.smarttrip.repository.PasswordResetTokenRepository;
import com.smarttrip.smarttrip.repository.UserRepository;
import com.smarttrip.smarttrip.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private UserService userService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Test
    void loginUser_WithCorrectPassword_ShouldReturnUser() {

        User user = new User();
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setPassword(passwordEncoder.encode("password"));

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        User result = userService.loginUser(
                "test@example.com",
                "password"
        );

        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
    }

    @Test
    void loginUser_WithWrongPassword_ShouldReturnNull() {

        User user = new User();
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setPassword(passwordEncoder.encode("password"));

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        User result = userService.loginUser(
                "test@example.com",
                "wrongPassword"
        );

        assertNull(result);
    }

    @Test
    void loginUser_WithUnknownEmail_ShouldReturnNull() {

        when(userRepository.findByEmail("unknown@example.com"))
                .thenReturn(Optional.empty());

        User result = userService.loginUser(
                "unknown@example.com",
                "password"
        );

        assertNull(result);
    }

    @Test
    void saveUser_ShouldHashPasswordBeforeSaving() {

        User user = new User();
        user.setName("Test User");
        user.setEmail("newuser@example.com");
        user.setPassword("password123");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.saveUser(user);

        assertNotNull(result);
        assertNotEquals("password123", result.getPassword());
        assertTrue(result.getPassword().startsWith("$2a$"));

        verify(userRepository, times(1)).save(user);
    }
}