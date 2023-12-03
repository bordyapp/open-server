package io.bordy.api;

import java.io.Serializable;

public record UpdateWorkspaceMemberDto(
        String name,
        String role,
        String responsibilities
) implements Serializable /* https://github.com/quarkusio/quarkus/issues/15892 */ {
}
