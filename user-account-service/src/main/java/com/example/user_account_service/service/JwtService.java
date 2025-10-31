package com.example.user_account_service.service;

import com.example.user_account_service.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${jwt.secret.key}") // Lấy key từ application.properties
    private String SECRET_KEY;

    // Tạo token từ thông tin User
    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        // Bạn có thể thêm bất cứ thông tin gì vào token (ví dụ: role)
        claims.put("userId", user.getUserId());
        claims.put("role", user.getRole());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getEmail()) // Dùng email làm subject
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24)) // 24 tiếng
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Lấy email (subject) từ token
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Hàm helper để lấy key bí mật
    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // Hàm helper chung để trích xuất thông tin từ token
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}