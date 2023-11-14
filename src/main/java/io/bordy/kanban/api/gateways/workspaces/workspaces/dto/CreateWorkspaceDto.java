package io.bordy.kanban.api.gateways.workspaces.workspaces.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CreateWorkspaceDto(
        String name
) {
}
