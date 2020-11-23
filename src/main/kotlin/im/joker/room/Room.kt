package im.joker.room

import im.joker.constants.ImConstants.Companion.EVENT_LOCK
import im.joker.device.Device
import im.joker.event.room.AbstractRoomEvent
import im.joker.helper.EventAuthorizationValidator
import im.joker.helper.GlobalStateHolder
import kotlinx.coroutines.reactive.awaitSingleOrNull
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

/**
 * @Author: mkCen
 * @Date: 2020/11/22
 * @Time: 21:55
 * @Desc:
 */
@Document
class Room {
    @Id
    lateinit var id: String

    lateinit var roomId: String

    lateinit var creator: String

    lateinit var createTime: LocalDateTime

    @Transient
    lateinit var globalStateHolder: GlobalStateHolder

    var direct: Boolean? = null

    suspend fun injectEvent(ev: AbstractRoomEvent, device: Device): AbstractRoomEvent {

        globalStateHolder.redissonClient.getLock(EVENT_LOCK.format(ev.roomId)).lock().awaitSingleOrNull()
        val ev2 = globalStateHolder.mongoStore.addEvent(ev)
        globalStateHolder.redissonClient.getLock(EVENT_LOCK.format(ev.roomId)).unlock()
        return ev2
    }

    suspend fun injectEvents(evs: List<AbstractRoomEvent>, device: Device) {
        globalStateHolder.mongoStore.addEvents(evs)
        evs.forEach {
            globalStateHolder.roomSubscribeManager.updateRelation(device, it)
        }
    }


}