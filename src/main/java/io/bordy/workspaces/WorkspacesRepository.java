package io.bordy.workspaces;

import io.quarkus.mongodb.panache.PanacheMongoRepositoryBase;

import javax.enterprise.context.ApplicationScoped;
import java.util.UUID;

@ApplicationScoped
public class WorkspacesRepository implements PanacheMongoRepositoryBase<Workspace, UUID> {
}
