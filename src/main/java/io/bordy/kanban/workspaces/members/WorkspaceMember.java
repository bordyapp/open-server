package io.bordy.kanban.workspaces.members;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import io.quarkus.mongodb.panache.PanacheMongoEntityBase;
import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.bson.codecs.pojo.annotations.BsonId;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.util.Date;
import java.util.UUID;

/**
 * Workspace member.
 *
 * @author Pavel Bodiachevskii
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@MongoEntity(collection="Workspace-Members")
public class WorkspaceMember extends PanacheMongoEntityBase {

    /**
     * Workspace member Id.
     */
    @BsonId
    @Nonnull
    @JsonProperty(value = "id", required = true)
    @JsonPropertyDescription("Workspace member Id.")
    private UUID id;

    /**
     * Workspace Id.
     */
    @Nonnull
    @JsonProperty(value = "workspaceId", required = true)
    @JsonPropertyDescription("Workspace Id.")
    private UUID workspaceId;

    /**
     * Workspace member user Id.
     */
    @Nonnull
    @JsonProperty(value = "userId", required = true)
    @JsonPropertyDescription("Workspace member user Id.")
    private String userId;

    /**
     * Workspace member name.
     */
    @Nonnull
    @JsonProperty(value = "name", required = true)
    @JsonPropertyDescription("Workspace member name.")
    private String name;

    /**
     * Workspace member role.
     */
    @CheckForNull
    @JsonProperty(value = "role")
    @JsonPropertyDescription("Workspace member role.")
    private String role;

    /**
     * Workspace member responsibilities.
     */
    @CheckForNull
    @JsonProperty(value = "responsibilities")
    @JsonPropertyDescription("Workspace member responsibilities.")
    private String responsibilities;

    /**
     * When workspace member was created.
     */
    @Nonnull
    @JsonProperty(value = "createdAt", required = true)
    @JsonPropertyDescription("When workspace member was created.")
    private Date createdAt;

    /**
     * When workspace member was edited.
     */
    @Nonnull
    @JsonProperty(value = "editedAt", required = true)
    @JsonPropertyDescription("When workspace member was edited.")
    private Date editedAt;

}
