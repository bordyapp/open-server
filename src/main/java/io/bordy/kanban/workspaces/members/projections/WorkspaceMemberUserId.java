package io.bordy.kanban.workspaces.members.projections;

import io.bordy.kanban.workspaces.members.WorkspaceMember;
import io.quarkus.mongodb.panache.common.ProjectionFor;

@ProjectionFor(WorkspaceMember.class)
public record WorkspaceMemberUserId(
        String userId
) {
}