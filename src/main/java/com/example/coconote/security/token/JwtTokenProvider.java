package com.example.coconote.security.token;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final Key SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS512);  // 비밀키 생성
    private final long ACCESS_TOKEN_EXPIRATION_TIME = 15 * 60 * 1000;  // 15분
    private final long REFRESH_TOKEN_EXPIRATION_TIME = 7 * 24 * 60 * 60 * 1000;  // 7일

    public String generateAccessToken(String email) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + ACCESS_TOKEN_EXPIRATION_TIME);

        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SECRET_KEY)
                .compact();
    }

    // 리프레시 토큰 생성
    public String generateRefreshToken() {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + REFRESH_TOKEN_EXPIRATION_TIME);

        return Jwts.builder()
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SECRET_KEY)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String getEmailFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}