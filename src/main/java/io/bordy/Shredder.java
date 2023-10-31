package io.bordy;

import io.bordy.cards.BoardListCard;
import io.bordy.cards.BoardListCardsRepository;
import io.bordy.cards.comments.Comment;
import io.bordy.files.File;
import io.bordy.kanban.workspaces.invites.WorkspaceInvitesRepository;
import io.bordy.kanban.workspaces.members.WorkspaceMembersService;
import io.bordy.kanban.workspaces.workspaces.Workspace;
import io.bordy.lists.BoardList;
import io.bordy.lists.BoardListsRepository;
import io.bordy.workspaces.*;

import javax.annotation.Nonnull;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

@ApplicationScoped
public class Shredder {

    @Inject
    WorkspaceElementsRepository workspaceElementsRepository;

    @Inject
    BoardListsRepository boardListsRepository;

    @Inject
    BoardListCardsRepository boardListCardsRepository;

    @Inject
    WorkspaceMembersService workspaceMembersService;

    @Inject
    WorkspaceInvitesRepository workspaceInvitesRepository;

    @Transactional
    public void deleteBoard(WorkspaceElement workspaceElement) {
        // Delete bound cards
        // TODO: Delete bound boards
        boardListCardsRepository.list(
                "boardId = ?1", workspaceElement.getId()
        ).forEach(boardListCard -> {
            Comment.delete(
                    "workspaceId = ?1 and cardId = ?2",
                    boardListCard.getWorkspaceId(),
                    boardListCard.getId()
            );
        });
        boardListCardsRepository.delete("boardId = ?1", workspaceElement.getId());
        // Delete bound lists
        // TODO: Delete bound boards
        boardListsRepository.delete("boardId = ?1", workspaceElement.getId());

        workspaceElementsRepository.deleteById(workspaceElement.getId());
    }

    @Transactional
    public void deleteFolder(WorkspaceElement workspaceElement) {
        var boundBoards = workspaceElementsRepository.list("folderId = ?1", workspaceElement.getId());

        for (WorkspaceElement boundBoard: boundBoards) {
            // Delete bound cards
            // TODO: Delete bound boards
            boardListCardsRepository.list(
                    "boardId", boundBoard.getId()
            ).forEach(boardListCard -> {
                Comment.delete(
                        "workspaceId = ?1 and cardId = ?2",
                        boardListCard.getWorkspaceId(),
                        boardListCard.getId()
                );
            });
            boardListCardsRepository.delete("boardId", boundBoard.getId());
            // Delete bound lists
            // TODO: Delete bound boards
            boardListsRepository.delete("boardId", boundBoard.getId());
            // Delete bound board
            // TODO: Delete bound boards
            workspaceElementsRepository.deleteById(boundBoard.getId());
        }

        workspaceElementsRepository.deleteById(workspaceElement.getId());
    }

    // TODO: Delete bound board and cards and bound boards to cards
    @Transactional
    public void deleteList(BoardList boardList) {
        // TODO: Delete bound boards
        boardListCardsRepository.list(
                "listId", boardList.getId()
        ).forEach(boardListCard -> {
            Comment.delete(
                    "workspaceId = ?1 and cardId = ?2",
                    boardListCard.getWorkspaceId(),
                    boardListCard.getId()
            );
        });
        boardListCardsRepository.delete("listId", boardList.getId());

        boardListsRepository.deleteById(boardList.getId());
    }

    // TODO: Delete bound boards
    @Transactional
    public void deleteCard(BoardListCard boardListCard) {
        boardListCardsRepository.deleteById(boardListCard.getId());
        Comment.delete(
                "workspaceId = ?1 and cardId = ?2",
                boardListCard.getWorkspaceId(),
                boardListCard.getId()
        );
    }

    @Transactional
    public void deleteWorkspace(@Nonnull Workspace workspace) {
        boardListCardsRepository.delete("workspaceId = ?1", workspace.getId());
        boardListsRepository.delete("workspaceId = ?1", workspace.getId());
        workspaceElementsRepository.delete("workspaceId = ?1", workspace.getId());
        workspaceInvitesRepository.delete("workspaceId = ?1", workspace.getId());
        workspaceMembersService.deleteAll(workspace.getId());
        Workspace.deleteById(workspace.getId());
        File.delete("workspaceId = ?1", workspace.getId());
        Comment.delete("workspaceId = ?1", workspace.getId());
    }

}
