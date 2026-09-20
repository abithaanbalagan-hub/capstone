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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
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

        String cleanDestination = normalizeDestination(destination);

        /*
         * First use curated India destinations.
         */
        List<String> fallbackAttractions =
                getFallbackAttractions(cleanDestination);

        if (!fallbackAttractions.isEmpty()) {

            System.out.println(
                    "Using curated India attractions for "
                            + cleanDestination
            );

            return removeDuplicates(fallbackAttractions);
        }

        /*
         * If destination is not in curated list,
         * use OpenStreetMap dynamically.
         */
        List<String> dynamicAttractions =
                getDynamicAttractions(cleanDestination);

        if (!dynamicAttractions.isEmpty()) {

            System.out.println(
                    "Using dynamic attractions for "
                            + cleanDestination
            );

            return dynamicAttractions;
        }

        System.out.println(
                "No attractions found for "
                        + cleanDestination
        );

        return new ArrayList<>();
    }

    // =====================================================
    // DESTINATION NORMALIZATION
    // =====================================================

    private String normalizeDestination(String destination) {

        String city = destination
                .trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9\\s]", "")
                .replaceAll("\\s+", " ");

        Map<String, String> aliases = new LinkedHashMap<>();

        // Kerala
        aliases.put("munar", "Munnar");
        aliases.put("munnar", "Munnar");
        aliases.put("munnar kerala", "Munnar");
        aliases.put("kochi", "Kochi");
        aliases.put("cochin", "Kochi");
        aliases.put("alleppey", "Alleppey");
        aliases.put("alappuzha", "Alleppey");
        aliases.put("wayanad", "Wayanad");
        aliases.put("thekkady", "Thekkady");
        aliases.put("periyar", "Thekkady");
        aliases.put("kovalam", "Kovalam");
        aliases.put("varkala", "Varkala");

        // Tamil Nadu
        aliases.put("chennai", "Chennai");
        aliases.put("madras", "Chennai");
        aliases.put("madurai", "Madurai");
        aliases.put("thanjavur", "Thanjavur");
        aliases.put("tanjore", "Thanjavur");
        aliases.put("trichy", "Trichy");
        aliases.put("tiruchirappalli", "Trichy");
        aliases.put("tiruchirapalli", "Trichy");
        aliases.put("coimbatore", "Coimbatore");
        aliases.put("ooty", "Ooty");
        aliases.put("udhagamandalam", "Ooty");
        aliases.put("kodaikanal", "Kodaikanal");
        aliases.put("rameswaram", "Rameswaram");
        aliases.put("kanyakumari", "Kanyakumari");
        aliases.put("cape comorin", "Kanyakumari");

        // Karnataka
        aliases.put("bengaluru", "Bengaluru");
        aliases.put("bangalore", "Bengaluru");
        aliases.put("mysore", "Mysore");
        aliases.put("mysuru", "Mysore");
        aliases.put("hampi", "Hampi");

        // Rajasthan
        aliases.put("jaipur", "Jaipur");
        aliases.put("udaipur", "Udaipur");
        aliases.put("jodhpur", "Jodhpur");

        // Maharashtra
        aliases.put("mumbai", "Mumbai");
        aliases.put("bombay", "Mumbai");
        aliases.put("pune", "Pune");

        // Other India destinations
        aliases.put("goa", "Goa");
        aliases.put("varanasi", "Varanasi");
        aliases.put("banaras", "Varanasi");
        aliases.put("agra", "Agra");
        aliases.put("delhi", "Delhi");
        aliases.put("new delhi", "Delhi");
        aliases.put("hyderabad", "Hyderabad");
        aliases.put("visakhapatnam", "Visakhapatnam");
        aliases.put("vizag", "Visakhapatnam");
        aliases.put("ahmedabad", "Ahmedabad");
        aliases.put("statue of unity", "Statue of Unity");
        aliases.put("kevadiya", "Statue of Unity");
        aliases.put("bhopal", "Bhopal");
        aliases.put("indore", "Indore");
        aliases.put("kolkata", "Kolkata");
        aliases.put("calcutta", "Kolkata");

        String normalized = aliases.get(city);

        if (normalized != null) {
            System.out.println(
                    "Normalized destination: "
                            + destination
                            + " -> "
                            + normalized
            );
            return normalized;
        }

        // Handle common suffixes such as "Munnar Kerala".
        for (Map.Entry<String, String> entry : aliases.entrySet()) {
            if (city.startsWith(entry.getKey() + " ")
                    || city.endsWith(" " + entry.getKey())) {

                System.out.println(
                        "Normalized destination: "
                                + destination
                                + " -> "
                                + entry.getValue()
                );

                return entry.getValue();
            }
        }

        return destination.trim();
    }

    // =====================================================
    // DYNAMIC OPENSTREETMAP DATA
    // =====================================================

    private List<String> getDynamicAttractions(
            String cleanDestination) {

        List<String> attractions =
                new ArrayList<>();

        try {

            String encodedDestination =
                    URLEncoder.encode(
                            cleanDestination,
                            StandardCharsets.UTF_8
                    );

            String nominatimUrl =
                    "https://nominatim.openstreetmap.org/search"
                            + "?q="
                            + encodedDestination
                            + ", India"
                            + "&format=json"
                            + "&limit=1";

            HttpHeaders headers =
                    new HttpHeaders();

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

            /*
             * Search a wider area so the trip planner
             * can discover more tourist destinations.
             */
            String overpassQuery =
                    "[out:json][timeout:25];"
                            + "("

                            + "nwr[\"tourism\"=\"attraction\"]"
                            + "(around:50000,"
                            + latitude + ","
                            + longitude + ");"

                            + "nwr[\"tourism\"=\"museum\"]"
                            + "(around:50000,"
                            + latitude + ","
                            + longitude + ");"

                            + "nwr[\"tourism\"=\"viewpoint\"]"
                            + "(around:50000,"
                            + latitude + ","
                            + longitude + ");"

                            + "nwr[\"tourism\"=\"gallery\"]"
                            + "(around:50000,"
                            + latitude + ","
                            + longitude + ");"

                            + "nwr[\"tourism\"=\"zoo\"]"
                            + "(around:50000,"
                            + latitude + ","
                            + longitude + ");"

                            + "nwr[\"tourism\"=\"theme_park\"]"
                            + "(around:50000,"
                            + latitude + ","
                            + longitude + ");"

                            + "nwr[\"tourism\"=\"aquarium\"]"
                            + "(around:50000,"
                            + latitude + ","
                            + longitude + ");"

                            + "nwr[\"historic\"]"
                            + "(around:50000,"
                            + latitude + ","
                            + longitude + ");"

                            + "nwr[\"leisure\"=\"park\"]"
                            + "(around:50000,"
                            + latitude + ","
                            + longitude + ");"

                            + "nwr[\"natural\"=\"beach\"]"
                            + "(around:50000,"
                            + latitude + ","
                            + longitude + ");"

                            + "nwr[\"natural\"=\"waterfall\"]"
                            + "(around:50000,"
                            + latitude + ","
                            + longitude + ");"

                            + "nwr[\"natural\"=\"peak\"]"
                            + "(around:50000,"
                            + latitude + ","
                            + longitude + ");"

                            + "nwr[\"amenity\"=\"place_of_worship\"]"
                            + "(around:50000,"
                            + latitude + ","
                            + longitude + ");"

                            + ");"
                            + "out center tags;";

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
                        name.toLowerCase(
                                Locale.ROOT
                        );

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

            candidates.sort(
                    Comparator.comparingInt(
                            PlaceCandidate::getScore
                    ).reversed()
            );

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

                /*
                 * Allow more attractions for longer trips.
                 */
                if (attractions.size() >= 30) {
                    break;
                }
            }

            System.out.println(
                    "Found "
                            + attractions.size()
                            + " dynamic attractions for "
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
    // INDIA CURATED DESTINATIONS
    // =====================================================

    private List<String> getFallbackAttractions(
            String destination) {

        String city =
                destination
                        .trim()
                        .toLowerCase(Locale.ROOT);

        // =================================================
        // TAMIL NADU
        // =================================================

        if (city.contains("chennai")) {

            return Arrays.asList(
                    "Marina Beach",
                    "Fort St. George",
                    "Government Museum",
                    "Guindy National Park",
                    "Besant Nagar Beach",
                    "Valluvar Kottam",
                    "San Thome Basilica",
                    "Kapaleeshwarar Temple",
                    "Birla Planetarium",
                    "DakshinaChitra",
                    "Cholamandal Artists' Village",
                    "Semmozhi Poonga",
                    "Muttukadu Boat House",
                    "Mahabalipuram Shore Temple",
                    "Mahabalipuram Group of Monuments"
            );
        }

        if (city.contains("madurai")) {

            return Arrays.asList(
                    "Meenakshi Amman Temple",
                    "Thirumalai Nayakkar Palace",
                    "Gandhi Memorial Museum",
                    "Vandiyur Mariamman Teppakulam",
                    "Samanar Hills",
                    "Alagar Koyil",
                    "Thirupparankundram Temple",
                    "Keezhadi Archaeological Site",
                    "Azhagar Hills",
                    "Vaigai Dam",
                    "Pazhamudircholai",
                    "Madurai Gandhi Museum"
            );
        }

        if (city.contains("munnar")) {

            return Arrays.asList(
                    "Munnar Tea Gardens",
                    "Eravikulam National Park",
                    "Mattupetty Dam",
                    "Echo Point",
                    "Kundala Lake",
                    "Top Station",
                    "Attukad Waterfalls",
                    "Lakkam Waterfalls",
                    "Pothamedu View Point",
                    "Tea Museum",
                    "Blossom Park",
                    "Chokramudi Peak",
                    "Anamudi Peak View",
                    "Mattupetty Indo-Swiss Farm",
                    "Lockhart Gap"
            );
        }

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
                    "Kumbakonam",
                    "Darasuram Airavatesvara Temple",
                    "Punnainallur Mariamman Temple",
                    "Thiruvaiyaru",
                    "Manora Fort"
            );
        }

        if (city.contains("trichy")
                || city.contains("tiruchirappalli")
                || city.contains("tiruchirapalli")) {

            return Arrays.asList(
                    "Rock Fort",
                    "Sri Ranganathaswamy Temple",
                    "Kallanai Dam",
                    "Tiruchirappalli Rail Museum",
                    "Jambukeswarar Temple",
                    "Mukkombu",
                    "St. Joseph's Church",
                    "Samayapuram",
                    "Uttamar Kovil",
                    "Puliyancholai",
                    "Viralimalai",
                    "Government Museum"
            );
        }

        if (city.contains("coimbatore")) {

            return Arrays.asList(
                    "Gedee Car Museum",
                    "VOC Park",
                    "Isha Yoga Center",
                    "Kovai Kondattam",
                    "Siruvani Waterfalls",
                    "Marudamalai Temple",
                    "Eachanari Temple",
                    "Black Thunder",
                    "Aliyar Dam",
                    "Monkey Falls",
                    "Valparai",
                    "Topslip",
                    "Anamalai Tiger Reserve"
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
                    "Nilgiri Mountain Railway",
                    "Avalanche Lake",
                    "Emerald Lake",
                    "Tea Museum",
                    "Wenlock Downs",
                    "Shooting Point",
                    "Catherine Falls",
                    "Needle Rock View Point"
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
                    "Bear Shola Falls",
                    "Berijam Lake",
                    "Dolphin's Nose",
                    "Kurinji Andavar Temple",
                    "Poombarai Village",
                    "Mannavanur Lake",
                    "Pine Forest",
                    "Chettiar Park"
            );
        }

        if (city.contains("rameswaram")) {

            return Arrays.asList(
                    "Ramanathaswamy Temple",
                    "Pamban Bridge",
                    "Dhanushkodi",
                    "Agni Theertham",
                    "APJ Abdul Kalam Memorial",
                    "Gandhamadhana Parvatham",
                    "Ariyaman Beach",
                    "Kothandaramaswamy Temple",
                    "Pamban Island",
                    "Dhanushkodi Beach"
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
                    "Gandhi Memorial Mandapam",
                    "Vattakottai Fort",
                    "Suchindram Temple",
                    "Mathur Aqueduct",
                    "Chitharal Jain Monuments",
                    "Thirparappu Waterfalls"
            );
        }

        // =================================================
        // KERALA
        // =================================================

        if (city.contains("kochi")
                || city.contains("cochin")) {

            return Arrays.asList(
                    "Fort Kochi",
                    "Chinese Fishing Nets",
                    "Mattancherry Palace",
                    "Jew Town",
                    "Kerala Folklore Museum",
                    "Marine Drive",
                    "St. Francis Church",
                    "Bolgatty Palace",
                    "Hill Palace Museum",
                    "Cherai Beach",
                    "Willingdon Island",
                    "Kumbalangi Village",
                    "Vypin Island",
                    "Kerala Kathakali Centre",
                    "Indo-Portuguese Museum"
            );
        }

        if (city.contains("alleppey")
                || city.contains("alappuzha")) {

            return Arrays.asList(
                    "Alappuzha Backwaters",
                    "Alappuzha Beach",
                    "Vembanad Lake",
                    "Marari Beach",
                    "Pathiramanal Island",
                    "Kuttanad",
                    "Krishnapuram Palace",
                    "Ambalappuzha Temple",
                    "Nehru Trophy Boat Race Area",
                    "Revi Karunakaran Museum",
                    "Thottappally Beach",
                    "Punnamada Lake"
            );
        }

        if (city.contains("wayanad")) {

            return Arrays.asList(
                    "Edakkal Caves",
                    "Banasura Sagar Dam",
                    "Soochipara Waterfalls",
                    "Chembra Peak",
                    "Kuruva Island",
                    "Wayanad Wildlife Sanctuary",
                    "Pookode Lake",
                    "Lakkidi View Point",
                    "Phantom Rock",
                    "Meenmutty Waterfalls",
                    "Tholpetty Wildlife Sanctuary",
                    "900 Kandi",
                    "Karlad Lake",
                    "Wayanad Heritage Museum"
            );
        }

        if (city.contains("thekkady")
                || city.contains("periyar")) {

            return Arrays.asList(
                    "Periyar Wildlife Sanctuary",
                    "Periyar Lake",
                    "Gavi",
                    "Spice Plantation",
                    "Bamboo Rafting",
                    "Thekkady Elephant Junction",
                    "Mangala Devi Temple",
                    "Pandikuzhi View Point",
                    "Vagamon",
                    "Parunthumpara View Point",
                    "Periyar Tiger Reserve",
                    "Kumily Spice Market"
            );
        }

        if (city.contains("kovalam")) {

            return Arrays.asList(
                    "Kovalam Beach",
                    "Lighthouse Beach",
                    "Hawa Beach",
                    "Vizhinjam Harbour",
                    "Vizhinjam Marine Aquarium",
                    "Vellayani Lake",
                    "Poovar Island",
                    "Aazhimala Cliff",
                    "Neyyar Dam",
                    "Neyyar Wildlife Sanctuary"
            );
        }

        if (city.contains("varkala")) {

            return Arrays.asList(
                    "Varkala Cliff",
                    "Varkala Beach",
                    "Janardhana Swamy Temple",
                    "Kappil Beach",
                    "Kappil Lake",
                    "Anchuthengu Fort",
                    "Sivagiri Mutt",
                    "Ponnumthuruthu Island",
                    "Black Sand Beach",
                    "Edava Beach"
            );
        }

        // =================================================
        // KARNATAKA
        // =================================================

        if (city.contains("bengaluru")
                || city.contains("bangalore")) {

            return Arrays.asList(
                    "Lalbagh Botanical Garden",
                    "Bangalore Palace",
                    "Cubbon Park",
                    "Vidhana Soudha",
                    "Bannerghatta National Park",
                    "Tipu Sultan's Summer Palace",
                    "Bangalore Fort",
                    "Visvesvaraya Museum",
                    "Ulsoor Lake",
                    "Wonderla",
                    "ISKCON Temple",
                    "Nandi Hills",
                    "Commercial Street",
                    "Innovative Film City"
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
                    "Karanji Lake",
                    "Railway Museum Mysore",
                    "Jaganmohan Palace",
                    "Devaraja Market",
                    "Ranganathittu Bird Sanctuary",
                    "Srirangapatna",
                    "Daria Daulat Bagh"
            );
        }

        if (city.contains("hampi")) {

            return Arrays.asList(
                    "Virupaksha Temple",
                    "Vijaya Vittala Temple",
                    "Hampi Bazaar",
                    "Lotus Mahal",
                    "Elephant Stables",
                    "Matanga Hill",
                    "Hemakuta Hill",
                    "Achyutaraya Temple",
                    "Queen's Bath",
                    "Hampi Archaeological Museum",
                    "Tungabhadra Dam",
                    "Sanapur Lake"
            );
        }

        // =================================================
        // RAJASTHAN
        // =================================================

        if (city.contains("jaipur")) {

            return Arrays.asList(
                    "Amber Fort",
                    "Hawa Mahal",
                    "City Palace",
                    "Jantar Mantar",
                    "Nahargarh Fort",
                    "Jal Mahal",
                    "Albert Hall Museum",
                    "Jaigarh Fort",
                    "Patrika Gate",
                    "Birla Mandir",
                    "Chokhi Dhani",
                    "Galtaji",
                    "Central Park",
                    "Anokhi Museum"
            );
        }

        if (city.contains("udaipur")) {

            return Arrays.asList(
                    "City Palace Udaipur",
                    "Lake Pichola",
                    "Sajjangarh Palace",
                    "Jagdish Temple",
                    "Saheliyon Ki Bari",
                    "Fateh Sagar Lake",
                    "Bagore Ki Haveli",
                    "Jag Mandir",
                    "Vintage Car Museum",
                    "Doodh Talai",
                    "Shilpgram",
                    "Badi Lake"
            );
        }

        if (city.contains("jodhpur")) {

            return Arrays.asList(
                    "Mehrangarh Fort",
                    "Jaswant Thada",
                    "Umaid Bhawan Palace",
                    "Clock Tower Market",
                    "Mandore Gardens",
                    "Rao Jodha Desert Rock Park",
                    "Kaylana Lake",
                    "Toorji Ka Jhalra",
                    "Machia Biological Park",
                    "Osian"
            );
        }

        // =================================================
        // MAHARASHTRA
        // =================================================

        if (city.contains("mumbai")
                || city.contains("bombay")) {

            return Arrays.asList(
                    "Gateway of India",
                    "Marine Drive",
                    "Chhatrapati Shivaji Maharaj Terminus",
                    "Elephanta Caves",
                    "Colaba Causeway",
                    "Juhu Beach",
                    "Sanjay Gandhi National Park",
                    "Kanheri Caves",
                    "Chhatrapati Shivaji Maharaj Vastu Sangrahalaya",
                    "Haji Ali Dargah",
                    "Bandra-Worli Sea Link",
                    "Siddhivinayak Temple",
                    "Nehru Planetarium"
            );
        }

        if (city.contains("pune")) {

            return Arrays.asList(
                    "Shaniwar Wada",
                    "Aga Khan Palace",
                    "Sinhagad Fort",
                    "Pataleshwar Cave Temple",
                    "Raja Dinkar Kelkar Museum",
                    "Dagdusheth Halwai Temple",
                    "Osho Garden",
                    "Lal Mahal",
                    "Parvati Hill",
                    "Khadakwasla Dam",
                    "Pashan Lake"
            );
        }

        // =================================================
        // GOA
        // =================================================

        if (city.contains("goa")) {

            return Arrays.asList(
                    "Baga Beach",
                    "Calangute Beach",
                    "Anjuna Beach",
                    "Palolem Beach",
                    "Fort Aguada",
                    "Chapora Fort",
                    "Dudhsagar Falls",
                    "Basilica of Bom Jesus",
                    "Se Cathedral",
                    "Candolim Beach",
                    "Butterfly Beach",
                    "Fontainhas",
                    "Salim Ali Bird Sanctuary",
                    "Grand Island"
            );
        }

        // =================================================
        // UTTAR PRADESH
        // =================================================

        if (city.contains("varanasi")
                || city.contains("banaras")) {

            return Arrays.asList(
                    "Dashashwamedh Ghat",
                    "Assi Ghat",
                    "Sarnath",
                    "Ramnagar Fort",
                    "Manikarnika Ghat",
                    "Ganga Aarti",
                    "Banaras Hindu University",
                    "Bharat Mata Temple",
                    "Ramnagar Museum",
                    "Tulsi Manas Temple",
                    "Sankat Mochan Temple",
                    "Godowlia Market"
            );
        }

        if (city.contains("agra")) {

            return Arrays.asList(
                    "Taj Mahal",
                    "Agra Fort",
                    "Mehtab Bagh",
                    "Itmad-ud-Daulah's Tomb",
                    "Agra Bear Rescue Facility",
                    "Akbar's Tomb",
                    "Fatehpur Sikri",
                    "Mariam-uz-Zamani Tomb",
                    "Agra Heritage Walk",
                    "Kinari Bazaar"
            );
        }

        // =================================================
        // DELHI
        // =================================================

        if (city.equals("delhi")
                || city.contains("new delhi")) {

            return Arrays.asList(
                    "India Gate",
                    "Red Fort",
                    "Qutub Minar",
                    "Lotus Temple",
                    "Humayun's Tomb",
                    "National Museum",
                    "Akshardham Temple",
                    "Jama Masjid",
                    "Lodhi Garden",
                    "Purana Qila",
                    "National Rail Museum",
                    "Dilli Haat",
                    "Raj Ghat",
                    "Connaught Place"
            );
        }

        // =================================================
        // TELANGANA
        // =================================================

        if (city.contains("hyderabad")) {

            return Arrays.asList(
                    "Charminar",
                    "Golconda Fort",
                    "Salar Jung Museum",
                    "Hussain Sagar Lake",
                    "Ramoji Film City",
                    "Chowmahalla Palace",
                    "Qutb Shahi Tombs",
                    "Nehru Zoological Park",
                    "Birla Mandir",
                    "Shilparamam",
                    "Durgam Cheruvu",
                    "Statue of Equality"
            );
        }

        // =================================================
        // ANDHRA PRADESH
        // =================================================

        if (city.contains("visakhapatnam")
                || city.contains("vizag")) {

            return Arrays.asList(
                    "RK Beach",
                    "Kailasagiri",
                    "Submarine Museum",
                    "Yarada Beach",
                    "Araku Valley",
                    "Borra Caves",
                    "Dolphin's Nose",
                    "Simhachalam",
                    "Indira Gandhi Zoological Park",
                    "Rushikonda Beach",
                    "Thotlakonda",
                    "Kambalakonda Wildlife Sanctuary"
            );
        }

        // =================================================
        // GUJARAT
        // =================================================

        if (city.contains("ahmedabad")) {

            return Arrays.asList(
                    "Sabarmati Ashram",
                    "Adalaj Stepwell",
                    "Kankaria Lake",
                    "Science City",
                    "Sidi Saiyyed Mosque",
                    "Sabarmati Riverfront",
                    "Jama Masjid",
                    "Auto World Vintage Car Museum",
                    "Calico Museum of Textiles",
                    "Manek Chowk",
                    "Hutheesing Jain Temple"
            );
        }

        if (city.contains("statue of unity")
                || city.contains("kevadiya")) {

            return Arrays.asList(
                    "Statue of Unity",
                    "Valley of Flowers",
                    "Sardar Sarovar Dam",
                    "Cactus Garden",
                    "Jungle Safari",
                    "Ekta Cruise",
                    "Zarwani Waterfalls",
                    "Shoolpaneshwar Wildlife Sanctuary",
                    "Arogya Van",
                    "Vishwa Van"
            );
        }

        // =================================================
        // MADHYA PRADESH
        // =================================================

        if (city.contains("bhopal")) {

            return Arrays.asList(
                    "Upper Lake",
                    "Van Vihar National Park",
                    "Sanchi Stupa",
                    "Bhimbetka Rock Shelters",
                    "Bharat Bhavan",
                    "Tribal Museum",
                    "Taj-ul-Masajid",
                    "Lower Lake",
                    "Gohar Mahal",
                    "Regional Science Centre"
            );
        }

        if (city.contains("indore")) {

            return Arrays.asList(
                    "Rajwada Palace",
                    "Lal Bagh Palace",
                    "Sarafa Bazaar",
                    "Patalpani Waterfall",
                    "Annapurna Temple",
                    "Central Museum",
                    "Kanch Mandir",
                    "Ralamandal Wildlife Sanctuary",
                    "Gomatgiri",
                    "Janapav Hill"
            );
        }

        // =================================================
        // WEST BENGAL
        // =================================================

        if (city.contains("kolkata")
                || city.contains("calcutta")) {

            return Arrays.asList(
                    "Victoria Memorial",
                    "Howrah Bridge",
                    "Indian Museum",
                    "St. Paul's Cathedral",
                    "Science City Kolkata",
                    "Eco Park",
                    "Marble Palace",
                    "Belur Math",
                    "Dakshineswar Kali Temple",
                    "Prinsep Ghat",
                    "Birla Planetarium",
                    "Park Street",
                    "Mother House",
                    "Maidan"
            );
        }

        // =================================================
        // INTERNATIONAL DESTINATIONS REMOVED
        // =================================================

        /*
         * SmartTrip is now India-focused.
         *
         * Unknown destinations can still use
         * dynamic India search through OSM.
         */

        return new ArrayList<>();
    }

    // =====================================================
    // REMOVE DUPLICATES
    // =====================================================

    private List<String> removeDuplicates(
            List<String> places) {

        Map<String, String> unique =
                new LinkedHashMap<>();

        for (String place : places) {

            if (place == null) {
                continue;
            }

            String clean =
                    place.trim();

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

        Object naturalObject =
                tags.get("natural");

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
                String.valueOf(
                        leisureObject
                ))) {

            score += 60;
        }

        if ("beach".equals(
                String.valueOf(
                        naturalObject
                ))) {

            score += 85;
        }

        if ("waterfall".equals(
                String.valueOf(
                        naturalObject
                ))) {

            score += 85;
        }

        if ("peak".equals(
                String.valueOf(
                        naturalObject
                ))) {

            score += 80;
        }

        /*
         * Worship places are allowed,
         * but ranked below other tourist categories.
         */
        if ("place_of_worship".equals(
                String.valueOf(
                        amenityObject
                ))) {

            score += 25;
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
                || name.contains("market")
                || name.contains("car shelter")
                || name.contains("dream house")
                || name.contains("bazaar");
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