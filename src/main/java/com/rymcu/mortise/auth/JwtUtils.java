package com.rymcu.mortise.auth;

import com.rymcu.mortise.core.result.ResultCode;
import com.rymcu.mortise.util.SpringContextHolder;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import javax.crypto.SecretKey;
import java.util.Objects;


/**
 * Created on 2024/4/14 0:03.
 *
 * @author ronger
 * @email ronger-x@outlook.com
 * @desc : com.rymcu.mortise.auth
 */
public class JwtUtils {

    private static final TokenManager tokenManager = SpringContextHolder.getBean(TokenManager.class);

    public static SecretKey getSecretKey() {
        byte[] keyBytes = Decoders.BASE64.decode(JwtConstants.JWT_SECRET);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public static TokenModel getTokenModel(String token) {
        if (StringUtils.isNotBlank(token)) {
            token = token.replace("Bearer ", "");
            // 验证token
            Claims claims;
            try {
                // 生成密钥
                SecretKey key = JwtUtils.getSecretKey();
                claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
            } catch (final Exception e) {
                throw new BadCredentialsException(e.getMessage());
            }
            Object account = claims.getId();
            if (StringUtils.isNotBlank(Objects.toString(account, ""))) {
                TokenModel tokenModel = tokenManager.getToken(token, account.toString());
                if (Objects.nonNull(tokenModel) && tokenManager.checkToken(tokenModel)) {
                    return tokenModel;
                }
            }
        }
        throw new BadCredentialsException(ResultCode.INCORRECT_ACCOUNT_OR_PASSWORD.getMessage());
    }

    public static String generateToken(OidcUser oidcUser) {
        return tokenManager.createToken(oidcUser.getEmail());
    }
}
