package im.joker.repository

import com.mongodb.client.model.IndexModel
import com.mongodb.client.model.IndexOptions
import com.mongodb.reactivestreams.client.MongoCollection
import im.joker.event.EventType
import im.joker.event.room.AbstractRoomEvent
import im.joker.event.room.AbstractRoomStateEvent
import im.joker.room.Room
import im.joker.user.User
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactive.awaitSingleOrNull
import org.bson.Document
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.util.*
import java.util.stream.Collectors
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
        const val COLLECTION_NAME_ROOM_STATES = "room_states"
        const val COLLECTION_NAME_EVENTS = "events"
        const val COLLECTION_USER = "users"
    }

    @PostConstruct
    fun init() {
        createCollectionAndIndex().subscribe()
    }

    private fun createRoomCollection(): Mono<String> {
        return mongoTemplate.createCollection(COLLECTION_NAME_ROOMS) // 创建room的索引
                .flatMap { o: MongoCollection<Document> -> Mono.from(o.createIndex(Document.parse("{room_id: 1}"), IndexOptions().unique(true))) }
    }

    private fun createEventCollection(): Mono<String> {
        return mongoTemplate.createCollection(COLLECTION_NAME_EVENTS)
                .flatMap { o: MongoCollection<Document> ->
                    val index1 = IndexModel(Document.parse("{room_id: 1, _id: -1}"))
                    val index2 = IndexModel(Document.parse("{event_id: 1}"))
                    val index3 = IndexModel(Document.parse("{room_id: 1, type: 1, sender: 1}"))
                    val index4 = IndexModel(Document.parse("{stream_id:1}"), IndexOptions().unique(true))
                    val indexes = listOf(index1, index2, index3, index4)
                    Mono.from(o.createIndexes(indexes))
                }
    }


    private fun createUserCollection(): Mono<String> {
        return mongoTemplate.createCollection(COLLECTION_USER) // 创建user的索引
                .flatMap { o: MongoCollection<Document> ->
                    val index1 = IndexModel(Document.parse("{username:1}"), IndexOptions().unique(true))
                    val index2 = IndexModel(Document.parse("{create_time:-1}"))
                    val index3 = IndexModel(Document.parse("{user_id:-1}"), IndexOptions().unique(true))
                    Mono.from(o.createIndexes(listOf(index1, index2, index3)))
                }
    }

    private fun createRoomStateCollection(): Mono<String> {
        return mongoTemplate.createCollection(COLLECTION_NAME_ROOM_STATES) // 创建room_state索引
                .flatMap { o: MongoCollection<Document> ->
                    val index1 = IndexModel(Document.parse("{room_id: -1, event_id: -1}"))
                    val index2 = IndexModel(Document.parse("{room_id: -1, _id: -1}"))
                    val indexes = listOf(index1, index2)
                    Mono.from(o.createIndexes(indexes))
                }
    }

    private fun createCollectionAndIndex(): Mono<Void> {
        // 创建room
        return mongoTemplate.collectionExists(COLLECTION_NAME_ROOMS)
                .flatMap { exists: Boolean? ->
                    if (!exists!!) {
                        return@flatMap createRoomCollection()
                    } else {
                        return@flatMap Mono.empty<String>()
                    }
                }
                .then(mongoTemplate.collectionExists(COLLECTION_NAME_EVENTS))
                .flatMap { exists: Boolean ->
                    if (!exists) {
                        return@flatMap createEventCollection()
                    } else {
                        return@flatMap Mono.empty<String>()
                    }
                }
                .then(mongoTemplate.collectionExists(COLLECTION_NAME_ROOM_STATES))
                .flatMap { exists: Boolean ->
                    if (!exists) {
                        return@flatMap createRoomStateCollection()
                    } else {
                        return@flatMap Mono.empty<String>()
                    }
                }
                .then(mongoTemplate.collectionExists(COLLECTION_USER))
                .flatMap { exists: Boolean ->
                    if (!exists) {
                        return@flatMap createUserCollection()
                    } else {
                        return@flatMap Mono.empty<String>()
                    }
                }.then()
    }

    suspend fun addUser(u: User): User {
        return mongoTemplate.insert(u, COLLECTION_USER).awaitSingle()
    }

    suspend fun findSpecifiedTypeEvents(eventType: EventType, stateKey: String): List<AbstractRoomEvent> {
        val query = Query()
        query.addCriteria(Criteria.where("type").`is`(eventType.id).and("state_key").`is`(stateKey))
        return mongoTemplate.find(query, AbstractRoomEvent::class.java, COLLECTION_NAME_EVENTS).collectList().awaitSingleOrNull()
    }

    suspend fun findUserByUsername(username: String): User? {
        val query = Query()
        query.addCriteria(Criteria.where("username").`is`(username))
        return mongoTemplate.findOne(query, User::class.java, COLLECTION_USER).awaitSingleOrNull();
    }

    suspend fun addRoom(room: Room): Room {
        return mongoTemplate.insert(room, COLLECTION_NAME_ROOMS).awaitSingle()
    }

    suspend fun addEvents(evs: List<AbstractRoomEvent>) {
        mongoTemplate.insertAll(Mono.just(evs), COLLECTION_NAME_EVENTS).awaitSingle()
    }

    suspend fun addEvent(ev: AbstractRoomEvent): AbstractRoomEvent {
        return mongoTemplate.insert(ev, COLLECTION_NAME_EVENTS).awaitSingle()
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
                .awaitSingleOrNull().sortedBy { it.streamId }
    }

}