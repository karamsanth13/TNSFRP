package com.tnsfrp;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.tnsfrp.document.*;
import com.tnsfrp.dto.ApiException;
import com.tnsfrp.dto.Dtos.ListingRequest;
import com.tnsfrp.repository.*;
import com.tnsfrp.service.ListingService;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.UpdateDefinition;
import org.springframework.http.HttpStatus;

class ListingServiceTest {
    FoodListingRepository listings = mock(FoodListingRepository.class);
    NgoProfileRepository ngos = mock(NgoProfileRepository.class);
    UserRepository users = mock(UserRepository.class);
    MongoOperations mongo = mock(MongoOperations.class);
    ListingService service;
    User donor, ngo;

    @BeforeEach
    void setUp() {
        service = new ListingService(listings, ngos, users, mongo);
        donor = user("donor1", Role.DONOR, true);
        ngo = user("ngo1", Role.NGO, true);
    }

    static User user(String id, Role role, boolean approved) {
        User u = new User();
        u.id = id; u.role = role; u.approved = approved; u.fullName = id;
        return u;
    }

    ListingRequest request(Instant prepared, Instant expiry, String district) {
        return new ListingRequest("Biryani", "Cooked meals", "desc", 10.0, Unit.KG,
                prepared, expiry, "12 Main St", district, 13.08, 80.27);
    }

    FoodListing listing(ListingStatus s, Instant expiry, String ngoId) {
        FoodListing l = new FoodListing();
        l.id = "l1"; l.status = s; l.expiryAt = expiry; l.acceptedByNgoId = ngoId; l.donorId = "donor1";
        return l;
    }

    @SuppressWarnings("unchecked")
    void stubUpdate(FoodListing result) {
        when(mongo.findAndModify(any(Query.class), any(UpdateDefinition.class),
                any(FindAndModifyOptions.class), eq(FoodListing.class))).thenReturn(result);
    }

    // ----- creation validation -----
    @Test
    void createRejectsPastExpiry() {
        Instant now = Instant.now();
        ApiException e = assertThrows(ApiException.class,
                () -> service.create(donor, request(now.minusSeconds(7200), now.minusSeconds(60), "Chennai")));
        assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
        verify(listings, never()).save(any());
    }

    @Test
    void createRejectsInvalidDistrictAndUnapprovedDonor() {
        Instant now = Instant.now();
        assertThrows(ApiException.class, () -> service.create(donor, request(now, now.plusSeconds(3600), "Atlantis")));
        User pending = user("d2", Role.DONOR, false);
        ApiException e = assertThrows(ApiException.class,
                () -> service.create(pending, request(now, now.plusSeconds(3600), "Chennai")));
        assertEquals(HttpStatus.FORBIDDEN, e.getStatus());
    }

    @Test
    void createSavesValidListingAsPosted() {
        Instant now = Instant.now();
        when(listings.save(any(FoodListing.class))).thenAnswer(i -> i.getArgument(0));
        FoodListing l = service.create(donor, request(now.minusSeconds(600), now.plusSeconds(3600), "Chennai"));
        assertEquals(ListingStatus.POSTED, l.status);
        assertEquals("donor1", l.donorId);
    }

    // ----- acceptance rules -----
    @Test
    void unapprovedNgoCannotAccept() {
        User pending = user("ngo2", Role.NGO, false);
        ApiException e = assertThrows(ApiException.class, () -> service.accept(pending, "l1"));
        assertEquals(HttpStatus.FORBIDDEN, e.getStatus());
        verifyNoInteractions(mongo);
    }

    @Test
    void approvedNgoCanAcceptAvailableListing() {
        stubUpdate(listing(ListingStatus.ACCEPTED, Instant.now().plusSeconds(600), "ngo1"));
        assertEquals(ListingStatus.ACCEPTED, service.accept(ngo, "l1").status);
    }

    @Test
    void secondAcceptanceIsRejectedAsConflict() {
        stubUpdate(null);
        when(listings.findById("l1")).thenReturn(Optional.of(listing(ListingStatus.ACCEPTED, Instant.now().plusSeconds(600), "other")));
        ApiException e = assertThrows(ApiException.class, () -> service.accept(ngo, "l1"));
        assertEquals(HttpStatus.CONFLICT, e.getStatus());
    }

    @Test
    void expiredListingCannotBeAccepted() {
        stubUpdate(null);
        when(listings.findById("l1")).thenReturn(Optional.of(listing(ListingStatus.POSTED, Instant.now().minusSeconds(60), null)));
        ApiException e = assertThrows(ApiException.class, () -> service.accept(ngo, "l1"));
        assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
    }

    @Test
    void missingListingIsNotFound() {
        stubUpdate(null);
        when(listings.findById("zzz")).thenReturn(Optional.empty());
        ApiException e = assertThrows(ApiException.class, () -> service.accept(ngo, "zzz"));
        assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
    }

    @Test
    void onlyAssignedNgoCanConfirmPickup() {
        stubUpdate(null);
        when(listings.findById("l1")).thenReturn(Optional.of(listing(ListingStatus.ACCEPTED, Instant.now().plusSeconds(600), "someoneElse")));
        ApiException e = assertThrows(ApiException.class, () -> service.confirmPickup(ngo, "l1"));
        assertEquals(HttpStatus.FORBIDDEN, e.getStatus());
    }
}
