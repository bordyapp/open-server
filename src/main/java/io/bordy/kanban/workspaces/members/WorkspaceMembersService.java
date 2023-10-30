package io.bordy.kanban.workspaces.members;

import io.bordy.api.UpdateWorkspaceMemberDto;
import io.bordy.kanban.workspaces.members.projections.WorkspaceMemberUserId;
import io.bordy.kanban.workspaces.members.projections.WorkspaceMemberWorkspaceId;
import io.bordy.workspaces.WorkspaceInvite;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * @author Pavel Bodiachevskii
 * @since 1.0.0
 */
@ApplicationScoped
public class WorkspaceMembersService {

    public boolean isMemberOf(@Nonnull UUID workspaceId, @Nonnull String userId) {
        return WorkspaceMember.count(
                "workspaceId = ?1 and userId = ?2",
                workspaceId,
                userId
        ) > 0;
    }

    @Nonnull
    public List<WorkspaceMember> membersOf(@Nonnull UUID workspaceId) {
        return WorkspaceMember.list("workspaceId", workspaceId);
    }

    @Nonnull
    public List<String> memberUserIdsOf(@Nonnull UUID workspaceId) {
        return WorkspaceMember.find("workspaceId", workspaceId)
                .project(WorkspaceMemberUserId.class).stream()
                .map(WorkspaceMemberUserId::userId)
                .toList();
    }

    /**
     * Return workspace ids where given user is member
     * @param userId user to search
     * @return list of ids
     */
    @Nonnull
    public List<UUID> memberOf(@Nonnull String userId) {
        return WorkspaceMember.find("userId", userId)
                .project(WorkspaceMemberWorkspaceId.class).stream()
                .map(WorkspaceMemberWorkspaceId::workspaceId)
                .toList();
    }

    @Nonnull
    @Transactional
    public WorkspaceMember create(@Nonnull String userId, @Nonnull WorkspaceInvite invite) {
        var createdAt = new Date();

        var workspaceMember = new WorkspaceMember(
                UUID.randomUUID(),
                invite.getWorkspaceId(),
                userId,
                invite.getName(),
                invite.getRole(),
                invite.getResponsibilities(),
                createdAt,
                createdAt
        );
        workspaceMember.persist();

        return workspaceMember;
    }

    @CheckForNull
    public WorkspaceMember find(@Nonnull String userId) {
        return WorkspaceMember.<WorkspaceMember>list("userId", userId).stream()
                .findFirst()
                .orElse(null);
    }

    @Transactional
    public void update(
            @Nonnull String workspaceId,
            @Nonnull String memberId,
            @Nonnull UpdateWorkspaceMemberDto updateWorkspaceMemberDto
    ) {
        WorkspaceMember.update(
                "name = ?1 and role = ?2 and responsibilities = ?3",
                updateWorkspaceMemberDto.name(),
                updateWorkspaceMemberDto.role(),
                updateWorkspaceMemberDto.responsibilities()
        ).where(
                "userId = ?1 and workspaceId = ?2",
                memberId,
                UUID.fromString(workspaceId)
        );
    }

    @Transactional
    public void delete(@Nonnull String workspaceId, @Nonnull String memberId) {
        WorkspaceMember.delete(
                "userId = ?1 and workspaceId = ?2",
                memberId,
                UUID.fromString(workspaceId)
        );
    }

    @Transactional
    public void deleteAll(@Nonnull UUID workspaceId) {
        WorkspaceMember.delete("workspaceId = ?1", workspaceId);
    }

}
