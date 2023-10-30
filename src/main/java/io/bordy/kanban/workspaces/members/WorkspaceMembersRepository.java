package io.bordy.kanban.workspaces.members;

import io.quarkus.mongodb.panache.PanacheMongoRepositoryBase;

import javax.enterprise.context.ApplicationScoped;
import java.util.UUID;

@ApplicationScoped
public class WorkspaceMembersRepository implements PanacheMongoRepositoryBase<WorkspaceMember, UUID> {
}
