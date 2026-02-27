package com.login.server.login.global.member.jwt;

import java.security.Key;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtTokenProvider {

    private final Key key;
    private final long accessTokenValidity;
    private final long refreshTokenValidity;

    public JwtTokenProvider(
            @Value("${jwt.token.secretKey}") String secret,
            @Value("${jwt.token.expiration.access}") long accessTokenValidity,
            @Value("${jwt.token.expiration.refresh}") long refreshTokenValidity) {

        byte[] keyBytes = io.jsonwebtoken.io.Decoders.BASE64.decode(secret);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.accessTokenValidity = accessTokenValidity;
        this.refreshTokenValidity =refreshTokenValidity;
    }

    public String generateAccessToken(String memberId) {
        return generateToken(memberId, accessTokenValidity);
    }

    public String generateRefreshToken() {
        return generateToken(null, refreshTokenValidity);
    }

    public String generateEmailToken(String email) {
        return generateToken(email, 180000);
    }

    public String generateTokenForPwd(String memberId) {
        return generateToken(memberId, 900000);
    }

    public String generateToken(String memberId, long validity) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + validity);

        JwtBuilder builder = Jwts.builder()
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key,SignatureAlgorithm.HS256);

        if(memberId != null) {
            builder.setSubject(memberId);
        }
        return builder.compact();
    }

    public boolean validateToken(String token) {
        try{
            getClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String getMemberIdFromToken(String token) {
            return Jwts.parser()
                    .verifyWith((SecretKey) key) // setSigningKey 대신 verifyWith 사용
                    .build()
                    .parseSignedClaims(token)    // parseClaimsJws 대신 parseSignedClaims 사용
                    .getPayload()                // getBody 대신 getPayload 사용
                    .getSubject();
        }

        private Claims getClaims(String token) {
            return Jwts.parser()
                    .verifyWith((SecretKey) key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        }
}
