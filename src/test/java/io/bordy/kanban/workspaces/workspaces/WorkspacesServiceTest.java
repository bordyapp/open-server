package io.bordy.kanban.workspaces.workspaces;

import io.bordy.api.WorkspaceDto;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;

@QuarkusTest
@TestTransaction
public class WorkspacesServiceTest {

    @Inject
    WorkspacesService workspacesService;

    @Test
    @DisplayName("Workspace must be created")
    public void createWorkspace() {
        var expectedName    = "Test Workspace";
        var expectedPhoto   = "s3://test-workspace.png";
        var expectedOwnerId = "auth0|id";

        var createdWorkspace = workspacesService.create(
                expectedName,
                expectedPhoto,
                expectedOwnerId
        );
        var persistedWorkspace = workspacesService.find(createdWorkspace.getId());

        Assertions.assertEquals(
                1, Workspace.count(),
                "Only 1 workspace must be created"
        );
        Assertions.assertEquals(
                createdWorkspace, persistedWorkspace,
                "Created workspace and persisted workspace must be equals"
        );

        Assertions.assertNotNull(createdWorkspace, "Created workspace must be returned");
        Assertions.assertNotNull(createdWorkspace.getId(), "Created workspace must has 'id'");
        Assertions.assertEquals(expectedName, createdWorkspace.getName(), "Created workspace must has given 'name'");
        Assertions.assertEquals(expectedPhoto, createdWorkspace.getPhoto(), "Created workspace must has given 'photo'");
        Assertions.assertEquals(expectedOwnerId, createdWorkspace.getOwnerId(), "Created workspace must has given 'ownerId'");
        Assertions.assertNotNull(createdWorkspace.getCreatedAt(), "Created workspace must has 'createdAt'");
        Assertions.assertNotNull(createdWorkspace.getCreatedAt(), "Created workspace must has 'editedAt'");
    }

    @Test
    @DisplayName("Workspace must be created and found by it's id")
    public void createAndFindWorkspace() {
        var expectedName    = "Test Workspace";
        var expectedPhoto   = "s3://test-workspace.png";
        var expectedOwnerId = "auth0|id";

        var createdWorkspace = workspacesService.create(
                expectedName,
                expectedPhoto,
                expectedOwnerId
        );
        Assertions.assertEquals(
                createdWorkspace, workspacesService.find(createdWorkspace.getId()),
                "Created workspace must be found by it's id and must be equal to persisted workspace"
        );
    }

    @Test
    @DisplayName("Return null instead of workspace when id is null")
    public void returnNullWhenIdIsNull() {
        Assertions.assertNull(
                workspacesService.find(null),
                "Must return null when id is null"
        );
    }

    @Test
    @DisplayName("Return null instead of workspace when id not belongs to workspace")
    public void returnNullWhenIdIsNotPresent() {
        Assertions.assertNull(
                workspacesService.find(UUID.randomUUID()),
                "Must return null when id not belongs to workspace"
        );
    }

    @Test
    @DisplayName("Return empty list when user dont has workspaces")
    public void returnEmptyListWhenUserDontHasWorkspaces() {
        var workspaces = workspacesService.findCreatedByUser("userId");

        Assertions.assertNotNull(
                workspaces,
                "Must return empty list when user dont has workspaces, not null"
        );
        Assertions.assertTrue(
                workspaces.isEmpty(),
                "Must return empty list when user dont has workspaces"
        );
    }

    @Test
    @DisplayName("Return user workspaces")
    public void returnUserWorkspaces() {
        var userId = "auth0|id";
        var userWorkspaces = new ArrayList<Workspace>();
        userWorkspaces.add(
                workspacesService.create(
                        "Test Workspace 1",
                        "s3://test-workspace-1.png",
                        userId
                )
        );
        userWorkspaces.add(
                workspacesService.create(
                        "Test Workspace 2",
                        "s3://test-workspace-2.png",
                        userId
                )
        );

        var foundWorkspaces = workspacesService.findCreatedByUser(userId);
        Assertions.assertFalse(
                foundWorkspaces.isEmpty(),
                "Must return non empty list when user has workspaces"
        );
        Assertions.assertEquals(userWorkspaces, foundWorkspaces);
    }

    @Test
    @DisplayName("Ensure that owner will be recognized correctly")
    public void recognizeOwnershipCorrectly() {
        var firstUser = "auth0|first-user";
        var firstUserWorkspace = workspacesService.create(
                "Test Workspace 1",
                "s3://test-workspace-1.png",
                firstUser
        );

        var secondUser = "auth0|second-user";
        var secondUserWorkspace = workspacesService.create(
                "Test Workspace 2",
                "s3://test-workspace-2.png",
                secondUser
        );

        Assertions.assertFalse(
                workspacesService.isWorkspaceOwner(firstUserWorkspace.getId(), secondUser),
                "Second user can't be owner of first user workspace"
        );
        Assertions.assertTrue(
                workspacesService.isWorkspaceOwner(firstUserWorkspace.getId(), firstUser),
                "First user must be owner of first user workspace"
        );

        Assertions.assertFalse(
                workspacesService.isWorkspaceOwner(secondUserWorkspace.getId(), firstUser),
                "First user can't be owner of second user workspace"
        );
        Assertions.assertTrue(
                workspacesService.isWorkspaceOwner(secondUserWorkspace.getId(), secondUser),
                "Second user must be owner of second user workspace"
        );
    }

    @Test
    @DisplayName("Ensure that only workspace owner can rename it")
    public void onlyWorkspaceOwnerCanRenameIt() {
        var firstUser = "auth0|first-user";
        var firstUserWorkspace = workspacesService.create(
                "Test Workspace 1",
                "s3://test-workspace-1.png",
                firstUser
        );

        var secondUser = "auth0|second-user";
        var secondUserWorkspace = workspacesService.create(
                "Test Workspace 2",
                "s3://test-workspace-2.png",
                secondUser
        );

        workspacesService.rename(firstUserWorkspace.getId(), secondUser, "New Test Workspace 1");
        Assertions.assertNotNull(
                workspacesService.find(firstUserWorkspace.getId()),
                "Ensure that renamed workspace still exists"
        );
        Assertions.assertEquals(
                firstUserWorkspace, workspacesService.find(firstUserWorkspace.getId()),
                "Ensure that first user can't modify workspace belonging to second user"
        );

        workspacesService.rename(secondUserWorkspace.getId(), secondUser, "New Test Workspace 2");
        var renamedWorkspace = workspacesService.find(secondUserWorkspace.getId());
        Assertions.assertNotNull(
                renamedWorkspace,
                "Ensure that renamed second workspace still exists"
        );
        Assertions.assertEquals(
                "New Test Workspace 2", renamedWorkspace.getName(),
                "Ensure that second workspace was renamed"
        );
        Assertions.assertEquals(
                secondUserWorkspace.getPhoto(), renamedWorkspace.getPhoto(),
                "Ensure that second workspace photo didn't change"
        );
        Assertions.assertEquals(
                secondUserWorkspace.getOwnerId(), renamedWorkspace.getOwnerId(),
                "Ensure that second workspace ownerId didn't change"
        );
        Assertions.assertEquals(
                secondUserWorkspace.getCreatedAt(), renamedWorkspace.getCreatedAt(),
                "Ensure that second workspace createdAt didn't change"
        );
        Assertions.assertTrue(
                renamedWorkspace.getEditedAt().after(secondUserWorkspace.getEditedAt()),
                "Ensure that second workspace editedAt field was edited"
        );
    }

    @Test
    @DisplayName("Ensure that user can change workspace photo")
    public void changeWorkspacePhoto() {
        var userId = "auth0|id";
        var workspace = workspacesService.create(
                "Test Workspace 1",
                "s3://test-workspace-new.png",
                userId
        );

        workspacesService.uploadPhoto(workspace.getId(), "s3://test-workspace-new.png");
        var updatedWorkspace = workspacesService.find(workspace.getId());
        Assertions.assertNotNull(
                updatedWorkspace,
                "Ensure that updated workspace still exists"
        );
        Assertions.assertEquals(
                workspace.getName(), updatedWorkspace.getName(),
                "Ensure that workspace name didn't change"
        );
        Assertions.assertEquals(
                "s3://test-workspace-new.png", updatedWorkspace.getPhoto(),
                "Ensure that workspace photo was changed"
        );
        Assertions.assertEquals(
                workspace.getOwnerId(), updatedWorkspace.getOwnerId(),
                "Ensure that workspace ownerId didn't change"
        );
        Assertions.assertEquals(
                workspace.getCreatedAt(), updatedWorkspace.getCreatedAt(),
                "Ensure that workspace createdAt didn't change"
        );
        Assertions.assertEquals(
                workspace.getEditedAt(), updatedWorkspace.getEditedAt(),
                "Ensure that workspace editedAt didn't change"
        );
    }

    @Test
    @DisplayName("Ensure that only workspace owner can delete it")
    public void onlyWorkspaceOwnerCanDeleteIt() {
        var firstUser = "auth0|first-user";
        var firstUserWorkspace = workspacesService.create(
                "Test Workspace 1",
                "s3://test-workspace-1.png",
                firstUser
        );

        var secondUser = "auth0|second-user";
        var secondUserWorkspace = workspacesService.create(
                "Test Workspace 2",
                "s3://test-workspace-2.png",
                secondUser
        );

        workspacesService.delete(firstUserWorkspace.getId(), secondUser);
        Assertions.assertNotNull(
                workspacesService.find(firstUserWorkspace.getId()),
                "Ensure that first workspace still exists"
        );
        Assertions.assertEquals(
                firstUserWorkspace, workspacesService.find(firstUserWorkspace.getId()),
                "Ensure that first workspace didn't change"
        );

        workspacesService.delete(secondUserWorkspace.getId(), secondUser);
        var deletedWorkspace = workspacesService.find(secondUserWorkspace.getId());
        Assertions.assertNull(
                deletedWorkspace,
                "Ensure that second workspace was deleted"
        );

        Assertions.assertNotNull(
                workspacesService.find(firstUserWorkspace.getId()),
                "Ensure that first workspace still exists"
        );
    }

    @Test
    @DisplayName("Ensure that will by converted correctly to base dto")
    public void toBaseDto() {
        var expectedId      = UUID.randomUUID();
        var expectedName    = "Test Workspace";
        var expectedPhoto   = "s3://test-workspace.png";
        var expectedOwnerId = "auth0|id";
        var expectedDate    = new Date();

        var workspace = new Workspace(
                expectedId,
                expectedName,
                expectedPhoto,
                expectedOwnerId,
                expectedDate,
                expectedDate
        );
        var expectedWorkspaceDto = new WorkspaceDto(
                expectedId,
                expectedName,
                expectedPhoto,
                expectedOwnerId,
                Collections.emptyList(),
                Collections.emptyList(),
                expectedDate,
                expectedDate
        );

        Assertions.assertEquals(
                expectedWorkspaceDto, workspacesService.toBaseDto(workspace),
                "Given workspace must be converted correctly to base dto"
        );
    }

    @Test
    @DisplayName("Ensure that will by converted correctly to dto")
    public void toDto() {
        var expectedId      = UUID.randomUUID();
        var expectedName    = "Test Workspace";
        var expectedPhoto   = "s3://test-workspace.png";
        var expectedOwnerId = "auth0|id";
        var expectedDate    = new Date();

        var workspace = new Workspace(
                expectedId,
                expectedName,
                expectedPhoto,
                expectedOwnerId,
                expectedDate,
                expectedDate
        );
        var expectedWorkspaceDto = new WorkspaceDto(
                expectedId,
                expectedName,
                expectedPhoto,
                expectedOwnerId,
                Collections.emptyList(),
                Collections.emptyList(),
                expectedDate,
                expectedDate
        );

        Assertions.assertEquals(
                expectedWorkspaceDto, workspacesService.toDto(workspace),
                "Given workspace must be converted correctly to dto"
        );
    }

}
