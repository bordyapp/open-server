package io.bordy.workspaces;

import io.bordy.api.*;
import io.bordy.cards.BoardListCardStatus;
import io.bordy.cards.BoardListCardsRepository;
import io.bordy.cards.comments.Comment;
import io.bordy.lists.BoardListsRepository;
import io.bordy.users.UsersService;
import io.quarkus.panache.common.Sort;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@ApplicationScoped
public class WorkspaceElementsService {

    private final static Logger logger = Logger.getLogger(WorkspaceElementsService.class.getName());

    @Inject
    WorkspaceElementsRepository workspaceElementsRepository;

    @Inject
    BoardListsRepository boardListsRepository;

    @Inject
    BoardListCardsRepository boardListCardsRepository;

    @Inject
    UsersService usersService;

    public List<WorkspaceElementDto> getOnlyBoards(UUID workspaceId) {
        var boards = workspaceElementsRepository.list(
                "{workspaceId: ?1, folderId: ?2, isRootBoard: {$in: [?3, ?4]}}",
                workspaceId,
                null,
                true, null
        );

        List<WorkspaceElementDto> dto = new LinkedList<>();
        boards.forEach(board -> {
            SharingDto sharingDto = new SharingDto(null, false);

            dto.add(new WorkspaceElementDto(
                    board.getId(),
                    board.getName(),
                    board.getWorkspaceId(),
                    board.getFolderId(),
                    Collections.emptyList(),
                    Collections.emptyList(),
                    sharingDto,
                    board.getType(),
                    board.getCreatedAt(),
                    board.getEditedAt()
            ));
        });

        return dto;
    }

    public WorkspaceElementDto getBoardOrFolder(UUID workspaceId, UUID workspaceElementId) {
        var workspaceElement = workspaceElementsRepository.find(
                "_id = ?1 and workspaceId = ?2",
                workspaceElementId,
                workspaceId
        ).firstResult();

        if (workspaceElement == null) {
            return null;
        }

        List<WorkspaceElementDto> boards = new LinkedList<>();
        List<BoardListDto> lists = new LinkedList<>();
        if (workspaceElement.getType() == WorkspaceElementType.FOLDER) {
            boards = getBoards(workspaceElementId);
        }

        if (workspaceElement.getType() == WorkspaceElementType.BOARD) {
            lists = getLists(workspaceElementId);
        }

        SharingDto sharingDto = new SharingDto(null, false);

        return new WorkspaceElementDto(
                workspaceElement.getId(),
                workspaceElement.getName(),
                workspaceElement.getWorkspaceId(),
                workspaceElement.getFolderId(),
                boards,
                lists,
                sharingDto,
                workspaceElement.getType(),
                workspaceElement.getCreatedAt(),
                workspaceElement.getEditedAt()
        );
    }

    private List<WorkspaceElementDto> getBoards(UUID workspaceElementId) {
        List<WorkspaceElementDto> boards = new LinkedList<>();

        workspaceElementsRepository.list(
                "{folderId: ?1, isRootBoard: {$in: [?2, ?3]}}",
                workspaceElementId,
                null,
                true, null
        ).forEach(board -> {
            boards.add(new WorkspaceElementDto(
                    board.getId(),
                    board.getName(),
                    board.getWorkspaceId(),
                    board.getFolderId(),
                    Collections.emptyList(),
                    Collections.emptyList(),
                    new SharingDto(null, false),
                    board.getType(),
                    board.getCreatedAt(),
                    board.getEditedAt()
            ));
        });

        return boards;
    }

    private List<BoardListDto> getLists(UUID workspaceElementId) {
        var knownAssignees = usersService.boardKnownAssignees(workspaceElementId);

        Instant start = Instant.now();
        List<BoardListDto> lists = new LinkedList<>();
        boardListsRepository.list(
                "boardId = ?1",
                Sort.ascending("lexorank"),
                workspaceElementId
        ).forEach(boardList -> {
            List<BoardListCardDto> boardListCardDtos = boardListCardsRepository.list(
                    "listId = ?1",
                    Sort.ascending("lexorank"),
                    boardList.getId()
            ).stream().parallel().map(boardListCard -> {
                long numberOfCardsOnBoundBoard = 0;
                long numberOfDoneCardsOnBoundBoard = 0;
                if (boardListCard.getBoundBoardId() != null) {
                    numberOfCardsOnBoundBoard = boardListCardsRepository.count("boardId", boardListCard.getBoundBoardId());
                    numberOfDoneCardsOnBoundBoard = boardListCardsRepository.count(
                            "boardId = ?1 and status = ?2",
                            boardListCard.getBoundBoardId(),
                            BoardListCardStatus.DONE
                    );
                }
                long numberOfComments = Comment.count(
                        "workspaceId = ?1 and cardId = ?2",
                        boardListCard.getWorkspaceId(),
                        boardListCard.getId()
                );

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
                        numberOfCardsOnBoundBoard,
                        numberOfDoneCardsOnBoundBoard,
                        numberOfComments,
                        boardListCard.getLexorank()
                );
            }).toList();

            long numberOfCardsOnBoundBoard = 0;
            long numberOfDoneCardsOnBoundBoard = 0;
            if (boardList.getBoundBoardId() != null) {
                numberOfCardsOnBoundBoard = boardListCardsRepository.count("boardId", boardList.getBoundBoardId());
                numberOfDoneCardsOnBoundBoard = boardListCardsRepository.count(
                        "boardId = ?1 and status = ?2",
                        boardList.getBoundBoardId(),
                        BoardListCardStatus.DONE
                );
            }
            lists.add(new BoardListDto(
                    boardList.getId(),
                    boardList.getName(),
                    boardList.getWorkspaceId(),
                    boardList.getBoardId(),
                    boardList.getBoundBoardId(),
                    boardList.getResources(),
                    boardListCardDtos,
                    boardList.getCreatedAt(),
                    boardList.getEditedAt(),
                    boardList.getLexorank(),
                    numberOfCardsOnBoundBoard,
                    numberOfDoneCardsOnBoundBoard
            ));
        });
        Instant finish = Instant.now();
        logger.info("getLists elapsed time: " + Duration.between(start, finish).toString());

        return lists;
    }

}
