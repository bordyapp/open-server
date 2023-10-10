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
@MongoEntity(collection="Workspaces")
@EqualsAndHashCode(callSuper = true)
public class Workspace extends PanacheMongoEntityBase {
    @BsonId
    private UUID id;
    private String name;
    private String photo;
    private String ownerId;
    private Date createdAt;
    private Date editedAt;
}
