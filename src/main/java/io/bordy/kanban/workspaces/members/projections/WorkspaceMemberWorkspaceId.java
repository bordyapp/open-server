package io.bordy.kanban.workspaces.members.projections;

import io.bordy.kanban.workspaces.members.WorkspaceMember;
import io.quarkus.mongodb.panache.common.ProjectionFor;

import java.util.UUID;

/**
 * Projection to extract only {@link WorkspaceMember#getWorkspaceId()} from {@link WorkspaceMember} entity.
 * @param workspaceId to extract
 *
 * @author Pavel Bodiachevskii
 * @since 1.0.0
 */
@ProjectionFor(WorkspaceMember.class)
public record WorkspaceMemberWorkspaceId(
        UUID workspaceId
) {
}