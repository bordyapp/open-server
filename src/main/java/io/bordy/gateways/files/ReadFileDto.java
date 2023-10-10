package io.bordy.gateways.files;

import io.bordy.files.FileType;

import java.util.Date;
import java.util.UUID;

public record ReadFileDto(
        UUID id,
        String name,
        UUID workspaceId,
        UUID folderId,
        FileType type,
        String url,
        String creatorUserId,
        Date createdAt,
        Date editedAt
) {
}
