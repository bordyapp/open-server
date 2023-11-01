package io.bordy.kanban.workspaces.invites;

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
 * Workspace invite.
 *
 * @author Pavel Bodiachevskii
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@MongoEntity(collection="Workspace-Invites")
@EqualsAndHashCode(callSuper = false)
public class WorkspaceInvite extends PanacheMongoEntityBase {

    /**
     * Workspace invite Id.
     */
    @BsonId
    @Nonnull
    @JsonProperty(value = "id", required = true)
    @JsonPropertyDescription("Workspace invite Id.")
    private UUID id;

    /**
     * Workspace Id.
     * <p>
     * Determines to which workspace this invite belongs.
     */
    @Nonnull
    @JsonProperty(value = "workspaceId", required = true)
    @JsonPropertyDescription("Determines to which workspace this invite belongs.")
    private UUID workspaceId;

    /**
     * Workspace invite user email.
     * <p>
     * Determines to which user this invite belongs.
     */
    @Nonnull
    @JsonProperty(value = "email", required = true)
    @JsonPropertyDescription("Determines to which user this invite belongs.")
    private String email;

    /**
     * Workspace invite username.
     * <p>
     * Determines with which name user will be created.
     */
    @Nonnull
    @JsonProperty(value = "name", required = true)
    @JsonPropertyDescription("Determines with which name user will be created.")
    private String name;

    /**
     * Workspace invite user role.
     * <p>
     * Determines with which role user will be created.
     */
    @CheckForNull
    @JsonProperty(value = "role")
    @JsonPropertyDescription("Determines with which role user will be created.")
    private String role;

    /**
     * Workspace invite user responsibilities.
     * <p>
     * Determines with which responsibilities user will be created.
     */
    @CheckForNull
    @JsonProperty(value = "responsibilities")
    @JsonPropertyDescription("Determines with which responsibilities user will be created.")
    private String responsibilities;

    /**
     * Workspace invite status.
     */
    @Nonnull
    @JsonProperty(value = "status", required = true)
    @JsonPropertyDescription("Workspace invite status.")
    private WorkspaceInviteStatus status;

    /**
     * When workspace invite was created.
     */
    @Nonnull
    @JsonProperty(value = "createdAt", required = true)
    @JsonPropertyDescription("When workspace invite was created.")
    private Date createdAt;

    /**
     * When workspace invite was edited.
     */
    @Nonnull
    @JsonProperty(value = "editedAt", required = true)
    @JsonPropertyDescription("When workspace invite was edited.")
    private Date editedAt;

}
