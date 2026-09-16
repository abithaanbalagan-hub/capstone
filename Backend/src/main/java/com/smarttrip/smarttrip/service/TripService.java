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

        String destination = trip.getDestination().trim();

        String[] places = getPlacesForDestination(destination);

        StringBuilder plan = new StringBuilder();

        plan.append("Trip Plan for ")
                .append(destination)
                .append("\n\n");

        for (int day = 1; day <= numberOfDays; day++) {

            plan.append("Day ")
                    .append(day)
                    .append("\n");

            if (places.length > 0) {

                int placeIndex = (day - 1) % places.length;

                plan.append("- Visit ")
                        .append(places[placeIndex])
                        .append("\n");

                if (places.length > 1) {
                    int secondPlaceIndex =
                            day % places.length;

                    plan.append("- Explore ")
                            .append(places[secondPlaceIndex])
                            .append("\n");
                }

            } else {

                plan.append("- Explore popular attractions in ")
                        .append(destination)
                        .append("\n");

                plan.append("- Enjoy local food and nearby activities\n");
            }

            plan.append("- Enjoy local food and activities\n");
            plan.append("- Return to accommodation and relax\n\n");
        }

        plan.append("Budget: ")
                .append(trip.getBudget())
                .append("\n");

        plan.append("Travelers: ")
                .append(trip.getTravelers());

        return plan.toString();
    }

    private String[] getPlacesForDestination(String destination) {

        String place = destination.toLowerCase();

        if (place.contains("ooty")) {
            return new String[]{
                    "Ooty Lake",
                    "Government Botanical Garden",
                    "Doddabetta Peak",
                    "Tea Factory",
                    "Coonoor",
                    "Pykara Lake"
            };
        }

        if (place.contains("kodaikanal")) {
            return new String[]{
                    "Kodaikanal Lake",
                    "Coaker's Walk",
                    "Bryant Park",
                    "Pillar Rocks",
                    "Pine Forest",
                    "Moir Point"
            };
        }

        if (place.contains("munnar")) {
            return new String[]{
                    "Tea Gardens",
                    "Mattupetty Dam",
                    "Echo Point",
                    "Top Station",
                    "Eravikulam National Park",
                    "Tea Museum"
            };
        }

        if (place.contains("goa")) {
            return new String[]{
                    "Baga Beach",
                    "Calangute Beach",
                    "Fort Aguada",
                    "Anjuna Beach",
                    "Basilica of Bom Jesus",
                    "Dudhsagar Falls"
            };
        }

        if (place.contains("paris")) {
            return new String[]{
                    "Eiffel Tower",
                    "Louvre Museum",
                    "Arc de Triomphe",
                    "Notre-Dame Cathedral",
                    "Montmartre",
                    "Seine River"
            };
        }

        if (place.contains("tokyo")) {
            return new String[]{
                    "Tokyo Tower",
                    "Shibuya Crossing",
                    "Senso-ji Temple",
                    "Meiji Shrine",
                    "Tokyo Skytree",
                    "Ueno Park"
            };
        }

        if (place.contains("dubai")) {
            return new String[]{
                    "Burj Khalifa",
                    "Dubai Mall",
                    "Palm Jumeirah",
                    "Dubai Marina",
                    "Jumeirah Beach",
                    "Dubai Frame"
            };
        }

        if (place.contains("rome")) {
            return new String[]{
                    "Colosseum",
                    "Roman Forum",
                    "Trevi Fountain",
                    "Pantheon",
                    "Piazza Navona",
                    "Spanish Steps"
            };
        }

        return new String[0];
    }
}