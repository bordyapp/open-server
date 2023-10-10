package io.bordy.cards;

import io.bordy.api.SharingDto;
import io.bordy.api.WorkspaceElementDto;
import io.bordy.api.WriteBoardListCardDto;
import io.bordy.lexorank.Lexorank;
import io.bordy.lists.BoardListsService;
import io.bordy.workspaces.WorkspaceElement;
import io.bordy.workspaces.WorkspaceElementType;
import io.bordy.workspaces.WorkspaceElementsRepository;
import org.bson.Document;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.*;
import java.util.logging.Logger;

@ApplicationScoped
public class BoardListCardsService {

    private final static Logger logger = Logger.getLogger(BoardListCardsService.class.getName());

    @Inject
    WorkspaceElementsRepository workspaceElementsRepository;

    @Inject
    BoardListsService boardListsService;

    @Inject
    BoardListCardsRepository boardListCardsRepository;

    @Inject
    Lexorank lexorank;

    @Transactional
    public BoardListCard create(
            String workspaceId,
            String boardId,
            String listId,
            String creatorUserId,
            WriteBoardListCardDto writeBoardListCardDto
    ) {
        var filter = new HashMap<String, UUID>();
        filter.put("workspaceId", UUID.fromString(workspaceId));
        filter.put("boardId", UUID.fromString(boardId));
        filter.put("listId", UUID.fromString(listId));
        var lastCardR = BoardListCard.mongoCollection().find(new Document(filter))
                .sort(com.mongodb.client.model.Sorts.descending("lexorank"))
                .limit(1);

        String receivedLexorank;
        var cursor = lastCardR.cursor();
        if (cursor.hasNext()) {
            var lastCard = ((BoardListCard) cursor.next());
            logger.info("[lexorank] last lexorank is - " + lastCard.getLexorank() + "(" + lastCard.getName() + ")" + ". Generating getRankBetween()");
            receivedLexorank = lexorank.getRankBetween(lastCard.getLexorank(), Lexorank.END_POSITION);
        } else {
            logger.info("[lexorank] lastCard is null. Generating getDefaultRank()");
            receivedLexorank = lexorank.getDefaultRank(1).get(0);
        }
        cursor.close();

        var createdAt = new Date();
        var boardListCard = new BoardListCard(
                UUID.randomUUID(),
                writeBoardListCardDto.name(),
                writeBoardListCardDto.description(),
                UUID.fromString(workspaceId),
                UUID.fromString(boardId),
                UUID.fromString(listId),
                null,
                writeBoardListCardDto.priority(),
                writeBoardListCardDto.status(),
                writeBoardListCardDto.dueDate(),
                writeBoardListCardDto.resources(),
                writeBoardListCardDto.labels(),
                writeBoardListCardDto.assignees(),
                writeBoardListCardDto.assignedUserId(),
                creatorUserId,
                writeBoardListCardDto.isDone(),
                createdAt,
                createdAt,
                receivedLexorank
        );
        boardListCardsRepository.persist(boardListCard);

        return boardListCard;
    }

    public List<BoardListCard> getCardsByCreatorUserId(UUID workspaceId, String creatorUserId) {
        return boardListCardsRepository.list(
                "workspaceId = ?1 and creatorUserId = ?2",
                workspaceId,
                creatorUserId
        );
    }

    public List<BoardListCard> getAssignedCards(UUID workspaceId, String userId) {
        return boardListCardsRepository.list(
                "workspaceId = ?1 and assignees = ?2",
                workspaceId,
                userId
        );
    }

    public void setDoneTo(
            UUID workspaceId,
            UUID boardId,
            UUID listId,
            UUID cardId,
            boolean isDone
    ) {
        boardListCardsRepository.update(
                "done = ?1",
                isDone
        ).where(
                "_id = ?1 and workspaceId = ?2 and boardId = ?3 and listId = ?4",
                cardId,
                workspaceId,
                boardId,
                listId
        );
    }

    public void moveToOtherList(
            UUID workspaceId,
            UUID boardId,
            UUID listId,
            UUID cardId,
            UUID targetListId
    ) {
        // TODO: check that in same workspace and board
        boardListCardsRepository.update(
                "listId = ?1",
                targetListId
        ).where(
                "_id = ?1 and workspaceId = ?2 and boardId = ?3 and listId = ?4",
                cardId,
                workspaceId,
                boardId,
                listId
        );
    }

    /**
     * Converts given card into board
     *
     * @param workspaceId which workspace stores list
     * @param boardId which board stores list
     * @param listId which list stores card
     * @param cardId card to convert
     * @return created board or null when given card not found by given path or already converted into board
     */
    @Transactional
    public WorkspaceElementDto convertToBoard(
            String userId,
            UUID workspaceId,
            UUID boardId,
            UUID listId,
            UUID cardId
    ) {
        var card = boardListCardsRepository.find(
                "_id = ?1 and workspaceId = ?2 and boardId = ?3 and listId = ?4",
                cardId,
                workspaceId,
                boardId,
                listId
        ).firstResult();
        if (card == null || card.getBoundBoardId() != null) {
            return null;
        }

        var creationDate = new Date();
        var board = new WorkspaceElement(
                UUID.randomUUID(),
                card.getName(),
                card.getWorkspaceId(),
                null, // TODO: Get folder id from given_list -> board.folder_id
                WorkspaceElementType.BOARD,
                userId,
                false,
                creationDate,
                creationDate
        );
        workspaceElementsRepository.persist(board);

        var lexoranks = lexorank.getDefaultRank(3);

        var todoList = boardListsService.createAndSaveList(
                userId, "To Do", workspaceId, board.getId(), Collections.emptyList(), new Date(), lexoranks.get(0)
        );

        var inProgressList = boardListsService.createAndSaveList(
                userId, "In Progress", workspaceId, board.getId(), Collections.emptyList(), new Date(), lexoranks.get(1)
        );

        var doneList = boardListsService.createAndSaveList(
                userId, "Done", workspaceId, board.getId(), Collections.emptyList(), new Date(), lexoranks.get(2)
        );

        boardListCardsRepository.update(
                "boundBoardId",
                board.getId()
        ).where(
                "_id = ?1 and workspaceId = ?2 and boardId = ?3 and listId = ?4",
                cardId,
                workspaceId,
                boardId,
                listId
        );

        return new WorkspaceElementDto(
                board.getId(),
                board.getName(),
                board.getWorkspaceId(),
                board.getFolderId(),
                Collections.emptyList(),
                List.of(
                        boardListsService.toDto(todoList, Collections.emptyList()),
                        boardListsService.toDto(inProgressList, Collections.emptyList()),
                        boardListsService.toDto(doneList, Collections.emptyList())
                ),
                new SharingDto(null, false),
                board.getType(),
                board.getCreatedAt(),
                board.getEditedAt()
        );
    }

    @Transactional
    public String reorder(
            UUID workspaceId,
            UUID boardId,
            UUID listId,
            UUID cardId,
            String placeAfterCardId,
            String placeBeforeCardId,
            String placeIntoListId
    ) {
        String afterLexorank = Lexorank.START_POSITION;
        if (placeAfterCardId != null && !placeAfterCardId.isBlank()) {
            var card = boardListCardsRepository.findById(UUID.fromString(placeAfterCardId));
            if (card != null) {
                afterLexorank = card.getLexorank();
            }
        }
        String beforeLexorank = Lexorank.END_POSITION;
        if (placeBeforeCardId != null && !placeBeforeCardId.isBlank()) {
            var card = boardListCardsRepository.findById(UUID.fromString(placeBeforeCardId));
            if (card != null) {
                beforeLexorank = card.getLexorank();
            }
        }

        var card = boardListCardsRepository.findById(cardId);
        if (card != null) {
            var rank = lexorank.getRankBetween(afterLexorank, beforeLexorank);
            card.setLexorank(rank);
            card.setListId(UUID.fromString(placeIntoListId));
            boardListCardsRepository.update(card);

            return rank;
        }

        return "";
    }

}
