package io.bordy.kanban.lists;

import io.quarkus.mongodb.panache.PanacheMongoEntityBase;
import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.bson.codecs.pojo.annotations.BsonId;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@MongoEntity(collection="Lists")
@EqualsAndHashCode(callSuper = true)
public class BoardList extends PanacheMongoEntityBase {
    @BsonId
    private UUID id;
    private String name;
    private UUID workspaceId;
    private UUID boardId;
    private UUID boundBoardId;
    private List<BoardListResource> resources;
    private String creatorUserId;
    private Date createdAt;
    private Date editedAt;
    private String lexorank;
}
