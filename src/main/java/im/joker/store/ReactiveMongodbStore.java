package im.joker.store;

import com.google.common.collect.Lists;
import com.mongodb.client.model.IndexModel;
import com.mongodb.client.model.IndexOptions;
import im.joker.event.EventType;
import im.joker.event.ImEvent;
import im.joker.room.IRoom;
import im.joker.user.IUser;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
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

    private static final String COLLECTION_NAME_EVENTS = "events";

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
                    IndexModel index3 = new IndexModel(Document.parse("{\"room_id\": 1, \"type\": 1, \"sender\": 1}"));
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
    public Mono<IRoom> addRoom(IRoom room) {
        return mongoTemplate.insert(room, COLLECTION_NAME_ROOMS);
    }

    @Override
    public Mono<ImEvent> addEvent(ImEvent event) {
        return mongoTemplate.insert(event, COLLECTION_NAME_EVENTS);
    }

    @Override
    public Mono<IUser> addUser(IUser user) {
        return mongoTemplate.insert(user, COLLECTION_USER);
    }

    @Override
    public Mono<IUser> findUserByUserId(String id) {
        Query query = new Query();
        query.addCriteria(Criteria.where("user_id").is(id));
        return mongoTemplate.findOne(query, IUser.class, COLLECTION_USER);
    }

    @Override
    public Mono<IUser> findUserByUsername(String username) {
        Query query = new Query();
        query.addCriteria(Criteria.where("username").is(username));
        return mongoTemplate.findOne(query, IUser.class, COLLECTION_USER);
    }

    @Override
    public Flux<IRoom> findRoomByUserId(String userId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("creator").is(userId));
        return mongoTemplate.find(query, IRoom.class, COLLECTION_NAME_ROOMS);
    }

    @Override
    public Flux<ImEvent> findEvents(EventType eventType, String sender) {
        Query query = new Query();
        query.addCriteria(Criteria.where("type").is(eventType.getId()).and("sender").is(sender));
        return mongoTemplate.find(query, ImEvent.class, COLLECTION_NAME_EVENTS);
    }

    @Override
    public Flux<ImEvent> findRoomStateEvents(String roomId) {
        Query query = new Query();
        Criteria criteria = Criteria.where("room_id").is(roomId);
        List<Criteria> or = Lists.newArrayList();
        for (EventType value : EventType.values()) {
            if (value.isState()) {
                or.add(Criteria.where("type").is(value.getId()));
            }
        }
        criteria.andOperator(new Criteria().orOperator(or.toArray(new Criteria[0])));
        query.addCriteria(criteria);
        return mongoTemplate.find(query, ImEvent.class, COLLECTION_NAME_EVENTS);
    }

    @Override
    public Mono<IRoom> findRoomByRoomId(String targetRoomId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("room_id").is(targetRoomId));
        return mongoTemplate.findOne(query, IRoom.class, COLLECTION_NAME_ROOMS);
    }
}
