package com.rymcu.mortise;

import com.rymcu.mortise.auth.JwtUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.crypto.SecretKey;
import javax.security.auth.login.CredentialException;
import java.util.Date;

@SpringBootTest
class MortiseApplicationTests {

    @Test
    void contextLoads() throws CredentialException {
        SecretKey key = JwtUtils.getSecretKey();
        String id = "rymcu";
        String token = Jwts.builder().id(id).subject(id).issuedAt(new Date()).signWith(key).compact();
        Claims claims;
        try {
            // 生成密钥
            claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
        } catch (final Exception e) {
            throw new CredentialException("凭证异常");
        }
        Object account = claims.getId();
        System.out.println(account);
    }

}
