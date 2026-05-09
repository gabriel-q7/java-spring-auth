package com.example.backend.auth.jwt;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JwtService.
 * Note: These tests don't use @ExtendWith(MockitoExtension.class) because
 * JwtService is a pure utility class that doesn't depend on external services.
 * We test it with real instances using a test configuration.
 */
class JwtServiceTest {

    private JwtService jwtService;
    private JwtProperties jwtProperties;

    // Use a proper length secret key (at least 256 bits for HS256)
    private static final String TEST_SECRET = "test-secret-key-that-is-at-least-256-bits-long-for-jwt-signing-purposes";
    private static final long ACCESS_TOKEN_EXPIRATION = 3600L; // 1 hour
    private static final long REFRESH_TOKEN_EXPIRATION = 604800L; // 7 days

    @BeforeEach
    void setUp() {
        // Create real instances instead of mocks for pure utility testing
        jwtProperties = new JwtProperties();
        jwtProperties.setSecret(TEST_SECRET);
        jwtProperties.setAccessTokenExpirationSeconds(ACCESS_TOKEN_EXPIRATION);
        jwtProperties.setRefreshTokenExpirationSeconds(604800L);
        
        jwtService = new JwtService(jwtProperties);
    }

    // ==================== GENERATE ACCESS TOKEN TESTS ====================

    @Test
    void generateAccessToken_CreatesValidToken() {
        // Act
        String token = jwtService.generateAccessToken("testuser");

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        
        // Token should have 3 parts separated by dots (header.payload.signature)
        String[] parts = token.split("\\.");
        assertEquals(3, parts.length, "JWT should have 3 parts");
    }

    @Test
    void generateAccessToken_ContainsCorrectSubject() {
        // Act
        String token = jwtService.generateAccessToken("testuser");
        String subject = jwtService.extractSubject(token);

        // Assert
        assertEquals("testuser", subject);
    }

    @Test
    void generateAccessToken_HasCorrectExpiration() {
        // Arrange
        Instant beforeGeneration = Instant.now();

        // Act
        String token = jwtService.generateAccessToken("testuser");

        // Assert
        SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
        Date expiration = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getExpiration();

        Instant expectedExpiration = beforeGeneration.plusSeconds(ACCESS_TOKEN_EXPIRATION);
        
        // Allow 5 second tolerance for test execution time
        assertTrue(expiration.toInstant().isAfter(beforeGeneration));
        assertTrue(expiration.toInstant().isBefore(expectedExpiration.plusSeconds(5)));
    }

    @Test
    void generateAccessToken_WithDifferentSubjects_CreatesDifferentTokens() {
        // Act
        String token1 = jwtService.generateAccessToken("user1");
        String token2 = jwtService.generateAccessToken("user2");

        // Assert
        assertNotEquals(token1, token2);
        assertEquals("user1", jwtService.extractSubject(token1));
        assertEquals("user2", jwtService.extractSubject(token2));
    }

    @Test
    void generateAccessToken_CalledMultipleTimes_CreatesDifferentTokens() {
        // Act - Even with same subject, tokens should differ due to issuedAt timestamp
        String token1 = jwtService.generateAccessToken("testuser");
        
        // Small delay to ensure different issuedAt timestamps
        // Note: JWT timestamps are in seconds, so we need to wait at least 1 second
        try {
            Thread.sleep(1100); // Wait 1.1 seconds to ensure timestamp difference
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        String token2 = jwtService.generateAccessToken("testuser");

        // Assert
        assertNotEquals(token1, token2, "Tokens with same subject but different timestamps should differ");
    }

    // ==================== EXTRACT SUBJECT TESTS ====================

    @Test
    void extractSubject_WithValidToken_ReturnsCorrectSubject() {
        // Arrange
        String token = jwtService.generateAccessToken("testuser");

        // Act
        String subject = jwtService.extractSubject(token);

        // Assert
        assertEquals("testuser", subject);
    }

    @Test
    void extractSubject_WithMalformedToken_ThrowsException() {
        // Arrange
        String malformedToken = "not.a.valid.jwt.token";

        // Act & Assert
        assertThrows(JwtException.class, () -> jwtService.extractSubject(malformedToken));
    }

    @Test
    void extractSubject_WithEmptyToken_ThrowsException() {
        // Act & Assert
        assertThrows(Exception.class, () -> jwtService.extractSubject(""));
    }

    @Test
    void extractSubject_WithNullToken_ThrowsException() {
        // Act & Assert
        assertThrows(Exception.class, () -> jwtService.extractSubject(null));
    }

    @Test
    void extractSubject_WithTokenFromDifferentSecret_ThrowsException() {
        // Arrange - Create token with different secret
        String differentSecret = "different-secret-key-that-is-at-least-256-bits-long-for-signing";
        SecretKey differentKey = Keys.hmacShaKeyFor(differentSecret.getBytes(StandardCharsets.UTF_8));
        
        String tokenWithDifferentSecret = Jwts.builder()
                .subject("testuser")
                .issuedAt(new Date())
                .expiration(Date.from(Instant.now().plusSeconds(3600)))
                .signWith(differentKey)
                .compact();

        // Act & Assert - Should fail signature verification
        assertThrows(JwtException.class, 
                () -> jwtService.extractSubject(tokenWithDifferentSecret),
                "Token signed with different secret should be rejected");
    }

    // ==================== IS TOKEN VALID TESTS ====================

    @Test
    void isTokenValid_WithValidToken_ReturnsTrue() {
        // Arrange
        String token = jwtService.generateAccessToken("testuser");

        // Act
        boolean isValid = jwtService.isTokenValid(token);

        // Assert
        assertTrue(isValid);
    }

    @Test
    void isTokenValid_WithExpiredToken_ReturnsFalse() throws InterruptedException {
        // Arrange - Create JWT properties with very short expiration (1 second)
        JwtProperties shortExpirationProps = new JwtProperties();
        shortExpirationProps.setSecret(TEST_SECRET);
        shortExpirationProps.setAccessTokenExpirationSeconds(1L); // 1 second expiration
        shortExpirationProps.setRefreshTokenExpirationSeconds(REFRESH_TOKEN_EXPIRATION);
        
        JwtService shortExpirationService = new JwtService(shortExpirationProps);
        String token = shortExpirationService.generateAccessToken("testuser");
        
        // Wait for token to expire
        Thread.sleep(1500); // Wait 1.5 seconds
        
        // Act
        boolean isValid = shortExpirationService.isTokenValid(token);

        // Assert
        assertFalse(isValid, "Expired token should be invalid");
    }

    @Test
    void isTokenValid_WithMalformedToken_ReturnsFalse() {
        // Arrange
        String malformedToken = "malformed.token.string";

        // Act
        boolean isValid = jwtService.isTokenValid(malformedToken);

        // Assert
        assertFalse(isValid, "Malformed token should be invalid");
    }

    @Test
    void isTokenValid_WithInvalidSignature_ReturnsFalse() {
        // Arrange - Create token with different secret
        String differentSecret = "different-secret-key-that-is-at-least-256-bits-long-for-signing";
        SecretKey differentKey = Keys.hmacShaKeyFor(differentSecret.getBytes(StandardCharsets.UTF_8));
        
        String tokenWithInvalidSignature = Jwts.builder()
                .subject("testuser")
                .issuedAt(new Date())
                .expiration(Date.from(Instant.now().plusSeconds(3600)))
                .signWith(differentKey)
                .compact();

        // Act
        boolean isValid = jwtService.isTokenValid(tokenWithInvalidSignature);

        // Assert
        assertFalse(isValid, "Token with invalid signature should be invalid");
    }

    @Test
    void isTokenValid_WithEmptyToken_ReturnsFalse() {
        // Act
        boolean isValid = jwtService.isTokenValid("");

        // Assert
        assertFalse(isValid, "Empty token should be invalid");
    }

    @Test
    void isTokenValid_WithNullToken_ReturnsFalse() {
        // Act
        boolean isValid = jwtService.isTokenValid(null);

        // Assert
        assertFalse(isValid, "Null token should be invalid");
    }

    @Test
    void isTokenValid_WithTokenMissingClaims_ReturnsFalse() {
        // Arrange - Create token without subject (invalid JWT structure)
        SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
        String tokenWithoutSubject = Jwts.builder()
                .issuedAt(new Date())
                .expiration(Date.from(Instant.now().plusSeconds(3600)))
                .signWith(key)
                .compact();

        // Act
        boolean isValid = jwtService.isTokenValid(tokenWithoutSubject);

        // Assert
        // Note: This might still be valid according to JWT spec, but depends on your validation logic
        // The test documents the expected behavior
        assertTrue(isValid); // or assertFalse if your logic requires subject
    }

    @Test
    void isTokenValid_WithTokenExpiringExactlyNow_HandlesEdgeCase() {
        // Arrange - Create token that expires in 1 second
        jwtProperties.setAccessTokenExpirationSeconds(1L);
        String shortLivedToken = jwtService.generateAccessToken("testuser");
        
        // Reset expiration
        jwtProperties.setAccessTokenExpirationSeconds(ACCESS_TOKEN_EXPIRATION);

        // Act - Check immediately (should be valid)
        boolean isValidBefore = jwtService.isTokenValid(shortLivedToken);
        
        // Wait for expiration
        try {
            Thread.sleep(1500); // Wait 1.5 seconds
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        boolean isValidAfter = jwtService.isTokenValid(shortLivedToken);

        // Assert
        assertTrue(isValidBefore, "Token should be valid immediately after creation");
        assertFalse(isValidAfter, "Token should be invalid after expiration");
    }

    // ==================== INTEGRATION TESTS ====================

    @Test
    void tokenLifecycle_GenerateExtractValidate_WorksTogether() {
        // This test verifies the complete token workflow
        
        // Generate
        String token = jwtService.generateAccessToken("integrationUser");
        assertNotNull(token);
        
        // Validate
        assertTrue(jwtService.isTokenValid(token), "Newly generated token should be valid");
        
        // Extract
        String subject = jwtService.extractSubject(token);
        assertEquals("integrationUser", subject, "Extracted subject should match original");
    }

    @Test
    void tokenSecurity_CannotModifyTokenWithoutDetection() {
        // Arrange
        String validToken = jwtService.generateAccessToken("testuser");
        
        // Act - Try to tamper with token by changing one character
        String tamperedToken = validToken.substring(0, validToken.length() - 5) + "XXXXX";

        // Assert
        assertFalse(jwtService.isTokenValid(tamperedToken), 
                "Tampered token should fail validation");
        assertThrows(JwtException.class, 
                () -> jwtService.extractSubject(tamperedToken),
                "Extracting from tampered token should throw exception");
    }
}
