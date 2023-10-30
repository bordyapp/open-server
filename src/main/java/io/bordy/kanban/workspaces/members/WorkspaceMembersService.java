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

    /**
     * Check is given user is member of given workspace or not.
     *
     * @param workspaceId workspace to check
     * @param userId user to check
     * @return is given user is member of given workspace or not
     */
    public boolean isMemberOf(@Nonnull UUID workspaceId, @Nonnull String userId) {
        return WorkspaceMember.count(
                "workspaceId = ?1 and userId = ?2",
                workspaceId,
                userId
        ) > 0;
    }

    /**
     * Get all members of given workspace.
     *
     * @param workspaceId workspace to check
     * @return all members of given workspace if any
     */
    @Nonnull
    public List<WorkspaceMember> membersOf(@Nonnull UUID workspaceId) {
        return WorkspaceMember.list("workspaceId", workspaceId);
    }

    /**
     * Get list of {@link WorkspaceMember#getUserId()} of members of given workspace.
     *
     * @param workspaceId workspace to check
     * @return list of {@link WorkspaceMember#getUserId()} of members of given workspace if any
     */
    @Nonnull
    public List<String> memberUserIdsOf(@Nonnull UUID workspaceId) {
        return WorkspaceMember.find("workspaceId", workspaceId)
                .project(WorkspaceMemberUserId.class).stream()
                .map(WorkspaceMemberUserId::userId)
                .toList();
    }

    /**
     * Get list of {@link WorkspaceMember#getWorkspaceId()} where given user is member.
     *
     * @param userId user to search
     * @return list of {@link WorkspaceMember#getWorkspaceId()} if any
     */
    @Nonnull
    public List<UUID> memberOf(@Nonnull String userId) {
        return WorkspaceMember.find("userId", userId)
                .project(WorkspaceMemberWorkspaceId.class).stream()
                .map(WorkspaceMemberWorkspaceId::workspaceId)
                .toList();
    }

    /**
     * Add given user to workspace with given {@link WorkspaceInvite}
     *
     * @param userId user to add
     * @param invite invite to use
     * @return user as workspace member
     */
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

    /**
     * Get user as {@link WorkspaceMember} where he is member.
     *
     * @param userId user to get
     * @return user as {@link WorkspaceMember} if he is member
     */
    @CheckForNull
    public WorkspaceMember find(@Nonnull String userId) {
        return WorkspaceMember.<WorkspaceMember>list("userId", userId).stream()
                .findFirst()
                .orElse(null);
    }

    /**
     * Update user as {@link WorkspaceMember} where he is member.
     *
     * @param workspaceId workspace where user is member of
     * @param memberId user member id
     * @param updateWorkspaceMemberDto changes to apply
     */
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

    /**
     * Delete user from workspace where he is member.
     *
     * @param workspaceId workspace where user is member of
     * @param memberId user member id
     */
    @Transactional
    public void delete(@Nonnull String workspaceId, @Nonnull String memberId) {
        WorkspaceMember.delete(
                "userId = ?1 and workspaceId = ?2",
                memberId,
                UUID.fromString(workspaceId)
        );
    }

    /**
     * Delete all members from given workspace.
     *
     * @param workspaceId workspace to clear from member
     */
    @Transactional
    public void deleteAll(@Nonnull UUID workspaceId) {
        WorkspaceMember.delete("workspaceId = ?1", workspaceId);
    }

}
