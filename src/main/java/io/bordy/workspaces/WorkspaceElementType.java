package io.bordy.workspaces;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum WorkspaceElementType {
    @JsonProperty("folder")
    FOLDER,
    @JsonProperty("board")
    BOARD
}
