package io.bordy.kanban.api.gateways.workspaces.workspaces.dto;

import io.bordy.api.UserDto;
import io.bordy.kanban.workspaces.invites.WorkspaceInvite;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public record WorkspaceDto(
        UUID id,
        String name,
        String photo,
        String ownerId,
        List<UserDto> members,
        List<WorkspaceInvite> invites,
        Date createdAt,
        Date editedAt
) {
}
