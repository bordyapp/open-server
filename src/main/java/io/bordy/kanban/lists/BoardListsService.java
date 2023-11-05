package io.bordy.kanban.lists;

import io.bordy.api.*;
import io.bordy.cards.BoardListCard;
import io.bordy.cards.BoardListCardStatus;
import io.bordy.cards.BoardListCardsRepository;
import io.bordy.lexorank.Lexorank;
import io.bordy.kanban.workspaces.elements.WorkspaceElement;
import io.bordy.kanban.workspaces.elements.WorkspaceElementType;
import io.bordy.kanban.workspaces.elements.WorkspaceElementsRepository;
import io.bordy.users.UsersService;
import org.bson.Document;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@ApplicationScoped
public class BoardListsService {

    private final static Logger logger = Logger.getLogger(BoardListsService.class.getName());

    @Inject
    WorkspaceElementsRepository workspaceElementsRepository;

    @Inject
    BoardListsRepository boardListsRepository;

    @Inject
    BoardListCardsRepository boardListCardsRepository;

    @Inject
    UsersService usersService;

    @Inject
    Lexorank lexorank;

    @Transactional
    public String reorder(
            UUID workspaceId,
            UUID boardId,
            UUID listId,
            String placeAfterListId,
            String placeBeforeListId
    ) {
        String afterListLexorank = Lexorank.START_POSITION;
        if (placeAfterListId != null && !placeAfterListId.isBlank()) {
            var list = boardListsRepository.findById(UUID.fromString(placeAfterListId));
            if (list != null) {
                afterListLexorank = list.getLexorank();
            }
        }
        String beforeListLexorank = Lexorank.END_POSITION;
        if (placeBeforeListId != null && !placeBeforeListId.isBlank()) {
            var list = boardListsRepository.findById(UUID.fromString(placeBeforeListId));
            if (list != null) {
                beforeListLexorank = list.getLexorank();
            }
        }

        var list = boardListsRepository.findById(listId);
        if (list != null) {
            var rank = lexorank.getRankBetween(afterListLexorank, beforeListLexorank);
            list.setLexorank(rank);
            boardListsRepository.update(list);

            return rank;
        }

        return "";
    }

    @Transactional
    public BoardList create(
            String workspaceId,
            String boardId,
            String creatorUserId,
            WriteBoardListDto writeBoardListDto
    ) {
        var filter = new HashMap<String, UUID>();
        filter.put("workspaceId", UUID.fromString(workspaceId));
        filter.put("boardId", UUID.fromString(boardId));
        var lastListR = BoardList.mongoCollection().find(new Document(filter))
                .sort(com.mongodb.client.model.Sorts.descending("lexorank"))
                .limit(1);

        String receivedLexorank;
        var cursor = lastListR.cursor();
        if (cursor.hasNext()) {
            var lastList = ((BoardList) cursor.next());
            logger.info("[lexorank] last lexorank is - " + lastList.getLexorank() + "(" + lastList.getName() + ")" + ". Generating getRankBetween()");
            receivedLexorank = lexorank.getRankBetween(lastList.getLexorank(), Lexorank.END_POSITION);
        } else {
            logger.info("[lexorank] lastCard is null. Generating getDefaultRank()");
            receivedLexorank = lexorank.getDefaultRank(1).get(0);
        }
        cursor.close();

        var createdAt = new Date();
        var boardList = new BoardList(
                UUID.randomUUID(),
                writeBoardListDto.name(),
                UUID.fromString(workspaceId),
                UUID.fromString(boardId),
                null,
                writeBoardListDto.resources(),
                creatorUserId,
                createdAt,
                createdAt,
                receivedLexorank
        );
        boardListsRepository.persist(boardList);

        return boardList;
    }

    @Transactional
    public BoardList update(
            String workspaceId,
            String boardId,
            String listId,
            WriteBoardListDto writeBoardListDto
    ) {
        var filter = new HashMap<String, UUID>();
        filter.put("_id", UUID.fromString(listId));
        filter.put("workspaceId", UUID.fromString(workspaceId));
        filter.put("boardId", UUID.fromString(boardId));

        var update = new HashMap<String, Object>();
        update.put("name", writeBoardListDto.name());
        update.put("resources", writeBoardListDto.resources());
        update.put("editedAt", new Date());
        boardListsRepository.mongoCollection().updateOne(
                new Document(filter),
                new Document("$set", new Document(update))
        );

        return boardListsRepository.find("_id = ?1", UUID.fromString(listId)).firstResult();
    }

    /**
     * Converts given list into board
     *
     * @param workspaceId which workspace stores list
     * @param boardId which board stores list
     * @param listId list to convert
     * @return created board or null when given list not found by given path or already converted into board
     */
    @Transactional
    public WorkspaceElementDto convertToBoard(
            String userId,
            UUID workspaceId,
            UUID boardId,
            UUID listId
    ) {
        var list = boardListsRepository.find(
                "_id = ?1 and workspaceId = ?2 and boardId = ?3",
                listId,
                workspaceId,
                boardId
        ).firstResult();
        if (list == null || list.getBoundBoardId() != null) {
            return null;
        }

        var creationDate = new Date();
        var board = new WorkspaceElement(
                UUID.randomUUID(),
                list.getName(),
                list.getWorkspaceId(),
                null, // TODO: Get folder id from given_list -> board.folder_id
                WorkspaceElementType.BOARD,
                userId,
                false,
                creationDate,
                creationDate
        );
        workspaceElementsRepository.persist(board);

        var lexoranks = lexorank.getDefaultRank(3);

        var todoList = createList(userId, "To Do", workspaceId, board.getId(), list.getResources(), new Date(), lexoranks.get(0));
        boardListsRepository.persist(todoList);

        var inProgressList = createList(userId, "In Progress", workspaceId, board.getId(), list.getResources(), new Date(), lexoranks.get(1));
        boardListsRepository.persist(inProgressList);

        var doneList = createList(userId, "Done", workspaceId, board.getId(), list.getResources(), new Date(), lexoranks.get(2));
        boardListsRepository.persist(doneList);

        var cardsBoundToGivenList = boardListCardsRepository.list(
                "workspaceId = ?1 and boardId = ?2 and listId = ?3",
                workspaceId,
                boardId,
                listId
        );
        var cardsCopy = new LinkedList<BoardListCardDto>();
        for (BoardListCard card : cardsBoundToGivenList) {
            var cardCopy = copyAndBindCard(card, workspaceId, board.getId(), todoList.getId());
            boardListCardsRepository.persist(cardCopy);

            var assignees = Collections.<AssigneeDto>emptySet();
            if (cardCopy.getAssignees() != null) {
                assignees = cardCopy.getAssignees().stream()
                        .map(assignee -> usersService.asAssignee(assignee))
                        .collect(Collectors.toSet());
            }
            cardsCopy.add(new BoardListCardDto(
                    cardCopy.getId(),
                    cardCopy.getName(),
                    cardCopy.getDescription(),
                    cardCopy.getWorkspaceId(),
                    cardCopy.getBoardId(),
                    cardCopy.getListId(),
                    cardCopy.getBoundBoardId(),
                    cardCopy.getPriority(),
                    cardCopy.getStatus(),
                    cardCopy.getDueDate(),
                    cardCopy.getResources(),
                    cardCopy.getLabels(),
                    assignees,
                    cardCopy.getAssignedUserId(),
                    cardCopy.getCreatorUserId(),
                    cardCopy.isDone(),
                    cardCopy.getCreatedAt(),
                    cardCopy.getEditedAt(),
                    0,
                    0,
                    0,
                    cardCopy.getLexorank()
            ));
        }

        boardListsRepository.update(
                "boundBoardId",
                board.getId()
        ).where(
                "_id = ?1 and workspaceId = ?2 and boardId = ?3",
                listId,
                workspaceId,
                boardId
        );

        return new WorkspaceElementDto(
                board.getId(),
                board.getName(),
                board.getWorkspaceId(),
                board.getFolderId(),
                Collections.emptyList(),
                List.of(
                        toDto(todoList, cardsCopy),
                        toDto(inProgressList, Collections.emptyList()),
                        toDto(doneList, Collections.emptyList())
                ),
                new SharingDto(null, false),
                board.getType(),
                board.getCreatedAt(),
                board.getEditedAt()
        );
    }

    public BoardList createAndSaveList(
            String userId,
            String name,
            UUID workspaceId,
            UUID boardId,
            List<BoardListResource> resources,
            Date creationDate,
            String lexorank
    ) {
        var list = createList(userId, name, workspaceId, boardId, resources, creationDate, lexorank);
        boardListsRepository.persist(list);

        return list;
    }

    public BoardList createList(
            String userId,
            String name,
            UUID workspaceId,
            UUID boardId,
            List<BoardListResource> resources,
            Date creationDate,
            String lexorank
    ) {
        return new BoardList(
                UUID.randomUUID(),
                name,
                workspaceId,
                boardId,
                null,
                resources,
                userId,
                creationDate,
                creationDate,
                lexorank
        );
    }

    public BoardListDto toDto(BoardList boardList, List<BoardListCardDto> cardDtos) {
        return new BoardListDto(
                boardList.getId(),
                boardList.getName(),
                boardList.getWorkspaceId(),
                boardList.getBoardId(),
                boardList.getBoundBoardId(),
                boardList.getResources(),
                cardDtos,
                boardList.getCreatedAt(),
                boardList.getEditedAt(),
                boardList.getLexorank(),
                0,
                0
        );
    }

    private BoardListCard copyAndBindCard(
            BoardListCard boardListCard,
            UUID workspaceId,
            UUID boardId,
            UUID listId
    ) {
        var creationDate = new Date();
        return new BoardListCard(
                UUID.randomUUID(),
                boardListCard.getName(),
                boardListCard.getDescription(),
                workspaceId,
                boardId,
                listId,
                null,
                boardListCard.getPriority(),
                BoardListCardStatus.NOT_STARTED,
                boardListCard.getDueDate(),
                boardListCard.getResources(),
                boardListCard.getLabels(),
                Collections.emptySet(),
                boardListCard.getAssignedUserId(),
                boardListCard.getCreatorUserId(),
                false,
                creationDate,
                creationDate,
                ""
        );
    }

}
