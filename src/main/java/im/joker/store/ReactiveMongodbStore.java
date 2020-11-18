package im.joker.store;

import com.mongodb.BasicDBObject;
import com.mongodb.client.model.IndexModel;
import com.mongodb.client.model.IndexOptions;
import im.joker.api.vo.RoomEvents;
import im.joker.event.EventType;
import im.joker.event.room.AbstractRoomEvent;
import im.joker.event.room.AbstractRoomStateEvent;
import im.joker.room.IRoom;
import im.joker.user.IUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static im.joker.event.EventType.Typing;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;

/**
 * @author linyurong
 * @date 2020/9/30 16:55
 */
@Component
@RequiredArgsConstructor
@Slf4j
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
                    IndexModel index3 = new IndexModel(Document.parse("{room_id: 1, type: 1, sender: 1}"));
                    IndexModel index4 = new IndexModel(Document.parse("{stream_id:1}"), new IndexOptions().unique(true));
                    List<IndexModel> indexes = List.of(index1, index2, index3, index4);
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
    public Mono<AbstractRoomEvent> addEvent(AbstractRoomEvent event) {
        // 正在输入类型不需要入库
        if (Typing.is(event.getType())) {
            return Mono.just(event);
        }
        return mongoTemplate.insert(event, COLLECTION_NAME_EVENTS);
    }

    @Override
    public Flux<AbstractRoomEvent> addEvents(List<AbstractRoomEvent> events) {
        return mongoTemplate.insertAll(Mono.just(events), COLLECTION_NAME_EVENTS);
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
    public Flux<AbstractRoomEvent> findEvents(EventType eventType, String sender) {
        Query query = new Query();
        query.addCriteria(Criteria.where("type").is(eventType.getId()).and("sender").is(sender));
        return mongoTemplate.find(query, AbstractRoomEvent.class, COLLECTION_NAME_EVENTS);
    }

    @Override
    public Flux<AbstractRoomStateEvent> findRoomStateEvents(String roomId) {
        return findRoomStateEvents(roomId, null);
    }

    @Override
    public Flux<AbstractRoomStateEvent> findRoomStateEvents(String roomId, Long beforeStreamId) {
        Query query = new Query();
        Criteria criteria = Criteria.where("room_id").is(roomId)
                .and("type")
                .in(Arrays.stream(EventType.values()).filter(EventType::isState).map(EventType::getId).collect(Collectors.toList()));
        if (beforeStreamId != null) {
            criteria.and("stream_id").lte(beforeStreamId);
        }
        return mongoTemplate.find(query, AbstractRoomStateEvent.class, COLLECTION_NAME_EVENTS);
    }

    @Override
    public Mono<IRoom> findRoomByRoomId(String targetRoomId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("room_id").is(targetRoomId));
        return mongoTemplate.findOne(query, IRoom.class, COLLECTION_NAME_ROOMS);
    }

    @Override
    public Flux<AbstractRoomStateEvent> findRoomStateEvents(Collection<String> roomIds, Long beforeStreamId) {
        Query query = new Query();
        Criteria.where("stream_id").lte(beforeStreamId)
                .and("room_id").in(roomIds)
                .and("type")
                .in(Arrays.stream(EventType.values()).filter(EventType::isState).map(EventType::getId).collect(Collectors.toList()));
        return mongoTemplate.find(query, AbstractRoomStateEvent.class, COLLECTION_NAME_EVENTS);
    }


    @Override
    public Mono<Long> findLatestStreamId() {
        Query query = new Query();
        query.with(Sort.by(Sort.Direction.DESC, "stream_id"));
        return mongoTemplate.findOne(query, AbstractRoomStateEvent.class).map(AbstractRoomEvent::getStreamId);
    }


    /**
     * 查询每个房间的top k ,即group by roomId,然后每个房间取最新的k条(逆序)
     *
     * @param roomIds
     * @param k
     * @return
     */
    @Override
    public Mono<Map<String, List<AbstractRoomEvent>>> findEventGroupByRoomTopK(List<String> roomIds, int k, boolean asc) {
        if (CollectionUtils.isEmpty(roomIds)) {
            return Mono.empty();
        }
        MatchOperation matchOperation = new MatchOperation(Criteria.where("room_id").in(roomIds));
        SortOperation sortOperation;
        if (asc) {
            sortOperation = new SortOperation(Sort.by(Sort.Direction.ASC, "stream_id"));
        } else {
            sortOperation = new SortOperation(Sort.by(Sort.Direction.DESC, "stream_id"));
        }

        GroupOperation groupOperation = group("room_id")
                .push(new BasicDBObject()
                        .append("room_id", "$room_id")
                        .append("state_key", "$state_key")
                        .append("content", "$content")
                        .append("_id", "$_id")
                        .append("sender", "$sender")
                        .append("stream_id", "$stream_id")
                        .append("transaction_id", "$transaction_id")
                        .append("event_id", "$event_id")
                        .append("type", "$type")
                        .append("origin_server_ts", "$origin_server_ts")
                        .append("_class", "$_class")
                        .append("unsigned", "$unsigned")).as("last_events");
        ProjectionOperation projection = project()
                .andExclude("_id")
                .and("$last_events.room_id").arrayElementAt(0).as("room_id")
                .and("$last_events").slice(k).as("slice_last_events");
        Aggregation aggregation = Aggregation.newAggregation(matchOperation, sortOperation, groupOperation, projection);
        return mongoTemplate.aggregate(aggregation, COLLECTION_NAME_EVENTS, RoomEvents.class)
                .collect(Collectors.toMap(RoomEvents::getRoomId, RoomEvents::getSliceLastEvents));
    }

}
