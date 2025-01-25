package com.tutorconnect.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.Claims;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    // Using a static key for consistent signing/verification
    private static final String SECRET = "5uper$ecure123!StaticK3y@2024#Strong"; // Use environment variable in production
    private final Key SECRET_KEY = new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), SignatureAlgorithm.HS256.getJcaName());

    // Generate a token for a given username (or email)
    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // 10 hours expiration
                .signWith(SECRET_KEY)
                .compact();
    }

    // Extract username from the token
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    // Check if the token is valid
    public boolean isTokenValid(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return (extractedUsername != null && extractedUsername.equals(username) && !isTokenExpired(token));
    }

    // Check if the token is expired
    private boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    // Extract all claims from the token with improved error logging
    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder().setSigningKey(SECRET_KEY).build().parseClaimsJws(token).getBody();
        } catch (Exception ex) {
            System.err.println("Error during JWT parsing: " + ex.getMessage());
            ex.printStackTrace();
            throw new RuntimeException("Invalid or expired token");
        }
    }
}
