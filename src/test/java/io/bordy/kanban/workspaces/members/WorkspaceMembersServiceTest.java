package io.bordy.kanban.workspaces.members;

import io.bordy.api.UpdateWorkspaceMemberDto;
import io.bordy.kanban.workspaces.workspaces.Workspace;
import io.bordy.users.User;
import io.bordy.workspaces.WorkspaceInvite;
import io.bordy.workspaces.WorkspaceInviteStatus;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@QuarkusTest
@TestTransaction
public class WorkspaceMembersServiceTest {

    @Inject
    WorkspaceMembersService workspaceMembersService;

    @Test
    @DisplayName("isMemberOf: Return false when workspace and user doesn't exist")
    public void isMemberOfReturnFalseWhenWorkspaceAndUserDoesntExist() {
        Assertions.assertFalse(
                workspaceMembersService.isMemberOf(UUID.randomUUID(), "user id"),
                "Must return false when workspace and user doesn't exist"
        );
    }

    @Test
    @DisplayName("isMemberOf: Return false when workspace exists but user doesn't")
    public void isMemberOfReturnFalseWhenWorkspaceExistsButUserDoesnt() {
        var workspace = new Workspace(
                UUID.randomUUID(),
                "Workspace to test",
                "",
                UUID.randomUUID().toString(),
                new Date(),
                new Date()
        );
        workspace.persist();

        Assertions.assertFalse(
                workspaceMembersService.isMemberOf(workspace.getId(), "user id"),
                "Must return false when workspace exists but user doesn't"
        );
    }

    @Test
    @DisplayName("isMemberOf: Return false when workspace doesn't exist but user does")
    public void isMemberOfReturnFalseWhenWorkspaceDoesntExistButUserDoes() {
        var user = new User(
                "auth0|id",
                "frog@wednesday.com",
                "Wednesday frog",
                "s3://robot.webp"
        );
        user.persist();

        Assertions.assertFalse(
                workspaceMembersService.isMemberOf(UUID.randomUUID(), user.getId()),
                "Must return false when workspace doesn't exist but user does"
        );
    }

    @Test
    @DisplayName("isMemberOf: Return true when user os member of Workspace")
    public void isMemberOfReturnTrueWhenUserIsMemberOfWorkspace() {
        var workspace = new Workspace(
                UUID.randomUUID(),
                "Workspace to test",
                "",
                UUID.randomUUID().toString(),
                new Date(),
                new Date()
        );
        workspace.persist();

        var user = new User(
                "auth0|id",
                "frog@wednesday.com",
                "Wednesday frog",
                "s3://robot.webp"
        );
        user.persist();

        var member = new WorkspaceMember(
                UUID.randomUUID(),
                workspace.getId(),
                user.getId(),
                "member name",
                "member role",
                "member responsibilities",
                new Date(),
                new Date()
        );
        member.persist();

        Assertions.assertTrue(
                workspaceMembersService.isMemberOf(workspace.getId(), user.getId()),
                "Must return true when user os member of Workspace"
        );
    }

    @Test
    @DisplayName("membersOf: Return empty list when workspace doesn't exist")
    public void membersOfReturnEmptyListWhenWorkspaceDoesntExist() {
        Assertions.assertTrue(
                workspaceMembersService.membersOf(UUID.randomUUID()).isEmpty(),
                "Must return empty list when workspace doesn't exist"
        );
    }

    @Test
    @DisplayName("membersOf: Return empty list when workspace without members")
    public void membersOfReturnEmptyListWhenWorkspaceWithoutMembers() {
        var workspace = new Workspace(
                UUID.randomUUID(),
                "Workspace to test",
                "",
                UUID.randomUUID().toString(),
                new Date(),
                new Date()
        );
        workspace.persist();

        Assertions.assertTrue(
                workspaceMembersService.membersOf(workspace.getId()).isEmpty(),
                "Must return empty list when workspace without members"
        );
    }

    @Test
    @DisplayName("membersOf: Return workspace members")
    public void membersOfReturnWorkspaceMembers() {
        var workspace = new Workspace(
                UUID.randomUUID(),
                "Workspace to test",
                "",
                UUID.randomUUID().toString(),
                new Date(),
                new Date()
        );
        workspace.persist();

        var member = new WorkspaceMember(
                UUID.randomUUID(),
                workspace.getId(),
                "member",
                "member name",
                "member role",
                "member responsibilities",
                new Date(),
                new Date()
        );
        member.persist();

        var member2 = new WorkspaceMember(
                UUID.randomUUID(),
                workspace.getId(),
                "member2",
                "member2 name",
                "member2 role",
                "member2 responsibilities",
                new Date(),
                new Date()
        );
        member2.persist();

        var member3 = new WorkspaceMember(
                UUID.randomUUID(),
                workspace.getId(),
                "member3",
                "member3 name",
                "member3 role",
                "member responsibilities",
                new Date(),
                new Date()
        );
        member3.persist();

        Assertions.assertEquals(
                List.of(member, member2, member3), workspaceMembersService.membersOf(workspace.getId()),
                "Must return workspace members"
        );
    }

    @Test
    @DisplayName("memberUserIdsOf: Return empty list when workspace doesn't exist")
    public void memberUserIdsOfReturnEmptyListWhenWorkspaceDoesntExist() {
        Assertions.assertTrue(
                workspaceMembersService.memberUserIdsOf(UUID.randomUUID()).isEmpty(),
                "Must return empty list when workspace doesn't exist"
        );
    }

    @Test
    @DisplayName("memberUserIdsOf: Return empty list when workspace without members")
    public void memberUserIdsOfReturnEmptyListWhenWorkspaceWithoutMembers() {
        var workspace = new Workspace(
                UUID.randomUUID(),
                "Workspace to test",
                "",
                UUID.randomUUID().toString(),
                new Date(),
                new Date()
        );
        workspace.persist();

        Assertions.assertTrue(
                workspaceMembersService.memberUserIdsOf(workspace.getId()).isEmpty(),
                "Must return empty list when workspace without members"
        );
    }

    @Test
    @DisplayName("memberUserIdsOf: Return workspace members")
    public void memberUserIdsOfReturnWorkspaceMembers() {
        var workspace = new Workspace(
                UUID.randomUUID(),
                "Workspace to test",
                "",
                UUID.randomUUID().toString(),
                new Date(),
                new Date()
        );
        workspace.persist();

        var member = new WorkspaceMember(
                UUID.randomUUID(),
                workspace.getId(),
                "member",
                "member name",
                "member role",
                "member responsibilities",
                new Date(),
                new Date()
        );
        member.persist();

        var member2 = new WorkspaceMember(
                UUID.randomUUID(),
                workspace.getId(),
                "member2",
                "member2 name",
                "member2 role",
                "member2 responsibilities",
                new Date(),
                new Date()
        );
        member2.persist();

        var member3 = new WorkspaceMember(
                UUID.randomUUID(),
                workspace.getId(),
                "member3",
                "member3 name",
                "member3 role",
                "member responsibilities",
                new Date(),
                new Date()
        );
        member3.persist();

        Assertions.assertEquals(
                List.of("member", "member2", "member3"), workspaceMembersService.memberUserIdsOf(workspace.getId()),
                "Must return workspace members"
        );
    }

    @Test
    @DisplayName("memberOf: Return empty list when user doesn't exist")
    public void memberOfReturnEmptyListWhenUserDoesntExist() {
        Assertions.assertTrue(
                workspaceMembersService.memberOf("user id").isEmpty(),
                "Must return empty list when user doesn't exist"
        );
    }

    @Test
    @DisplayName("memberOf: Return empty list when user without workspaces")
    public void memberOfReturnEmptyListWhenUserWithoutWorkspaces() {
        var user = new User(
                "auth0|id",
                "frog@wednesday.com",
                "Wednesday frog",
                "s3://robot.webp"
        );
        user.persist();

        Assertions.assertTrue(
                workspaceMembersService.memberOf(user.getId()).isEmpty(),
                "Must return empty list when user without workspaces"
        );
    }

    @Test
    @DisplayName("memberOf: Return workspace ids")
    public void memberOfReturnWorkspaceIds() {
        var userId = "auth0|id";
        var user = new User(
                userId,
                "frog@wednesday.com",
                "Wednesday frog",
                "s3://robot.webp"
        );
        user.persist();

        var workspace = UUID.randomUUID();
        var member = new WorkspaceMember(
                UUID.randomUUID(),
                workspace,
                userId,
                "member name",
                "member role",
                "member responsibilities",
                new Date(),
                new Date()
        );
        member.persist();

        var workspace2 = UUID.randomUUID();
        var member2 = new WorkspaceMember(
                UUID.randomUUID(),
                workspace2,
                userId,
                "member name",
                "member role",
                "member responsibilities",
                new Date(),
                new Date()
        );
        member2.persist();

        var workspace3 = UUID.randomUUID();
        var member3 = new WorkspaceMember(
                UUID.randomUUID(),
                workspace3,
                userId,
                "member name",
                "member role",
                "member responsibilities",
                new Date(),
                new Date()
        );
        member3.persist();

        Assertions.assertEquals(
                List.of(workspace, workspace2, workspace3), workspaceMembersService.memberOf(user.getId()),
                "Must workspace ids"
        );
    }

    @Test
    @DisplayName("Create and find workspace member")
    public void createAndFind() {
        var workspaceId = UUID.randomUUID();
        var userId = "auth0|id";
        var userName = "User name";
        var userRole = "User role";
        var userResponsibilities = "User responsibilities";

        var created = workspaceMembersService.create(
                userId,
                new WorkspaceInvite(
                        UUID.randomUUID(),
                        workspaceId,
                        "user@mail.com",
                        userName,
                        userRole,
                        userResponsibilities,
                        WorkspaceInviteStatus.PENDING,
                        new Date(),
                        new Date()
                )
        );
        Assertions.assertNotNull(created, "Must return workspace member");
        Assertions.assertEquals(
                workspaceId, created.getWorkspaceId(),
                "Workspace member id must be equal"
        );
        Assertions.assertEquals(
                userName, created.getName(),
                "Workspace member name must be equal"
        );
        Assertions.assertEquals(
                userRole, created.getRole(),
                "Workspace member role must be equal"
        );
        Assertions.assertEquals(
                userResponsibilities, created.getResponsibilities(),
                "Workspace member responsibilities must be equal"
        );

        var persisted = workspaceMembersService.find(userId);
        Assertions.assertNotNull(persisted, "Must persist workspace member");
        Assertions.assertEquals(
                created, persisted,
                "Created and persisted workspace member must be equals"
        );
        Assertions.assertEquals(
                workspaceId, persisted.getWorkspaceId(),
                "Workspace member id must be equal"
        );
        Assertions.assertEquals(
                userName, persisted.getName(),
                "Workspace member name must be equal"
        );
        Assertions.assertEquals(
                userRole, persisted.getRole(),
                "Workspace member role must be equal"
        );
        Assertions.assertEquals(
                userResponsibilities, persisted.getResponsibilities(),
                "Workspace member responsibilities must be equal"
        );
    }

    @Test
    @DisplayName("find: Return null when workspace member doesn't exist")
    public void findReturnNullWhenWorkspaceMemberDoesntExist() {
        Assertions.assertNull(
                workspaceMembersService.find("auth0|id"),
                "Must return null when workspace member doesn't exist"
        );
    }

    @Test
    @DisplayName("update: Update nothing when workspace and member doesn't exist")
    public void updateNothingWhenWorkspaceAndMemberDoesntExist() {
        var patch = new UpdateWorkspaceMemberDto(
                "New user name",
                "New user role",
                "New user responsibilities"
        );
        workspaceMembersService.update(UUID.randomUUID().toString(), "userId", patch);
        Assertions.assertTrue(
                WorkspaceMember.listAll().isEmpty(),
                "Must don't create or update any workspace member"
        );
    }

    @Test
    @DisplayName("update: Update nothing when workspace exists but member doesn't")
    public void updateNothingWhenWorkspaceExistsButMemberDoesnt() {
        var workspace = new Workspace(
                UUID.randomUUID(),
                "Workspace to test",
                "",
                UUID.randomUUID().toString(),
                new Date(),
                new Date()
        );
        workspace.persist();

        var patch = new UpdateWorkspaceMemberDto(
                "New user name",
                "New user role",
                "New user responsibilities"
        );
        workspaceMembersService.update(workspace.getId().toString(), "userId", patch);
        Assertions.assertTrue(
                WorkspaceMember.listAll().isEmpty(),
                "Must don't create or update any workspace member"
        );
    }

    @Test
    @DisplayName("update: Update nothing when workspace doesn't exist but member does")
    public void updateNothingWhenWorkspaceDoesntExistButMemberDoes() {
        var userId = "auth0|id";

        var member = new WorkspaceMember(
                UUID.randomUUID(),
                UUID.randomUUID(),
                userId,
                "member name",
                "member role",
                "member responsibilities",
                new Date(),
                new Date()
        );
        member.persist();

        var patch = new UpdateWorkspaceMemberDto(
                "New user name",
                "New user role",
                "New user responsibilities"
        );
        workspaceMembersService.update(UUID.randomUUID().toString(), userId, patch);
        Assertions.assertEquals(
                List.of(member), WorkspaceMember.listAll(),
                "Must don't update given workspace member"
        );
    }

    @Test
    @DisplayName("update: Update nothing when workspace exists but user is not it's member")
    public void updateNothingWhenWorkspaceExistsButUserIsNotItsMember() {
        var workspace = new Workspace(
                UUID.randomUUID(),
                "Workspace to test",
                "",
                UUID.randomUUID().toString(),
                new Date(),
                new Date()
        );
        workspace.persist();

        var userId = "auth0|id";
        var member = new WorkspaceMember(
                UUID.randomUUID(),
                UUID.randomUUID(),
                userId,
                "member name",
                "member role",
                "member responsibilities",
                new Date(),
                new Date()
        );
        member.persist();

        var patch = new UpdateWorkspaceMemberDto(
                "New user name",
                "New user role",
                "New user responsibilities"
        );
        workspaceMembersService.update(workspace.getId().toString(), userId, patch);
        Assertions.assertEquals(
                List.of(member), WorkspaceMember.listAll(),
                "Must don't update given workspace member"
        );
    }

    @Test
    @DisplayName("Update workspace member")
    public void update() {
        var workspaceId = UUID.randomUUID();
        var userId = "auth0|id";

        var member = new WorkspaceMember(
                UUID.randomUUID(),
                workspaceId,
                userId,
                "User name",
                "User role",
                "User responsibilities",
                new Date(),
                new Date()
        );
        member.persist();

        var newName = "New user name";
        var newRole = "New user role";
        var newResponsibilities = "New user responsibilities";
        var patch = new UpdateWorkspaceMemberDto(
                newName,
                newRole,
                newResponsibilities
        );
        workspaceMembersService.update(workspaceId.toString(), userId, patch);

        member.setName(newName);
        member.setRole(newRole);
        member.setResponsibilities(newResponsibilities);
        var updatedMember = workspaceMembersService.find(member.getUserId());
        Assertions.assertEquals(
                member, updatedMember,
                "Member must be updated"
        );
    }

    @Test
    @DisplayName("delete: Delete nothing when workspace and member doesn't exist")
    public void deleteNothingWhenWorkspaceAndMemberDoesntExist() {
        var workspaceId = UUID.randomUUID();

        var member = new WorkspaceMember(
                UUID.randomUUID(),
                workspaceId,
                "member",
                "member: User name",
                "member: User role",
                "member: User responsibilities",
                new Date(),
                new Date()
        );
        member.persist();

        var member2 = new WorkspaceMember(
                UUID.randomUUID(),
                workspaceId,
                "member2",
                "member2: User name",
                "member2: User role",
                "member2: User responsibilities",
                new Date(),
                new Date()
        );
        member2.persist();

        workspaceMembersService.delete(UUID.randomUUID().toString(), "user id");
        Assertions.assertEquals(
                List.of(member, member2), WorkspaceMember.listAll(),
                "Must delete nothing when workspace and member doesn't exist"
        );
    }

    @Test
    @DisplayName("delete: Delete nothing when workspace exists but member doesn't")
    public void deleteNothingWhenWorkspaceExistsButMemberDoesnt() {
        var workspaceId = UUID.randomUUID();

        var member = new WorkspaceMember(
                UUID.randomUUID(),
                workspaceId,
                "member",
                "member: User name",
                "member: User role",
                "member: User responsibilities",
                new Date(),
                new Date()
        );
        member.persist();

        var member2 = new WorkspaceMember(
                UUID.randomUUID(),
                workspaceId,
                "member2",
                "member2: User name",
                "member2: User role",
                "member2: User responsibilities",
                new Date(),
                new Date()
        );
        member2.persist();

        workspaceMembersService.delete(workspaceId.toString(), "user id");
        Assertions.assertEquals(
                List.of(member, member2), WorkspaceMember.listAll(),
                "Must delete nothing when workspace exists but member doesn't"
        );
    }

    @Test
    @DisplayName("delete: Delete nothing when workspace doesn't exist but member does")
    public void deleteNothingWhenWorkspaceDoesntExistButMemberDoes() {
        var workspaceId = UUID.randomUUID();

        var member = new WorkspaceMember(
                UUID.randomUUID(),
                workspaceId,
                "member",
                "member: User name",
                "member: User role",
                "member: User responsibilities",
                new Date(),
                new Date()
        );
        member.persist();

        var member2 = new WorkspaceMember(
                UUID.randomUUID(),
                workspaceId,
                "member2",
                "member2: User name",
                "member2: User role",
                "member2: User responsibilities",
                new Date(),
                new Date()
        );
        member2.persist();

        workspaceMembersService.delete(UUID.randomUUID().toString(), member.getUserId());
        Assertions.assertEquals(
                List.of(member, member2), WorkspaceMember.listAll(),
                "Must delete nothing when workspace doesn't exist but member does"
        );
    }

    @Test
    @DisplayName("Delete workspace member")
    public void delete() {
        var workspaceId = UUID.randomUUID();

        var member = new WorkspaceMember(
                UUID.randomUUID(),
                workspaceId,
                "member",
                "member: User name",
                "member: User role",
                "member: User responsibilities",
                new Date(),
                new Date()
        );
        member.persist();

        var member2 = new WorkspaceMember(
                UUID.randomUUID(),
                workspaceId,
                "member2",
                "member2: User name",
                "member2: User role",
                "member2: User responsibilities",
                new Date(),
                new Date()
        );
        member2.persist();

        workspaceMembersService.delete(member.getWorkspaceId().toString(), member.getUserId());
        Assertions.assertEquals(
                List.of(member2), WorkspaceMember.listAll(),
                "Must delete only one member"
        );
    }

    @Test
    @DisplayName("deleteAll: Delete nothing when workspace doesn't exist")
    public void deleteAllDeleteNothingWhenWorkspaceDoesntExist() {
        var workspaceId = UUID.randomUUID();

        var member = new WorkspaceMember(
                UUID.randomUUID(),
                workspaceId,
                "member",
                "member: User name",
                "member: User role",
                "member: User responsibilities",
                new Date(),
                new Date()
        );
        member.persist();

        var member2 = new WorkspaceMember(
                UUID.randomUUID(),
                workspaceId,
                "member2",
                "member2: User name",
                "member2: User role",
                "member2: User responsibilities",
                new Date(),
                new Date()
        );
        member2.persist();

        workspaceMembersService.deleteAll(UUID.randomUUID());
        Assertions.assertEquals(
                List.of(member, member2), WorkspaceMember.listAll(),
                "Must delete nothing"
        );
    }

    @Test
    @DisplayName("deleteAll: Delete nothing when workspace doesn't exist")
    public void deleteAll() {
        var workspaceId = UUID.randomUUID();

        var member = new WorkspaceMember(
                UUID.randomUUID(),
                workspaceId,
                "member",
                "member: User name",
                "member: User role",
                "member: User responsibilities",
                new Date(),
                new Date()
        );
        member.persist();

        var member2 = new WorkspaceMember(
                UUID.randomUUID(),
                workspaceId,
                "member2",
                "member2: User name",
                "member2: User role",
                "member2: User responsibilities",
                new Date(),
                new Date()
        );
        member2.persist();

        var workspaceId2 = UUID.randomUUID();

        var member3 = new WorkspaceMember(
                UUID.randomUUID(),
                workspaceId2,
                "member3",
                "member3: User name",
                "member3: User role",
                "member: User responsibilities",
                new Date(),
                new Date()
        );
        member3.persist();

        var member4 = new WorkspaceMember(
                UUID.randomUUID(),
                workspaceId2,
                "member4",
                "member4: User name",
                "member4: User role",
                "member2: User responsibilities",
                new Date(),
                new Date()
        );
        member4.persist();

        workspaceMembersService.deleteAll(workspaceId);
        Assertions.assertEquals(
                List.of(member3, member4), WorkspaceMember.listAll(),
                "Must delete members only from given workspace"
        );
    }

}
