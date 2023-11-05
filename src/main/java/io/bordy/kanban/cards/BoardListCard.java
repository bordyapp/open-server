package io.bordy.kanban.cards;

import io.quarkus.mongodb.panache.PanacheMongoEntityBase;
import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.bson.codecs.pojo.annotations.BsonId;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@MongoEntity(collection="Cards")
@EqualsAndHashCode(callSuper = true)
public class BoardListCard extends PanacheMongoEntityBase {
    @BsonId
    private UUID id;
    private String name;
    private String description;
    private UUID workspaceId;
    private UUID boardId;
    private UUID listId;
    private UUID boundBoardId;
    private BoardListCardPriority priority;
    private BoardListCardStatus status;
    private Date dueDate;
    private List<BoardListCardResource> resources;
    private Set<String> labels;
    private Set<String> assignees;
    private String assignedUserId;
    private String creatorUserId;
    private boolean isDone;
    private Date createdAt;
    private Date editedAt;
    private String lexorank;
}
