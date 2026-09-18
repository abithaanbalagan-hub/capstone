package com.smarttrip.smarttrip.service;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
public class DestinationService {

    private final RestTemplate restTemplate;

    public DestinationService() {
        this.restTemplate = new RestTemplate();
    }

    public List<String> getAttractions(String destination) {

        if (destination == null || destination.trim().isEmpty()) {
            return new ArrayList<>();
        }

        String cleanDestination = destination.trim();

        // -------------------------------------------------
        // STEP 1: Try dynamic OpenStreetMap data
        // -------------------------------------------------

        List<String> dynamicAttractions =
                getDynamicAttractions(cleanDestination);

        if (dynamicAttractions.size() >= 3) {

            System.out.println(
                    "Using dynamic attractions for "
                            + cleanDestination
            );

            return dynamicAttractions;
        }

        // -------------------------------------------------
        // STEP 2: If API fails, use curated popular places
        // -------------------------------------------------

        List<String> fallbackAttractions =
                getFallbackAttractions(cleanDestination);

        if (!fallbackAttractions.isEmpty()) {

            System.out.println(
                    "Using fallback attractions for "
                            + cleanDestination
            );

            return fallbackAttractions;
        }

        // -------------------------------------------------
        // STEP 3: Unknown destination
        // TripService will create generic itinerary
        // -------------------------------------------------

        System.out.println(
                "No curated attractions for "
                        + cleanDestination
        );

        return dynamicAttractions;
    }

    // =====================================================
    // DYNAMIC OPENSTREETMAP DATA
    // =====================================================

    private List<String> getDynamicAttractions(
            String cleanDestination) {

        List<String> attractions = new ArrayList<>();

        try {

            // -------------------------------------------------
            // STEP 1: Find destination coordinates
            // -------------------------------------------------

            String encodedDestination =
                    URLEncoder.encode(
                            cleanDestination,
                            StandardCharsets.UTF_8
                    );

            String nominatimUrl =
                    "https://nominatim.openstreetmap.org/search"
                            + "?q="
                            + encodedDestination
                            + "&format=json"
                            + "&limit=1";

            HttpHeaders headers = new HttpHeaders();

            headers.set(
                    "User-Agent",
                    "SmartTripPlanner/1.0"
            );

            headers.set(
                    "Accept",
                    "application/json"
            );

            HttpEntity<String> requestEntity =
                    new HttpEntity<>(headers);

            ResponseEntity<List> locationResponse =
                    restTemplate.exchange(
                            nominatimUrl,
                            HttpMethod.GET,
                            requestEntity,
                            List.class
                    );

            List<?> locations =
                    locationResponse.getBody();

            if (locations == null
                    || locations.isEmpty()) {

                System.out.println(
                        "Destination not found: "
                                + cleanDestination
                );

                return attractions;
            }

            Object firstLocation =
                    locations.get(0);

            if (!(firstLocation instanceof Map<?, ?>)) {
                return attractions;
            }

            Map<?, ?> location =
                    (Map<?, ?>) firstLocation;

            Object latitudeObject =
                    location.get("lat");

            Object longitudeObject =
                    location.get("lon");

            if (latitudeObject == null
                    || longitudeObject == null) {

                return attractions;
            }

            String latitude =
                    latitudeObject.toString();

            String longitude =
                    longitudeObject.toString();

            System.out.println(
                    "Destination coordinates: "
                            + cleanDestination
                            + " -> "
                            + latitude
                            + ", "
                            + longitude
            );

            // -------------------------------------------------
            // STEP 2: Smaller Overpass query
            // -------------------------------------------------

            String overpassQuery =
                    "[out:json][timeout:15];"
                            + "("

                            + "nwr[\"tourism\"=\"attraction\"]"
                            + "(around:10000,"
                            + latitude + ","
                            + longitude + ");"

                            + "nwr[\"tourism\"=\"museum\"]"
                            + "(around:10000,"
                            + latitude + ","
                            + longitude + ");"

                            + "nwr[\"tourism\"=\"viewpoint\"]"
                            + "(around:10000,"
                            + latitude + ","
                            + longitude + ");"

                            + "nwr[\"tourism\"=\"gallery\"]"
                            + "(around:10000,"
                            + latitude + ","
                            + longitude + ");"

                            + "nwr[\"tourism\"=\"zoo\"]"
                            + "(around:10000,"
                            + latitude + ","
                            + longitude + ");"

                            + "nwr[\"historic\"]"
                            + "(around:10000,"
                            + latitude + ","
                            + longitude + ");"

                            + "nwr[\"leisure\"=\"park\"]"
                            + "(around:10000,"
                            + latitude + ","
                            + longitude + ");"

                            + "nwr[\"amenity\"=\"place_of_worship\"]"
                            + "(around:10000,"
                            + latitude + ","
                            + longitude + ");"

                            + ");"
                            + "out center tags;";

            // -------------------------------------------------
            // STEP 3: Send request
            // -------------------------------------------------

            HttpHeaders overpassHeaders =
                    new HttpHeaders();

            overpassHeaders.set(
                    "User-Agent",
                    "SmartTripPlanner/1.0"
            );

            overpassHeaders.setContentType(
                    MediaType.APPLICATION_FORM_URLENCODED
            );

            String requestBody =
                    "data="
                            + URLEncoder.encode(
                            overpassQuery,
                            StandardCharsets.UTF_8
                    );

            HttpEntity<String> overpassRequest =
                    new HttpEntity<>(
                            requestBody,
                            overpassHeaders
                    );

            String overpassUrl =
                    "https://overpass-api.de/api/interpreter";

            ResponseEntity<Map> overpassResponse =
                    restTemplate.exchange(
                            overpassUrl,
                            HttpMethod.POST,
                            overpassRequest,
                            Map.class
                    );

            Map<?, ?> responseBody =
                    overpassResponse.getBody();

            if (responseBody == null) {
                return attractions;
            }

            Object elementsObject =
                    responseBody.get("elements");

            if (!(elementsObject instanceof List<?>)) {
                return attractions;
            }

            List<?> elements =
                    (List<?>) elementsObject;

            // -------------------------------------------------
            // STEP 4: Convert to candidates
            // -------------------------------------------------

            List<PlaceCandidate> candidates =
                    new ArrayList<>();

            for (Object elementObject : elements) {

                if (!(elementObject instanceof Map<?, ?>)) {
                    continue;
                }

                Map<?, ?> element =
                        (Map<?, ?>) elementObject;

                Object tagsObject =
                        element.get("tags");

                if (!(tagsObject instanceof Map<?, ?>)) {
                    continue;
                }

                Map<?, ?> tags =
                        (Map<?, ?>) tagsObject;

                Object nameObject =
                        tags.get("name");

                if (nameObject == null) {
                    continue;
                }

                String name =
                        nameObject.toString().trim();

                if (name.isEmpty()) {
                    continue;
                }

                String lowerName =
                        name.toLowerCase();

                if (isUnwantedPlace(lowerName)) {
                    continue;
                }

                int score =
                        calculateTouristScore(tags);

                candidates.add(
                        new PlaceCandidate(
                                name,
                                score
                        )
                );
            }

            // -------------------------------------------------
            // STEP 5: Sort
            // -------------------------------------------------

            candidates.sort(
                    Comparator.comparingInt(
                            PlaceCandidate::getScore
                    ).reversed()
            );

            // -------------------------------------------------
            // STEP 6: Remove duplicates
            // -------------------------------------------------

            for (PlaceCandidate candidate : candidates) {

                String name =
                        candidate.getName();

                boolean duplicate =
                        attractions.stream()
                                .anyMatch(
                                        existing ->
                                                existing.equalsIgnoreCase(name)
                                );

                if (!duplicate) {
                    attractions.add(name);
                }

                if (attractions.size() >= 15) {
                    break;
                }
            }

            System.out.println(
                    "Found "
                            + attractions.size()
                            + " suitable attractions for "
                            + cleanDestination
            );

        } catch (Exception e) {

            System.out.println(
                    "Dynamic attraction API failed for "
                            + cleanDestination
            );

            System.out.println(
                    "Reason: "
                            + e.getMessage()
            );
        }

        return attractions;
    }

    // =====================================================
    // FALLBACK ATTRACTIONS
    // =====================================================

    private List<String> getFallbackAttractions(
            String destination) {

        String city =
                destination
                        .trim()
                        .toLowerCase();

        // -------------------------------------------------
        // Tamil Nadu
        // -------------------------------------------------

        if (city.contains("thanjavur")
                || city.contains("tanjore")) {

            return Arrays.asList(
                    "Brihadeeswarar Temple",
                    "Thanjavur Maratha Palace",
                    "Saraswathi Mahal Library",
                    "Thanjavur Art Gallery",
                    "Schwartz Church",
                    "Sivaganga Park",
                    "Gangaikonda Cholapuram",
                    "Kumbakonam Temples"
            );
        }

        if (city.contains("trichy")
                || city.contains("tiruchirappalli")
                || city.contains("tiruchirapalli")) {

            return Arrays.asList(
                    "Rock Fort Temple",
                    "Sri Ranganathaswamy Temple",
                    "Jambukeswarar Temple",
                    "Kallanai Dam",
                    "Tiruchirappalli Rail Museum",
                    "Samayapuram Mariamman Temple",
                    "Uttamar Kovil",
                    "St. Joseph's Church"
            );
        }

        if (city.contains("madurai")) {

            return Arrays.asList(
                    "Meenakshi Amman Temple",
                    "Thirumalai Nayakkar Palace",
                    "Gandhi Memorial Museum",
                    "Alagar Koyil",
                    "Thirupparankundram Temple",
                    "Vandiyur Mariamman Teppakulam",
                    "Samanar Hills"
            );
        }

        if (city.contains("chennai")) {

            return Arrays.asList(
                    "Marina Beach",
                    "Kapaleeshwarar Temple",
                    "Fort St. George",
                    "Government Museum",
                    "San Thome Basilica",
                    "Guindy National Park",
                    "Besant Nagar Beach",
                    "Valluvar Kottam"
            );
        }

        if (city.contains("coimbatore")) {

            return Arrays.asList(
                    "Marudamalai Temple",
                    "Gedee Car Museum",
                    "VOC Park",
                    "Isha Yoga Center",
                    "Kovai Kondattam",
                    "Siruvani Waterfalls",
                    "Eachanari Vinayagar Temple"
            );
        }

        if (city.contains("ooty")
                || city.contains("udhagamandalam")) {

            return Arrays.asList(
                    "Ooty Botanical Gardens",
                    "Ooty Lake",
                    "Doddabetta Peak",
                    "Rose Garden",
                    "Tea Factory",
                    "Pykara Lake",
                    "Pykara Waterfalls",
                    "Nilgiri Mountain Railway"
            );
        }

        if (city.contains("kodaikanal")) {

            return Arrays.asList(
                    "Kodaikanal Lake",
                    "Coaker's Walk",
                    "Bryant Park",
                    "Pillar Rocks",
                    "Silver Cascade Falls",
                    "Guna Caves",
                    "Moir Point",
                    "Bear Shola Falls"
            );
        }

        if (city.contains("rameswaram")) {

            return Arrays.asList(
                    "Ramanathaswamy Temple",
                    "Pamban Bridge",
                    "Dhanushkodi",
                    "Agni Theertham",
                    "APJ Abdul Kalam Memorial",
                    "Gandhamadhana Parvatham"
            );
        }

        if (city.contains("kanyakumari")
                || city.contains("cape comorin")) {

            return Arrays.asList(
                    "Vivekananda Rock Memorial",
                    "Thiruvalluvar Statue",
                    "Kanyakumari Beach",
                    "Padmanabhapuram Palace",
                    "Sunset Point",
                    "Gandhi Memorial Mandapam"
            );
        }

        if (city.contains("pondicherry")
                || city.contains("puducherry")) {

            return Arrays.asList(
                    "Promenade Beach",
                    "Auroville",
                    "Sri Aurobindo Ashram",
                    "Paradise Beach",
                    "White Town",
                    "Basilica of the Sacred Heart of Jesus",
                    "Pondicherry Museum"
            );
        }

        // -------------------------------------------------
        // Kerala
        // -------------------------------------------------

        if (city.contains("munnar")) {

            return Arrays.asList(
                    "Munnar Tea Gardens",
                    "Mattupetty Dam",
                    "Echo Point",
                    "Top Station",
                    "Eravikulam National Park",
                    "Attukad Waterfalls",
                    "Kundala Lake"
            );
        }

        if (city.contains("kochi")
                || city.contains("cochin")) {

            return Arrays.asList(
                    "Fort Kochi",
                    "Chinese Fishing Nets",
                    "Mattancherry Palace",
                    "Jew Town",
                    "St. Francis Church",
                    "Marine Drive",
                    "Kerala Folklore Museum"
            );
        }

        // -------------------------------------------------
        // Goa
        // -------------------------------------------------

        if (city.contains("goa")) {

            return Arrays.asList(
                    "Baga Beach",
                    "Calangute Beach",
                    "Anjuna Beach",
                    "Basilica of Bom Jesus",
                    "Fort Aguada",
                    "Dudhsagar Falls",
                    "Palolem Beach",
                    "Chapora Fort"
            );
        }

        // -------------------------------------------------
        // Karnataka
        // -------------------------------------------------

        if (city.contains("bengaluru")
                || city.contains("bangalore")) {

            return Arrays.asList(
                    "Lalbagh Botanical Garden",
                    "Bangalore Palace",
                    "Cubbon Park",
                    "Vidhana Soudha",
                    "ISKCON Temple",
                    "Bannerghatta National Park",
                    "Tipu Sultan's Summer Palace"
            );
        }

        if (city.contains("mysore")
                || city.contains("mysuru")) {

            return Arrays.asList(
                    "Mysore Palace",
                    "Chamundi Hill",
                    "Brindavan Gardens",
                    "Mysore Zoo",
                    "St. Philomena's Cathedral",
                    "Karanji Lake"
            );
        }

        // -------------------------------------------------
        // Maharashtra
        // -------------------------------------------------

        if (city.contains("mumbai")
                || city.contains("bombay")) {

            return Arrays.asList(
                    "Gateway of India",
                    "Marine Drive",
                    "Chhatrapati Shivaji Maharaj Terminus",
                    "Elephanta Caves",
                    "Siddhivinayak Temple",
                    "Colaba Causeway",
                    "Juhu Beach"
            );
        }

        // -------------------------------------------------
        // Delhi
        // -------------------------------------------------

        if (city.equals("delhi")
                || city.contains("new delhi")) {

            return Arrays.asList(
                    "India Gate",
                    "Red Fort",
                    "Qutub Minar",
                    "Lotus Temple",
                    "Humayun's Tomb",
                    "Akshardham Temple",
                    "Jama Masjid"
            );
        }

        // -------------------------------------------------
        // Rajasthan
        // -------------------------------------------------

        if (city.contains("jaipur")) {

            return Arrays.asList(
                    "Amber Fort",
                    "Hawa Mahal",
                    "City Palace",
                    "Jantar Mantar",
                    "Nahargarh Fort",
                    "Jal Mahal",
                    "Albert Hall Museum"
            );
        }

        // -------------------------------------------------
        // Uttar Pradesh
        // -------------------------------------------------

        if (city.contains("varanasi")
                || city.contains("banaras")) {

            return Arrays.asList(
                    "Kashi Vishwanath Temple",
                    "Dashashwamedh Ghat",
                    "Manikarnika Ghat",
                    "Sarnath",
                    "Assi Ghat",
                    "Ramnagar Fort",
                    "Ganga Aarti"
            );
        }

        // -------------------------------------------------
        // International destinations
        // -------------------------------------------------

        if (city.contains("paris")) {

            return Arrays.asList(
                    "Eiffel Tower",
                    "Louvre Museum",
                    "Notre-Dame Cathedral",
                    "Arc de Triomphe",
                    "Champs-Élysées",
                    "Montmartre",
                    "Sacré-Cœur"
            );
        }

        if (city.contains("tokyo")) {

            return Arrays.asList(
                    "Tokyo Skytree",
                    "Senso-ji Temple",
                    "Shibuya Crossing",
                    "Meiji Shrine",
                    "Tokyo Tower",
                    "Ueno Park",
                    "teamLab Borderless"
            );
        }

        if (city.contains("dubai")) {

            return Arrays.asList(
                    "Burj Khalifa",
                    "Dubai Mall",
                    "Palm Jumeirah",
                    "Dubai Marina",
                    "Museum of the Future",
                    "Jumeirah Mosque",
                    "Dubai Frame"
            );
        }

        if (city.contains("rome")) {

            return Arrays.asList(
                    "Colosseum",
                    "Trevi Fountain",
                    "Pantheon",
                    "Roman Forum",
                    "Vatican Museums",
                    "St. Peter's Basilica",
                    "Piazza Navona"
            );
        }

        if (city.contains("london")) {

            return Arrays.asList(
                    "Big Ben",
                    "Tower of London",
                    "London Eye",
                    "Buckingham Palace",
                    "British Museum",
                    "Tower Bridge",
                    "Hyde Park"
            );
        }

        if (city.contains("singapore")) {

            return Arrays.asList(
                    "Marina Bay Sands",
                    "Gardens by the Bay",
                    "Sentosa Island",
                    "Singapore Flyer",
                    "Merlion Park",
                    "Universal Studios Singapore",
                    "Chinatown"
            );
        }

        if (city.contains("bangkok")) {

            return Arrays.asList(
                    "Grand Palace",
                    "Wat Arun",
                    "Wat Pho",
                    "Chatuchak Market",
                    "Jim Thompson House",
                    "Lumphini Park"
            );
        }

        return new ArrayList<>();
    }

    // =====================================================
    // TOURIST RELEVANCE SCORE
    // =====================================================

    private int calculateTouristScore(
            Map<?, ?> tags) {

        int score = 0;

        Object tourismObject =
                tags.get("tourism");

        Object historicObject =
                tags.get("historic");

        Object leisureObject =
                tags.get("leisure");

        Object amenityObject =
                tags.get("amenity");

        if (tourismObject != null) {

            String tourism =
                    tourismObject.toString();

            switch (tourism) {

                case "attraction":
                    score += 100;
                    break;

                case "museum":
                    score += 95;
                    break;

                case "viewpoint":
                    score += 90;
                    break;

                case "gallery":
                    score += 85;
                    break;

                case "zoo":
                    score += 85;
                    break;

                case "theme_park":
                    score += 85;
                    break;

                case "aquarium":
                    score += 85;
                    break;

                case "artwork":
                    score += 75;
                    break;

                default:
                    score += 50;
                    break;
            }
        }

        if (historicObject != null) {
            score += 70;
        }

        if ("park".equals(
                String.valueOf(leisureObject))) {

            score += 55;
        }

        if ("place_of_worship".equals(
                String.valueOf(amenityObject))) {

            score += 35;
        }

        return score;
    }

    // =====================================================
    // REMOVE NON-TOURIST PLACES
    // =====================================================

    private boolean isUnwantedPlace(
            String name) {

        return name.contains("hotel")
                || name.contains("resort")
                || name.contains("restaurant")
                || name.contains("cafe")
                || name.contains("shop")
                || name.contains("store")
                || name.contains("supermarket")
                || name.contains("hospital")
                || name.contains("school")
                || name.contains("college")
                || name.contains("office")
                || name.contains("bank")
                || name.contains("atm")
                || name.contains("pharmacy")
                || name.contains("salon")
                || name.contains("market");
    }

    // =====================================================
    // INTERNAL CLASS
    // =====================================================

    private static class PlaceCandidate {

        private final String name;
        private final int score;

        public PlaceCandidate(
                String name,
                int score) {

            this.name = name;
            this.score = score;
        }

        public String getName() {
            return name;
        }

        public int getScore() {
            return score;
        }
    }
}