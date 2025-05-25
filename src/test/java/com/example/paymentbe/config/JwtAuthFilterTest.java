package com.example.paymentbe.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthFilter jwtAuthFilter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_ValidToken_SetsAuthentication() throws Exception {
        // Arrange
        String token = "valid.jwt.token";
        String userId = "testUserId";
        
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtils.validateJwtToken(token)).thenReturn(true);
        when(jwtUtils.getUserIdFromJwtToken(token)).thenReturn(userId);

        // Act
        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        verify(jwtUtils).validateJwtToken(token);
        verify(jwtUtils).getUserIdFromJwtToken(token);
        
        // Verify that authentication was set
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(userId, SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }

    @Test
    void doFilterInternal_InvalidToken_DoesNotSetAuthentication() throws Exception {
        // Arrange
        String token = "invalid.jwt.token";
        
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtils.validateJwtToken(token)).thenReturn(false);

        // Act
        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        verify(jwtUtils).validateJwtToken(token);
        verify(jwtUtils, never()).getUserIdFromJwtToken(anyString());
        
        // Verify that no authentication was set
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_NoAuthHeader_DoesNotSetAuthentication() throws Exception {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn(null);

        // Act
        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        verify(jwtUtils, never()).validateJwtToken(anyString());
        verify(jwtUtils, never()).getUserIdFromJwtToken(anyString());
        
        // Verify that no authentication was set
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_InvalidAuthHeaderFormat_DoesNotSetAuthentication() throws Exception {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn("InvalidFormat token");

        // Act
        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        verify(jwtUtils, never()).validateJwtToken(anyString());
        verify(jwtUtils, never()).getUserIdFromJwtToken(anyString());
        
        // Verify that no authentication was set
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_TokenValidationException_ContinuesChain() throws Exception {
        // Arrange
        String token = "valid.jwt.token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtils.validateJwtToken(token)).thenThrow(new RuntimeException("Token validation failed"));

        // Act
        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        verify(jwtUtils).validateJwtToken(token);
        verify(jwtUtils, never()).getUserIdFromJwtToken(anyString());
        
        // Verify that no authentication was set
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
} 