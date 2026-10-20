package com.tnsfrp;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.tnsfrp.document.Role;
import com.tnsfrp.document.User;
import com.tnsfrp.dto.ApiException;
import com.tnsfrp.dto.Dtos.RegisterRequest;
import com.tnsfrp.repository.NgoProfileRepository;
import com.tnsfrp.repository.UserRepository;
import com.tnsfrp.service.AuthService;
import org.springframework.dao.DuplicateKeyException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class AuthServiceTest {
    private final UserRepository users = mock(UserRepository.class);
    private final AuthService service = new AuthService(users,
            mock(NgoProfileRepository.class), new BCryptPasswordEncoder());

    private RegisterRequest req(Role role, String district) {
        return new RegisterRequest("Test", "t@example.com", "password1", "9876543210", role,
                "RESTAURANT", district, "Some street", 13.0, 80.0, null, null, null, null);
    }

    @Test
    void adminCannotSelfRegister() {
        ApiException e = assertThrows(ApiException.class, () -> service.register(req(Role.ADMIN, "Chennai")));
        assertEquals(HttpStatus.FORBIDDEN, e.getStatus());
    }

    @Test
    void invalidDistrictIsRejected() {
        ApiException e = assertThrows(ApiException.class, () -> service.register(req(Role.DONOR, "Atlantis")));
        assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
    }

    @Test
    void concurrentDuplicateEmailRegistrationReturnsConflict() {
        when(users.existsByEmail("t@example.com")).thenReturn(false);
        doThrow(new DuplicateKeyException("duplicate email index"))
                .when(users).save(any(User.class));

        ApiException e = assertThrows(ApiException.class,
                () -> service.register(req(Role.DONOR, "Chennai")));
        assertEquals(HttpStatus.CONFLICT, e.getStatus());
        assertEquals("An account with this email already exists", e.getMessage());
    }
}
