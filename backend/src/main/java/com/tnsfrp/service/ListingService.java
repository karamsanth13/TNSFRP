package com.tnsfrp.service;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

import com.tnsfrp.document.FoodListing;
import com.tnsfrp.document.ListingStatus;
import com.tnsfrp.document.NgoProfile;
import com.tnsfrp.document.User;
import com.tnsfrp.dto.ApiException;
import com.tnsfrp.dto.Dtos.ListingRequest;
import com.tnsfrp.dto.Dtos.NearbyListing;
import com.tnsfrp.repository.FoodListingRepository;
import com.tnsfrp.repository.NgoProfileRepository;
import com.tnsfrp.repository.UserRepository;
import com.tnsfrp.util.Districts;
import com.tnsfrp.util.GeoUtil;
import java.time.Instant;
import java.util.*;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ListingService {
    private final FoodListingRepository listings;
    private final NgoProfileRepository ngos;
    private final UserRepository users;
    private final MongoOperations mongo;

    public ListingService(FoodListingRepository listings, NgoProfileRepository ngos, UserRepository users, MongoOperations mongo) {
        this.listings = listings;
        this.ngos = ngos;
        this.users = users;
        this.mongo = mongo;
    }

    // ---------------- donor ----------------
    public FoodListing create(User donor, ListingRequest r) {
        if (!donor.approved) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Your account is awaiting admin approval");
        }
        if (!Districts.isValid(r.district())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Please choose a valid Tamil Nadu district");
        }
        if (!r.expiryAt().isAfter(Instant.now())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Expiry time must be in the future");
        }
        if (!r.preparedAt().isBefore(r.expiryAt())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Preparation time must be before expiry time");
        }
        FoodListing l = new FoodListing();
        l.donorId = donor.id;
        l.donorName = donor.fullName;
        l.foodTitle = r.foodTitle().trim();
        l.foodType = r.foodType();
        l.description = r.description();
        l.quantity = r.quantity();
        l.unit = r.unit();
        l.preparedAt = r.preparedAt();
        l.expiryAt = r.expiryAt();
        l.pickupAddress = r.pickupAddress().trim();
        l.district = r.district();
        l.latitude = r.latitude();
        l.longitude = r.longitude();
        l.status = ListingStatus.POSTED;
        return listings.save(l);
    }

    public List<FoodListing> donorListings(User donor) {
        expireOld();
        return listings.findByDonorIdOrderByCreatedAtDesc(donor.id);
    }

    /** Donor confirms the food was handed over: PICKED_UP -> COMPLETED. */
    public FoodListing complete(User donor, String id) {
        Query q = query(where("id").is(id).and("donorId").is(donor.id).and("status").is(ListingStatus.PICKED_UP));
        Update u = new Update().set("status", ListingStatus.COMPLETED).set("completedAt", Instant.now());
        FoodListing updated = mongo.findAndModify(q, u, FindAndModifyOptions.options().returnNew(true), FoodListing.class);
        if (updated == null) {
            FoodListing l = listings.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Donation not found"));
            if (!donor.id.equals(l.donorId)) throw new ApiException(HttpStatus.FORBIDDEN, "This is not your donation");
            throw new ApiException(HttpStatus.CONFLICT, "Only picked-up donations can be completed");
        }
        return updated;
    }

    // ---------------- NGO ----------------
    public List<NearbyListing> nearby(User ngoUser) {
        NgoProfile p = ngos.findByUserId(ngoUser.id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NGO profile not found"));
        expireOld();
        List<NearbyListing> result = new ArrayList<>();
        for (FoodListing l : listings.findByStatusAndExpiryAtAfter(ListingStatus.POSTED, Instant.now())) {
            double d = GeoUtil.distanceKm(p.latitude, p.longitude, l.latitude, l.longitude);
            if (d <= p.serviceRadiusKm) {
                result.add(new NearbyListing(l, Math.round(d * 10.0) / 10.0));
            }
        }
        result.sort(Comparator.comparingDouble(NearbyListing::distanceKm));
        return result;
    }

    /** Atomic: only one NGO can move a still-available, unexpired listing from POSTED to ACCEPTED. */
    public FoodListing accept(User ngoUser, String id) {
        if (!ngoUser.approved) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Your NGO account is not approved yet");
        }
        Instant now = Instant.now();
        Query q = query(where("id").is(id).and("status").is(ListingStatus.POSTED).and("expiryAt").gt(now));
        Update u = new Update().set("status", ListingStatus.ACCEPTED)
                .set("acceptedByNgoId", ngoUser.id).set("acceptedAt", now);
        FoodListing updated = mongo.findAndModify(q, u, FindAndModifyOptions.options().returnNew(true), FoodListing.class);
        if (updated == null) {
            FoodListing l = listings.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Donation not found"));
            boolean expired = l.status == ListingStatus.EXPIRED
                    || (l.status == ListingStatus.POSTED && !l.expiryAt.isAfter(now));
            if (expired) throw new ApiException(HttpStatus.BAD_REQUEST, "This donation has expired");
            throw new ApiException(HttpStatus.CONFLICT, "This donation is no longer available");
        }
        return updated;
    }

    /** Only the assigned NGO can confirm pickup: ACCEPTED -> PICKED_UP. */
    public FoodListing confirmPickup(User ngoUser, String id) {
        Query q = query(where("id").is(id).and("status").is(ListingStatus.ACCEPTED).and("acceptedByNgoId").is(ngoUser.id));
        Update u = new Update().set("status", ListingStatus.PICKED_UP).set("pickedUpAt", Instant.now());
        FoodListing updated = mongo.findAndModify(q, u, FindAndModifyOptions.options().returnNew(true), FoodListing.class);
        if (updated == null) {
            FoodListing l = listings.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Donation not found"));
            if (!ngoUser.id.equals(l.acceptedByNgoId)) {
                throw new ApiException(HttpStatus.FORBIDDEN, "Only the assigned NGO can confirm pickup");
            }
            throw new ApiException(HttpStatus.CONFLICT, "Only accepted donations can be picked up");
        }
        return updated;
    }

    public List<FoodListing> ngoHistory(User ngoUser) {
        return listings.findByAcceptedByNgoIdOrderByAcceptedAtDesc(ngoUser.id);
    }

    // ---------------- shared / admin ----------------
    @Scheduled(fixedRate = 60000)
    public void expireOld() {
        mongo.updateMulti(
                query(where("status").is(ListingStatus.POSTED).and("expiryAt").lt(Instant.now())),
                new Update().set("status", ListingStatus.EXPIRED), FoodListing.class);
    }

    public Map<String, Long> countByStatus(List<FoodListing> list) {
        Map<String, Long> m = new LinkedHashMap<>();
        for (ListingStatus s : ListingStatus.values()) m.put(s.name(), 0L);
        list.forEach(l -> m.merge(l.status.name(), 1L, Long::sum));
        return m;
    }

    public Map<String, Object> adminStats() {
        expireOld();
        List<FoodListing> all = listings.findAll();
        Map<String, Long> byStatus = countByStatus(all);
        Map<String, Long> byDistrict = new TreeMap<>();
        all.forEach(l -> byDistrict.merge(l.district, 1L, Long::sum));
        List<User> people = users.findByRoleIn(List.of(com.tnsfrp.document.Role.DONOR, com.tnsfrp.document.Role.NGO));

        Map<String, Object> m = new LinkedHashMap<>();
        m.put("totalDonations", all.size());
        m.put("completedDonations", byStatus.get("COMPLETED"));
        m.put("activeListings", byStatus.get("POSTED"));
        m.put("expiredListings", byStatus.get("EXPIRED"));
        m.put("byStatus", byStatus);
        m.put("byDistrict", byDistrict);
        m.put("donors", people.stream().filter(u -> u.role == com.tnsfrp.document.Role.DONOR).count());
        m.put("ngos", people.stream().filter(u -> u.role == com.tnsfrp.document.Role.NGO).count());
        m.put("pendingApprovals", people.stream().filter(u -> !u.approved).count());
        return m;
    }

    public List<FoodListing> allListings() {
        expireOld();
        return listings.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    public String exportCsv() {
        StringBuilder sb = new StringBuilder("id,foodTitle,category,quantity,unit,donor,district,status,preparedAt,expiryAt,acceptedByNgoId,createdAt\n");
        for (FoodListing l : allListings()) {
            sb.append(String.join(",",
                    csv(l.id), csv(l.foodTitle), csv(l.foodType), String.valueOf(l.quantity), csv(l.unit),
                    csv(l.donorName), csv(l.district), csv(l.status), csv(l.preparedAt), csv(l.expiryAt),
                    csv(l.acceptedByNgoId), csv(l.createdAt))).append('\n');
        }
        return sb.toString();
    }

    private static String csv(Object v) {
        if (v == null) return "";
        String s = v.toString();
        // quote values containing separators and neutralise spreadsheet formulas
        if (!s.isEmpty() && "=+-@".indexOf(s.charAt(0)) >= 0) s = "'" + s;
        return (s.contains(",") || s.contains("\"") || s.contains("\n")) ? "\"" + s.replace("\"", "\"\"") + "\"" : s;
    }
}
