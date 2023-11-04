package io.bordy.kanban.workspaces.invites;

import io.bordy.gateways.CreateInviteDto;
import io.bordy.kanban.workspaces.invites.exceptions.WorkspaceInviteAlreadyExistsException;
import io.bordy.kanban.workspaces.invites.exceptions.WorkspaceInviteException;
import io.bordy.kanban.workspaces.invites.exceptions.WorkspaceInviteWorkspaceNotFoundException;
import io.bordy.kanban.workspaces.invites.exceptions.WorkspaceInviteWorkspaceOwnerNotFoundException;
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

    /**
     * Check does invite exits for given email in given workspace.
     * @param workspaceId workspace to check
     * @param email email to check
     * @return  does invite exits for given email in given workspace or not
     */
    public boolean exists(@Nonnull UUID workspaceId, @Nonnull String email) {
        return workspaceInvitesRepository.count(
                "workspaceId = ?1 and email = ?2 and status = ?3",
                workspaceId,
                email,
                WorkspaceInviteStatus.PENDING
        ) > 0;
    }

    @Nonnull
    @Transactional
    public WorkspaceInvite create(
            @Nonnull UUID workspaceId,
            @Nonnull CreateInviteDto inviteDto
    ) throws WorkspaceInviteException {
        var workspace = workspacesService.find(workspaceId);
        if (workspace == null) {
            var message = String.format("can't invite user - workspace with id(%s) not found", workspaceId);

            logger.log(Level.ERROR, message);
            throw new WorkspaceInviteWorkspaceNotFoundException(message);
        }

        var workspaceOwner = User.<User>findById(workspace.getOwnerId());
        if (workspaceOwner == null) {
            var message = String.format(
                    "can't invite user - workspace owner(%s) for workspace with id(%s) not found",
                    workspaceId, workspace.getOwnerId()
            );

            logger.log(Level.ERROR, message);
            throw new WorkspaceInviteWorkspaceOwnerNotFoundException(message);
        }

        if (exists(workspaceId, inviteDto.email())) {
            var message = "can't invite user - invite already exists";

            logger.info(message);
            throw new WorkspaceInviteAlreadyExistsException(message);
        }

        var createdAt = new Date();
        var invite = new WorkspaceInvite(
                UUID.randomUUID(),
                workspaceId,
                inviteDto.email(),
                inviteDto.name(),
                inviteDto.role(),
                inviteDto.responsibilities(),
                WorkspaceInviteStatus.PENDING,
                createdAt,
                createdAt
        );
        workspaceInvitesRepository.persist(invite);

        try {
            postman.sendInvitationEmail(
                    inviteDto.email(),
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
