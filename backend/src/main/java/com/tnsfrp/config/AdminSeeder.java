package com.tnsfrp.config;

import com.tnsfrp.document.Role;
import com.tnsfrp.document.User;
import com.tnsfrp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Creates the development admin at startup, only when ADMIN_EMAIL and ADMIN_PASSWORD are set. */
@Component
public class AdminSeeder implements CommandLineRunner {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    @Value("${app.admin.email:}") private String email;
    @Value("${app.admin.password:}") private String password;
    @Value("${app.admin.name:System Admin}") private String name;

    public AdminSeeder(UserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        if (email.isBlank() || password.isBlank()) {
            System.out.println("[TNSFRP] ADMIN_EMAIL / ADMIN_PASSWORD not set - no admin account created.");
            return;
        }
        String e = email.trim().toLowerCase();
        if (users.existsByEmail(e)) return;
        User a = new User();
        a.fullName = name;
        a.email = e;
        a.passwordHash = encoder.encode(password);
        a.phone = "0000000000";
        a.role = Role.ADMIN;
        a.district = "Chennai";
        a.address = "Administration";
        a.approved = true;
        users.save(a);
        System.out.println("[TNSFRP] Development admin created: " + e);
    }
}
