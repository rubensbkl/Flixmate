package util;

import java.util.Date;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;

public class JWTUtil {
    private String SECRET = null;
    private static Algorithm algorithm;
    private static final long EXPIRATION = 1000 * 60 * 60 * 24; // 24 horas

    public JWTUtil(String secret) {
        if (secret != null) {
            this.SECRET = secret;
        } else if (secret.isEmpty()) {
            throw new IllegalArgumentException("Secret cannot be null");
        } else {
            throw new IllegalArgumentException("Secret cannot be empty");
        }
        algorithm = Algorithm.HMAC256(SECRET);
    }

    public static String generateToken(String email) {
        return JWT.create()
            .withSubject(email)
            .withIssuer("cinematch")
            .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION))
            .sign(algorithm);
    }

    public static DecodedJWT verifyToken(String token) {
        JWTVerifier verifier = JWT.require(algorithm)
            .withIssuer("cinematch")
            .build();
        return verifier.verify(token);
    }
}
