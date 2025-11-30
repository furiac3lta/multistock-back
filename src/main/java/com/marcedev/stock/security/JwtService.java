package com.marcedev.stock.security;

import com.marcedev.stock.entity.Role;
import com.marcedev.stock.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class JwtService {

    private final Key key = Keys.hmacShaKeyFor(
            "miClaveSecretaSuperLargaParaJWT1234567890".getBytes()
    );

    // ========================================
    // GENERAR TOKEN
    // ========================================
    public String generateToken(User user) {

        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", user.getRoles().stream()
                .map(r -> "ROLE_" + r.getName())
                .toList());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24))
                .signWith(key)
                .compact();
    }

    // ========================================
    // EXTRAER USERNAME
    // ========================================
    public String extractUsername(String token) {
        try {
            return getAllClaims(token).getSubject();
        } catch (Exception e) {
            return null;
        }
    }


    // ========================================
    // EXTRAER ROLES (BLINDADO)
    // ========================================
    public List<String> extractRoles(String token) {
        try {
            Object rolesObj = getAllClaims(token).get("roles");
            if (rolesObj instanceof List<?> list) {
                return list.stream().map(String::valueOf).toList();
            }
            return List.of();
        } catch (Exception e) {
            return List.of();
        }
    }

    // ========================================
    // HELPERS
    // ========================================
    private Claims getAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
