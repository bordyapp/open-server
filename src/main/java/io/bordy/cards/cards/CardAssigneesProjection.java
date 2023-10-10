package io.bordy.cards.cards;

import io.bordy.cards.BoardListCard;
import io.quarkus.mongodb.panache.common.ProjectionFor;

import java.util.Set;

@ProjectionFor(BoardListCard.class)
public record CardAssigneesProjection(
        Set<String> assignees
) {
}
