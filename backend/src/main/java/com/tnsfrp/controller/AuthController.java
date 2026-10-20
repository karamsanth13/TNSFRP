package com.tnsfrp.controller;

import com.tnsfrp.document.User;
import com.tnsfrp.dto.ApiException;
import com.tnsfrp.dto.Dtos.LoginRequest;
import com.tnsfrp.dto.Dtos.RegisterRequest;
import com.tnsfrp.service.AuthService;
import com.tnsfrp.util.Districts;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AuthController {
    private final AuthService auth;
    private final AuthenticationManager authManager;
    private final HttpSessionSecurityContextRepository contextRepo = new HttpSessionSecurityContextRepository();

    public AuthController(AuthService auth, AuthenticationManager authManager) {
        this.auth = auth;
        this.authManager = authManager;
    }

    @GetMapping("/districts")
    public List<String> districts() { return Districts.ALL; }

    @PostMapping("/auth/register")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody RegisterRequest r) {
        User u = auth.register(r);
        return ResponseEntity.status(HttpStatus.CREATED).body(auth.userView(u));
    }

    @PostMapping("/auth/login")
    public Map<String, Object> login(@Valid @RequestBody LoginRequest r, HttpServletRequest req, HttpServletResponse res) {
        try {
            Authentication a = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(r.email().trim().toLowerCase(), r.password()));
            SecurityContext ctx = SecurityContextHolder.createEmptyContext();
            ctx.setAuthentication(a);
            SecurityContextHolder.setContext(ctx);
            req.getSession(true);
            contextRepo.saveContext(ctx, req, res);
            return auth.userView(auth.currentUser(a));
        } catch (AuthenticationException e) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }
    }

    @GetMapping("/auth/me")
    public Map<String, Object> me(Authentication a) {
        return auth.userView(auth.currentUser(a));
    }
}
