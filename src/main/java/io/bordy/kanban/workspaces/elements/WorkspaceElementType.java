package io.bordy.kanban.workspaces.elements;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum WorkspaceElementType {
    @JsonProperty("folder")
    FOLDER,
    @JsonProperty("board")
    BOARD
}
