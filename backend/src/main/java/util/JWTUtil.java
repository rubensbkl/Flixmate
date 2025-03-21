package util;

import java.util.Date;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;

import io.github.cdimascio.dotenv.Dotenv;

public class JWTUtil {
    private static final Dotenv dotenv = Dotenv.load(); // Carrega do .env
    private static final String SECRET = dotenv.get("JWT_SECRET");
    private static final Algorithm algorithm = Algorithm.HMAC256(SECRET);
    private static final long EXPIRATION = 1000 * 60 * 60 * 24; // 24 horas

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
