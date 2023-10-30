package io.bordy.kanban.workspaces.members.projections;

import io.bordy.kanban.workspaces.members.WorkspaceMember;
import io.quarkus.mongodb.panache.common.ProjectionFor;

/**
 * Projection to extract only {@link WorkspaceMember#getUserId()} from {@link WorkspaceMember} entity.
 * @param userId to extract
 *
 * @author Pavel Bodiachevskii
 * @since 1.0.0
 */
@ProjectionFor(WorkspaceMember.class)
public record WorkspaceMemberUserId(
        String userId
) {
}