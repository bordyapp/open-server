package io.bordy.gateways.files;

import io.bordy.Gandalf;
import io.bordy.files.FilesService;
import io.quarkus.security.Authenticated;
import org.eclipse.microprofile.jwt.JsonWebToken;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.UUID;

@Authenticated
@RequestScoped
@Path("/api/v1/gateway")
public class FilesGateway {

    @Inject
    JsonWebToken jwt;

    @Inject
    FilesService filesService;

    @Inject
    Gandalf gandalf;

    @GET()
    @Path("/workspaces/{workspaceId}/files")
    @Produces(MediaType.APPLICATION_JSON)
    public Response files(
            @PathParam("workspaceId") String workspaceId
    ) {
        if (gandalf.youShallNotPass(UUID.fromString(workspaceId), jwt.getSubject())) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        return Response.ok(filesService.find(UUID.fromString(workspaceId))).build();
    }

    @POST()
    @Path("/workspaces/{workspaceId}/files")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createFile(
            @PathParam("workspaceId") String workspaceId,
            WriteFileDto writeFileDto
    ) {
        if (gandalf.youShallNotPass(UUID.fromString(workspaceId), jwt.getSubject())) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        return Response.ok(
                filesService.create(UUID.fromString(workspaceId), jwt.getSubject(), writeFileDto)
        ).build();
    }

    @PUT()
    @Path("/workspaces/{workspaceId}/files/{fileId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateFile(
            @PathParam("workspaceId") String workspaceId,
            @PathParam("fileId") String fileId,
            UpdateFileDto writeFileDto
    ) {
        var userId = jwt.getSubject();
        if (gandalf.youShallNotPass(UUID.fromString(workspaceId), userId)) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        var file = filesService.update(
                UUID.fromString(workspaceId),
                UUID.fromString(fileId),
                userId,
                writeFileDto
        );
        if (file == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return Response.ok(file).build();
    }

    @DELETE()
    @Path("/workspaces/{workspaceId}/files/{fileId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteFile(
            @PathParam("workspaceId") String workspaceId,
            @PathParam("fileId") String fileId
    ) {
        var userId = jwt.getSubject();
        if (gandalf.youShallNotPass(UUID.fromString(workspaceId), userId)) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        filesService.delete(
                UUID.fromString(workspaceId),
                UUID.fromString(fileId),
                userId
        );
        return Response.ok().build();
    }

}
