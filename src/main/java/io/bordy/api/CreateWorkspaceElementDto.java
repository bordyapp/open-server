package io.bordy.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.bordy.kanban.workspaces.elements.WorkspaceElementType;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CreateWorkspaceElementDto(
        String name,
        String folderId,
        WorkspaceElementType type
) {
}
