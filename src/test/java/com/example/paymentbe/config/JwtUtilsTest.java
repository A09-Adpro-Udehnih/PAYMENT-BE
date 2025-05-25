package com.example.paymentbe.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.security.Key;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@TestPropertySource(properties = {
    "jwt.secret=dWRlaG5paHNlY3JldHlhbmdwYW5qYW5nYmFuZ2V0eWFuZ3Rlcm55YXRhbWFzaWhrdXJhbmdwYW5qYW5n"
})
class JwtUtilsTest {

    @Value("${jwt.secret}")
    private String jwtSecret;

    private JwtUtils jwtUtils;
    private String validToken;
    private Key signingKey;

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils(jwtSecret);
        signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
        
        // Generate a valid token for testing
        validToken = Jwts.builder()
            .setSubject("testUserId")
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + 60000)) // 1 minute validity
            .signWith(signingKey, SignatureAlgorithm.HS256)
            .compact();
    }

    @Test
    void validateJwtToken_ValidToken_ReturnsTrue() {
        assertTrue(jwtUtils.validateJwtToken(validToken));
    }

    @Test
    void validateJwtToken_InvalidToken_ReturnsFalse() {
        String invalidToken = "invalid.jwt.token";
        assertFalse(jwtUtils.validateJwtToken(invalidToken));
    }

    @Test
    void validateJwtToken_ExpiredToken_ReturnsFalse() {
        String expiredToken = Jwts.builder()
            .setSubject("testUserId")
            .setIssuedAt(new Date(System.currentTimeMillis() - 120000)) // 2 minutes ago
            .setExpiration(new Date(System.currentTimeMillis() - 60000)) // expired 1 minute ago
            .signWith(signingKey, SignatureAlgorithm.HS256)
            .compact();

        assertFalse(jwtUtils.validateJwtToken(expiredToken));
    }

    @Test
    void validateJwtToken_NullToken_ReturnsFalse() {
        assertFalse(jwtUtils.validateJwtToken(null));
    }

    @Test
    void getUserIdFromJwtToken_ValidToken_ReturnsUserId() {
        String userId = jwtUtils.getUserIdFromJwtToken(validToken);
        assertEquals("testUserId", userId);
    }

    @Test
    void getUserIdFromJwtToken_InvalidToken_ThrowsException() {
        String invalidToken = "invalid.jwt.token";
        Exception exception = assertThrows(Exception.class, () -> 
            jwtUtils.getUserIdFromJwtToken(invalidToken)
        );
        assertTrue(exception.getMessage().contains("JWT"));
    }
} 