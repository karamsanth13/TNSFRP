package com.tnsfrp.document;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "food_listings")
public class FoodListing {
    @Id public String id;
    @Indexed public String donorId;
    public String donorName;
    public String foodTitle;
    public String foodType;
    public String description;
    public double quantity;
    public Unit unit;
    public Instant preparedAt;
    @Indexed public Instant expiryAt;
    public String pickupAddress;
    @Indexed public String district;
    public double latitude;
    public double longitude;
    @Indexed public ListingStatus status = ListingStatus.POSTED;
    @Indexed public String acceptedByNgoId;
    public Instant acceptedAt;
    public Instant pickedUpAt;
    public Instant completedAt;
    public Instant createdAt = Instant.now();
}
