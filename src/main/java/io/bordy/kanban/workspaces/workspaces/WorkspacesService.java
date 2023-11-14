package io.bordy.kanban.workspaces.workspaces;

import io.bordy.Shredder;
import io.bordy.kanban.api.gateways.workspaces.workspaces.dto.WorkspaceDto;
import io.bordy.kanban.workspaces.invites.WorkspaceInviteStatus;
import io.bordy.kanban.workspaces.invites.WorkspaceInvitesRepository;
import io.bordy.kanban.workspaces.members.WorkspaceMembersService;
import io.bordy.users.UsersService;

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

    /**
     * Is given user owner of given {@link Workspace}
     *
     * @param workspaceId workspace to check
     * @param userId user to check
     * @return is given user owner of given {@link Workspace}
     */
    public boolean isWorkspaceOwner(@Nonnull UUID workspaceId, @Nonnull String userId) {
        return Workspace.count(
                "_id = ?1 and ownerId = ?2",
                workspaceId,
                userId
        ) > 0;
    }

    /**
     * Create and bind workspace to given user.
     *
     * @param name workspace name
     * @param photo workspace photo
     * @param ownerId workspace owner
     * @return created workspace
     */
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

    /**
     * Return {@link Workspace} with given id.
     *
     * @param workspaceId workspace id to find for
     * @return {@link Workspace} with given id
     */
    @CheckForNull
    public Workspace find(UUID workspaceId) {
        return Workspace.findById(workspaceId);
    }

    /**
     * Return list of {@link Workspace} created by given user.
     *
     * @param userId user to check for
     * @return list of {@link Workspace} created by given user
     */
    @Nonnull
    public List<Workspace> findCreatedByUser(@Nonnull String userId) {
        return Workspace.list("ownerId", userId);
    }

    /**
     * Rename given {@link Workspace}.
     * @param workspaceId workspace to rename
     * @param ownerId workspace owner
     * @param name new workspace name
     */
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

    /**
     * Update photo of given {@link Workspace}.
     *
     * @param workspaceId workspace to update
     * @param photoUrl new photo url
     */
    @Transactional
    public void uploadPhoto(
            @Nonnull UUID workspaceId,
            @Nonnull String photoUrl
    ) {
        Workspace.update("photo", photoUrl)
                .where("_id", workspaceId);
    }

    /**
     * Delete given {@link Workspace}.
     *
     * @param workspaceId workspace to delete
     * @param userId workspace owner
     */
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
