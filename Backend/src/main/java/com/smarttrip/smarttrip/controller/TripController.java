package com.smarttrip.smarttrip.controller;

import com.smarttrip.smarttrip.entity.Trip;
import com.smarttrip.smarttrip.service.TripService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trips")
@CrossOrigin(origins = "http://localhost:5173")
public class TripController {

    private final TripService tripService;

    public TripController(TripService tripService) {
        this.tripService = tripService;
    }

    @PostMapping
    public ResponseEntity<Trip> createTrip(
            @RequestBody Trip trip,
            @RequestHeader("Authorization") String authorizationHeader) {

        String token = authorizationHeader.substring(7);

        Trip savedTrip =
                tripService.saveTrip(trip, token);

        return ResponseEntity.ok(savedTrip);
    }

    @GetMapping
    public ResponseEntity<List<Trip>> getAllTrips(
            @RequestHeader("Authorization") String authorizationHeader) {

        String token = authorizationHeader.substring(7);

        return ResponseEntity.ok(
                tripService.getAllTrips(token)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTrip(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authorizationHeader) {

        String token = authorizationHeader.substring(7);

        boolean deleted =
                tripService.deleteTrip(id, token);

        if (!deleted) {
            return ResponseEntity
                    .status(404)
                    .body("Trip not found");
        }

        return ResponseEntity.ok(
                "Trip deleted successfully"
        );
    }
}