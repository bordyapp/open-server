package io.bordy.api;

public record UserDto(
        String id,
        String email,
        String nickname,
        String role,
        String responsibilities,
        String picture
) {
}
