package io.bordy.api;

import io.bordy.lists.BoardListResource;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public record BoardListDto(
        UUID id,
        String name,
        UUID workspaceId,
        UUID boardId,
        UUID boundBoardId,
        List<BoardListResource>resources,
        List<BoardListCardDto> cards,
        Date createdAt,
        Date editedAt,
        String lexorank,
        long numberOfCardsOnBoundBoard,
        long numberOfDoneCardsOnBoundBoard
) {
}
