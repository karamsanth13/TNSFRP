package com.tnsfrp;

import static org.junit.jupiter.api.Assertions.*;

import com.tnsfrp.util.Districts;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

class DistrictsAndPasswordTest {
    @Test
    void thereAre38DistrictsAndValidationWorks() {
        assertEquals(38, Districts.ALL.size());
        assertTrue(Districts.isValid("Chennai"));
        assertFalse(Districts.isValid("Mumbai"));
        assertFalse(Districts.isValid(null));
    }

    @Test
    void passwordsAreHashedWithBcrypt() {
        PasswordEncoder enc = new BCryptPasswordEncoder();
        String hash = enc.encode("Secret123");
        assertNotEquals("Secret123", hash);
        assertTrue(hash.startsWith("$2"));
        assertTrue(enc.matches("Secret123", hash));
        assertFalse(enc.matches("wrong", hash));
    }
}
