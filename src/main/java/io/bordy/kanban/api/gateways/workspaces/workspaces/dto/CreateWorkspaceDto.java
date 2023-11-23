package io.bordy.kanban.api.gateways.workspaces.workspaces.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CreateWorkspaceDto(
        String name
) implements Serializable /* https://github.com/quarkusio/quarkus/issues/15892 */ {
}
