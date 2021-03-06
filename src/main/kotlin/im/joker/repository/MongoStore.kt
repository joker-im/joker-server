package im.joker.repository

import com.mongodb.BasicDBObject
import com.mongodb.client.model.IndexModel
import com.mongodb.client.model.IndexOptions
import im.joker.api.vo.RoomEvents
import im.joker.event.EventType
import im.joker.event.room.AbstractRoomEvent
import im.joker.event.room.AbstractRoomStateEvent
import im.joker.event.room.other.ReceiptEvent
import im.joker.room.Room
import im.joker.room.RoomReadMarker
import im.joker.upload.UploadFile
import im.joker.user.User
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactive.awaitSingleOrDefault
import kotlinx.coroutines.reactive.awaitSingleOrNull
import org.bson.Document
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.aggregation.MatchOperation
import org.springframework.data.mongodb.core.aggregation.SortOperation
import org.springframework.data.mongodb.core.query.BasicQuery
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import javax.annotation.PostConstruct

/**
 * @Author: mkCen
 * @Date: 2020/11/21
 * @Time: 18:10
 * @Desc:
 */
@Component
class MongoStore {

    @Autowired
    private lateinit var mongoTemplate: ReactiveMongoTemplate

    companion object {
        const val COLLECTION_NAME_ROOMS = "rooms"
        const val COLLECTION_NAME_EVENTS = "events"
        const val COLLECTION_USER = "users"
        const val COLLECTION_FILE = "files"
        const val COLLECTION_ROOM_FULL_READ_MARKER = "room_full_read_marker"
    }

    @PostConstruct
    fun init() {
        createCollectionAndIndex().subscribe()
    }

    private fun createRoomCollection(): Mono<String> {
        return mongoTemplate.createCollection(COLLECTION_NAME_ROOMS) // 创建room的索引
                .flatMap { Mono.from(it.createIndex(Document.parse("{room_id: 1}"), IndexOptions().unique(true))) }
    }

    private fun createEventCollection(): Mono<String> {
        return mongoTemplate.createCollection(COLLECTION_NAME_EVENTS)
                .flatMap {
                    val index1 = IndexModel(Document.parse("{stream_id:1,room_id: 1}"))
                    val index2 = IndexModel(Document.parse("{event_id: 1}"), IndexOptions().unique(true))
                    val index3 = IndexModel(Document.parse("{sender: 1,room_id: 1, type: 1}"))
                    val index4 = IndexModel(Document.parse("{room_id:1}"))
                    val index5 = IndexModel(Document.parse("{transaction_id:1}"), IndexOptions().unique(true))
                    Mono.from(it.createIndexes(listOf(index1, index2, index3, index4, index5)))
                }
    }


    private fun createUserCollection(): Mono<String> {
        return mongoTemplate.createCollection(COLLECTION_USER) // 创建user的索引
                .flatMap {
                    val index1 = IndexModel(Document.parse("{username:1}"), IndexOptions().unique(true))
                    val index2 = IndexModel(Document.parse("{create_time:-1}"))
                    val index3 = IndexModel(Document.parse("{user_id:-1}"), IndexOptions().unique(true))
                    Mono.from(it.createIndexes(listOf(index1, index2, index3)))
                }
    }

    private fun createFileCollection(): Mono<String> {
        return mongoTemplate.createCollection(COLLECTION_FILE)
                .flatMap {
                    val index1 = IndexModel(Document.parse("{'filepath': 'hashed' }"))
                    val index2 = IndexModel(Document.parse("{upload_time:-1}"))
                    Mono.from(it.createIndexes(listOf(index1, index2)))
                }
    }

    private fun createReadMarkerCollection(): Mono<String> {
        return mongoTemplate.createCollection(COLLECTION_ROOM_FULL_READ_MARKER)
                .flatMap {
                    val index1 = IndexModel(Document.parse("{user_id:1,room_id:1}"), IndexOptions().unique(true))
                    val index2 = IndexModel(Document.parse("{read_marker_time:-1}"))
                    Mono.from(it.createIndexes(listOf(index1, index2)))
                }

    }


    private fun createCollectionAndIndex(): Mono<Void> {
        // 创建room
        return mongoTemplate.collectionExists(COLLECTION_NAME_ROOMS)
                .flatMap {
                    if (!it) createRoomCollection() else Mono.empty()
                }
                .then(mongoTemplate.collectionExists(COLLECTION_NAME_EVENTS))
                .flatMap {
                    if (!it) createEventCollection() else Mono.empty()
                }
                .then(mongoTemplate.collectionExists(COLLECTION_USER))
                .flatMap {
                    if (!it) createUserCollection() else Mono.empty()
                }.then(mongoTemplate.collectionExists(COLLECTION_FILE))
                .flatMap {
                    if (!it) createFileCollection() else Mono.empty()
                }
                .then(mongoTemplate.collectionExists(COLLECTION_ROOM_FULL_READ_MARKER))
                .flatMap {
                    if (!it) createReadMarkerCollection() else Mono.empty()
                }.then()
    }

    suspend fun addUser(u: User): User {
        return mongoTemplate.insert(u, COLLECTION_USER).awaitSingleOrNull()
    }

    suspend fun findSpecifiedTypeEvents(eventType: EventType, stateKey: String): List<AbstractRoomEvent> {
        val query = Query()
        query.addCriteria(Criteria.where("type").`is`(eventType.id).and("state_key").`is`(stateKey))
        return mongoTemplate.find(query, AbstractRoomEvent::class.java, COLLECTION_NAME_EVENTS).collectList().awaitSingleOrNull()
    }

    suspend fun findUserByUsername(username: String): User? {
        val query = Query()
        query.addCriteria(Criteria.where("username").`is`(username))
        return mongoTemplate.findOne(query, User::class.java, COLLECTION_USER).awaitSingleOrNull()
    }

    suspend fun addRoom(room: Room): Room {
        return mongoTemplate.insert(room, COLLECTION_NAME_ROOMS).awaitSingle()
    }

    suspend fun addEvents(evs: List<AbstractRoomEvent>) {
        mongoTemplate.insertAll(Mono.just(evs), COLLECTION_NAME_EVENTS).collectList().awaitSingle()
    }

    suspend fun addEvent(ev: AbstractRoomEvent): AbstractRoomEvent {
        return mongoTemplate.insert(ev, COLLECTION_NAME_EVENTS).awaitSingle()
    }

    suspend fun findByEventIds(eventIds: List<String>): List<AbstractRoomEvent> {
        val query = Query()
        query.addCriteria(Criteria.where("event_id").`in`(eventIds))
        return mongoTemplate.find(query, AbstractRoomEvent::class.java, COLLECTION_NAME_EVENTS).collectList().awaitSingleOrNull()
    }

    suspend fun findRoomStateEvents(roomId: String): List<AbstractRoomStateEvent> {
        return findRoomStateEvents(listOf(roomId), null)
    }


    suspend fun findRoomStateEvents(roomIds: List<String>, beforeStreamId: Long?): List<AbstractRoomStateEvent> {
        val query = Query()
        val criteria = Criteria.where("room_id").`in`(roomIds)
        beforeStreamId?.let {
            criteria.and("stream_id").lte(beforeStreamId)
        }
        criteria.and("type")
                .`in`(EventType.values().filter { it.isState }.map { it.id })
        query.addCriteria(criteria)
        return mongoTemplate.find(query, AbstractRoomStateEvent::class.java, COLLECTION_NAME_EVENTS).collectList()
                .awaitSingleOrNull()
    }


    suspend fun findEventGroupByRoomTopK(joinRoomIds: List<String>, k: Int, beforeStreamId: Long, asc: Boolean = false): List<RoomEvents> {
        if (joinRoomIds.isEmpty()) {
            return emptyList()
        }
        val matchOperation = MatchOperation(Criteria.where("room_id").`in`(joinRoomIds).and("stream_id").lte(beforeStreamId))
        val sortOperation = if (asc) {
            SortOperation(Sort.by(Sort.Direction.ASC, "stream_id"))
        } else {
            SortOperation(Sort.by(Sort.Direction.DESC, "stream_id"))
        }
        val groupOperation = Aggregation.group("room_id")
                .push(BasicDBObject()
                        .append("room_id", "\$room_id")
                        .append("state_key", "\$state_key")
                        .append("content", "\$content")
                        .append("_id", "\$_id")
                        .append("sender", "\$sender")
                        .append("stream_id", "\$stream_id")
                        .append("transaction_id", "\$transaction_id")
                        .append("event_id", "\$event_id")
                        .append("type", "\$type")
                        .append("origin_server_ts", "\$origin_server_ts")
                        .append("_class", "\$_class")
                        .append("unsigned", "\$unsigned")).`as`("last_events")
        val projection = Aggregation.project()
                .andExclude("_id")
                .and("\$last_events.room_id").arrayElementAt(0).`as`("room_id")
                .and("\$last_events").slice(k).`as`("slice_last_events")
        val aggregation = Aggregation.newAggregation(matchOperation, sortOperation, groupOperation, projection)
        return mongoTemplate.aggregate(aggregation, COLLECTION_NAME_EVENTS, RoomEvents::class.java).collectList().awaitSingleOrNull()
    }

    suspend fun findRoom(roomId: String): Room? {
        val query = Query()
        query.addCriteria(Criteria.where("room_id").`is`(roomId))
        return mongoTemplate.findOne(query, Room::class.java, COLLECTION_NAME_ROOMS).awaitSingleOrNull()
    }

    suspend fun addUploadFile(up: UploadFile): UploadFile {
        return mongoTemplate.insert(up, COLLECTION_FILE).awaitSingle()
    }

    suspend fun findUploadFile(mediaId: String): UploadFile? {
        val query = Query()
        query.addCriteria(Criteria.where("_id").`is`(mediaId))
        return mongoTemplate.findOne(query, UploadFile::class.java, COLLECTION_FILE).awaitSingleOrNull()
    }


    /**
     * 时间线往前查询count条该房间的消息
     */
    suspend fun findForwardRoomEvents(roomId: String, gteStreamId: Long, limit: Int): List<AbstractRoomEvent> {
        val query = Query()
        query.addCriteria(Criteria.where("stream_id").gte(gteStreamId).and("room_id").`is`(roomId))
                .limit(limit)
        query.with(Sort.by(Sort.Direction.ASC, "stream_id"))
        return mongoTemplate.find(query, AbstractRoomEvent::class.java, COLLECTION_NAME_EVENTS).collectList().awaitSingleOrNull()
    }

    /**
     * 时间线往后查询count条房间的消息
     */
    suspend fun findBackwardEvents(roomId: String, gteStreamId: Long, lteStreamId: Long, limit: Int): List<AbstractRoomEvent> {
        val query = Query()
        query.addCriteria(Criteria.where("room_id").`is`(roomId)
                .andOperator(Criteria.where("stream_id").lte(lteStreamId), Criteria.where("stream_id").gte(gteStreamId))
        )
                .limit(limit)
        query.with(Sort.by(Sort.Direction.DESC, "stream_id"))
        return mongoTemplate.find(query, AbstractRoomEvent::class.java, COLLECTION_NAME_EVENTS).collectList().awaitSingleOrNull()
    }

    suspend fun findUserByUserId(userId: String): User? {
        val query = Query()
        query.addCriteria(Criteria.where("user_id").`is`(userId))
        return mongoTemplate.findOne(query, User::class.java, COLLECTION_USER).awaitSingleOrNull()
    }

    /**
     * 设置完全读标志
     */
    suspend fun insertFullReadEvent(roomReadMarker: RoomReadMarker) {
        mongoTemplate.insert(roomReadMarker, COLLECTION_ROOM_FULL_READ_MARKER).awaitSingleOrNull()
    }

    suspend fun updateFullReadEvent(ev: ReceiptEvent, oldVersion: Int): Long {
        val query = Query().addCriteria(Criteria.where("user_id")
                .`is`(ev.sender).and("room_id")
                .`is`(ev.roomId).and("version").`is`(oldVersion))
        val update = Update().set("event_id", ev.eventId)
                .set("user_id", ev.sender)
                .set("room_id", ev.roomId)
                .set("read_marker_time", ev.originServerTs)
                .set("version", oldVersion + 1)
        return mongoTemplate.updateFirst(query, update, COLLECTION_ROOM_FULL_READ_MARKER).awaitSingle().modifiedCount
    }

    suspend fun findUsersBySearchTerm(searchItem: String, limit: Int): List<User> {
        val query = BasicQuery("""{${'$'}or:[{"user_id":/$searchItem/},{"display_name":/$searchItem/}]}""")
                .limit(limit)
        return mongoTemplate.find(query, User::class.java, COLLECTION_USER).collectList().awaitSingleOrNull()
    }

    suspend fun updateUserProfile(oldVersion: Int, userId: String, displayName: String?, avatarUrl: String?): Long {
        val query = Query()
        query.addCriteria(Criteria.where("user_id").`is`(userId))
        query.addCriteria(Criteria.where("version").`is`(oldVersion))
        val update = Update()
                .set("version", oldVersion + 1)
        displayName?.let {
            update.set("display_name", it)
        }
        avatarUrl?.let {
            update.set("avatar", it)
        }
        return mongoTemplate.updateFirst(query, update, COLLECTION_USER).awaitSingle().modifiedCount

    }

    suspend fun findLatestStreamId(): Long {
        val query = Query().with(Sort.by(Sort.Direction.DESC, "stream_id"))
        return mongoTemplate.findOne(query, AbstractRoomEvent::class.java, COLLECTION_NAME_EVENTS).map { it.streamId }.awaitSingleOrDefault(-1L)
    }

    /**
     * 查询某用户某房间读取的消息
     */
    suspend fun findAlreadyReadEventId(roomId: String, userId: String): RoomReadMarker? {
        val query = Query()
                .addCriteria(Criteria.where("user_id").`is`(userId))
                .addCriteria(Criteria.where("room_id").`is`(roomId))
        return mongoTemplate.findOne(query, RoomReadMarker::class.java, COLLECTION_ROOM_FULL_READ_MARKER).awaitSingle()
    }


}