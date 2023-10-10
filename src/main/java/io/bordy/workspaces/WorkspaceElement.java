package io.bordy.workspaces;

import io.quarkus.mongodb.panache.PanacheMongoEntityBase;
import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;

import java.util.Date;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@MongoEntity(collection="Workspace-Elements")
@EqualsAndHashCode(callSuper = true)
public class WorkspaceElement extends PanacheMongoEntityBase {
    @BsonId
    private UUID id;
    private String name;
    private UUID workspaceId;
    private UUID folderId;
    private WorkspaceElementType type;
    private String creatorUserId;
    @BsonProperty("isRootBoard")
    private Boolean isRootBoard;
    private Date createdAt;
    private Date editedAt;
}