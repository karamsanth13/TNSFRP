package com.tnsfrp.dto;

import com.tnsfrp.document.FoodListing;
import com.tnsfrp.document.Role;
import com.tnsfrp.document.Unit;
import jakarta.validation.constraints.*;
import java.time.Instant;

/** Request/response objects grouped in one file to keep the project small. */
public final class Dtos {
    private Dtos() {}

    public record LoginRequest(
            @NotBlank(message = "Email is required") @Email(message = "Enter a valid email") String email,
            @NotBlank(message = "Password is required") String password) {}

    public record RegisterRequest(
            @NotBlank(message = "Name is required") @Size(max = 100) String fullName,
            @NotBlank(message = "Email is required") @Email(message = "Enter a valid email") String email,
            @NotBlank(message = "Password is required") @Size(min = 8, max = 72, message = "Password must be 8-72 characters") String password,
            @NotBlank(message = "Phone is required") @Pattern(regexp = "^[0-9+\\- ]{8,15}$", message = "Enter a valid phone number") String phone,
            @NotNull(message = "Role is required") Role role,
            String organizationType,
            @NotBlank(message = "District is required") String district,
            @NotBlank(message = "Address is required") String address,
            @NotNull(message = "Latitude is required") @DecimalMin(value = "-90", message = "Latitude must be between -90 and 90") @DecimalMax(value = "90", message = "Latitude must be between -90 and 90") Double latitude,
            @NotNull(message = "Longitude is required") @DecimalMin(value = "-180", message = "Longitude must be between -180 and 180") @DecimalMax(value = "180", message = "Longitude must be between -180 and 180") Double longitude,
            String ngoName,
            String registrationNumber,
            @Positive(message = "Service radius must be positive") Double serviceRadiusKm,
            @Positive(message = "Capacity must be positive") Integer capacityPerDay) {}

    public record ListingRequest(
            @NotBlank(message = "Food title is required") @Size(max = 100) String foodTitle,
            @NotBlank(message = "Food category is required") String foodType,
            @Size(max = 500) String description,
            @NotNull(message = "Quantity is required") @Positive(message = "Quantity must be greater than 0") Double quantity,
            @NotNull(message = "Unit is required") Unit unit,
            @NotNull(message = "Preparation time is required") Instant preparedAt,
            @NotNull(message = "Expiry time is required") Instant expiryAt,
            @NotBlank(message = "Pickup address is required") String pickupAddress,
            @NotBlank(message = "District is required") String district,
            @NotNull(message = "Latitude is required") @DecimalMin("-90") @DecimalMax("90") Double latitude,
            @NotNull(message = "Longitude is required") @DecimalMin("-180") @DecimalMax("180") Double longitude) {}

    public record ApprovalRequest(@NotNull Boolean approved) {}

    public record NearbyListing(FoodListing listing, double distanceKm) {}
}
