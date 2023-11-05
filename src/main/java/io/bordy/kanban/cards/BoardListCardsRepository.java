package io.bordy.kanban.cards;

import io.quarkus.mongodb.panache.PanacheMongoRepositoryBase;

import javax.enterprise.context.ApplicationScoped;
import java.util.UUID;

@ApplicationScoped
public class BoardListCardsRepository implements PanacheMongoRepositoryBase<BoardListCard, UUID> {
}
