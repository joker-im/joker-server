package im.joker.store;

import im.joker.event.room.IRoomEvent;
import im.joker.room.IRoom;
import org.bson.Document;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.stereotype.Component;

/**
 * @author linyurong
 * @date 2020/9/30 16:55
 */
@Component
public class ReactiveMongodbStore implements IStore {

    private final ReactiveMongoTemplate mongoTemplate;

    private static final String COLLECTION_NAME_ROOMS = "rooms";

    private static final String COLLECTION_NAME_ROOM_STATES = "room_states";

    private static final String COLLECTION_NAME_EVENTS = "pdus";

    public ReactiveMongodbStore(ReactiveMongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
        createCollectionAndIndex();
    }
    private void createCollectionAndIndex() {
        try {
            mongoTemplate.createCollection(COLLECTION_NAME_ROOMS);
            mongoTemplate.createCollection(COLLECTION_NAME_ROOM_STATES);
            mongoTemplate.createCollection(COLLECTION_NAME_EVENTS);

            mongoTemplate.getCollection(COLLECTION_NAME_ROOMS).subscribe(o -> o.createIndex(Document.parse("{room_id: 1}")));
            mongoTemplate.getCollection(COLLECTION_NAME_ROOM_STATES).subscribe(o -> o.createIndex(Document.parse("{room_id: -1, _id: -1}")));
            mongoTemplate.getCollection(COLLECTION_NAME_ROOM_STATES).subscribe(o -> o.createIndex(Document.parse("{room_id: -1, event_id: -1}")));
            mongoTemplate.getCollection(COLLECTION_NAME_EVENTS).subscribe(o -> o.createIndex(Document.parse("{room_id: 1, _id: -1}")));
            mongoTemplate.getCollection(COLLECTION_NAME_EVENTS).subscribe(o -> o.createIndex(Document.parse("{event_id: 1}")));
            mongoTemplate.getCollection(COLLECTION_NAME_EVENTS).subscribe(o -> o.createIndex(Document.parse("{\"room_id\": 1, \"json.type\": 1, \"json.sender\": 1}")));
        } catch (Exception e) {
            // ignore already exists.
        }
    }

    @Override
    public void addRoom(IRoom room) {

    }

    @Override
    public IRoomEvent addEvent(IRoomEvent event) {
        return null;
    }
}
