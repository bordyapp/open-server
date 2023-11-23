package io.bordy;

import io.smallrye.jwt.build.Jwt;
import org.junit.jupiter.params.provider.Arguments;

import java.util.stream.Stream;

public abstract class ApiTestUtils {

    public static Stream<Arguments> brokenTokens() {
        return Stream.of(
                Arguments.of(""),
                Arguments.of("Bearer"),
                Arguments.of("Bearer <token>"),
                Arguments.of("Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c")
        );
    }

    public static String getAccessToken(String id) {
        return Jwt.subject(id)
                .issuer("https://server.example.com")
                .audience("https://service.example.com")
                .sign();
    }

}
