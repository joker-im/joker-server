package im.joker.store;

import com.mongodb.client.model.IndexModel;
import com.mongodb.client.model.IndexOptions;
import im.joker.event.room.IRoomEvent;
import im.joker.room.IRoom;
import im.joker.user.IUser;
import im.joker.user.User;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.data.mongodb.core.ChangeStreamOptions;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * @author linyurong
 * @date 2020/9/30 16:55
 */
@Component
@RequiredArgsConstructor
public class ReactiveMongodbStore implements IStore {

    private final ReactiveMongoTemplate mongoTemplate;

    private static final String COLLECTION_NAME_ROOMS = "rooms";

    private static final String COLLECTION_NAME_ROOM_STATES = "room_states";

    private static final String COLLECTION_NAME_EVENTS = "pdus";

    private static final String COLLECTION_USER = "users";

    @PostConstruct
    public void init() {
        createCollectionAndIndex().subscribe();
    }

    private Mono<String> createRoomCollection() {
        return mongoTemplate.createCollection(COLLECTION_NAME_ROOMS)
                // 创建room的索引
                .flatMap(o -> Mono.from(o.createIndex(Document.parse("{room_id: 1}"), new IndexOptions().unique(true))));
    }

    private Mono<String> createEventCollection() {
        return mongoTemplate.createCollection(COLLECTION_NAME_EVENTS)
                .flatMap(o -> {
                    IndexModel index1 = new IndexModel(Document.parse("{room_id: 1, _id: -1}"));
                    IndexModel index2 = new IndexModel(Document.parse("{event_id: 1}"));
                    IndexModel index3 = new IndexModel(Document.parse("{\"room_id\": 1, \"json.type\": 1, \"json.sender\": 1}"));
                    List<IndexModel> indexes = List.of(index1, index2, index3);
                    return Mono.from(o.createIndexes(indexes));
                });
    }


    private Mono<String> createUserCollection() {
        return mongoTemplate.createCollection(COLLECTION_USER)
                // 创建user的索引
                .flatMap(o -> {
                    IndexModel index1 = new IndexModel(Document.parse("{username:1}"), new IndexOptions().unique(true));
                    IndexModel index2 = new IndexModel(Document.parse("{create_time:-1}"));
                    IndexModel index3 = new IndexModel(Document.parse("{user_id:-1}"), new IndexOptions().unique(true));
                    return Mono.from(o.createIndexes(List.of(index1, index2, index3)));
                });
    }

    private Mono<String> createRoomStateCollection() {
        return mongoTemplate.createCollection(COLLECTION_NAME_ROOM_STATES)
                // 创建room_state索引
                .flatMap(o -> {
                    IndexModel index1 = new IndexModel(Document.parse("{room_id: -1, event_id: -1}"));
                    IndexModel index2 = new IndexModel(Document.parse("{room_id: -1, _id: -1}"));
                    List<IndexModel> indexes = List.of(index1, index2);
                    return Mono.from(o.createIndexes(indexes));
                });
    }

    private Mono<Void> createCollectionAndIndex() {
        // 创建room
        return mongoTemplate.collectionExists(COLLECTION_NAME_ROOMS)
                .flatMap(exists -> {
                    if (!exists) {
                        return createRoomCollection();
                    } else {
                        return Mono.empty();
                    }
                })
                .then(mongoTemplate.collectionExists(COLLECTION_NAME_EVENTS))
                .flatMap(exists -> {
                    if (!exists) {
                        return createEventCollection();
                    } else {
                        return Mono.empty();
                    }
                })
                .then(mongoTemplate.collectionExists(COLLECTION_NAME_ROOM_STATES))
                .flatMap(exists -> {
                    if (!exists) {
                        return createRoomStateCollection();
                    } else {
                        return Mono.empty();
                    }
                })
                .then(mongoTemplate.collectionExists(COLLECTION_USER))
                .flatMap(exists -> {
                    if (!exists) {
                        return createUserCollection();
                    } else {
                        return Mono.empty();
                    }
                }).then();

    }

    @Override
    public void addRoom(IRoom room) {

    }

    @Override
    public IRoomEvent addEvent(IRoomEvent event) {
        return null;
    }

    @Override
    public Mono<IUser> addUser(IUser user) {
        return mongoTemplate.insert(user, COLLECTION_USER);
    }

    @Override
    public Mono<IUser> retrieveById(String id) {
        return null;
    }

    @Override
    public Mono<IUser> retrieveByUsername(String username) {
        Query query = new Query();
        query.addCriteria(Criteria.where("username").is(username));
        return mongoTemplate.findOne(query, User.class, COLLECTION_USER).map(e -> e);
    }
}
