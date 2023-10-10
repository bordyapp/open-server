package io.bordy.workspaces;

import io.quarkus.mongodb.panache.PanacheMongoRepositoryBase;

import javax.enterprise.context.ApplicationScoped;
import java.util.UUID;

@ApplicationScoped
public class WorkspaceInvitesRepository implements PanacheMongoRepositoryBase<WorkspaceInvite, UUID> {
}
