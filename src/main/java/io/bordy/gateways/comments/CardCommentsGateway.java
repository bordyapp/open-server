package io.bordy.gateways.comments;

import io.bordy.Gandalf;
import io.bordy.cards.comments.CommentsService;
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

public class CardCommentsGateway {

    @Inject
    JsonWebToken jwt;

    @Inject
    CommentsService commentsService;

    @Inject
    Gandalf gandalf;

    @GET()
    @Path("/workspaces/{workspaceId}/cards/{cardId}/comments")
    @Produces(MediaType.APPLICATION_JSON)
    public Response comments(
            @PathParam("workspaceId") String workspaceId,
            @PathParam("cardId") String cardId
    ) {
        if (gandalf.youShallNotPass(UUID.fromString(workspaceId), jwt.getSubject())) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        return Response.ok(
                commentsService.get(UUID.fromString(workspaceId), UUID.fromString(cardId))
        ).build();
    }

    @POST()
    @Path("/workspaces/{workspaceId}/cards/{cardId}/comments")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response writeComment(
            @PathParam("workspaceId") String workspaceId,
            @PathParam("cardId") String cardId,
            WriteCommentDto writeCommentDto
    ) {
        if (gandalf.youShallNotPass(UUID.fromString(workspaceId), jwt.getSubject())) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        return Response.ok(
                commentsService.write(
                        UUID.fromString(workspaceId),
                        UUID.fromString(cardId),
                        jwt.getSubject(),
                        writeCommentDto
                )
        ).build();
    }

    @PUT()
    @Path("/workspaces/{workspaceId}/cards/{cardId}/comments/{commentId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateComment(
            @PathParam("workspaceId") String workspaceId,
            @PathParam("cardId") String cardId,
            @PathParam("commentId") String commentId,
            UpdateCommentDto updateCommentDto
    ) {
        if (gandalf.youShallNotPass(UUID.fromString(workspaceId), jwt.getSubject())) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        if (!commentsService.isCreator(
                UUID.fromString(workspaceId),
                UUID.fromString(cardId),
                UUID.fromString(commentId),
                jwt.getSubject()
        )) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        var comment = commentsService.update(
                UUID.fromString(workspaceId),
                UUID.fromString(cardId),
                UUID.fromString(commentId),
                jwt.getSubject(),
                updateCommentDto
        );
        if (comment == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return Response.ok(comment).build();
    }

    @DELETE()
    @Path("/workspaces/{workspaceId}/cards/{cardId}/comments/{commentId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteComment(
            @PathParam("workspaceId") String workspaceId,
            @PathParam("cardId") String cardId,
            @PathParam("commentId") String commentId
    ) {
        if (gandalf.youShallNotPass(UUID.fromString(workspaceId), jwt.getSubject())) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        if (!commentsService.isCreator(
                UUID.fromString(workspaceId),
                UUID.fromString(cardId),
                UUID.fromString(commentId),
                jwt.getSubject()
        )) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        commentsService.delete(
                UUID.fromString(workspaceId),
                UUID.fromString(cardId),
                UUID.fromString(commentId),
                jwt.getSubject()
        );

        return Response.ok().build();
    }

}
