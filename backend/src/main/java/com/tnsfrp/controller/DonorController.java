package com.tnsfrp.controller;

import com.tnsfrp.document.FoodListing;
import com.tnsfrp.document.User;
import com.tnsfrp.dto.Dtos.ListingRequest;
import com.tnsfrp.service.AuthService;
import com.tnsfrp.service.ListingService;
import jakarta.validation.Valid;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/donor")
public class DonorController {
    private final AuthService auth;
    private final ListingService listings;

    public DonorController(AuthService auth, ListingService listings) {
        this.auth = auth;
        this.listings = listings;
    }

    @GetMapping("/dashboard")
    public Map<String, Object> dashboard(Authentication a) {
        User donor = auth.currentUser(a);
        List<FoodListing> mine = listings.donorListings(donor);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("total", mine.size());
        m.put("byStatus", listings.countByStatus(mine));
        m.put("approved", donor.approved);
        return m;
    }

    @PostMapping("/listings")
    public ResponseEntity<FoodListing> create(Authentication a, @Valid @RequestBody ListingRequest r) {
        return ResponseEntity.status(HttpStatus.CREATED).body(listings.create(auth.currentUser(a), r));
    }

    @GetMapping("/listings")
    public List<FoodListing> mine(Authentication a) {
        return listings.donorListings(auth.currentUser(a));
    }

    @PostMapping("/listings/{id}/complete")
    public FoodListing complete(Authentication a, @PathVariable String id) {
        return listings.complete(auth.currentUser(a), id);
    }
}
