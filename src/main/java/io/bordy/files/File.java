package io.bordy.files;

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
@MongoEntity(collection="Files")
@EqualsAndHashCode(callSuper = true)
public class File extends PanacheMongoEntityBase {
    @BsonId
    private UUID id;
    private String name;
    private UUID workspaceId;
    private UUID folderId;
    private FileType type;
    private String url;
    private String creatorUserId;
    private Date createdAt;
    private Date editedAt;
}