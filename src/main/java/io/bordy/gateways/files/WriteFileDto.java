package io.bordy.gateways.files;

import io.bordy.files.FileType;

public record WriteFileDto(
        String name,
        FileType type,
        String url
) {
}
