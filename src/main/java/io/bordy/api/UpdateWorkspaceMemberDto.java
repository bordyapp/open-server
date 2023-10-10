package io.bordy.api;

public record UpdateWorkspaceMemberDto(
        String name,
        String role,
        String responsibilities
) {
}
