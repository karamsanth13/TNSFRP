package com.tnsfrp.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "ngo_profiles")
public class NgoProfile {
    @Id public String id;
    @Indexed(unique = true) public String userId;
    public String ngoName;
    public String registrationNumber;
    public double serviceRadiusKm;
    public int capacityPerDay;
    public String district;
    public double latitude;
    public double longitude;
    public boolean approved;
}
