package io.bordy.gateways.comments;

import java.util.Date;
import java.util.UUID;

public record ReadCommentDto(
        UUID id,
        UUID workspaceId,
        UUID cardId,
        String text,
        CommentCreatorDto creator,
        Date createdAt,
        Date editedAt
) {
}
