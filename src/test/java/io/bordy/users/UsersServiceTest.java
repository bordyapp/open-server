package io.bordy.users;

import io.bordy.api.AssigneeDto;
import io.bordy.api.UserDto;
import io.bordy.kanban.workspaces.members.WorkspaceMember;
import io.bordy.kanban.workspaces.workspaces.Workspace;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

@QuarkusTest
@TestTransaction
public class UsersServiceTest {

    @Inject
    UsersService usersService;

    public void boardKnownAssignees() {
        // TODO
    }

    @Test
    @DisplayName("workspaceKnownAssignees: return empty map when workspace doesn't exist")
    public void workspaceKnownAssigneesReturnEmptyMapWhenWorkspaceDoesntExist() {
        Assertions.assertEquals(
                new HashMap<String, AssigneeDto>(), usersService.workspaceKnownAssignees(UUID.randomUUID()),
                "Must return empty map when workspace doesn't exist"
        );
    }

    @Test
    @DisplayName("workspaceKnownAssignees: return only owner when workspace without members")
    public void workspaceKnownAssigneesReturnOnlyOwnerWhenWorkspaceWithoutMembers() {
        var user = new User(
                "auth0|id",
                "frog@wednesday.com",
                "Wednesday frog",
                "s3://robot.webp"
        );
        user.persist();

        var workspace = new Workspace(
                UUID.randomUUID(),
                "Workspace to test",
                "",
                user.getId(),
                new Date(),
                new Date()
        );
        workspace.persist();

        var assigneeDto = new AssigneeDto(
                user.getId(),
                user.getNickname(),
                user.getPicture()
        );

        var workspaceKnownAssignees = new HashMap<String, AssigneeDto>();
        workspaceKnownAssignees.put(user.getId(), assigneeDto);
        Assertions.assertEquals(
                workspaceKnownAssignees, usersService.workspaceKnownAssignees(workspace.getId()),
                "Must return only owner when workspace without members"
        );
    }

    @Test
    @DisplayName("workspaceKnownAssignees: return owner and members when workspace has members")
    public void workspaceKnownAssigneesReturnOwnerAndMembersWhenWorkspaceHasMembers() {
        var user = new User(
                "auth0|id",
                "frog@wednesday.com",
                "Wednesday frog",
                "s3://robot.webp"
        );
        user.persist();
        var userAssigneeDto = new AssigneeDto(
                user.getId(),
                user.getNickname(),
                user.getPicture()
        );

        var workspace = new Workspace(
                UUID.randomUUID(),
                "20 minute adventure",
                "",
                user.getId(),
                new Date(),
                new Date()
        );
        workspace.persist();

        var rick = new User(
                "auth0|rick",
                "rick@sanchez.com",
                "Rick Sanchez",
                "s3://rick.webp"
        );
        rick.persist();
        var rickMember = new WorkspaceMember(
                UUID.randomUUID(),
                workspace.getId(),
                rick.getId(),
                "Rick Sanchez",
                "frog",
                "move cards",
                new Date(),
                new Date()
        );
        rickMember.persist();
        var rickAssigneeDto = new AssigneeDto(
                rick.getId(),
                rickMember.getName(),
                rick.getPicture()
        );

        var morty = new User(
                "auth0|morty",
                "morty@smith.com",
                "Morty Smith",
                "s3://morty.webp"
        );
        morty.persist();
        var mortyMember = new WorkspaceMember(
                UUID.randomUUID(),
                workspace.getId(),
                morty.getId(),
                "Morty Smith",
                "not frog",
                "move cards",
                new Date(),
                new Date()
        );
        mortyMember.persist();
        var mortyAssigneeDto = new AssigneeDto(
                morty.getId(),
                mortyMember.getName(),
                morty.getPicture()
        );

        var deletedUserId = "deletedUser";
        var deletedUserAsMember = new WorkspaceMember(
                UUID.randomUUID(),
                workspace.getId(),
                deletedUserId,
                "Morty Smith",
                "not frog",
                "move cards",
                new Date(),
                new Date()
        );
        deletedUserAsMember.persist();
        var deletedUserAsAssigneeDto = new AssigneeDto(
                deletedUserId,
                "Deleted user",
                ""
        );

        var workspaceKnownAssignees = new HashMap<String, AssigneeDto>();
        workspaceKnownAssignees.put(user.getId(), userAssigneeDto);
        workspaceKnownAssignees.put(rick.getId(), rickAssigneeDto);
        workspaceKnownAssignees.put(morty.getId(), mortyAssigneeDto);
        workspaceKnownAssignees.put(deletedUserId, deletedUserAsAssigneeDto);
        Assertions.assertEquals(
                workspaceKnownAssignees, usersService.workspaceKnownAssignees(workspace.getId()),
                "Must return owner and members when workspace has members"
        );
    }

    @Test
    @DisplayName("asAssignee: return assignee as 'Deleted User' when user doesn't exists")
    public void asAssigneeReturnAssigneeAsDeletedUserWhenUserDoesntExist() {
        var userId = "random id";
        var assigneeDto = new AssigneeDto(
                userId,
                "Deleted user",
                ""
        );

        Assertions.assertEquals(
                assigneeDto, usersService.asAssignee(userId),
                "Must return assignee as 'Deleted User' when user doesn't exists"
        );
    }

    @Test
    @DisplayName("asAssignee: return assignee from user when user exists and is not workspace member")
    public void asAssigneeReturnAssigneeAsUserWhenUserExistsAndIsNotWorkspaceMember() {
        var userId = "auth0|id";
        var user = new User(
                userId,
                "frog@wednesday.com",
                "Wednesday frog",
                "s3://robot.webp"
        );
        user.persist();

        var assigneeDto = new AssigneeDto(
                userId,
                user.getNickname(),
                user.getPicture()
        );

        Assertions.assertEquals(
                assigneeDto, usersService.asAssignee(userId),
                "Must return assignee from user when user exists and is not workspace member"
        );
    }

    @Test
    @DisplayName("asAssignee: return assignee from user when user exists and is workspace member")
    public void asAssigneeReturnAssigneeAsUserWhenUserExistsAndIsWorkspaceMember() {
        var userId = "auth0|id";
        var user = new User(
                userId,
                "frog@wednesday.com",
                "Wednesday frog",
                "s3://robot.webp"
        );
        user.persist();

        var workspaceMember = new WorkspaceMember(
                UUID.randomUUID(),
                UUID.randomUUID(),
                user.id,
                "Mr. Robot",
                "frog",
                "move cards",
                new Date(),
                new Date()
        );
        workspaceMember.persist();

        var assigneeDto = new AssigneeDto(
                userId,
                workspaceMember.getName(),
                user.getPicture()
        );

        Assertions.assertEquals(
                assigneeDto, usersService.asAssignee(userId),
                "Must return assignee from user when user exists and is workspace member"
        );
    }

    @Test
    @DisplayName("asUser: Return null when user doesn't exist")
    public void asUserReturnNullWhenUserDoesntExist() {
        var workspaceMember = new WorkspaceMember(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "mr.robot",
                "Mr. Robot",
                "user",
                "move cards",
                new Date(),
                new Date()
        );

        Assertions.assertNull(
                usersService.asUser(workspaceMember),
                "Must return null when user doesn't exist"
        );
    }

    @Test
    @DisplayName("asUser: Return user dto when user exists")
    public void asUserReturnUserDtoWhenUserExists() {
        var user = new User(
                "auth0|id",
                "frog@wednesday.com",
                "Wednesday frog",
                "s3://robot.webp"
        );
        user.persist();

        var workspaceMember = new WorkspaceMember(
                UUID.randomUUID(),
                UUID.randomUUID(),
                user.id,
                "Mr. Robot",
                "frog",
                "move cards",
                new Date(),
                new Date()
        );
        workspaceMember.persist();

        var userDto = new UserDto(
                user.id,
                user.email,
                workspaceMember.getName(),
                workspaceMember.getRole(),
                workspaceMember.getResponsibilities(),
                user.picture
        );
        Assertions.assertEquals(
                userDto, usersService.asUser(workspaceMember),
                "Must return correct user dto"
        );
    }

}
