package io.bordy.kanban.workspaces.workspaces;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import io.quarkus.mongodb.panache.PanacheMongoEntityBase;
import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.bson.codecs.pojo.annotations.BsonId;

import javax.annotation.Nonnull;
import java.util.Date;
import java.util.UUID;

/**
 * Workspace groups everything in application:
 * <ul>
 *     <li>Users</li>
 *     <li>Boards</li>
 *     <li>Files and Docs</li>
 *     <li>Members</li>
 *     <li>Invites</li>
 * </ul>
 *
 * @author Pavel Bodiachevskii
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@MongoEntity(collection="Workspaces")
public class Workspace extends PanacheMongoEntityBase {

    /**
     * Workspace Id.
     */
    @BsonId
    @Nonnull
    @JsonProperty(value = "id", required = true)
    @JsonPropertyDescription("Workspace Id.")
    private UUID id;

    /**
     * Workspace name.
     */
    @Nonnull
    @JsonProperty(value = "name", required = true)
    @JsonPropertyDescription("Workspace name.")
    private String name;

    /**
     * Workspace photo.
     */
    @Nonnull
    @JsonProperty(value = "photo")
    @JsonPropertyDescription("Workspace photo.")
    private String photo;

    /**
     * Workspace owner.
     */
    @Nonnull
    @JsonProperty(value = "ownerId", required = true)
    @JsonPropertyDescription("Workspace owner.")
    private String ownerId;

    /**
     * When workspace was created.
     */
    @Nonnull
    @JsonProperty(value = "createdAt", required = true)
    @JsonPropertyDescription("When workspace was created.")
    private Date createdAt;

    /**
     * When workspace was edited.
     */
    @Nonnull
    @JsonProperty(value = "editedAt", required = true)
    @JsonPropertyDescription("When workspace was edited.")
    private Date editedAt;

}
