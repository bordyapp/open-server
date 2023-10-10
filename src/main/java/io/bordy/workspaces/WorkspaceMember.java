package io.bordy.workspaces;

import io.quarkus.mongodb.panache.PanacheMongoEntityBase;
import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.bson.codecs.pojo.annotations.BsonId;

import java.util.Date;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@MongoEntity(collection="Workspace-Members")
@EqualsAndHashCode(callSuper = true)
public class WorkspaceMember extends PanacheMongoEntityBase {
    @BsonId
    private UUID id;
    private UUID workspaceId;
    private String userId;
    private String name;
    private String role;
    private String responsibilities;
    private Date createdAt;
    private Date editedAt;
}
