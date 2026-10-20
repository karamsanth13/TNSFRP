package com.tnsfrp.controller;

import com.tnsfrp.document.FoodListing;
import com.tnsfrp.document.ListingStatus;
import com.tnsfrp.document.User;
import com.tnsfrp.dto.Dtos.NearbyListing;
import com.tnsfrp.service.AuthService;
import com.tnsfrp.service.ListingService;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ngo")
public class NgoController {
    private final AuthService auth;
    private final ListingService listings;

    public NgoController(AuthService auth, ListingService listings) {
        this.auth = auth;
        this.listings = listings;
    }

    @GetMapping("/dashboard")
    public Map<String, Object> dashboard(Authentication a) {
        User ngo = auth.currentUser(a);
        List<FoodListing> history = listings.ngoHistory(ngo);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("approved", ngo.approved);
        m.put("accepted", history.stream().filter(l -> l.status == ListingStatus.ACCEPTED).count());
        m.put("pickedUp", history.stream().filter(l -> l.status == ListingStatus.PICKED_UP).count());
        m.put("completed", history.stream().filter(l -> l.status == ListingStatus.COMPLETED).count());
        m.put("nearbyAvailable", listings.nearby(ngo).size());
        return m;
    }

    @GetMapping("/listings/nearby")
    public List<NearbyListing> nearby(Authentication a) {
        return listings.nearby(auth.currentUser(a));
    }

    @PostMapping("/listings/{id}/accept")
    public FoodListing accept(Authentication a, @PathVariable String id) {
        return listings.accept(auth.currentUser(a), id);
    }

    @PostMapping("/listings/{id}/pickup")
    public FoodListing pickup(Authentication a, @PathVariable String id) {
        return listings.confirmPickup(auth.currentUser(a), id);
    }

    @GetMapping("/history")
    public List<FoodListing> history(Authentication a) {
        return listings.ngoHistory(auth.currentUser(a));
    }
}
