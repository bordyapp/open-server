package io.bordy.kanban.cards;

import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@MongoEntity(collection="Cards")
public class BoardListCardResource {
    public String name;
    public int cost;
}
