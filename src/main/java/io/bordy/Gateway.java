package io.bordy;

import io.bordy.api.*;
import io.bordy.cards.BoardListCardsRepository;
import io.bordy.cards.BoardListCardsService;
import io.bordy.lexorank.Lexorank;
import io.bordy.lists.BoardList;
import io.bordy.lists.BoardListsRepository;
import io.bordy.lists.BoardListsService;
import io.bordy.users.User;
import io.bordy.users.UsersService;
import io.bordy.workspaces.*;
import io.quarkus.security.Authenticated;
import org.bson.Document;
import org.eclipse.microprofile.jwt.JsonWebToken;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

// https://auth0.com/blog/quarkus-and-auth0-integration/
// https://quarkus.io/guides/security-jwt
// https://itnext.io/secures-restful-apis-with-quarkus-oidc-and-auth0-643475a9710c
@Authenticated
@Path("/api/v1/gateway")
@RequestScoped
public class Gateway {

    private final static Logger logger = Logger.getLogger(Gateway.class.getName());

    @Inject
    JsonWebToken jwt;

    @Inject
    WorkspaceMembersRepository workspaceMembersRepository;

    @Inject
    WorkspaceElementsRepository workspaceElementsRepository;

    @Inject
    BoardListsRepository boardListsRepository;

    @Inject
    BoardListCardsRepository boardListCardsRepository;

    @Inject
    Shredder shredder;

    @Inject
    Gandalf gandalf;

    @Inject
    BoardListsService boardListsService;

    @Inject
    BoardListCardsService boardListCardsService;

    @Inject
    WorkspaceElementsService workspaceElementsService;

    @Inject
    WorkspaceInvitesRepository workspaceInvitesRepository;

    @Inject
    Lexorank lexorank;

    @Inject
    UsersService usersService;

    @POST
    @Path("/my-self")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response mySelf(MySelf mySelf) {
        var me = User.findById(jwt.getSubject());
        if (me == null) {
            User.persist(new User(
                    jwt.getSubject(),
                    mySelf.email(),
                    mySelf.nickname(),
                    mySelf.picture()
            ));
        }

        return Response.ok().build();
    }

    @GET()
    @Path("/workspaces/{workspaceId}/my-tasks")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMyTasks(@PathParam("workspaceId") String workspaceId) {
        if (gandalf.youShallNotPass(UUID.fromString(workspaceId), jwt.getSubject())) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        var knownAssignees = usersService.workspaceKnownAssignees(UUID.fromString(workspaceId));
        var response = new LinkedHashSet<BoardListCardDto>();
        response.addAll(boardListCardsService.getCardsByCreatorUserId(UUID.fromString(workspaceId), jwt.getSubject()).stream().parallel().map(boardListCard -> {
            var assignees = Collections.<AssigneeDto>emptySet();
            if (boardListCard.getAssignees() != null) {
                assignees = boardListCard.getAssignees().stream().map(knownAssignees::get).filter(Objects::nonNull).collect(Collectors.toSet());
            }

            return new BoardListCardDto(
                    boardListCard.getId(),
                    boardListCard.getName(),
                    boardListCard.getDescription(),
                    boardListCard.getWorkspaceId(),
                    boardListCard.getBoardId(),
                    boardListCard.getListId(),
                    boardListCard.getBoundBoardId(),
                    boardListCard.getPriority(),
                    boardListCard.getStatus(),
                    boardListCard.getDueDate(),
                    boardListCard.getResources(),
                    boardListCard.getLabels(),
                    assignees,
                    boardListCard.getAssignedUserId(),
                    boardListCard.getCreatorUserId(),
                    boardListCard.isDone(),
                    boardListCard.getCreatedAt(),
                    boardListCard.getEditedAt(),
                    0,
                    0,
                    0,
                    boardListCard.getLexorank()
            );
        }).toList());
        response.addAll(boardListCardsService.getAssignedCards(UUID.fromString(workspaceId), jwt.getSubject()).stream().parallel().map(boardListCard -> {
            var assignees = Collections.<AssigneeDto>emptySet();
            if (boardListCard.getAssignees() != null) {
                assignees = boardListCard.getAssignees().stream().map(knownAssignees::get).filter(Objects::nonNull).collect(Collectors.toSet());
            }

            return new BoardListCardDto(
                    boardListCard.getId(),
                    boardListCard.getName(),
                    boardListCard.getDescription(),
                    boardListCard.getWorkspaceId(),
                    boardListCard.getBoardId(),
                    boardListCard.getListId(),
                    boardListCard.getBoundBoardId(),
                    boardListCard.getPriority(),
                    boardListCard.getStatus(),
                    boardListCard.getDueDate(),
                    boardListCard.getResources(),
                    boardListCard.getLabels(),
                    assignees,
                    boardListCard.getAssignedUserId(),
                    boardListCard.getCreatorUserId(),
                    boardListCard.isDone(),
                    boardListCard.getCreatedAt(),
                    boardListCard.getEditedAt(),
                    0,
                    0,
                    0,
                    boardListCard.getLexorank()
            );
        }).toList());

        return Response.ok(response).build();
    }

    @GET()
    @Path("/workspaces/{workspaceId}/elements")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserWorkspaceElements(@PathParam("workspaceId") String workspaceId) {
        if (gandalf.youShallNotPass(UUID.fromString(workspaceId), jwt.getSubject())) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        return Response.ok(
                workspaceElementsService.getOnlyBoards(UUID.fromString(workspaceId))
        ).build();
    }

    @POST()
    @Path("/workspaces/{workspaceId}/elements")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createUserWorkspaceElement(@PathParam("workspaceId") String workspaceId, CreateWorkspaceElementDto workspaceElementDto) {
        if (gandalf.youShallNotPass(UUID.fromString(workspaceId), jwt.getSubject())) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        var creationDate = new Date();
        UUID folderId = null;
        if (workspaceElementDto.folderId() != null) {
            folderId = UUID.fromString(workspaceElementDto.folderId());
        }
        var workspaceElement = new WorkspaceElement(
                UUID.randomUUID(),
                workspaceElementDto.name(),
                UUID.fromString(workspaceId),
                folderId,
                workspaceElementDto.type(),
                jwt.getSubject(),
                workspaceElementDto.type() == WorkspaceElementType.BOARD ? true : null,
                creationDate,
                creationDate
        );
        workspaceElementsRepository.persist(workspaceElement);

        if (workspaceElement.getType() == WorkspaceElementType.BOARD) {
            var lexoranks = lexorank.getDefaultRank(3);
            boardListsRepository.persist(new BoardList(
                    UUID.randomUUID(),
                    "To Do",
                    UUID.fromString(workspaceId),
                    workspaceElement.getId(),
                    null,
                    Collections.emptyList(),
                    jwt.getSubject(),
                    creationDate,
                    creationDate,
                    lexoranks.get(0)
            ));
            boardListsRepository.persist(new BoardList(
                    UUID.randomUUID(),
                    "In Progress",
                    UUID.fromString(workspaceId),
                    workspaceElement.getId(),
                    null,
                    Collections.emptyList(),
                    jwt.getSubject(),
                    creationDate,
                    creationDate,
                    lexoranks.get(1)
            ));
            boardListsRepository.persist(new BoardList(
                    UUID.randomUUID(),
                    "Done",
                    UUID.fromString(workspaceId),
                    workspaceElement.getId(),
                    null,
                    Collections.emptyList(),
                    jwt.getSubject(),
                    creationDate,
                    creationDate,
                    lexoranks.get(2)
            ));
        }

        return Response.ok(workspaceElement).build();
    }

    @GET()
    @Path("/workspaces/{workspaceId}/elements/{workspaceElementId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBoardOrFolder(@PathParam("workspaceId") String workspaceId, @PathParam("workspaceElementId") String workspaceElementId) {
        if (gandalf.youShallNotPass(UUID.fromString(workspaceId), jwt.getSubject())) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        return Response.ok(
                workspaceElementsService.getBoardOrFolder(
                        UUID.fromString(workspaceId),
                        UUID.fromString(workspaceElementId)
                )
        ).build();
    }

    @DELETE()
    @Path("/workspaces/{workspaceId}/elements/{workspaceElementId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteBoardOrFolder(@PathParam("workspaceId") String workspaceId, @PathParam("workspaceElementId") String workspaceElementId) {
        if (gandalf.youShallNotPass(UUID.fromString(workspaceId), jwt.getSubject())) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        var workspaceElement = workspaceElementsRepository.findById(UUID.fromString(workspaceElementId));
        if (workspaceElement == null) {
            logger.warning("workspaceElement is null - " + workspaceElementId);
            return Response.ok().build();
        }

        if (WorkspaceElementType.BOARD == workspaceElement.getType()) {
            shredder.deleteBoard(workspaceElement);
        }
        if (WorkspaceElementType.FOLDER == workspaceElement.getType()) {
            shredder.deleteFolder(workspaceElement);
        }
        logger.info("workspaceElement - " + workspaceElementId + " was deleted");

        return Response.ok().build();
    }

    @POST()
    @Path("/workspaces/{workspaceId}/elements/{workspaceElementId}/remove-from-folder")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response removeBoardFromFolder(@PathParam("workspaceId") String workspaceId, @PathParam("workspaceElementId") String workspaceElementId) {
        if (gandalf.youShallNotPass(UUID.fromString(workspaceId), jwt.getSubject())) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        workspaceElementsRepository.update(
                        "folderId = ?1 and editedAt = ?2",
                        null,
                        new Date()
                )
                .where(
                        "_id = ?1 and workspaceId = ?2",
                        UUID.fromString(workspaceElementId),
                        UUID.fromString(workspaceId)
                );

        return Response.ok().build();
    }

    @POST()
    @Path("/workspaces/{workspaceId}/elements/{workspaceElementId}/add-to-folder")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addBoardToFolder(@PathParam("workspaceId") String workspaceId, @PathParam("workspaceElementId") String workspaceElementId, MoveOperation moveOperation) {
        if (gandalf.youShallNotPass(UUID.fromString(workspaceId), jwt.getSubject())) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        // TODO: Check that is board
        workspaceElementsRepository.update(
                        "folderId = ?1 and editedAt = ?2",
                        UUID.fromString(moveOperation.folderId()),
                        new Date()
                )
                .where(
                        "_id = ?1 and workspaceId = ?2",
                        UUID.fromString(workspaceElementId),
                        UUID.fromString(workspaceId)
                );

        return Response.ok().build();
    }

    @POST()
    @Path("/workspaces/{workspaceId}/elements/{workspaceElementId}/rename")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response renameBoardOrFolder(@PathParam("workspaceId") String workspaceId, @PathParam("workspaceElementId") String workspaceElementId, RenameOperation renameOperation) {
        if (gandalf.youShallNotPass(UUID.fromString(workspaceId), jwt.getSubject())) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        workspaceElementsRepository.update(
                        "name = ?1 and editedAt = ?2",
                        renameOperation.name(),
                        new Date()
                )
                .where(
                        "_id = ?1 and workspaceId = ?2",
                        UUID.fromString(workspaceElementId),
                        UUID.fromString(workspaceId)
                );

        return Response.ok().build();
    }

    @POST()
    @Path("/invites/{inviteId}/accept")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response acceptWorkspaceInvite(@PathParam("inviteId") String inviteId) {
        var invite = workspaceInvitesRepository.findById(UUID.fromString(inviteId));
        User me = User.findById(jwt.getSubject());
        if (invite == null || me == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        if (invite.getStatus() == WorkspaceInviteStatus.ACCEPTED) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        if (!invite.getEmail().equals(me.email)) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        // http://localhost:5173/#/invite3df53003-d5ba-45e8-91e5-94c54afaa332
        var member = workspaceMembersRepository.list(
                "userId = ?1 and workspaceId = ?2",
                jwt.getSubject(),
                invite.getWorkspaceId()
        );
        if (member.isEmpty()) {
            var createdAt = new Date();
            workspaceMembersRepository.persist(new WorkspaceMember(
                    UUID.randomUUID(),
                    invite.getWorkspaceId(),
                    jwt.getSubject(),
                    invite.getName(),
                    invite.getRole(),
                    invite.getResponsibilities(),
                    createdAt,
                    createdAt
            ));

            invite.setStatus(WorkspaceInviteStatus.ACCEPTED);
            invite.update();
        }

        return Response.ok().build();
    }

    @POST()
    @Path("/workspaces/{workspaceId}/boards/{boardId}/lists")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createBoardList(
            @PathParam("workspaceId") String workspaceId,
            @PathParam("boardId") String boardId,
            WriteBoardListDto writeBoardListDto
    ) {
        if (gandalf.youShallNotPass(UUID.fromString(workspaceId), jwt.getSubject())) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        var boardList = boardListsService.create(
                workspaceId,
                boardId,
                jwt.getSubject(),
                writeBoardListDto
        );

        return Response.ok(boardListsService.toDto(boardList, Collections.emptyList())).build();
    }

    @PUT()
    @Path("/workspaces/{workspaceId}/boards/{boardId}/lists/{listId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateBoardList(
            @PathParam("workspaceId") String workspaceId,
            @PathParam("boardId") String boardId,
            @PathParam("listId") String listId,
            WriteBoardListDto writeBoardListDto
    ) {
        if (gandalf.youShallNotPass(UUID.fromString(workspaceId), jwt.getSubject())) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        var boardList = boardListsService.update(workspaceId, boardId, listId, writeBoardListDto);
        return Response.ok(boardListsService.toDto(boardList, Collections.emptyList())).build();
    }

    @DELETE()
    @Path("/workspaces/{workspaceId}/boards/{boardId}/lists/{listId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteBoardList(
            @PathParam("workspaceId") String workspaceId,
            @PathParam("boardId") String boardId,
            @PathParam("listId") String listId
    ) {
        if (gandalf.youShallNotPass(UUID.fromString(workspaceId), jwt.getSubject())) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        var boardList = boardListsRepository.findById(UUID.fromString(listId));
        if (boardList == null) {
            return Response.ok().build();
        }

        shredder.deleteList(boardList);

        return Response.ok().build();
    }

    @POST()
    @Path("/workspaces/{workspaceId}/boards/{boardId}/lists/{listId}/reorder")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response reorderBoardList(
            @PathParam("workspaceId") String workspaceId,
            @PathParam("boardId") String boardId,
            @PathParam("listId") String listId,
            MoveOperation moveOperation
    ) {
        if (gandalf.youShallNotPass(UUID.fromString(workspaceId), jwt.getSubject())) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        var lexorank = boardListsService.reorder(
                UUID.fromString(workspaceId),
                UUID.fromString(boardId),
                UUID.fromString(listId),
                moveOperation.placeAfterElementWithId(),
                moveOperation.placeBeforeElementWithId()
        );

        return Response.ok(lexorank).build();
    }

    @POST()
    @Path("/workspaces/{workspaceId}/boards/{boardId}/lists/{listId}/convert-to-board")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response convertBoardListToBoard(
            @PathParam("workspaceId") String workspaceId,
            @PathParam("boardId") String boardId,
            @PathParam("listId") String listId
    ) {
        if (gandalf.youShallNotPass(UUID.fromString(workspaceId), jwt.getSubject())) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        var board = boardListsService.convertToBoard(
                jwt.getSubject(),
                UUID.fromString(workspaceId),
                UUID.fromString(boardId),
                UUID.fromString(listId)
        );
        if (board == null) {
            // TODO: Return ok if board already converted
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return Response.ok(board).build();
    }

    @POST()
    @Path("/workspaces/{workspaceId}/boards/{boardId}/lists/{listId}/cards")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createBoardListCard(
            @PathParam("workspaceId") String workspaceId,
            @PathParam("boardId") String boardId,
            @PathParam("listId") String listId,
            WriteBoardListCardDto writeBoardListCardDto
    ) {
        if (gandalf.youShallNotPass(UUID.fromString(workspaceId), jwt.getSubject())) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        var boardListCard = boardListCardsService.create(
                workspaceId,
                boardId,
                listId,
                jwt.getSubject(),
                writeBoardListCardDto
        );

        var assignees = Collections.<AssigneeDto>emptySet();
        if (boardListCard.getAssignees() != null) {
            assignees = boardListCard.getAssignees().stream().map(assagnee -> {
                var user = User.<User>findById(assagnee);
                var workspaceMember = workspaceMembersRepository.list("userId", user.id).stream().findFirst().orElse(null);
                if (user != null) {
                    if (workspaceMember != null) {
                        return new AssigneeDto(
                                user.id,
                                workspaceMember.getName(),
                                user.picture
                        );
                    } else {
                        return new AssigneeDto(
                                user.id,
                                user.getNickname(),
                                user.picture
                        );
                    }
                }
                return null;
            }).filter(Objects::nonNull).collect(Collectors.toSet());
        }
        return Response.ok(new BoardListCardDto(
                boardListCard.getId(),
                boardListCard.getName(),
                boardListCard.getDescription(),
                boardListCard.getWorkspaceId(),
                boardListCard.getBoardId(),
                boardListCard.getListId(),
                boardListCard.getBoundBoardId(),
                boardListCard.getPriority(),
                boardListCard.getStatus(),
                boardListCard.getDueDate(),
                boardListCard.getResources(),
                boardListCard.getLabels(),
                assignees,
                boardListCard.getAssignedUserId(),
                boardListCard.getCreatorUserId(),
                boardListCard.isDone(),
                boardListCard.getCreatedAt(),
                boardListCard.getEditedAt(),
                0, // TODO: FILL
                0, // TODO: FILL,
                0,
                boardListCard.getLexorank()
        )).build();
    }

    @PUT()
    @Path("/workspaces/{workspaceId}/boards/{boardId}/lists/{listId}/cards/{cardId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateBoardListCard(
            @PathParam("workspaceId") String workspaceId,
            @PathParam("boardId") String boardId,
            @PathParam("listId") String listId,
            @PathParam("cardId") String cardId,
            WriteBoardListCardDto writeBoardListCardDto
    ) {
        if (gandalf.youShallNotPass(UUID.fromString(workspaceId), jwt.getSubject())) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        var filter = new HashMap<String, UUID>();
        filter.put("_id", UUID.fromString(cardId));
        filter.put("workspaceId", UUID.fromString(workspaceId));
        filter.put("boardId", UUID.fromString(boardId));
        filter.put("listId", UUID.fromString(listId));

        var update = new HashMap<String, Object>();
        update.put("name", writeBoardListCardDto.name());
        update.put("description", writeBoardListCardDto.description());
        update.put("priority", writeBoardListCardDto.priority());
        update.put("status", writeBoardListCardDto.status());
        update.put("dueDate", writeBoardListCardDto.dueDate());
        update.put("resources", writeBoardListCardDto.resources());
        update.put("labels", writeBoardListCardDto.labels());
        update.put("assignedUserId", writeBoardListCardDto.assignedUserId());
        update.put("assignees", writeBoardListCardDto.assignees());
        update.put("done", writeBoardListCardDto.isDone());
        update.put("editedAt", new Date());
        boardListCardsRepository.mongoCollection().updateOne(
                new Document(filter),
                new Document("$set", new Document(update))
        );

        var boardListCard = boardListCardsRepository.find("_id = ?1", UUID.fromString(cardId)).firstResult();
        var assignees = Collections.<AssigneeDto>emptySet();
        if (boardListCard.getAssignees() != null) {
            assignees = boardListCard.getAssignees().stream().map(assagnee -> {
                var user = User.<User>findById(assagnee);
                var workspaceMember = workspaceMembersRepository.list("userId", user.id).stream().findFirst().orElse(null);
                if (user != null) {
                    if (workspaceMember != null) {
                        return new AssigneeDto(
                                user.id,
                                workspaceMember.getName(),
                                user.picture
                        );
                    } else {
                        return new AssigneeDto(
                                user.id,
                                user.getNickname(),
                                user.picture
                        );
                    }
                }
                return null;
            }).filter(Objects::nonNull).collect(Collectors.toSet());
        }
        return Response.ok(new BoardListCardDto(
                boardListCard.getId(),
                boardListCard.getName(),
                boardListCard.getDescription(),
                boardListCard.getWorkspaceId(),
                boardListCard.getBoardId(),
                boardListCard.getListId(),
                boardListCard.getBoundBoardId(),
                boardListCard.getPriority(),
                boardListCard.getStatus(),
                boardListCard.getDueDate(),
                boardListCard.getResources(),
                boardListCard.getLabels(),
                assignees,
                boardListCard.getAssignedUserId(),
                boardListCard.getCreatorUserId(),
                boardListCard.isDone(),
                boardListCard.getCreatedAt(),
                boardListCard.getEditedAt(),
                0, // TODO: FILL
                0, // TODO: FILL,
                0,
                boardListCard.getLexorank()
        )).build();
    }

    @DELETE()
    @Path("/workspaces/{workspaceId}/boards/{boardId}/lists/{listId}/cards/{cardId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteBoardListCard(
            @PathParam("workspaceId") String workspaceId,
            @PathParam("boardId") String boardId,
            @PathParam("listId") String listId,
            @PathParam("cardId") String cardId
    ) {
        if (gandalf.youShallNotPass(UUID.fromString(workspaceId), jwt.getSubject())) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        var boardListCard = boardListCardsRepository.findById(UUID.fromString(cardId));
        if (boardListCard == null) {
            return Response.ok().build();
        }

        shredder.deleteCard(boardListCard);

        return Response.ok().build();
    }

    @POST()
    @Path("/workspaces/{workspaceId}/boards/{boardId}/lists/{listId}/cards/{cardId}/convert-to-board")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response convertBoardListCardToBoard(
            @PathParam("workspaceId") String workspaceId,
            @PathParam("boardId") String boardId,
            @PathParam("listId") String listId,
            @PathParam("cardId") String cardId
    ) {
        if (gandalf.youShallNotPass(UUID.fromString(workspaceId), jwt.getSubject())) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        var board = boardListCardsService.convertToBoard(
                jwt.getSubject(),
                UUID.fromString(workspaceId),
                UUID.fromString(boardId),
                UUID.fromString(listId),
                UUID.fromString(cardId)
        );
        if (board == null) {
            // TODO: Return ok if board already converted
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return Response.ok(board).build();
    }

    @POST()
    @Path("/workspaces/{workspaceId}/boards/{boardId}/lists/{listId}/cards/{cardId}/mark-as-done")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response markBoardListCardAsDone(
            @PathParam("workspaceId") String workspaceId,
            @PathParam("boardId") String boardId,
            @PathParam("listId") String listId,
            @PathParam("cardId") String cardId
    ) {
        if (gandalf.youShallNotPass(UUID.fromString(workspaceId), jwt.getSubject())) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        boardListCardsService.setDoneTo(
                UUID.fromString(workspaceId),
                UUID.fromString(boardId),
                UUID.fromString(listId),
                UUID.fromString(cardId),
                true
        );

        return Response.ok().build();
    }

    @POST()
    @Path("/workspaces/{workspaceId}/boards/{boardId}/lists/{listId}/cards/{cardId}/mark-as-not-done")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response markBoardListCardAsNotDone(
            @PathParam("workspaceId") String workspaceId,
            @PathParam("boardId") String boardId,
            @PathParam("listId") String listId,
            @PathParam("cardId") String cardId
    ) {
        if (gandalf.youShallNotPass(UUID.fromString(workspaceId), jwt.getSubject())) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        boardListCardsService.setDoneTo(
                UUID.fromString(workspaceId),
                UUID.fromString(boardId),
                UUID.fromString(listId),
                UUID.fromString(cardId),
                false
        );

        return Response.ok().build();
    }

    @POST()
    @Path("/workspaces/{workspaceId}/boards/{boardId}/lists/{listId}/cards/{cardId}/move")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response moveBoardListCard(
            @PathParam("workspaceId") String workspaceId,
            @PathParam("boardId") String boardId,
            @PathParam("listId") String listId,
            @PathParam("cardId") String cardId,
            MoveOperation moveOperation
    ) {
        if (gandalf.youShallNotPass(UUID.fromString(workspaceId), jwt.getSubject())) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        var lexorank = boardListCardsService.reorder(
                UUID.fromString(workspaceId),
                UUID.fromString(boardId),
                UUID.fromString(listId),
                UUID.fromString(cardId),
                moveOperation.placeAfterElementWithId(),
                moveOperation.placeBeforeElementWithId(),
                moveOperation.listId()
        );

        return Response.ok(lexorank).build();
    }

}
