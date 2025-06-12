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

    // JavaDoc
    
    public JWTUtil(String secret) {
        if (secret == null || secret.isEmpty()) {
            throw new IllegalArgumentException("Secret cannot be null or empty");
        } else {
            this.SECRET = secret;
        }
        algorithm = Algorithm.HMAC256(SECRET);
    }

    public String generateToken(String email, int id) {
        return JWT.create()
                .withSubject(email)
                .withClaim("userId", id)
                .withIssuer("cinematch")
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION))
                .sign(algorithm);
    }

    public DecodedJWT verifyToken(String token) {
        JWTVerifier verifier = JWT.require(algorithm)
                .withIssuer("cinematch")
                .build();
        return verifier.verify(token);
    }

    // Get the userId from the token
    public int getUserId(String token) {
        DecodedJWT decodedJWT = verifyToken(token);
        return decodedJWT.getClaim("userId").asInt();
    }

  

    // Get the email from the token
    public String getEmail(String token) {
        DecodedJWT decodedJWT = verifyToken(token);
        return decodedJWT.getSubject();
    }

    // Check if the token is expired
    public boolean isTokenExpired(String token) {
        DecodedJWT decodedJWT = verifyToken(token);
        return decodedJWT.getExpiresAt().before(new Date());
    }

}
