package com.smarttrip.smarttrip.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.LocalDateTime;

@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;
    private String password;

    private boolean emailVerified;

    private String registrationOtp;

    private LocalDateTime registrationOtpExpiry;

    public User() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public String getRegistrationOtp() {
        return registrationOtp;
    }

    public void setRegistrationOtp(String registrationOtp) {
        this.registrationOtp = registrationOtp;
    }

    public LocalDateTime getRegistrationOtpExpiry() {
        return registrationOtpExpiry;
    }

    public void setRegistrationOtpExpiry(LocalDateTime registrationOtpExpiry) {
        this.registrationOtpExpiry = registrationOtpExpiry;
    }
}