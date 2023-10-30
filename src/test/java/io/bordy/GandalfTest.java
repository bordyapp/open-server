package io.bordy;

import io.bordy.kanban.workspaces.members.WorkspaceMember;
import io.bordy.kanban.workspaces.workspaces.Workspace;
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
public class GandalfTest {

    @Inject
    Gandalf gandalf;

    @Test
    @DisplayName("Return true when user and workspace doesn't exists")
    public void returnTrueWhenUserAndWorkspaceDoesntExists() {
        Assertions.assertTrue(
                gandalf.youShallNotPass(UUID.randomUUID(), "random user id"),
                "Must return true when user and workspace doesn't exists"
        );
    }

    @Test
    @DisplayName("Return true when workspace exists but user doesn't")
    public void returnTrueWhenWorkspaceExistsButUserDoesnt() {
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
                gandalf.youShallNotPass(workspace.getId(), "random user id"),
                "Must return true when workspace exists but user doesn't"
        );
    }

    @Test
    @DisplayName("Return true when user exists but workspace doesn't")
    public void returnTrueWhenUserExistsButWorkspaceDoesnt() {
        var workspaceMember = new WorkspaceMember(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "auth0|id",
                "Mr. Robot",
                "user",
                "move cards",
                new Date(),
                new Date()
        );
        workspaceMember.persist();

        Assertions.assertTrue(
                gandalf.youShallNotPass(UUID.randomUUID(), workspaceMember.getUserId()),
                "Must return true when user exists but workspace doesn't"
        );
    }

    @Test
    @DisplayName("Return true when user and workspace exist but user is not owner or member")
    public void returnTrueWhenUserAndWorkspaceExistButUserIsNotOwnerOrMember() {
        var workspace = new Workspace(
                UUID.randomUUID(),
                "Workspace to test",
                "",
                UUID.randomUUID().toString(),
                new Date(),
                new Date()
        );
        workspace.persist();

        var workspaceMember = new WorkspaceMember(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "auth0|id",
                "Mr. Robot",
                "user",
                "move cards",
                new Date(),
                new Date()
        );
        workspaceMember.persist();

        Assertions.assertTrue(
                gandalf.youShallNotPass(workspace.getId(), workspaceMember.getUserId()),
                "Must return true when user and workspace exist but user is not owner or member"
        );
    }

    @Test
    @DisplayName("Return false when user and workspace exist but user is only owner")
    public void returnFalseWhenUserAndWorkspaceExistButUserIsOnlyOwner() {
        var userId = "auth0|owner";

        var workspace = new Workspace(
                UUID.randomUUID(),
                "Workspace to test",
                "",
                userId,
                new Date(),
                new Date()
        );
        workspace.persist();

        var workspaceMember = new WorkspaceMember(
                UUID.randomUUID(),
                UUID.randomUUID(),
                userId,
                "Mr. Robot",
                "user",
                "move cards",
                new Date(),
                new Date()
        );
        workspaceMember.persist();

        Assertions.assertFalse(
                gandalf.youShallNotPass(workspace.getId(), workspaceMember.getUserId()),
                "Must return false when user and workspace exist but user is only owner"
        );
    }

    @Test
    @DisplayName("Return false when user and workspace exist but user is only member")
    public void returnFalseWhenUserAndWorkspaceExistButUserIsOnlyMember() {
        var userId = "auth0|member";

        var workspace = new Workspace(
                UUID.randomUUID(),
                "Workspace to test",
                "",
                "auth0|owner",
                new Date(),
                new Date()
        );
        workspace.persist();

        var workspaceMember = new WorkspaceMember(
                UUID.randomUUID(),
                workspace.getId(),
                userId,
                "Mr. Robot",
                "user",
                "move cards",
                new Date(),
                new Date()
        );
        workspaceMember.persist();

        Assertions.assertFalse(
                gandalf.youShallNotPass(workspace.getId(), workspaceMember.getUserId()),
                "Must return false when user and workspace exist but user is only member"
        );
    }

    @Test
    @DisplayName("Return false when user and workspace exist but user is owner and member")
    public void returnFalseWhenUserAndWorkspaceExistButUserIsOwnerAndMember() {
        var userId = "auth0|owner+member";

        var workspace = new Workspace(
                UUID.randomUUID(),
                "Workspace to test",
                "",
                userId,
                new Date(),
                new Date()
        );
        workspace.persist();

        var workspaceMember = new WorkspaceMember(
                UUID.randomUUID(),
                workspace.getId(),
                userId,
                "Mr. Robot",
                "user",
                "move cards",
                new Date(),
                new Date()
        );
        workspaceMember.persist();

        Assertions.assertFalse(
                gandalf.youShallNotPass(workspace.getId(), workspaceMember.getUserId()),
                "Must return false when user and workspace exist but user is only owner and member"
        );
    }

}
