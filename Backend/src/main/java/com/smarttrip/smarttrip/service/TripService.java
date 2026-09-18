package com.smarttrip.smarttrip.service;

import com.smarttrip.smarttrip.entity.Trip;
import com.smarttrip.smarttrip.repository.TripRepository;
import com.smarttrip.smarttrip.util.JwtUtil;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class TripService {

    private final TripRepository tripRepository;
    private final JwtUtil jwtUtil;
    private final DestinationService destinationService;

    public TripService(
            TripRepository tripRepository,
            JwtUtil jwtUtil,
            DestinationService destinationService) {

        this.tripRepository = tripRepository;
        this.jwtUtil = jwtUtil;
        this.destinationService = destinationService;
    }

    public Trip saveTrip(Trip trip, String token) {

        String email =
                jwtUtil.extractEmail(token);

        trip.setUserEmail(email);

        String tripPlan =
                generateTripPlan(trip);

        trip.setTripPlan(tripPlan);

        return tripRepository.save(trip);
    }

    public List<Trip> getAllTrips(String token) {

        String email =
                jwtUtil.extractEmail(token);

        return tripRepository.findByUserEmail(email);
    }

    public boolean deleteTrip(
            Long tripId,
            String token) {

        String email =
                jwtUtil.extractEmail(token);

        Trip trip =
                tripRepository
                        .findByIdAndUserEmail(
                                tripId,
                                email
                        )
                        .orElse(null);

        if (trip == null) {
            return false;
        }

        tripRepository.delete(trip);

        return true;
    }

    private String generateTripPlan(Trip trip) {

        LocalDate start =
                LocalDate.parse(
                        trip.getStartDate()
                );

        LocalDate end =
                LocalDate.parse(
                        trip.getEndDate()
                );

        long numberOfDays =
                ChronoUnit.DAYS.between(
                        start,
                        end
                ) + 1;

        String destination =
                trip.getDestination().trim();

        List<String> attractions =
                destinationService
                        .getAttractions(destination);

        StringBuilder plan =
                new StringBuilder();

        plan.append(
                "Trip Plan for "
        ).append(destination)
         .append("\n\n");

        if (attractions.isEmpty()) {

            plan.append(
                    "No specific attractions "
                    + "were found for this destination.\n"
            );

            plan.append(
                    "Explore popular local places "
                    + "and enjoy local food and activities.\n\n"
            );

        } else {

            plan.append(
                    "Recommended Attractions:\n"
            );

            for (String attraction : attractions) {

                plan.append("- ")
                    .append(attraction)
                    .append("\n");
            }

            plan.append("\n");
        }

        for (
                int day = 1;
                day <= numberOfDays;
                day++
        ) {

            plan.append("Day ")
                .append(day)
                .append("\n");

            if (!attractions.isEmpty()) {

                int firstIndex =
                        (day - 1)
                                % attractions.size();

                plan.append("- Visit ")
                    .append(
                            attractions.get(
                                    firstIndex
                            )
                    )
                    .append("\n");

                if (attractions.size() > 1) {

                    int secondIndex =
                            day
                                    % attractions.size();

                    plan.append("- Explore ")
                        .append(
                                attractions.get(
                                        secondIndex
                                )
                        )
                        .append("\n");
                }

            } else {

                plan.append(
                        "- Explore popular attractions in "
                ).append(destination)
                 .append("\n");

                plan.append(
                        "- Enjoy local food and nearby activities\n"
                );
            }

            plan.append(
                    "- Enjoy local food and activities\n"
            );

            plan.append(
                    "- Return to accommodation and relax\n\n"
            );
        }

        plan.append("Budget: ")
            .append(trip.getBudget())
            .append("\n");

        plan.append("Travelers: ")
            .append(trip.getTravelers());

        return plan.toString();
    }
}