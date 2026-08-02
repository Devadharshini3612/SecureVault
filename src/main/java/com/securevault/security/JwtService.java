package com.securevault.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JwtService
 * 
 * Service for generating and validating JWT (JSON Web Tokens).
 * 
 * JWT Structure:
 * Header.Payload.Signature
 * 
 * Example JWT:
 * eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJqb2huQGV4YW1wbGUuY29tIiwiaWF0IjoxNjE2MjM5MDIyfQ.abc123...
 * 
 * What's inside:
 * - Header: Algorithm (HS256)
 * - Payload: Email, userId, expiration time
 * - Signature: Ensures token hasn't been tampered with
 */
@Service
public class JwtService {

    /**
     * Secret key for signing JWT tokens
     * 
     * IMPORTANT: In production, store this in environment variables!
     * This should be a strong, random 256-bit key.
     */
    private static final String SECRET_KEY = "5367566B59703373367639792F423F4528482B4D6251655468576D5A71347437";
    
    /**
     * Token expiration time: 24 hours (in milliseconds)
     * 1000 ms * 60 sec * 60 min * 24 hours = 86,400,000 ms
     */
    private static final long JWT_EXPIRATION = 1000 * 60 * 60 * 24;

    /**
     * Generates a JWT token for a user
     * 
     * @param email the user's email (used as subject/username)
     * @param userId the user's ID (stored in claims)
     * @return JWT token as a String
     */
    public String generateToken(String email, Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        return createToken(claims, email);
    }

    /**
     * Creates a JWT token with claims and subject
     * 
     * @param claims additional data to store in token (userId, roles, etc.)
     * @param subject the username/email
     * @return JWT token
     */
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + JWT_EXPIRATION))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Extracts the username (email) from the JWT token
     * 
     * @param token the JWT token
     * @return the username/email
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extracts the userId from the JWT token
     * 
     * @param token the JWT token
     * @return the userId
     */
    public Long extractUserId(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("userId", Long.class);
    }

    /**
     * Extracts the expiration date from the JWT token
     * 
     * @param token the JWT token
     * @return the expiration date
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extracts a specific claim from the JWT token
     * 
     * @param token the JWT token
     * @param claimsResolver function to extract the desired claim
     * @return the claim value
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extracts all claims from the JWT token
     * 
     * @param token the JWT token
     * @return all claims
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Checks if the JWT token is expired
     * 
     * @param token the JWT token
     * @return true if expired, false otherwise
     */
    public Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Validates the JWT token
     * 
     * Checks:
     * 1. Username in token matches the provided username
     * 2. Token is not expired
     * 
     * @param token the JWT token
     * @param username the username to validate against
     * @return true if valid, false otherwise
     */
    public Boolean validateToken(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return (extractedUsername.equals(username) && !isTokenExpired(token));
    }

    /**
     * Gets the signing key for JWT
     * 
     * @return the signing key
     */
    private Key getSigningKey() {
        byte[] keyBytes = hexToBytes(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Converts hex string to byte array
     * 
     * @param hex the hex string
     * @return byte array
     */
    private byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }
}
