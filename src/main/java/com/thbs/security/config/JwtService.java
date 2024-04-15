package com.thbs.security.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.thbs.security.user.User;

import org.springframework.context.annotation.PropertySource;

@Service
@PropertySource("classpath:application.yml")
public class JwtService {

    public final Key secretKey;
    public final long jwtExpiration;

    public JwtService(@Value("${application.security.jwt.expiration}") long jwtExpiration) {
        this.secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        this.jwtExpiration = jwtExpiration;
    }

    // Method to extract username from JWT token
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Method to extract specific claim from JWT token
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Method to generate JWT token
    public String generateToken(UserDetails userDetails) {    User user = (User) userDetails; 
     Map<String, Object> extraClaims = new HashMap<>();    extraClaims.put(
    "firstName", user.getFirstname());                extraClaims.put(
    "role", user.getRole().toString());        
 return generateToken(extraClaims, userDetails);     }

    // Method to generate JWT token with extra claims
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return buildToken(extraClaims, userDetails, jwtExpiration);
    }

    // Method to build JWT token
    private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails, long expiration) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

// Method to check if JWT token is expired
private boolean isTokenExpired(String token) {
    Date expirationDate = extractExpiration(token);
    return expirationDate != null && expirationDate.before(new Date());
}

// Method to validate JWT token
public boolean isTokenValid(String token, UserDetails userDetails) {
    final String username = extractUsername(token);
    return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
}



    // Method to extract token expiration date from JWT token
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Method to extract all claims from JWT token
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
