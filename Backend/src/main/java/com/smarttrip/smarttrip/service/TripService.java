package com.smarttrip.smarttrip.service;

import com.smarttrip.smarttrip.entity.Trip;
import com.smarttrip.smarttrip.repository.TripRepository;
import com.smarttrip.smarttrip.util.JwtUtil;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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

        String email = jwtUtil.extractEmail(token);

        trip.setUserEmail(email);

        String tripPlan = generateTripPlan(trip);

        trip.setTripPlan(tripPlan);

        return tripRepository.save(trip);
    }

    public List<Trip> getAllTrips(String token) {

        String email = jwtUtil.extractEmail(token);

        return tripRepository.findByUserEmail(email);
    }

    public boolean deleteTrip(
            Long tripId,
            String token) {

        String email = jwtUtil.extractEmail(token);

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

        if (numberOfDays <= 0) {
            numberOfDays = 1;
        }

        String destination =
                trip.getDestination() == null
                        ? "India"
                        : trip.getDestination().trim();

        double totalBudget =
                trip.getBudget() == null
                        ? 0
                        : trip.getBudget();

        int travelers =
                trip.getTravelers() == null
                        ? 1
                        : trip.getTravelers();

        if (travelers <= 0) {
            travelers = 1;
        }

        /*
         * Get destination attractions.
         */
        List<String> attractions =
                destinationService
                        .getAttractions(destination);

        attractions =
                removeDuplicates(attractions);

        /*
         * Select attractions based on trip duration.
         *
         * 3 attractions per day:
         * Morning
         * Afternoon
         * Evening
         */
        int requiredPlaces =
                (int) numberOfDays * 3;

        List<String> selectedAttractions =
                selectAttractions(
                        attractions,
                        requiredPlaces
                );

        /*
         * Budget calculations.
         */
        double hotelBudget =
                calculateHotelBudget(
                        totalBudget
                );

        double foodBudget =
                calculateFoodBudget(
                        totalBudget
                );

        double travelBudget =
                calculateTravelBudget(
                        totalBudget
                );

        double activityBudget =
                calculateActivityBudget(
                        totalBudget
                );

        String hotelType =
                getHotelType(
                        totalBudget,
                        travelers,
                        numberOfDays
                );

        HotelOption hotel =
                getHotelOption(
                        destination,
                        hotelType,
                        hotelBudget,
                        numberOfDays
                );

        StringBuilder plan =
                new StringBuilder();

        plan.append("Trip Plan for ")
                .append(destination)
                .append("\n\n");

        plan.append("Travel Style: ")
                .append(hotelType)
                .append("\n\n");

        // =================================================
        // HOTEL
        // =================================================

        plan.append("Recommended Stay:\n");

        plan.append("- ")
                .append(hotel.getName())
                .append("\n");

        plan.append("- Hotel Category: ")
                .append(hotel.getCategory())
                .append("\n");

        plan.append("- Estimated Price: ₹")
                .append(
                        formatAmount(
                                hotel.getPricePerNight()
                        )
                )
                .append(" per night\n");

        plan.append("- Estimated Stay Cost: ₹")
                .append(
                        formatAmount(
                                hotel.getPricePerNight()
                                        * Math.max(
                                                1,
                                                numberOfDays - 1
                                        )
                        )
                )
                .append("\n");

        plan.append(
                "- Hotel price is an estimate; "
                        + "actual price may vary.\n"
        );

        plan.append("\n");

        // =================================================
        // ATTRACTIONS
        // =================================================

        plan.append("Recommended Attractions:\n");

        if (selectedAttractions.isEmpty()) {

            plan.append(
                    "- No curated attractions found.\n"
            );

            plan.append(
                    "- Explore popular tourist attractions "
                            + "available at the destination.\n"
            );

        } else {

            for (String attraction :
                    selectedAttractions) {

                plan.append("- ")
                        .append(attraction)
                        .append("\n");
            }
        }

        plan.append("\n");

        // =================================================
        // DAY-WISE PLAN
        // =================================================

        int attractionIndex = 0;

        for (
                int day = 1;
                day <= numberOfDays;
                day++
        ) {

            plan.append("Day ")
                    .append(day)
                    .append("\n");

            /*
             * Morning
             */
            plan.append("Morning:\n");

            if (attractionIndex <
                    selectedAttractions.size()) {

                plan.append("- Visit ")
                        .append(
                                selectedAttractions.get(
                                        attractionIndex
                                )
                        )
                        .append("\n");

                attractionIndex++;

            } else {

                plan.append(
                        "- Relax at the hotel "
                                + "and enjoy a slow morning\n"
                );
            }

            /*
             * Afternoon
             */
            plan.append("Afternoon:\n");

            if (attractionIndex <
                    selectedAttractions.size()) {

                plan.append("- Explore ")
                        .append(
                                selectedAttractions.get(
                                        attractionIndex
                                )
                        )
                        .append("\n");

                attractionIndex++;

            } else {

                plan.append(
                        "- Enjoy local food "
                                + "and cultural experiences\n"
                );
            }

            /*
             * Evening
             */
            plan.append("Evening:\n");

            if (attractionIndex <
                    selectedAttractions.size()) {

                plan.append("- Experience ")
                        .append(
                                selectedAttractions.get(
                                        attractionIndex
                                )
                        )
                        .append("\n");

                attractionIndex++;

            } else {

                plan.append(
                        "- Explore local food streets, "
                                + "shopping areas or cultural activities\n"
                );
            }

            plan.append("\n");
        }

        // =================================================
        // BUDGET BREAKDOWN
        // =================================================

        plan.append("Estimated Budget Breakdown:\n");

        plan.append("Hotel Stay: ₹")
                .append(
                        formatAmount(
                                hotelBudget
                        )
                )
                .append("\n");

        plan.append("Food: ₹")
                .append(
                        formatAmount(
                                foodBudget
                        )
                )
                .append("\n");

        plan.append("Local Travel: ₹")
                .append(
                        formatAmount(
                                travelBudget
                        )
                )
                .append("\n");

        plan.append("Activities & Attractions: ₹")
                .append(
                        formatAmount(
                                activityBudget
                        )
                )
                .append("\n");

        plan.append("\n");

        plan.append("Total Budget: ₹")
                .append(
                        formatAmount(
                                totalBudget
                        )
                )
                .append("\n");

        plan.append("Travelers: ")
                .append(travelers)
                .append("\n");

        plan.append("Duration: ")
                .append(numberOfDays)
                .append(" days");

        return plan.toString();
    }

    // =====================================================
    // ATTRACTION SELECTION
    // =====================================================

    private List<String> selectAttractions(
            List<String> attractions,
            int requiredPlaces) {

        List<String> selected =
                new ArrayList<>();

        if (attractions == null
                || attractions.isEmpty()) {

            return selected;
        }

        for (String attraction : attractions) {

            if (attraction == null) {
                continue;
            }

            String clean =
                    attraction.trim();

            if (clean.isEmpty()) {
                continue;
            }

            boolean duplicate =
                    selected.stream()
                            .anyMatch(
                                    existing ->
                                            existing.equalsIgnoreCase(
                                                    clean
                                            )
                            );

            if (!duplicate) {
                selected.add(clean);
            }

            if (selected.size() >= requiredPlaces) {
                break;
            }
        }

        return selected;
    }

    // =====================================================
    // REMOVE DUPLICATES
    // =====================================================

    private List<String> removeDuplicates(
            List<String> attractions) {

        Map<String, String> unique =
                new LinkedHashMap<>();

        if (attractions == null) {
            return new ArrayList<>();
        }

        for (String attraction :
                attractions) {

            if (attraction == null) {
                continue;
            }

            String clean =
                    attraction.trim();

            if (clean.isEmpty()) {
                continue;
            }

            String key =
                    clean.toLowerCase(
                            Locale.ROOT
                    );

            if (!unique.containsKey(key)) {

                unique.put(
                        key,
                        clean
                );
            }
        }

        return new ArrayList<>(
                unique.values()
        );
    }

    // =====================================================
    // BUDGET
    // =====================================================

    private double calculateHotelBudget(
            double totalBudget) {

        /*
         * 30% of total budget.
         */
        return totalBudget * 0.30;
    }

    private double calculateFoodBudget(
            double totalBudget) {

        /*
         * 25% of total budget.
         */
        return totalBudget * 0.25;
    }

    private double calculateTravelBudget(
            double totalBudget) {

        /*
         * 25% of total budget.
         */
        return totalBudget * 0.25;
    }

    private double calculateActivityBudget(
            double totalBudget) {

        /*
         * 20% of total budget.
         */
        return totalBudget * 0.20;
    }

    // =====================================================
    // HOTEL TYPE
    // =====================================================

    private String getHotelType(
            double totalBudget,
            int travelers,
            long days) {

        if (travelers <= 0) {
            travelers = 1;
        }

        if (days <= 0) {
            days = 1;
        }

        /*
         * Approximate per-person-per-day budget.
         */
        double perPersonPerDay =
                totalBudget
                        / travelers
                        / days;

        if (perPersonPerDay < 1800) {

            return "Budget Friendly";

        } else if (perPersonPerDay < 4000) {

            return "Comfort";

        } else {

            return "Premium";
        }
    }

    // =====================================================
    // HOTEL OPTIONS
    // =====================================================

    private HotelOption getHotelOption(
            String destination,
            String hotelType,
            double hotelBudget,
            long numberOfDays) {

        String key =
                destination
                        .toLowerCase(
                                Locale.ROOT
                        );

        /*
         * Number of nights.
         */
        long nights =
                Math.max(
                        1,
                        numberOfDays - 1
                );

        /*
         * Maximum allowed price per night.
         */
        double maximumPerNight =
                hotelBudget / nights;

        // =================================================
        // MUNNAR
        // =================================================

        if (key.contains("munnar")) {

            if (hotelType.equals(
                    "Budget Friendly")) {

                return chooseHotel(
                        "Munnar Inn",
                        "Budget Hotel",
                        1800,
                        maximumPerNight
                );
            }

            if (hotelType.equals(
                    "Comfort")) {

                return chooseHotel(
                        "Munnar Tea Hills Resort",
                        "Comfort Hotel",
                        2800,
                        maximumPerNight
                );
            }

            return chooseHotel(
                    "Blanket Hotel & Spa",
                    "Premium Hotel",
                    5000,
                    maximumPerNight
            );
        }

        // =================================================
        // KOCHI
        // =================================================

        if (key.contains("kochi")
                || key.contains("cochin")) {

            if (hotelType.equals(
                    "Budget Friendly")) {

                return chooseHotel(
                        "Abad Metro",
                        "Budget Hotel",
                        1600,
                        maximumPerNight
                );
            }

            if (hotelType.equals(
                    "Comfort")) {

                return chooseHotel(
                        "Holiday Inn Cochin",
                        "Comfort Hotel",
                        3200,
                        maximumPerNight
                );
            }

            return chooseHotel(
                    "Taj Malabar Resort & Spa",
                    "Premium Hotel",
                    6500,
                    maximumPerNight
            );
        }

        // =================================================
        // ALLEPPEY
        // =================================================

        if (key.contains("alleppey")
                || key.contains("alappuzha")) {

            if (hotelType.equals(
                    "Budget Friendly")) {

                return chooseHotel(
                        "Alleppey Beach Resort",
                        "Budget Hotel",
                        1700,
                        maximumPerNight
                );
            }

            if (hotelType.equals(
                    "Comfort")) {

                return chooseHotel(
                        "Punnamada Resort",
                        "Comfort Resort",
                        3000,
                        maximumPerNight
                );
            }

            return chooseHotel(
                    "Kumarakom Lake Resort",
                    "Premium Resort",
                    7000,
                    maximumPerNight
            );
        }

        // =================================================
        // WAYANAD
        // =================================================

        if (key.contains("wayanad")) {

            if (hotelType.equals(
                    "Budget Friendly")) {

                return chooseHotel(
                        "Wayanad Gate Hotel",
                        "Budget Hotel",
                        1800,
                        maximumPerNight
                );
            }

            if (hotelType.equals(
                    "Comfort")) {

                return chooseHotel(
                        "Vythiri Village Resort",
                        "Comfort Resort",
                        3500,
                        maximumPerNight
                );
            }

            return chooseHotel(
                    "Vythiri Resort",
                    "Premium Resort",
                    6500,
                    maximumPerNight
            );
        }

        // =================================================
        // MADURAI
        // =================================================

        if (key.contains("madurai")) {

            if (hotelType.equals(
                    "Budget Friendly")) {

                return chooseHotel(
                        "Hotel Supreme",
                        "Budget Hotel",
                        1500,
                        maximumPerNight
                );
            }

            if (hotelType.equals(
                    "Comfort")) {

                return chooseHotel(
                        "JC Residency Madurai",
                        "Comfort Hotel",
                        3000,
                        maximumPerNight
                );
            }

            return chooseHotel(
                    "Courtyard by Marriott Madurai",
                    "Premium Hotel",
                    6000,
                    maximumPerNight
            );
        }

        // =================================================
        // CHENNAI
        // =================================================

        if (key.contains("chennai")) {

            if (hotelType.equals(
                    "Budget Friendly")) {

                return chooseHotel(
                        "Hotel Pandian",
                        "Budget Hotel",
                        1600,
                        maximumPerNight
                );
            }

            if (hotelType.equals(
                    "Comfort")) {

                return chooseHotel(
                        "The Residency Towers",
                        "Comfort Hotel",
                        3500,
                        maximumPerNight
                );
            }

            return chooseHotel(
                    "ITC Grand Chola",
                    "Premium Hotel",
                    7000,
                    maximumPerNight
            );
        }

        // =================================================
        // OOTY
        // =================================================

        if (key.contains("ooty")
                || key.contains("udhagamandalam")) {

            if (hotelType.equals(
                    "Budget Friendly")) {

                return chooseHotel(
                        "Hotel Darshan",
                        "Budget Hotel",
                        1800,
                        maximumPerNight
                );
            }

            if (hotelType.equals(
                    "Comfort")) {

                return chooseHotel(
                        "Gem Park Ooty",
                        "Comfort Hotel",
                        3200,
                        maximumPerNight
                );
            }

            return chooseHotel(
                    "Savoy Ooty",
                    "Premium Hotel",
                    6500,
                    maximumPerNight
            );
        }

        // =================================================
        // BENGALURU
        // =================================================

        if (key.contains("bengaluru")
                || key.contains("bangalore")) {

            if (hotelType.equals(
                    "Budget Friendly")) {

                return chooseHotel(
                        "Hotel Bangalore Gate",
                        "Budget Hotel",
                        1800,
                        maximumPerNight
                );
            }

            if (hotelType.equals(
                    "Comfort")) {

                return chooseHotel(
                        "The Chancery Pavilion",
                        "Comfort Hotel",
                        3500,
                        maximumPerNight
                );
            }

            return chooseHotel(
                    "The Leela Palace Bengaluru",
                    "Premium Hotel",
                    8000,
                    maximumPerNight
            );
        }

        // =================================================
        // JAIPUR
        // =================================================

        if (key.contains("jaipur")) {

            if (hotelType.equals(
                    "Budget Friendly")) {

                return chooseHotel(
                        "Hotel Kalyan",
                        "Budget Hotel",
                        1700,
                        maximumPerNight
                );
            }

            if (hotelType.equals(
                    "Comfort")) {

                return chooseHotel(
                        "Shahpura House",
                        "Comfort Hotel",
                        3200,
                        maximumPerNight
                );
            }

            return chooseHotel(
                    "ITC Rajputana",
                    "Premium Hotel",
                    7000,
                    maximumPerNight
            );
        }

        // =================================================
        // HYDERABAD
        // =================================================

        if (key.contains("hyderabad")) {

            if (hotelType.equals(
                    "Budget Friendly")) {

                return chooseHotel(
                        "Hotel Haridwar",
                        "Budget Hotel",
                        1600,
                        maximumPerNight
                );
            }

            if (hotelType.equals(
                    "Comfort")) {

                return chooseHotel(
                        "Avasa Hotel",
                        "Comfort Hotel",
                        3200,
                        maximumPerNight
                );
            }

            return chooseHotel(
                    "ITC Kohenur",
                    "Premium Hotel",
                    7000,
                    maximumPerNight
            );
        }

        // =================================================
        // MUMBAI
        // =================================================

        if (key.contains("mumbai")
                || key.contains("bombay")) {

            if (hotelType.equals(
                    "Budget Friendly")) {

                return chooseHotel(
                        "Hotel Supreme",
                        "Budget Hotel",
                        1800,
                        maximumPerNight
                );
            }

            if (hotelType.equals(
                    "Comfort")) {

                return chooseHotel(
                        "Holiday Inn Mumbai",
                        "Comfort Hotel",
                        3500,
                        maximumPerNight
                );
            }

            return chooseHotel(
                    "Taj Mahal Palace",
                    "Premium Hotel",
                    9000,
                    maximumPerNight
            );
        }

        // =================================================
        // GOA
        // =================================================

        if (key.contains("goa")) {

            if (hotelType.equals(
                    "Budget Friendly")) {

                return chooseHotel(
                        "Hotel Calangute",
                        "Budget Hotel",
                        1800,
                        maximumPerNight
                );
            }

            if (hotelType.equals(
                    "Comfort")) {

                return chooseHotel(
                        "Radisson Goa",
                        "Comfort Hotel",
                        3500,
                        maximumPerNight
                );
            }

            return chooseHotel(
                    "Taj Exotica Resort & Spa Goa",
                    "Premium Resort",
                    8000,
                    maximumPerNight
            );
        }

        // =================================================
        // DEFAULT INDIA HOTEL
        // =================================================

        if (hotelType.equals(
                "Budget Friendly")) {

            return new HotelOption(
                    "Local Budget Hotel / Homestay",
                    "Budget Stay",
                    Math.min(
                            1500,
                            Math.max(
                                    800,
                                    maximumPerNight
                            )
                    )
            );
        }

        if (hotelType.equals(
                "Comfort")) {

            return new HotelOption(
                    "Comfort 3-Star Hotel",
                    "Comfort Hotel",
                    Math.min(
                            3000,
                            Math.max(
                                    1800,
                                    maximumPerNight
                            )
                    )
            );
        }

        return new HotelOption(
                "Premium 4-Star / 5-Star Hotel",
                "Premium Hotel",
                Math.min(
                        6000,
                        Math.max(
                                3500,
                                maximumPerNight
                        )
                )
        );
    }

    // =====================================================
    // CHOOSE HOTEL
    // =====================================================

    private HotelOption chooseHotel(
            String name,
            String category,
            double normalPrice,
            double maximumPerNight) {

        /*
         * If the normal hotel price fits the allocated
         * hotel budget, use it.
         */
        if (normalPrice <= maximumPerNight) {

            return new HotelOption(
                    name,
                    category,
                    normalPrice
            );
        }

        /*
         * If normal price does not fit, show the same
         * hotel as an estimated option capped to the
         * allocated budget.
         *
         * This prevents the displayed hotel allocation
         * from exceeding the trip hotel budget.
         */
        return new HotelOption(
                name,
                category,
                Math.max(
                        800,
                        maximumPerNight
                )
        );
    }

    // =====================================================
    // FORMAT
    // =====================================================

    private String formatAmount(
            double amount) {

        return String.format(
                Locale.US,
                "%.0f",
                amount
        );
    }

    // =====================================================
    // HOTEL OPTION CLASS
    // =====================================================

    private static class HotelOption {

        private final String name;
        private final String category;
        private final double pricePerNight;

        public HotelOption(
                String name,
                String category,
                double pricePerNight) {

            this.name = name;
            this.category = category;
            this.pricePerNight =
                    pricePerNight;
        }

        public String getName() {
            return name;
        }

        public String getCategory() {
            return category;
        }

        public double getPricePerNight() {
            return pricePerNight;
        }
    }
}