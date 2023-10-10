package io.bordy.gateways;

public record CreateInviteDto(
        String email,
        String name,
        String role,
        String responsibilities
) {
}
