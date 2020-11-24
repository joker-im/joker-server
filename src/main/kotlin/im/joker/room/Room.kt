package im.joker.room

import im.joker.constants.ImConstants.Companion.EVENT_LOCK
import im.joker.device.Device
import im.joker.event.room.AbstractRoomEvent
import im.joker.exception.ErrorCode
import im.joker.exception.ImException
import im.joker.helper.EventAuthorizationValidator
import im.joker.helper.GlobalStateHolder
import kotlinx.coroutines.reactive.awaitSingleOrNull
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.http.HttpStatus
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
        val ev2: AbstractRoomEvent
        try {
            globalStateHolder.redissonClient.getLock(EVENT_LOCK.format(ev.roomId)).lock().awaitSingleOrNull()
            if (!globalStateHolder.eventAuthorizationValidator.canPost(ev, device)) {
                throw ImException(ErrorCode.FORBIDDEN, HttpStatus.FORBIDDEN, "无权限发此种类型的消息")
            }
            ev2 = globalStateHolder.mongoStore.addEvent(ev)
            // 更新设备和房间的订阅消息
            globalStateHolder.roomSubscribeManager.updateRelation(device, ev)
            // 更新房间状态消息
            globalStateHolder.roomStateCache.notifyStateChange(ev)
            // 添加事件到redis队列
            globalStateHolder.eventSyncQueueManager.addToEventQueue(ev)
            // 长轮询唤醒
            globalStateHolder.longPollingHelper.notifySyncDevice(device.deviceId)
        } finally {
            globalStateHolder.redissonClient.getLock(EVENT_LOCK.format(ev.roomId)).unlock()
        }
        return ev2
    }

    // 此方法暂时不加锁,不校验权限.因为暂时只用于创房
    suspend fun injectEvents(evs: List<AbstractRoomEvent>, device: Device) {
        globalStateHolder.mongoStore.addEvents(evs)
        evs.forEach {
            // 更新设备和房间的订阅消息
            globalStateHolder.roomSubscribeManager.updateRelation(device, it)
            // 更新房间状态消息
            globalStateHolder.roomStateCache.notifyStateChange(it)
            // 添加事件到redis队列
            globalStateHolder.eventSyncQueueManager.addToEventQueue(it)
        }
        // 长轮询唤醒
        globalStateHolder.longPollingHelper.notifySyncDevice(device.deviceId)
    }


}