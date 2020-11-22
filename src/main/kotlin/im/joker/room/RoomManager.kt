package im.joker.room

import im.joker.api.vo.room.CreateRoomRequest
import im.joker.api.vo.room.CreateRoomResponse
import im.joker.device.Device
import im.joker.event.EventType
import im.joker.event.MembershipType
import im.joker.event.content.state.MembershipContent
import im.joker.event.room.AbstractRoomEvent
import im.joker.event.room.AbstractRoomStateEvent
import im.joker.repository.MongoStore
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import org.springframework.stereotype.Component

/**
 * @Author: mkCen
 * @Date: 2020/11/22
 * @Time: 15:44
 * @Desc:
 */
@Component
class RoomManager {

    val log: Logger = LoggerFactory.getLogger(RoomManager::class.java)

    @Autowired
    private lateinit var redisTemplate: ReactiveStringRedisTemplate

    @Autowired
    private lateinit var mongoStore: MongoStore


    /**
     * 创建房间
     */
    suspend fun createRoom(loginDevice: Device, roomRequest: CreateRoomRequest): Room {

    }








    /**
     * 查询state_key当前加入的所有房间(不包含历史)
     */
    suspend fun searchJoinRoomIdsFromDb(stateKey: String): List<String> {
        val membershipEventsMap =
                findSpecifiedEvents(EventType.Membership, stateKey)
                        .map { it as AbstractRoomStateEvent }
                        .associateBy { it.roomId + it.stateKey }
        val destRoomIds = ArrayList<String>()
        membershipEventsMap.forEach { (k, v) ->
            val content = v.content as MembershipContent
            if (MembershipType.Join.`is`(content.membership)) {
                destRoomIds.add(k)
            }
        }
        return destRoomIds
    }


    /**
     * 找出所有房间中指定的时间
     */
    suspend fun findSpecifiedEvents(eventType: EventType, stateKey: String): List<AbstractRoomEvent> {
        return mongoStore.findSpecifiedEvents(eventType, stateKey).sortedBy { it.streamId }
    }


}