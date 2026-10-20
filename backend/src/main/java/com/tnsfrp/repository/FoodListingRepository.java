package com.tnsfrp.repository;

import com.tnsfrp.document.FoodListing;
import com.tnsfrp.document.ListingStatus;
import java.time.Instant;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface FoodListingRepository extends MongoRepository<FoodListing, String> {
    List<FoodListing> findByDonorIdOrderByCreatedAtDesc(String donorId);
    List<FoodListing> findByAcceptedByNgoIdOrderByAcceptedAtDesc(String ngoId);
    List<FoodListing> findByStatusAndExpiryAtAfter(ListingStatus status, Instant now);
}
