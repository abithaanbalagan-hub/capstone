package com.smarttrip.smarttrip.service;

import com.smarttrip.smarttrip.entity.Trip;
import com.smarttrip.smarttrip.repository.TripRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class TripService {

    private final TripRepository tripRepository;

    public TripService(TripRepository tripRepository) {
        this.tripRepository = tripRepository;
    }

    public Trip saveTrip(Trip trip) {
        String tripPlan = generateTripPlan(trip);
        trip.setTripPlan(tripPlan);

        return tripRepository.save(trip);
    }

    public List<Trip> getAllTrips() {
        return tripRepository.findAll();
    }

    private String generateTripPlan(Trip trip) {
        LocalDate start = LocalDate.parse(trip.getStartDate());
        LocalDate end = LocalDate.parse(trip.getEndDate());

        long numberOfDays = ChronoUnit.DAYS.between(start, end) + 1;

        StringBuilder plan = new StringBuilder();

        plan.append("Trip Plan for ")
                .append(trip.getDestination())
                .append("\n\n");

        for (int day = 1; day <= numberOfDays; day++) {
            plan.append("Day ")
                    .append(day)
                    .append("\n");

            plan.append("- Explore popular attractions in ")
                    .append(trip.getDestination())
                    .append("\n");

            plan.append("- Enjoy local food and nearby activities\n");

            plan.append("- Return to accommodation and relax\n\n");
        }

        plan.append("Budget: ")
                .append(trip.getBudget())
                .append("\n");

        plan.append("Travelers: ")
                .append(trip.getTravelers());

        return plan.toString();
    }
}