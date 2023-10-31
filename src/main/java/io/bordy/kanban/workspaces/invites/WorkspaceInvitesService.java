package io.bordy.kanban.workspaces.invites;

import io.bordy.mail.Postman;
import io.bordy.users.User;
import io.bordy.kanban.workspaces.workspaces.WorkspacesService;
import org.jboss.logmanager.Level;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Logger;

@ApplicationScoped
public class WorkspaceInvitesService {

    private final static Logger logger = Logger.getLogger(WorkspaceInvitesService.class.getName());

    @Inject
    WorkspaceInvitesRepository workspaceInvitesRepository;

    @Inject
    WorkspacesService workspacesService;

    @Inject
    Postman postman;

    @CheckForNull
    public WorkspaceInvite find(
            @Nonnull UUID workspaceId,
            @Nonnull String email
    ) {
        return workspaceInvitesRepository.find(
                "workspaceId = ?1 and email = ?2 and status in [PENDING, ACCEPTED]",
                workspaceId,
                email
        ).firstResult();
    }

    @CheckForNull
    @Transactional
    public WorkspaceInvite create(
            @Nonnull UUID workspaceId,
            @Nonnull String email,
            @Nonnull String name,
            @Nonnull String role,
            @Nonnull String responsibilities
    ) {
        var workspace = workspacesService.find(workspaceId);
        if (workspace == null) {
            logger.log(Level.ERROR, String.format("can't invite user - workspace with id(%s) not found", workspaceId));
            return null;
        }
        var workspaceOwner = User.<User>findById(workspace.getOwnerId());
        if (workspaceOwner == null) {
            logger.log(Level.ERROR, String.format("can't invite user - workspace owner(%s) for workspace with id(%s) not found", workspaceId, workspace.getOwnerId()));
            return null;
        }

        var inviteExists = workspaceInvitesRepository.count(
                "workspaceId = ?1 and email = ?2 and status = ?3",
                workspaceId,
                email,
                WorkspaceInviteStatus.PENDING
        ) > 0;
        if (inviteExists) {
            logger.info("can't invite user - invite already exists");
            return null;
        }

        var createdAt = new Date();
        var invite = new WorkspaceInvite(
                UUID.randomUUID(),
                workspaceId,
                email,
                name,
                role,
                responsibilities,
                WorkspaceInviteStatus.PENDING,
                createdAt,
                createdAt
        );
        workspaceInvitesRepository.persist(invite);
        try {
            postman.sendInvitationEmail(
                    email,
                    workspaceOwner.nickname,
                    workspaceOwner.picture,
                    workspace.getName(),
                    String.format("https://app.bordy.io/#/invite%s", invite.getId())
            );
        } catch (Exception e) {
            logger.log(Level.ERROR, String.format("can't send email for invite(%s)", invite.getId()));
        }

        return invite;
    }

    @Transactional
    public void delete(
            @Nonnull UUID inviteId,
            @Nonnull UUID workspaceId
    ) {
        workspaceInvitesRepository.delete(
                "_id = ?1 and workspaceId = ?2",
                inviteId,
                workspaceId
        );
    }

}
