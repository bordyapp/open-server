package io.bordy.kanban.cards.cards;

import io.bordy.kanban.cards.BoardListCard;
import io.quarkus.mongodb.panache.common.ProjectionFor;

import java.util.Set;

@ProjectionFor(BoardListCard.class)
public record CardAssigneesProjection(
        Set<String> assignees
) {
}
