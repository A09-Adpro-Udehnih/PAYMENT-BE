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
    void constructorWithValidSecretShouldCreateInstance() {
        JwtUtils utils = new JwtUtils(validSecret);
        assertNotNull(utils);
    }

    @Test
    void constructorWithNullSecretShouldCreateInstance() {
        assertDoesNotThrow(() -> new JwtUtils(null));
    }

    @Test
    void getSigningKeyWithValidSecretShouldReturnKey() throws Exception {
        JwtUtils utils = new JwtUtils(validSecret);

        java.lang.reflect.Method method = JwtUtils.class.getDeclaredMethod("getSigningKey");
        method.setAccessible(true);
        Key result = (Key) method.invoke(utils);

        assertNotNull(result);
    }

    @Test
    void getSigningKeyWithNullSecretShouldThrowException() throws Exception {
        JwtUtils utils = new JwtUtils(null);

        java.lang.reflect.Method method = JwtUtils.class.getDeclaredMethod("getSigningKey");
        method.setAccessible(true);
        
        assertThrows(java.lang.reflect.InvocationTargetException.class, () -> {
            method.invoke(utils);
        });
    }

    @Test
    void getUserIdFromJwtTokenWithValidTokenShouldReturnUserId() {
        String userId = "testUser123";
        String token = createValidToken(userId);

        String result = jwtUtils.getUserIdFromJwtToken(token);

        assertEquals(userId, result);
    }

    @Test
    void getUserIdFromJwtTokenWithInvalidTokenShouldThrowException() {
        String invalidToken = "invalid.token.here";

        assertThrows(Exception.class, () -> {
            jwtUtils.getUserIdFromJwtToken(invalidToken);
        });
    }

    @Test
    void validateJwtTokenWithValidTokenShouldReturnTrue() {
        String token = createValidToken("testUser");

        boolean result = jwtUtils.validateJwtToken(token);

        assertTrue(result);
    }

    @Test
    void validateJwtTokenWithInvalidSignatureShouldReturnFalse() {
        String tokenWithInvalidSignature = createTokenWithInvalidSignature();

        boolean result = jwtUtils.validateJwtToken(tokenWithInvalidSignature);

        assertFalse(result);
    }

    @Test
    void validateJwtTokenWithMalformedTokenShouldReturnFalse() {
        String malformedToken = "malformed.token";
        boolean result = jwtUtils.validateJwtToken(malformedToken);
        assertFalse(result);
    }

    @Test
    void validateJwtTokenWithNullTokenShouldReturnFalse() {
        String nullToken = null;
        boolean result = jwtUtils.validateJwtToken(nullToken);
        assertFalse(result);
    }

    @Test
    void validateJwtTokenWithEmptyTokenShouldReturnFalse() {
        String emptyToken = "";
        boolean result = jwtUtils.validateJwtToken(emptyToken);
        assertFalse(result);
    }

    @Test
    void validateJwtTokenWithExpiredTokenShouldReturnFalse() {
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