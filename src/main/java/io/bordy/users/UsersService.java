package io.bordy.users;

import io.bordy.api.AssigneeDto;
import io.bordy.cards.BoardListCard;
import io.bordy.cards.cards.CardAssigneesProjection;
import io.bordy.kanban.workspaces.workspaces.Workspace;
import io.bordy.workspaces.WorkspaceMember;
import io.bordy.workspaces.WorkspaceMembersRepository;
import org.bson.Document;

import javax.annotation.Nonnull;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@ApplicationScoped
public class UsersService {

    private final static Logger logger = Logger.getLogger(UsersService.class.getName());

    @Inject
    WorkspaceMembersRepository workspaceMembersRepository;

    @Nonnull
    public Map<String, AssigneeDto> boardKnownAssignees(@Nonnull UUID boardId) {
        Instant start = Instant.now();
        var knownAssignees = new LinkedHashMap<String, AssigneeDto>();
        BoardListCard.find(
                        new Document("boardId", boardId)
                                .append(
                                        "assignees", new Document("$exists", true)
                                                .append("$type", "array")
                                                .append("$ne", List.of())
                                )
                ).project(CardAssigneesProjection.class).list().stream()
                .map(CardAssigneesProjection::assignees)
                .collect(Collectors.toSet())
                .stream()
                .flatMap(Set::stream)
                .collect(Collectors.toSet())
                .forEach(assignee -> knownAssignees.put(assignee, asAssignee(assignee)));

        Instant finish = Instant.now();
        logger.info("boardKnownAssignees elapsed time: " + Duration.between(start, finish).toString());
        return knownAssignees;
    }

    @Nonnull
    public Map<String, AssigneeDto> workspaceKnownAssignees(@Nonnull UUID workspaceId) {
        Instant start = Instant.now();
        var knownAssignees = new LinkedHashMap<String, AssigneeDto>();
        var workspace = Workspace.<Workspace>findById(workspaceId);
        if (workspace != null) {
            knownAssignees.put(workspace.getOwnerId(), asAssignee(workspace.getOwnerId()));
        }

        WorkspaceMember.<WorkspaceMember>find("workspaceId", workspaceId).stream()
                .map(WorkspaceMember::getUserId)
                .forEach(userId -> knownAssignees.put(userId, asAssignee(userId)));

        Instant finish = Instant.now();
        logger.info("workspaceKnownAssignees elapsed time: " + Duration.between(start, finish).toString());
        return knownAssignees;
    }

    @Nonnull
    private AssigneeDto asAssignee(@Nonnull String userId) {
        var user = User.<User>findById(userId);
        var workspaceMember = workspaceMembersRepository.list("userId", user.id).stream().findFirst().orElse(null);
        if (user != null) {
            return new AssigneeDto(
                    user.id,
                    workspaceMember != null ? workspaceMember.getName() : user.getNickname(),
                    user.picture
            );
        } else {
            return new AssigneeDto(
                    user.id,
                    "Deleted user",
                    ""
            );
        }
    }

}
