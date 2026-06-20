package com.nexusbank.banking_service.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${nexusbank.jwt.secret}")
    private String secret_key;

    // ✅ Generate Token
    public String generateToken(String username) {

        Key key = Keys.hmacShaKeyFor(
                secret_key.getBytes(StandardCharsets.UTF_8)
        );

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(
                        new Date(
                                System.currentTimeMillis()
                                        + 1000 * 60 * 60 * 10
                        )
                )
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // ✅ Extract Username
    public String extractUsername(String token) {

        return extractClaim(
                token,
                Claims::getSubject
        );
    }

    // ✅ Extract Role
    public String extractRole(String token) {

        return extractClaim(
                token,
                claims -> claims.get(
                        "role",
                        String.class
                )
        );
    }

    // ✅ Generic Claim Extractor
    public <T> T extractClaim(
            String token,
            Function<Claims, T> claimsResolver
    ) {

        final Claims claims =
                Jwts.parserBuilder()
                        .setSigningKey(
                                secret_key.getBytes(
                                        StandardCharsets.UTF_8
                                )
                        )
                        .build()
                        .parseClaimsJws(token)
                        .getBody();

        return claimsResolver.apply(claims);
    }

    // ✅ Validate Token
    public boolean isTokenValid(String token) {

        return !isTokenExpired(token);
    }

    // ✅ Expiry Check
    private boolean isTokenExpired(String token) {

        return extractClaim(
                token,
                Claims::getExpiration
        ).before(new Date());
    }
}