package io.bordy.api;

import java.util.UUID;

public record SharingDto(
        UUID id,
        boolean isActive

) {
}
