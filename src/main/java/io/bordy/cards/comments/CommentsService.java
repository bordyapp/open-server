package io.bordy.cards.comments;

import io.bordy.gateways.comments.CommentCreatorDto;
import io.bordy.gateways.comments.ReadCommentDto;
import io.bordy.gateways.comments.UpdateCommentDto;
import io.bordy.gateways.comments.WriteCommentDto;
import io.bordy.users.User;
import io.bordy.kanban.workspaces.members.WorkspaceMembersRepository;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class CommentsService {

    @Inject
    WorkspaceMembersRepository workspaceMembersRepository;

    public List<ReadCommentDto> get(
            @Nonnull UUID workspaceId,
            @Nonnull UUID cardId
    ) {
        return Comment.<Comment>find(
                "cardId = ?1 and workspaceId = ?2",
                        cardId, workspaceId
                ).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ReadCommentDto write(
            @Nonnull UUID workspaceId,
            @Nonnull UUID cardId,
            @Nonnull String userId,
            WriteCommentDto writeCommentDto
    ) {
        var createdAt = new Date();
        var comment = new Comment(
                UUID.randomUUID(),
                workspaceId,
                cardId,
                writeCommentDto.text(),
                userId,
                createdAt,
                createdAt
        );

        comment.persist();
        return toDto(comment);
    }

    @CheckForNull
    @Transactional
    public ReadCommentDto update(
            @Nonnull UUID workspaceId,
            @Nonnull UUID cardId,
            @Nonnull UUID commentId,
            @Nonnull String userId,
            UpdateCommentDto updateCommentDto
    ) {
        var comment = Comment.<Comment>find(
                "_id = ?1 and workspaceId = ?2 and cardId = ?3 and creatorUserId = ?4",
                commentId, workspaceId, cardId, userId
        ).firstResult();
        if (comment == null) {
            return null;
        }

        comment.setText(updateCommentDto.text());
        comment.setEditedAt(new Date());
        comment.update();
        return toDto(comment);
    }

    @Transactional
    public void delete(
            @Nonnull UUID workspaceId,
            @Nonnull UUID cardId,
            @Nonnull UUID commentId,
            @Nonnull String userId
    ) {
        Comment.delete(
                "_id = ?1 and workspaceId = ?2 and cardId = ?3 and creatorUserId = ?4",
                commentId, workspaceId, cardId, userId
        );
    }

    @Nonnull
    public ReadCommentDto toDto(@Nonnull Comment comment) {
        return new ReadCommentDto(
                comment.getId(),
                comment.getWorkspaceId(),
                comment.getCardId(),
                comment.getText(),
                resolveCreator(comment.getCreatorUserId()),
                comment.getCreatedAt(),
                comment.getEditedAt()
        );
    }

    public boolean isCreator(
            @Nonnull UUID workspaceId,
            @Nonnull UUID cardId,
            @Nonnull UUID commentId,
            @Nonnull String userId
    ) {
        return Comment.count(
                "_id = ?1 and workspaceId = ?2 and cardId = ?3 and creatorUserId = ?4",
                commentId, workspaceId, cardId, userId
        ) > 0;
    }

    @CheckForNull
    private CommentCreatorDto resolveCreator(@Nonnull String creatorUserId) {
        var user = User.<User>findById(creatorUserId);
        var workspaceMember = workspaceMembersRepository.list("userId", user.id).stream().findFirst().orElse(null);
        if (user != null) {
            if (workspaceMember != null) {
                return new CommentCreatorDto(
                        user.id,
                        workspaceMember.getName(),
                        user.picture
                );
            } else {
                return new CommentCreatorDto(
                        user.id,
                        user.getNickname(),
                        user.picture
                );
            }
        }

        return null;
    }

}
