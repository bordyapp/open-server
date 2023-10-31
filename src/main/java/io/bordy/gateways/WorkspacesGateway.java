package io.bordy.gateways;

import io.bordy.api.CreateWorkspaceDto;
import io.bordy.api.UpdateWorkspaceMemberDto;
import io.bordy.api.WorkspaceDto;
import io.bordy.storage.CloudStorage;
import io.bordy.kanban.workspaces.members.WorkspaceMembersService;
import io.bordy.kanban.workspaces.invites.WorkspaceInvitesService;
import io.bordy.kanban.workspaces.workspaces.WorkspacesService;
import io.quarkus.security.Authenticated;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Stream;

@Authenticated
@RequestScoped
@Path("/api/v1/gateway")
public class WorkspacesGateway {

    private final static Logger logger = Logger.getLogger(WorkspacesGateway.class.getName());

    @Inject
    JsonWebToken jwt;

    @Inject
    WorkspacesService workspacesService;

    @Inject
    WorkspaceInvitesService workspaceInvitesService;

    @Inject
    WorkspaceMembersService workspaceMembersService;

    @Inject
    CloudStorage cloudStorage;

    @GET()
    @Path("/workspaces")
    @Produces(MediaType.APPLICATION_JSON)
    public List<WorkspaceDto> myWorkspaces() {
        var userId = jwt.getSubject();

        List<WorkspaceDto> created = workspacesService.findCreatedByUser(userId).stream()
                .map(workspace -> workspacesService.toDto(workspace))
                .toList();
        List<WorkspaceDto> memberOf = workspaceMembersService.memberOf(userId).stream()
                .map(workspaceId -> {
                    var workspace = workspacesService.find(workspaceId);
                    if (workspace == null) {
                        return null;
                    }

                    return workspacesService.toDto(workspace);
                })
                .filter(Objects::nonNull)
                .toList();

        return Stream.concat(created.stream(), memberOf.stream()).toList();
    }

    @POST()
    @Path("/workspaces")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public WorkspaceDto create(CreateWorkspaceDto createWorkspaceDto) {
        var workspace = workspacesService.create(
                createWorkspaceDto.name(),
                "",
                jwt.getSubject()
        );

        return workspacesService.toBaseDto(workspace);
    }

    @GET()
    @Path("/workspaces/{workspaceId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response find(@PathParam("workspaceId") String workspaceId) {
        if (!workspacesService.isWorkspaceOwner(UUID.fromString(workspaceId), jwt.getSubject())) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        var workspace = workspacesService.find(UUID.fromString(workspaceId));
        if (workspace == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return Response.ok(workspacesService.toDto(workspace)).build();
    }

    @POST()
    @Path("/workspaces/{workspaceId}/rename")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response rename(@PathParam("workspaceId") String workspaceId, CreateWorkspaceDto createWorkspaceDto) {
        if (!workspacesService.isWorkspaceOwner(UUID.fromString(workspaceId), jwt.getSubject())) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        workspacesService.rename(
                UUID.fromString(workspaceId),
                jwt.getSubject(),
                createWorkspaceDto.name()
        );
        return Response.ok().build();
    }

    @POST
    @Path("/workspaces/{workspaceId}/upload-photo")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadPhoto(
            @PathParam("workspaceId") String workspaceId,
            @RestForm("workspacePhoto") FileUpload image
    ) throws IOException {
        if (!workspacesService.isWorkspaceOwner(UUID.fromString(workspaceId), jwt.getSubject())) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        var photoUrl = cloudStorage.uploadWorkspacePhoto(image, workspaceId);
        workspacesService.uploadPhoto(UUID.fromString(workspaceId), photoUrl);
        return Response.ok(photoUrl).build();
    }

    @DELETE()
    @Path("/workspaces/{workspaceId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response delete(@PathParam("workspaceId") String workspaceId) {
        if (!workspacesService.isWorkspaceOwner(UUID.fromString(workspaceId), jwt.getSubject())) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        workspacesService.delete(UUID.fromString(workspaceId), jwt.getSubject());
        return Response.ok().build();
    }

    @PUT()
    @Path("/workspaces/{workspaceId}/members/{memberId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateMember(
            @PathParam("workspaceId") String workspaceId,
            @PathParam("memberId") String memberId,
            UpdateWorkspaceMemberDto updateWorkspaceMemberDto
    ) {
        if (!workspacesService.isWorkspaceOwner(UUID.fromString(workspaceId), jwt.getSubject())) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        workspaceMembersService.update(workspaceId, memberId, updateWorkspaceMemberDto);
        return Response.ok().build();
    }

    @DELETE()
    @Path("/workspaces/{workspaceId}/members/{memberId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteMember(@PathParam("workspaceId") String workspaceId, @PathParam("memberId") String memberId) {
        if (!workspacesService.isWorkspaceOwner(UUID.fromString(workspaceId), jwt.getSubject())) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        if (workspacesService.isWorkspaceOwner(UUID.fromString(workspaceId), memberId)) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        workspaceMembersService.delete(workspaceId, memberId);
        return Response.ok().build();
    }

    @POST()
    @Path("/workspaces/{workspaceId}/invites")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createInvite(@PathParam("workspaceId") String workspaceId, CreateInviteDto inviteDto) {
        if (!workspacesService.isWorkspaceOwner(UUID.fromString(workspaceId), jwt.getSubject())) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        var invite = workspaceInvitesService.create(
                UUID.fromString(workspaceId),
                inviteDto.email(),
                inviteDto.name(),
                inviteDto.role(),
                inviteDto.responsibilities()
        );

        return Response.ok(invite).build();
    }

    @DELETE()
    @Path("/workspaces/{workspaceId}/invites/{inviteId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteInvite(@PathParam("workspaceId") String workspaceId, @PathParam("inviteId") String inviteId) {
        if (!workspacesService.isWorkspaceOwner(UUID.fromString(workspaceId), jwt.getSubject())) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        workspaceInvitesService.delete(UUID.fromString(inviteId), UUID.fromString(workspaceId));
        return Response.ok().build();
    }

}
