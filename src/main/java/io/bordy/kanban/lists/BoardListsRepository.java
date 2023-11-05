package io.bordy.kanban.lists;

import io.quarkus.mongodb.panache.PanacheMongoRepositoryBase;

import javax.enterprise.context.ApplicationScoped;
import java.util.UUID;

@ApplicationScoped
public class BoardListsRepository implements PanacheMongoRepositoryBase<BoardList, UUID> {
}
