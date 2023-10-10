package io.bordy.users;

import io.quarkus.mongodb.panache.PanacheMongoEntityBase;
import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@MongoEntity(collection="Users")
@EqualsAndHashCode(callSuper = true)
public class User extends PanacheMongoEntityBase {
    public String id;
    public String email;
    public String nickname;
    public String picture;
}