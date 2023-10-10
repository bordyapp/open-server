package io.bordy.workspaces;

import io.quarkus.mongodb.panache.PanacheMongoRepositoryBase;

import javax.enterprise.context.ApplicationScoped;
import java.util.UUID;

@ApplicationScoped
public class WorkspaceElementsRepository implements PanacheMongoRepositoryBase<WorkspaceElement, UUID> {
}
