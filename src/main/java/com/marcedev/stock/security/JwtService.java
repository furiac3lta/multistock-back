package com.marcedev.stock.security;

import com.marcedev.stock.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.List;

@Service
public class JwtService {

    private final Key key;

    public JwtService(@Value("${jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    // ==========================================
    //           GENERAR TOKEN CON ROLES
    // ==========================================
    public String generateToken(User user) {

        // Lista de roles en formato válido para Spring Security
        List<String> roles = user.getRoles().stream()
                .map(r -> "ROLE_" + r.getName()) // ROLE_ADMIN, ROLE_USER
                .toList();

        return Jwts.builder()
                .setSubject(user.getUsername())
                .claim("roles", roles)
                .setIssuedAt(new Date())
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // ==========================================
    //           EXTRAER CLAIMS DEL TOKEN
    // ==========================================
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // ==========================================
    //          EXTRAER USERNAME
    // ==========================================
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    // ==========================================
    //          EXTRAER ROLES DEL TOKEN
    // ==========================================
    public List<String> extractRoles(String token) {
        return extractAllClaims(token).get("roles", List.class);
    }
}
