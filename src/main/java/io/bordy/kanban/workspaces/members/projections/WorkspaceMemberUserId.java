package io.bordy.kanban.workspaces.members.projections;

import io.bordy.kanban.workspaces.members.WorkspaceMember;
import io.quarkus.mongodb.panache.common.ProjectionFor;

import java.util.UUID;

@ProjectionFor(WorkspaceMember.class)
public record WorkspaceMemberUserId(
        String userId
) {
}