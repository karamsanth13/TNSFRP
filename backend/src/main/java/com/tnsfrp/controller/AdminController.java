package com.tnsfrp.controller;

import com.tnsfrp.document.FoodListing;
import com.tnsfrp.document.User;
import com.tnsfrp.dto.Dtos.ApprovalRequest;
import com.tnsfrp.service.AuthService;
import com.tnsfrp.service.ListingService;
import jakarta.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final AuthService auth;
    private final ListingService listings;

    public AdminController(AuthService auth, ListingService listings) {
        this.auth = auth;
        this.listings = listings;
    }

    @GetMapping("/dashboard")
    public Map<String, Object> dashboard() { return listings.adminStats(); }

    @GetMapping("/users")
    public List<User> users() { return auth.listUsers(false); }

    @GetMapping("/users/pending")
    public List<User> pending() { return auth.listUsers(true); }

    @PatchMapping("/users/{id}/approval")
    public User approval(@PathVariable String id, @Valid @RequestBody ApprovalRequest r) {
        return auth.setApproval(id, r.approved());
    }

    @GetMapping("/listings")
    public List<FoodListing> allListings() { return listings.allListings(); }

    @GetMapping("/reports/export")
    public ResponseEntity<byte[]> export() {
        byte[] body = listings.exportCsv().getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"donations.csv\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(body);
    }
}
