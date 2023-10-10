package io.bordy.workspaces.workspaces;

import io.bordy.Shredder;
import io.bordy.api.UserDto;
import io.bordy.api.WorkspaceDto;
import io.bordy.users.User;
import io.bordy.workspaces.*;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.*;

@ApplicationScoped
public class WorkspacesService {

    @Inject
    WorkspacesRepository workspacesRepository;

    @Inject
    WorkspaceMembersRepository workspaceMembersRepository;

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
        var workspaceMembers = workspaceMembersRepository.list(
                "workspaceId",
                        workspace.getId()
                ).stream()
                .map(workspaceMember -> {
                    var user = User.<User>findById(workspaceMember.getUserId());
                    if (user == null) {
                        return null;
                    }

                    return new UserDto(
                            user.id,
                            user.email,
                            workspaceMember.getName(),
                            workspaceMember.getRole(),
                            workspaceMember.getResponsibilities(),
                            user.picture
                    );
                })
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
        return workspacesRepository.count(
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

        workspacesRepository.persist(workspace);

        return workspace;
    }

    @CheckForNull
    public Workspace find(UUID workspaceId) {
        return workspacesRepository.findById(workspaceId);
    }

    @Nonnull
    public List<Workspace> findCreatedByUser(@Nonnull String userId) {
        return workspacesRepository.list("ownerId", userId);
    }

    @Transactional
    public void rename(
            @Nonnull UUID workspaceId,
            @Nonnull String ownerId,
            @Nonnull String name
    ) {
        workspacesRepository.update(
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
        workspacesRepository.update("photo", photoUrl)
                .where("_id", workspaceId);
    }

    @Transactional
    public void delete(@Nonnull UUID workspaceId, @Nonnull String userId) {
        if (!isWorkspaceOwner(workspaceId, userId)) {
            return;
        }

        var workspace = workspacesRepository.findById(workspaceId);
        if (workspace != null) {
            shredder.deleteWorkspace(workspace);
        }
    }

}
