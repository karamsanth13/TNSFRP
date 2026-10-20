package com.tnsfrp.document;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "users")
public class User {
    @Id public String id;
    public String fullName;
    @Indexed(unique = true) public String email;
    @JsonIgnore public String passwordHash;   // never sent to the client
    public String phone;
    public Role role;
    public String organizationType;           // donors only: RESTAURANT, HOSTEL, CANTEEN, MARRIAGE_HALL
    @Indexed public String district;
    public String address;
    public double latitude;
    public double longitude;
    public boolean approved;
    public Instant createdAt = Instant.now();
}
