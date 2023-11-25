package io.bordy.kanban.api.gateways.workspaces.workspaces;

import io.bordy.kanban.api.gateways.workspaces.workspaces.dto.CreateWorkspaceDto;
import io.bordy.kanban.api.gateways.workspaces.workspaces.dto.WorkspaceDto;
import io.bordy.kanban.workspaces.invites.WorkspaceInvite;
import io.bordy.kanban.workspaces.invites.WorkspaceInviteStatus;
import io.bordy.kanban.workspaces.members.WorkspaceMembersService;
import io.bordy.kanban.workspaces.workspaces.Workspace;
import io.bordy.kanban.workspaces.workspaces.WorkspacesService;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.oidc.Claim;
import io.quarkus.test.security.oidc.OidcSecurity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@QuarkusTest
@TestTransaction
@TestSecurity(user = WorkspacesGatewayTest.SUMMER_SMITH)
@OidcSecurity(claims = {
        @Claim(key = "sub", value = WorkspacesGatewayTest.SUMMER_SMITH),
})
public class WorkspacesGatewayTest {

    @Inject
    WorkspacesService workspacesService;

    @Inject
    WorkspaceMembersService workspaceMembersService;

    @Inject
    WorkspacesGateway workspacesGateway;

    public static final String RICK_SANCHEZ = "auth0|rick-sanchez";
    public static final String MORTY_SMITH  = "auth0|morty-smith";
    public static final String SUMMER_SMITH = "auth0|summer-smith";

    private List<WorkspaceDto> createWorkspaces(String userId) {
        var workspaces = new LinkedList<WorkspaceDto>();
        workspaces.add(workspacesService.toDto(workspacesService.create("first workspace", "", userId)));
        workspaces.add(workspacesService.toDto(workspacesService.create("second workspace", "", userId)));

        return workspaces;
    }

    private void addUserToWorkspace(
            String userId,
            String userName,
            String userRole,
            String userResponsibilities,
            UUID workspaceId
    ) {
        workspaceMembersService.create(userId, new WorkspaceInvite(
                UUID.randomUUID(),
                workspaceId,
                "user@mail.com",
                userName,
                userRole,
                userResponsibilities,
                WorkspaceInviteStatus.PENDING,
                new Date(),
                new Date()
        ));
    }

    @Test
    @DisplayName("myWorkspaces: return empty list when user doesn't have workspaces or isn't member of any")
    public void myWorkspacesReturnEmptyListWhenUserDoesntHaveWorkspacesOrIsNotMemberOfAny() {
        var rickWorkspaces = createWorkspaces(RICK_SANCHEZ);
        rickWorkspaces.forEach(
                workspaceDto -> addUserToWorkspace(
                        MORTY_SMITH, "Morty", "grandson", "", workspaceDto.id()
                )
        );

        var mortyWorkspaces = createWorkspaces(MORTY_SMITH);
        mortyWorkspaces.forEach(
                workspaceDto -> addUserToWorkspace(
                        RICK_SANCHEZ, "Morty", "grandson", "", workspaceDto.id()
                )
        );

        Assertions.assertTrue(
                workspacesGateway.myWorkspaces().isEmpty(),
                "Must be empty list when user doesn't have workspaces or isn't member of any"
        );
    }

    @Test
    @DisplayName("myWorkspaces: return only workspaces created by user")
    public void myWorkspacesReturnOnlyWorkspacesCreatedByUser() {
        var rickWorkspaces = createWorkspaces(RICK_SANCHEZ);
        rickWorkspaces.forEach(
                workspaceDto -> addUserToWorkspace(
                        MORTY_SMITH, "Morty", "grandson", "", workspaceDto.id()
                )
        );

        var mortyWorkspaces = createWorkspaces(MORTY_SMITH);
        mortyWorkspaces.forEach(
                workspaceDto -> addUserToWorkspace(
                        RICK_SANCHEZ, "Morty", "grandson", "", workspaceDto.id()
                )
        );

        var summerWorkspaces = createWorkspaces(SUMMER_SMITH);
        Assertions.assertEquals(
                summerWorkspaces, workspacesGateway.myWorkspaces(),
                "Must return only workspaces created by user"
        );
    }

    @Test
    @DisplayName("myWorkspaces: return only workspaces where user is member")
    public void myWorkspacesReturnOnlyWorkspacesWhereUserIsMember() {
        var rickWorkspaces = createWorkspaces(RICK_SANCHEZ);
        rickWorkspaces.forEach(
                workspaceDto -> {
                    addUserToWorkspace(
                            MORTY_SMITH, "Morty", "grandson", "", workspaceDto.id()
                    );
                    addUserToWorkspace(
                            SUMMER_SMITH, "Summer", "granddaughter", "", workspaceDto.id()
                    );
                }
        );

        var mortyWorkspaces = createWorkspaces(MORTY_SMITH);
        mortyWorkspaces.forEach(
                workspaceDto -> addUserToWorkspace(
                        RICK_SANCHEZ, "Morty", "grandson", "", workspaceDto.id()
                )
        );

        Assertions.assertEquals(
                rickWorkspaces, workspacesGateway.myWorkspaces(),
                "Must return only workspaces where user is member"
        );
    }

    @Test
    @DisplayName("myWorkspaces: return only workspaces created by user or where user is member")
    public void myWorkspacesReturnOnlyWorkspacesCreatedByUserOrWhereUserIsMember() {
        var rickWorkspaces = createWorkspaces(RICK_SANCHEZ);
        rickWorkspaces.forEach(
                workspaceDto -> {
                    addUserToWorkspace(
                            MORTY_SMITH, "Morty", "grandson", "", workspaceDto.id()
                    );
                    addUserToWorkspace(
                            SUMMER_SMITH, "Summer", "granddaughter", "", workspaceDto.id()
                    );
                }
        );

        var mortyWorkspaces = createWorkspaces(MORTY_SMITH);
        mortyWorkspaces.forEach(
                workspaceDto -> addUserToWorkspace(
                        RICK_SANCHEZ, "Morty", "grandson", "", workspaceDto.id()
                )
        );

        var summerWorkspaces = createWorkspaces(SUMMER_SMITH);
        summerWorkspaces.addAll(rickWorkspaces);
        Assertions.assertEquals(
                summerWorkspaces, workspacesGateway.myWorkspaces(),
                "Must return only workspaces created by user"
        );
    }

    public static Stream<Arguments> workspacesToCreate() {
        return Stream.of(
                Arguments.of(new CreateWorkspaceDto("")),
                Arguments.of(new CreateWorkspaceDto(" ")),
                Arguments.of(new CreateWorkspaceDto("Workspace to create"))
        );
    }

    @ParameterizedTest
    @MethodSource("workspacesToCreate")
    @DisplayName("create: create workspace")
    public void create(CreateWorkspaceDto createWorkspaceDto) {
        var createdWorkspace = workspacesGateway.create(createWorkspaceDto);
        var persistedWorkspace = Workspace.<Workspace>findById(createdWorkspace.id());

        Assertions.assertNotNull(createdWorkspace, "Workspace must be created");
        Assertions.assertEquals(
                createdWorkspace.name(), createWorkspaceDto.name(),
                "Workspace must be created with given name"
        );
        Assertions.assertEquals(
                createdWorkspace.ownerId(), SUMMER_SMITH,
                "Workspace must be created with given owner"
        );
        Assertions.assertEquals(
                createdWorkspace, workspacesService.toBaseDto(persistedWorkspace),
                "Created workspace and persisted workspace must be equal"
        );
    }

    @Test
    @DisplayName("find: return forbidden when user is not workspace owner")
    public void findReturnForbiddenWhenUserIsNotOwner() {
        var rickWorkspaces = createWorkspaces(RICK_SANCHEZ);
        var workspace = rickWorkspaces.get(0);

        var response = workspacesGateway.find(workspace.id().toString());
        Assertions.assertEquals(
                response.getStatusInfo(), Response.Status.FORBIDDEN,
                "Must return FORBIDDEN when user is not workspace owner"
        );
        Assertions.assertNull(
                response.getEntity(),
                "Response must be without body"
        );
    }

    @Test
    @DisplayName("find: return forbidden when workspace doesn't exist")
    public void findReturnNothingWhenWorkspaceDoesntExist() {
        createWorkspaces(SUMMER_SMITH);

        var response = workspacesGateway.find("93a106b0-7f1f-458d-8810-6ec998065c48");
        Assertions.assertEquals(
                response.getStatusInfo(), Response.Status.FORBIDDEN,
                "Must return NOT_FOUND when workspace doesn't exist"
        );
        Assertions.assertNull(
                response.getEntity(),
                "Response must be without body"
        );
    }

    @Test
    @DisplayName("find: find and return workspace")
    public void find() {
        var summerWorkspaces = createWorkspaces(SUMMER_SMITH);
        var workspace = summerWorkspaces.get(0);

        var response = workspacesGateway.find(workspace.id().toString());
        Assertions.assertEquals(
                response.getStatusInfo(), Response.Status.OK,
                "Must return OK"
        );
        Assertions.assertEquals(
                response.getEntity(), workspace,
                "Must return correct workspace"
        );
    }

}
