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
        // Given & When
        JwtUtils utils = new JwtUtils(validSecret);
        
        // Then
        assertNotNull(utils);
    }

    @Test
    void constructor_WithNullSecret_ShouldCreateInstance() {
        // Given & When & Then
        assertDoesNotThrow(() -> new JwtUtils(null));
    }

    @Test
    void getSigningKey_WithValidSecret_ShouldReturnKey() throws Exception {
        // Given
        JwtUtils utils = new JwtUtils(validSecret);
        
        // When - using reflection to access private method
        java.lang.reflect.Method method = JwtUtils.class.getDeclaredMethod("getSigningKey");
        method.setAccessible(true);
        Key result = (Key) method.invoke(utils);
        
        // Then
        assertNotNull(result);
    }

    @Test
    void getSigningKey_WithNullSecret_ShouldThrowException() throws Exception {
        // Given
        JwtUtils utils = new JwtUtils(null);
        
        // When & Then
        java.lang.reflect.Method method = JwtUtils.class.getDeclaredMethod("getSigningKey");
        method.setAccessible(true);
        
        assertThrows(java.lang.reflect.InvocationTargetException.class, () -> {
            method.invoke(utils);
        });
    }

    @Test
    void getUserIdFromJwtToken_WithValidToken_ShouldReturnUserId() {
        // Given
        String userId = "testUser123";
        String token = createValidToken(userId);
        
        // When
        String result = jwtUtils.getUserIdFromJwtToken(token);
        
        // Then
        assertEquals(userId, result);
    }

    @Test
    void getUserIdFromJwtToken_WithInvalidToken_ShouldThrowException() {
        // Given
        String invalidToken = "invalid.token.here";
        
        // When & Then
        assertThrows(Exception.class, () -> {
            jwtUtils.getUserIdFromJwtToken(invalidToken);
        });
    }

    @Test
    void validateJwtToken_WithValidToken_ShouldReturnTrue() {
        // Given
        String token = createValidToken("testUser");
        
        // When
        boolean result = jwtUtils.validateJwtToken(token);
        
        // Then
        assertTrue(result);
    }

    @Test
    void validateJwtToken_WithInvalidSignature_ShouldReturnFalse() {
        // Given
        String tokenWithInvalidSignature = createTokenWithInvalidSignature();
        
        // When
        boolean result = jwtUtils.validateJwtToken(tokenWithInvalidSignature);
        
        // Then
        assertFalse(result);
    }

    @Test
    void validateJwtToken_WithMalformedToken_ShouldReturnFalse() {
        // Given
        String malformedToken = "malformed.token";
        
        // When
        boolean result = jwtUtils.validateJwtToken(malformedToken);
        
        // Then
        assertFalse(result);
    }

    @Test
    void validateJwtToken_WithNullToken_ShouldReturnFalse() {
        // Given
        String nullToken = null;
        
        // When
        boolean result = jwtUtils.validateJwtToken(nullToken);
        
        // Then
        assertFalse(result);
    }

    @Test
    void validateJwtToken_WithEmptyToken_ShouldReturnFalse() {
        // Given
        String emptyToken = "";
        
        // When
        boolean result = jwtUtils.validateJwtToken(emptyToken);
        
        // Then
        assertFalse(result);
    }

    @Test
    void validateJwtToken_WithExpiredToken_ShouldReturnFalse() {
        // Given
        String expiredToken = createExpiredToken("testUser");
        
        // When
        boolean result = jwtUtils.validateJwtToken(expiredToken);
        
        // Then
        assertFalse(result);
    }

    private String createValidToken(String userId) {
        Key signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(validSecret));
        
        return Jwts.builder()
                .setSubject(userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // 24 hours
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    private String createTokenWithInvalidSignature() {
        // Create token with different secret
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