$path = ".\src\main\java\com\smarttrip\smarttrip\service\TripService.java"

$text = Get-Content $path -Raw

$start = $text.IndexOf("        int attractionIndex = 0;")
$budget = $text.IndexOf("// BUDGET BREAKDOWN", $start)

if ($start -lt 0 -or $budget -lt 0) {
    Write-Host "ERROR: Itinerary section not found."
    exit 1
}

$newBlock = @"
        int attractionIndex = 0;

        List<String> experiencePool =
                getDestinationExperiences(destination);

        int experienceIndex = 0;

        /*
         * Itinerary strategy:
         * Days 1 to N-1: destination sightseeing / experiences.
         * Final day: shopping + local food + culture + relaxation.
         * Curated attractions are consumed first without repetition.
         */
        for (
                int day = 1;
                day <= numberOfDays;
                day++
        ) {

            plan.append("Day ")
                    .append(day)
                    .append("\n");

            if (day == numberOfDays) {

                plan.append("Morning:\n");
                plan.append("- Enjoy a relaxed breakfast and "
                        + "slow morning at the hotel.\n");

                plan.append("Afternoon:\n");
                plan.append("- Explore local shopping areas and "
                        + "buy destination-specific souvenirs.\n");

                plan.append("Evening:\n");
                plan.append("- Enjoy local food and a cultural "
                        + "experience before relaxing for the night.\n");

                plan.append("\n");
                continue;
            }

            plan.append("Morning:\n");

            if (attractionIndex < selectedAttractions.size()) {
                plan.append("- Visit ")
                        .append(selectedAttractions.get(attractionIndex))
                        .append("\n");
                attractionIndex++;
            } else if (experienceIndex < experiencePool.size()) {
                plan.append("- Explore ")
                        .append(experiencePool.get(experienceIndex))
                        .append("\n");
                experienceIndex++;
            } else {
                plan.append("- Explore a local sightseeing area in ")
                        .append(destination)
                        .append(".\n");
            }

            plan.append("Afternoon:\n");

            if (attractionIndex < selectedAttractions.size()) {
                plan.append("- Explore ")
                        .append(selectedAttractions.get(attractionIndex))
                        .append("\n");
                attractionIndex++;
            } else if (experienceIndex < experiencePool.size()) {
                plan.append("- Experience ")
                        .append(experiencePool.get(experienceIndex))
                        .append("\n");
                experienceIndex++;
            } else {
                plan.append("- Discover another local scenic or "
                        + "heritage area in ")
                        .append(destination)
                        .append(".\n");
            }

            plan.append("Evening:\n");

            if (attractionIndex < selectedAttractions.size()) {
                plan.append("- Experience ")
                        .append(selectedAttractions.get(attractionIndex))
                        .append("\n");
                attractionIndex++;
            } else if (experienceIndex < experiencePool.size()) {
                plan.append("- Enjoy ")
                        .append(experiencePool.get(experienceIndex))
                        .append("\n");
                experienceIndex++;
            } else {
                plan.append("- Enjoy the destination's "
                        + "evening atmosphere and local surroundings.\n");
            }

            plan.append("\n");
        }


        // =================================================

        // BUDGET BREAKDOWN

"@

$text = $text.Substring(0, $start) + $newBlock + $text.Substring($budget + "// BUDGET BREAKDOWN".Length)

$marker = "    // ATTRACTION SELECTION"
$pos = $text.IndexOf($marker)

if ($pos -lt 0) {
    Write-Host "ERROR: Attraction section not found."
    exit 1
}

$helper = @"
    /*
     * Destination-specific fallback experiences for longer trips.
     */
    private List<String> getDestinationExperiences(
            String destination) {

        List<String> experiences = new ArrayList<>();

        String key = destination == null
                ? ""
                : destination.toLowerCase(Locale.ROOT);

        if (key.contains("trichy")
                || key.contains("tiruchirappalli")) {

            experiences.add("Explore the Rockfort hill surroundings");
            experiences.add("Visit the Srirangam temple area");
            experiences.add("Walk around the Cauvery river-side areas");
            experiences.add("Explore local heritage streets");
            experiences.add("Discover traditional handicraft and local markets");
            experiences.add("Enjoy a peaceful temple-town evening");
            experiences.add("Explore local vegetarian food spots");
            experiences.add("Visit nearby heritage and cultural areas");

        } else if (key.contains("madurai")) {

            experiences.add("Explore the heritage streets around the temple area");
            experiences.add("Visit local handicraft and textile areas");
            experiences.add("Explore traditional food streets");
            experiences.add("Discover local cultural neighbourhoods");
            experiences.add("Enjoy an evening heritage walk");
            experiences.add("Explore local markets and artisan shops");
            experiences.add("Experience traditional South Indian cuisine");
            experiences.add("Visit nearby historical surroundings");

        } else if (key.contains("chennai")) {

            experiences.add("Explore Marina Beach and the surrounding area");
            experiences.add("Discover Mylapore heritage streets");
            experiences.add("Explore local art and cultural spaces");
            experiences.add("Visit traditional food streets");
            experiences.add("Explore Chennai's heritage neighbourhoods");
            experiences.add("Discover local handicraft and shopping areas");
            experiences.add("Enjoy a coastal evening experience");
            experiences.add("Explore local South Indian cuisine");

        } else if (key.contains("munnar")) {

            experiences.add("Explore the tea plantation landscapes");
            experiences.add("Visit a local tea tasting experience");
            experiences.add("Enjoy a scenic mountain drive");
            experiences.add("Explore local spice shops");
            experiences.add("Discover nearby village areas");
            experiences.add("Enjoy a peaceful nature walk");
            experiences.add("Explore local Kerala food spots");
            experiences.add("Visit local handicraft markets");

        } else if (key.contains("kochi")
                || key.contains("cochin")) {

            experiences.add("Explore Fort Kochi heritage streets");
            experiences.add("Visit the waterfront areas");
            experiences.add("Explore local art and cultural spaces");
            experiences.add("Discover traditional Kerala food spots");
            experiences.add("Explore local spice and handicraft markets");
            experiences.add("Enjoy a heritage evening walk");
            experiences.add("Discover local neighbourhoods");
            experiences.add("Experience Kerala cultural traditions");

        } else if (key.contains("goa")) {

            experiences.add("Explore a scenic coastal area");
            experiences.add("Discover Portuguese-influenced heritage streets");
            experiences.add("Visit local handicraft markets");
            experiences.add("Enjoy Goan local cuisine");
            experiences.add("Explore a quiet beach area");
            experiences.add("Discover local cultural spaces");
            experiences.add("Enjoy a coastal sunset experience");
            experiences.add("Explore local village surroundings");

        } else if (key.contains("jaipur")) {

            experiences.add("Explore the old city heritage streets");
            experiences.add("Visit local handicraft markets");
            experiences.add("Discover traditional Rajasthani food");
            experiences.add("Explore artisan and textile areas");
            experiences.add("Enjoy a heritage evening walk");
            experiences.add("Discover local cultural neighbourhoods");
            experiences.add("Explore traditional craft shops");
            experiences.add("Experience Rajasthani cultural traditions");

        } else if (key.contains("hyderabad")) {

            experiences.add("Explore the old city heritage area");
            experiences.add("Discover traditional food streets");
            experiences.add("Explore local pearl and handicraft markets");
            experiences.add("Visit historic neighbourhoods");
            experiences.add("Enjoy a local cultural experience");
            experiences.add("Explore traditional Hyderabadi cuisine");
            experiences.add("Discover local shopping areas");
            experiences.add("Enjoy an evening heritage walk");

        } else if (key.contains("mumbai")
                || key.contains("bombay")) {

            experiences.add("Explore a historic South Mumbai neighbourhood");
            experiences.add("Discover local food streets");
            experiences.add("Explore local markets");
            experiences.add("Enjoy a coastal evening walk");
            experiences.add("Discover local art and cultural spaces");
            experiences.add("Explore traditional shopping areas");
            experiences.add("Experience Mumbai street food");
            experiences.add("Explore a heritage neighbourhood");

        } else if (key.contains("bengaluru")
                || key.contains("bangalore")) {

            experiences.add("Explore the city's heritage neighbourhoods");
            experiences.add("Discover local food streets");
            experiences.add("Visit local art and cultural spaces");
            experiences.add("Explore traditional shopping areas");
            experiences.add("Enjoy a peaceful city park experience");
            experiences.add("Discover local craft and lifestyle stores");
            experiences.add("Explore the city's café culture");
            experiences.add("Enjoy an evening city walk");

        } else if (key.contains("wayanad")) {

            experiences.add("Explore a scenic hill and plantation area");
            experiences.add("Discover local spice-growing areas");
            experiences.add("Enjoy a peaceful nature walk");
            experiences.add("Explore local tribal handicrafts");
            experiences.add("Discover village surroundings");
            experiences.add("Enjoy local Kerala food");
            experiences.add("Explore local markets");
            experiences.add("Enjoy a scenic mountain evening");

        } else if (key.contains("alleppey")
                || key.contains("alappuzha")) {

            experiences.add("Explore the backwater village surroundings");
            experiences.add("Enjoy a peaceful waterfront experience");
            experiences.add("Discover local coir handicrafts");
            experiences.add("Explore traditional Kerala food");
            experiences.add("Visit local village markets");
            experiences.add("Enjoy a scenic evening near the backwaters");
            experiences.add("Discover local cultural traditions");
            experiences.add("Explore nearby heritage areas");

        } else {

            experiences.add("Explore a popular local neighbourhood");
            experiences.add("Discover local heritage and architecture");
            experiences.add("Explore a traditional local market");
            experiences.add("Taste popular local cuisine");
            experiences.add("Discover local cultural spaces");
            experiences.add("Explore local handicrafts");
            experiences.add("Enjoy a scenic local viewpoint");
            experiences.add("Experience the destination's local traditions");
        }

        return removeDuplicates(experiences);
    }


"@

$text = $text.Substring(0, $pos) + $helper + $text.Substring($pos)

Set-Content -Path $path -Value $text -Encoding UTF8

Write-Host ""
Write-Host "SUCCESS: TripService.java updated."
Write-Host "Now run: mvn test"
