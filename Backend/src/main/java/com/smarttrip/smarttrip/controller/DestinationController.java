package com.smarttrip.smarttrip.controller;

import com.smarttrip.smarttrip.service.DestinationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/destinations")
public class DestinationController {

    private final DestinationService destinationService;

    public DestinationController(
            DestinationService destinationService
    ) {
        this.destinationService = destinationService;
    }

    @GetMapping("/search")
    public List<String> searchDestinations(
            @RequestParam String destination
    ) {
        return destinationService.getAttractions(destination);
    }
}