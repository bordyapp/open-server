package io.bordy.kanban.workspaces.workspaces;

import io.bordy.Shredder;
import io.bordy.api.WorkspaceDto;
import io.bordy.kanban.workspaces.members.WorkspaceMembersService;
import io.bordy.users.UsersService;
import io.bordy.workspaces.*;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.*;

/**
 * @author Pavel Bodiachevskii
 * @since 1.0.0
 */
@ApplicationScoped
public class WorkspacesService {

    @Inject
    WorkspaceMembersService workspaceMembersService;

    @Inject
    UsersService usersService;

    @Inject
    WorkspaceInvitesRepository workspaceInvitesRepository;

    @Inject
    Shredder shredder;

    @Nonnull
    public WorkspaceDto toBaseDto(@Nonnull Workspace workspace) {
        return new WorkspaceDto(
                workspace.getId(),
                workspace.getName(),
                workspace.getPhoto(),
                workspace.getOwnerId(),
                Collections.emptyList(),
                Collections.emptyList(),
                workspace.getCreatedAt(),
                workspace.getEditedAt()
        );
    }

    @Nonnull
    public WorkspaceDto toDto(@Nonnull Workspace workspace) {
        var workspaceMembers = workspaceMembersService.membersOf(workspace.getId())
                .stream()
                .map(workspaceMember -> usersService.asUser(workspaceMember))
                .filter(Objects::nonNull)
                .toList();
        var workspaceInvites = workspaceInvitesRepository.list(
                "workspaceId = ?1 and status = ?2",
                workspace.getId(),
                WorkspaceInviteStatus.PENDING
        );

        return new WorkspaceDto(
                workspace.getId(),
                workspace.getName(),
                workspace.getPhoto(),
                workspace.getOwnerId(),
                workspaceMembers,
                workspaceInvites,
                workspace.getCreatedAt(),
                workspace.getEditedAt()
        );
    }

    public boolean isWorkspaceOwner(@Nonnull UUID workspaceId, @Nonnull String userId) {
        return Workspace.count(
                "_id = ?1 and ownerId = ?2",
                workspaceId,
                userId
        ) > 0;
    }

    @Nonnull
    @Transactional
    public Workspace create(
            @Nonnull String name,
            @Nonnull String photo,
            @Nonnull String ownerId
    ) {
        var creationDate = new Date();

        var workspace = new Workspace(
                UUID.randomUUID(),
                name,
                photo,
                ownerId,
                creationDate,
                creationDate
        );

        Workspace.persist(workspace);

        return workspace;
    }

    @CheckForNull
    public Workspace find(UUID workspaceId) {
        return Workspace.findById(workspaceId);
    }

    @Nonnull
    public List<Workspace> findCreatedByUser(@Nonnull String userId) {
        return Workspace.list("ownerId", userId);
    }

    @Transactional
    public void rename(
            @Nonnull UUID workspaceId,
            @Nonnull String ownerId,
            @Nonnull String name
    ) {
        Workspace.update(
                        "name = ?1 and editedAt = ?2",
                        name,
                        new Date()
                )
                .where(
                        "_id = ?1 and ownerId = ?2",
                        workspaceId,
                        ownerId
                );
    }

    @Transactional
    public void uploadPhoto(
            @Nonnull UUID workspaceId,
            @Nonnull String photoUrl
    ) {
        Workspace.update("photo", photoUrl)
                .where("_id", workspaceId);
    }

    @Transactional
    public void delete(@Nonnull UUID workspaceId, @Nonnull String userId) {
        if (!isWorkspaceOwner(workspaceId, userId)) {
            return;
        }

        var workspace = Workspace.<Workspace>findById(workspaceId);
        if (workspace != null) {
            shredder.deleteWorkspace(workspace);
        }
    }

}
