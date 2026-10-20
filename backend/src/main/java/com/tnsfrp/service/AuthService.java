package com.tnsfrp.service;

import com.tnsfrp.document.NgoProfile;
import com.tnsfrp.document.Role;
import com.tnsfrp.document.User;
import com.tnsfrp.dto.ApiException;
import com.tnsfrp.dto.Dtos.RegisterRequest;
import com.tnsfrp.repository.NgoProfileRepository;
import com.tnsfrp.repository.UserRepository;
import com.tnsfrp.util.Districts;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.dao.DuplicateKeyException;

@Service
public class AuthService {
    static final Set<String> ORG_TYPES = Set.of("RESTAURANT", "HOSTEL", "CANTEEN", "MARRIAGE_HALL");

    private final UserRepository users;
    private final NgoProfileRepository ngos;
    private final PasswordEncoder encoder;

    public AuthService(UserRepository users, NgoProfileRepository ngos, PasswordEncoder encoder) {
        this.users = users;
        this.ngos = ngos;
        this.encoder = encoder;
    }

    public User register(RegisterRequest r) {
        if (r.role() == Role.ADMIN) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Administrator accounts cannot be self-registered");
        }
        if (!Districts.isValid(r.district())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Please choose a valid Tamil Nadu district");
        }
        String email = r.email().trim().toLowerCase();
        if (users.existsByEmail(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "An account with this email already exists");
        }
        if (r.role() == Role.DONOR && (r.organizationType() == null || !ORG_TYPES.contains(r.organizationType()))) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Choose a donor type (restaurant, hostel, canteen or marriage hall)");
        }
        if (r.role() == Role.NGO && (isBlank(r.ngoName()) || isBlank(r.registrationNumber()))) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "NGO name and registration number are required");
        }

        User u = new User();
        u.fullName = r.fullName().trim();
        u.email = email;
        u.passwordHash = encoder.encode(r.password());
        u.phone = r.phone().trim();
        u.role = r.role();
        u.organizationType = r.role() == Role.DONOR ? r.organizationType() : null;
        u.district = r.district();
        u.address = r.address().trim();
        u.latitude = r.latitude();
        u.longitude = r.longitude();
        u.approved = false;           // admin must approve
        try {
            u = users.save(u);
        } catch (DuplicateKeyException e) {
            // The unique MongoDB index is the source of truth when two registrations
            // with the same email arrive concurrently after the existence check.
            throw new ApiException(HttpStatus.CONFLICT, "An account with this email already exists");
        }

        if (r.role() == Role.NGO) {
            NgoProfile p = new NgoProfile();
            p.userId = u.id;
            p.ngoName = r.ngoName().trim();
            p.registrationNumber = r.registrationNumber().trim();
            p.serviceRadiusKm = r.serviceRadiusKm() != null ? r.serviceRadiusKm() : 10;
            p.capacityPerDay = r.capacityPerDay() != null ? r.capacityPerDay() : 100;
            p.district = u.district;
            p.latitude = u.latitude;
            p.longitude = u.longitude;
            ngos.save(p);
        }
        return u;
    }

    public User currentUser(Authentication auth) {
        return users.findByEmail(auth.getName())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Please log in again"));
    }

    /** User + NGO profile (if any), in the shape the frontend expects. */
    public Map<String, Object> userView(User u) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("user", u);
        m.put("ngoProfile", u.role == Role.NGO ? ngos.findByUserId(u.id).orElse(null) : null);
        return m;
    }

    public List<User> listUsers(boolean pendingOnly) {
        List<Role> roles = List.of(Role.DONOR, Role.NGO);
        return pendingOnly ? users.findByRoleInAndApproved(roles, false) : users.findByRoleIn(roles);
    }

    public User setApproval(String userId, boolean approved) {
        User u = users.findById(userId).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        if (u.role == Role.ADMIN) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Administrator accounts cannot be changed here");
        }
        u.approved = approved;
        users.save(u);
        if (u.role == Role.NGO) {
            ngos.findByUserId(u.id).ifPresent(p -> { p.approved = approved; ngos.save(p); });
        }
        return u;
    }

    private static boolean isBlank(String s) { return s == null || s.isBlank(); }
}
