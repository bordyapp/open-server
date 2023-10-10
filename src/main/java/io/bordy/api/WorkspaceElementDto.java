package io.bordy.api;

import io.bordy.workspaces.WorkspaceElementType;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public record WorkspaceElementDto(
        UUID id,
        String name,
        UUID workspaceId,
        UUID folderId,
        List<WorkspaceElementDto> boards,
        List<BoardListDto> lists,
        SharingDto sharing,
        WorkspaceElementType type,
        Date createdAt,
        Date editedAt
) {
}
