package io.bordy.api;

import io.bordy.kanban.cards.BoardListCardPriority;
import io.bordy.kanban.cards.BoardListCardResource;
import io.bordy.kanban.cards.BoardListCardStatus;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public record BoardListCardDto(
        UUID id,
        String name,
        String description,
        UUID workspaceId,
        UUID boardId,
        UUID listId,
        UUID boundBoardId,
        BoardListCardPriority priority,
        BoardListCardStatus status,
        Date dueDate,
        List<BoardListCardResource> resources,
        Set<String> labels,
        Set<AssigneeDto> assignees,
        String assignedUserId,
        String creatorUserId,
        boolean isDone,
        Date createdAt,
        Date editedAt,
        long numberOfCardsOnBoundBoard,
        long numberOfDoneCardsOnBoundBoard,
        long numberOfComments,
        String lexorank
) {
}
