package io.bordy.api;

import io.bordy.cards.BoardListCardPriority;
import io.bordy.cards.BoardListCardResource;
import io.bordy.cards.BoardListCardStatus;

import java.util.Date;
import java.util.List;
import java.util.Set;

public record WriteBoardListCardDto(
        String name,
        String description,
        BoardListCardPriority priority,
        BoardListCardStatus status,
        Date dueDate,
        List<BoardListCardResource> resources,
        Set<String> labels,
        Set<String> assignees,
        String assignedUserId,
        boolean isDone
) {
}
