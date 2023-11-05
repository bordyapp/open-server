package io.bordy.kanban.workspaces.invites;

import io.bordy.gateways.CreateInviteDto;
import io.bordy.kanban.workspaces.invites.exceptions.WorkspaceInviteAlreadyExistsException;
import io.bordy.kanban.workspaces.invites.exceptions.WorkspaceInviteWorkspaceNotFoundException;
import io.bordy.kanban.workspaces.invites.exceptions.WorkspaceInviteWorkspaceOwnerNotFoundException;
import io.bordy.kanban.workspaces.workspaces.Workspace;
import io.bordy.users.User;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.util.Date;
import java.util.UUID;

@QuarkusTest
@TestTransaction
public class WorkspaceInvitesServiceTest {

    @Inject
    WorkspaceInvitesService workspaceInvitesService;

    @Test
    @DisplayName("find: Return null when workspace and email doesnt exist")
    public void findReturnNullWhenWorkspaceAndEmailDoesntExist() {
        var workspaceId = UUID.randomUUID();
        var email = "user@email";
        var name = "name";
        var role = "role";
        var responsibilities = "responsibilities";
        var inviteDto = new CreateInviteDto(email, name, role, responsibilities);

        var date = new Date();
        var invite = new WorkspaceInvite(
                UUID.randomUUID(),
                workspaceId,
                inviteDto.email(),
                inviteDto.name(),
                inviteDto.role(),
                inviteDto.responsibilities(),
                WorkspaceInviteStatus.PENDING,
                date,
                date
        );
        invite.persist();

        Assertions.assertNull(
                workspaceInvitesService.find(UUID.randomUUID(), "email@domain"),
                "Must return null when workspace and email doesnt exist"
        );
    }

    @Test
    @DisplayName("find: Return null when workspace doesn't exist but email does")
    public void findReturnNullWhenWorkspaceDoesntExistButEmailDoes() {
        var workspaceId = UUID.randomUUID();
        var email = "user@email";
        var name = "name";
        var role = "role";
        var responsibilities = "responsibilities";
        var inviteDto = new CreateInviteDto(email, name, role, responsibilities);

        var date = new Date();
        var invite = new WorkspaceInvite(
                UUID.randomUUID(),
                workspaceId,
                inviteDto.email(),
                inviteDto.name(),
                inviteDto.role(),
                inviteDto.responsibilities(),
                WorkspaceInviteStatus.PENDING,
                date,
                date
        );
        invite.persist();

        Assertions.assertNull(
                workspaceInvitesService.find(UUID.randomUUID(), email),
                "Must return null when workspace doesn't exist but email does"
        );
    }

    @Test
    @DisplayName("find: Return null when workspace exists but email doesn't")
    public void findReturnNullWhenWorkspaceExistsButEmailDoesnt() {
        var workspaceId = UUID.randomUUID();
        var email = "user@email";
        var name = "name";
        var role = "role";
        var responsibilities = "responsibilities";
        var inviteDto = new CreateInviteDto(email, name, role, responsibilities);

        var date = new Date();
        var invite = new WorkspaceInvite(
                UUID.randomUUID(),
                workspaceId,
                inviteDto.email(),
                inviteDto.name(),
                inviteDto.role(),
                inviteDto.responsibilities(),
                WorkspaceInviteStatus.PENDING,
                date,
                date
        );
        invite.persist();

        Assertions.assertNull(
                workspaceInvitesService.find(workspaceId, "email@domain"),
                "Must return null when workspace exists but email doesn't"
        );
    }

    @Test
    @DisplayName("find")
    public void find() {
        var workspaceId = UUID.randomUUID();
        var email = "user@email";
        var name = "name";
        var role = "role";
        var responsibilities = "responsibilities";
        var inviteDto = new CreateInviteDto(email, name, role, responsibilities);

        var date = new Date();
        var invite = new WorkspaceInvite(
                UUID.randomUUID(),
                workspaceId,
                inviteDto.email(),
                inviteDto.name(),
                inviteDto.role(),
                inviteDto.responsibilities(),
                WorkspaceInviteStatus.PENDING,
                date,
                date
        );
        invite.persist();

        Assertions.assertEquals(
                invite, workspaceInvitesService.find(workspaceId, email),
                "Must find correct invite"
        );
    }

    @Test
    @DisplayName("create: Throw exception when workspace doesn't exist")
    public void createThrowExceptionWhenWorkspaceDoesntExist() {
        var workspaceId = UUID.randomUUID();
        var exception = Assertions.assertThrows(
                WorkspaceInviteWorkspaceNotFoundException.class,
                () -> workspaceInvitesService.create(
                        workspaceId,
                        new CreateInviteDto("email", "name", "role", "responsibilities")
                ),
                "Must throw WorkspaceInviteWorkspaceNotFoundException when workspace doesn't exist"
        );

        Assertions.assertEquals(
                String.format("can't invite user - workspace with id(%s) not found", workspaceId),
                exception.getMessage(),
                "Must throw exception with correct message"
        );
    }

    @Test
    @DisplayName("create: Throw exception when workspace owner doesn't exist")
    public void createThrowExceptionWhenWorkspaceOwnerDoesntExist() {
        var userId = "auth0|id";

        var workspaceId = UUID.randomUUID();
        var workspace = new Workspace(
                workspaceId,
                "Workspace to test",
                "s3://workspace-to-test.webp",
                userId,
                new Date(),
                new Date()
        );
        workspace.persist();

        var exception = Assertions.assertThrows(
                WorkspaceInviteWorkspaceOwnerNotFoundException.class,
                () -> workspaceInvitesService.create(
                        workspaceId,
                        new CreateInviteDto("email", "name", "role", "responsibilities")
                ),
                "Must throw WorkspaceInviteWorkspaceOwnerNotFoundException when workspace doesn't exist"
        );

        Assertions.assertEquals(
                String.format(
                        "can't invite user - workspace owner(%s) for workspace with id(%s) not found",
                        workspaceId, workspace.getOwnerId()
                ),
                exception.getMessage(),
                "Must throw exception with correct message"
        );
    }

    @Test
    @DisplayName("create: Throw exception when invite already exists")
    public void createThrowExceptionWhenInviteAlreadyExists() {
        var userId = "auth0|id";
        var user = new User(
                userId,
                "owner@email",
                "owner",
                ""
        );
        user.persist();

        var workspaceId = UUID.randomUUID();
        var workspace = new Workspace(
                workspaceId,
                "Workspace to test",
                "s3://workspace-to-test.webp",
                userId,
                new Date(),
                new Date()
        );
        workspace.persist();

        var email = "user@email";
        var inviteDto = new CreateInviteDto(email, "name", "role", "responsibilities");
        var workspaceInvite = new WorkspaceInvite(
                UUID.randomUUID(),
                workspaceId,
                inviteDto.email(),
                inviteDto.name(),
                inviteDto.role(),
                inviteDto.responsibilities(),
                WorkspaceInviteStatus.PENDING,
                new Date(),
                new Date()
        );
        workspaceInvite.persist();

        var exception = Assertions.assertThrows(
                WorkspaceInviteAlreadyExistsException.class,
                () -> workspaceInvitesService.create(
                        workspaceId,
                        new CreateInviteDto(email, "new name", "new role", "new responsibilities")
                ),
                "Must throw WorkspaceInviteAlreadyExistsException when workspace doesn't exist"
        );

        Assertions.assertEquals(
                "can't invite user - invite already exists",
                exception.getMessage(),
                "Must throw exception with correct message"
        );
        Assertions.assertEquals(
                1, WorkspaceInvite.count(),
                "Must not create extra invites"
        );
    }

    @Test
    @DisplayName("create")
    public void create() {
        var userId = "auth0|id";
        var user = new User(
                userId,
                "owner@email",
                "owner",
                ""
        );
        user.persist();

        var workspaceId = UUID.randomUUID();
        var workspace = new Workspace(
                workspaceId,
                "Workspace to test",
                "s3://workspace-to-test.webp",
                userId,
                new Date(),
                new Date()
        );
        workspace.persist();

        var email = "user@email";
        var name = "name";
        var role = "role";
        var responsibilities = "responsibilities";
        var inviteDto = new CreateInviteDto(email, name, role, responsibilities);

        var date = new Date();
        var createdInvite = Assertions.assertDoesNotThrow(
                () -> workspaceInvitesService.create(workspaceId, inviteDto),
                "Must create invite"
        );

        Assertions.assertTrue(
                createdInvite.getCreatedAt().after(date),
                "Created date must be actual"
        );
        Assertions.assertTrue(
                createdInvite.getEditedAt().after(date),
                "Created date must be actual"
        );

        Assertions.assertEquals(
                createdInvite, WorkspaceInvite.findById(createdInvite.getId()),
                "Created and found invites must be equals"
        );
    }

}
