package com.example.paymentbe.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.Key;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtUtilsTest {

    private JwtUtils jwtUtils;
    private final String validSecret = "dGVzdFNlY3JldEtleUZvckpXVFRva2VuVGVzdGluZ1B1cnBvc2VzT25seUxvbmdFbm91Z2g="; // Base64 encoded test secret
    private final String invalidSecret = "invalid";

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils(validSecret);
    }

    @Test
    void constructor_WithValidSecret_ShouldCreateInstance() {
        JwtUtils utils = new JwtUtils(validSecret);
        assertNotNull(utils);
    }

    @Test
    void constructor_WithNullSecret_ShouldCreateInstance() {
        assertDoesNotThrow(() -> new JwtUtils(null));
    }

    @Test
    void getSigningKey_WithValidSecret_ShouldReturnKey() throws Exception {
        JwtUtils utils = new JwtUtils(validSecret);

        java.lang.reflect.Method method = JwtUtils.class.getDeclaredMethod("getSigningKey");
        method.setAccessible(true);
        Key result = (Key) method.invoke(utils);

        assertNotNull(result);
    }

    @Test
    void getSigningKey_WithNullSecret_ShouldThrowException() throws Exception {
        JwtUtils utils = new JwtUtils(null);

        java.lang.reflect.Method method = JwtUtils.class.getDeclaredMethod("getSigningKey");
        method.setAccessible(true);
        
        assertThrows(java.lang.reflect.InvocationTargetException.class, () -> {
            method.invoke(utils);
        });
    }

    @Test
    void getUserIdFromJwtToken_WithValidToken_ShouldReturnUserId() {
        String userId = "testUser123";
        String token = createValidToken(userId);

        String result = jwtUtils.getUserIdFromJwtToken(token);

        assertEquals(userId, result);
    }

    @Test
    void getUserIdFromJwtToken_WithInvalidToken_ShouldThrowException() {
        String invalidToken = "invalid.token.here";

        assertThrows(Exception.class, () -> {
            jwtUtils.getUserIdFromJwtToken(invalidToken);
        });
    }

    @Test
    void validateJwtToken_WithValidToken_ShouldReturnTrue() {
        String token = createValidToken("testUser");

        boolean result = jwtUtils.validateJwtToken(token);

        assertTrue(result);
    }

    @Test
    void validateJwtToken_WithInvalidSignature_ShouldReturnFalse() {
        String tokenWithInvalidSignature = createTokenWithInvalidSignature();

        boolean result = jwtUtils.validateJwtToken(tokenWithInvalidSignature);

        assertFalse(result);
    }

    @Test
    void validateJwtToken_WithMalformedToken_ShouldReturnFalse() {
        String malformedToken = "malformed.token";
        boolean result = jwtUtils.validateJwtToken(malformedToken);
        assertFalse(result);
    }

    @Test
    void validateJwtToken_WithNullToken_ShouldReturnFalse() {
        String nullToken = null;
        boolean result = jwtUtils.validateJwtToken(nullToken);
        assertFalse(result);
    }

    @Test
    void validateJwtToken_WithEmptyToken_ShouldReturnFalse() {
        String emptyToken = "";
        boolean result = jwtUtils.validateJwtToken(emptyToken);
        assertFalse(result);
    }

    @Test
    void validateJwtToken_WithExpiredToken_ShouldReturnFalse() {
        String expiredToken = createExpiredToken("testUser");
        boolean result = jwtUtils.validateJwtToken(expiredToken);
        assertFalse(result);
    }

    private String createValidToken(String userId) {
        Key signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(validSecret));
        
        return Jwts.builder()
                .setSubject(userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    private String createTokenWithInvalidSignature() {
        String differentSecret = "ZGlmZmVyZW50U2VjcmV0S2V5Rm9yVGVzdGluZ1B1cnBvc2VzT25seUxvbmdFbm91Z2g=";
        Key differentSigningKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(differentSecret));
        
        return Jwts.builder()
                .setSubject("testUser")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(differentSigningKey, SignatureAlgorithm.HS256)
                .compact();
    }

    private String createExpiredToken(String userId) {
        Key signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(validSecret));
        
        return Jwts.builder()
                .setSubject(userId)
                .setIssuedAt(new Date(System.currentTimeMillis() - 86400000)) // 24 hours ago
                .setExpiration(new Date(System.currentTimeMillis() - 3600000)) // 1 hour ago (expired)
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }
}