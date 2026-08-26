package com.smarttrip.smarttrip.repository;

import com.smarttrip.smarttrip.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}