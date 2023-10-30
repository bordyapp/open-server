package io.bordy.users;

import io.bordy.api.AssigneeDto;
import io.bordy.api.UserDto;
import io.bordy.cards.BoardListCard;
import io.bordy.cards.cards.CardAssigneesProjection;
import io.bordy.kanban.workspaces.members.WorkspaceMember;
import io.bordy.kanban.workspaces.members.WorkspaceMembersService;
import io.bordy.kanban.workspaces.workspaces.WorkspacesService;
import org.bson.Document;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author Pavel Bodiachevskii
 * @since 1.0.0
 */
@ApplicationScoped
public class UsersService {

    private final static Logger logger = Logger.getLogger(UsersService.class.getName());

    @Inject
    WorkspaceMembersService workspaceMembersService;

    @Inject
    WorkspacesService workspacesService;

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
        var workspace = workspacesService.find(workspaceId);
        if (workspace != null) {
            knownAssignees.put(workspace.getOwnerId(), asAssignee(workspace.getOwnerId()));
        }

        workspaceMembersService.memberUserIdsOf(workspaceId).forEach(
                userId -> knownAssignees.put(userId, asAssignee(userId))
        );

        Instant finish = Instant.now();
        logger.info("workspaceKnownAssignees elapsed time: " + Duration.between(start, finish).toString());
        return knownAssignees;
    }

    @Nonnull
    public AssigneeDto asAssignee(@Nonnull String userId) {
        var user = User.<User>findById(userId);
        if (user != null) {
            var workspaceMember = workspaceMembersService.find(user.getId());

            return new AssigneeDto(
                    user.getId(),
                    workspaceMember != null ? workspaceMember.getName() : user.getNickname(),
                    user.getPicture()
            );
        } else {
            return new AssigneeDto(
                    userId,
                    "Deleted user",
                    ""
            );
        }
    }

    @CheckForNull
    public UserDto asUser(@Nonnull WorkspaceMember workspaceMember) {
        var user = User.<User>findById(workspaceMember.getUserId());
        if (user == null) {
            return null;
        }

        return new UserDto(
                user.getId(),
                user.getEmail(),
                workspaceMember.getName(),
                workspaceMember.getRole(),
                workspaceMember.getResponsibilities(),
                user.getPicture()
        );
    }

}
