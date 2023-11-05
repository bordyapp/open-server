package io.bordy.kanban.cards.comments;

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
@MongoEntity(collection="CardComments")
@EqualsAndHashCode(callSuper = true)
public class Comment extends PanacheMongoEntityBase {
    @BsonId
    private UUID id;
    private UUID workspaceId;
    private UUID cardId;
    private String text;
    private String creatorUserId;
    private Date createdAt;
    private Date editedAt;
}
