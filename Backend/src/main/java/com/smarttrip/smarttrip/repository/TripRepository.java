package com.smarttrip.smarttrip.repository;

import com.smarttrip.smarttrip.entity.Trip;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TripRepository extends JpaRepository<Trip, Long> {
} 