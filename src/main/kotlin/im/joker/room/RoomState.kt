package im.joker.room

import im.joker.event.EventType
import im.joker.event.MembershipType
import im.joker.event.room.AbstractRoomEvent
import im.joker.event.room.AbstractRoomStateEvent
import im.joker.event.room.state.MembershipEvent
import im.joker.event.room.state.PowerLevelEvent
import im.joker.helper.GlobalStateHolder

class RoomState {

    lateinit var descStateEvent: List<AbstractRoomStateEvent>

    private lateinit var descStateMap: Map<String, AbstractRoomStateEvent>

    companion object {
        /**
         * 从数据库里面查询最新的状态事件
         */
        suspend fun from(roomId: String, globalStateHolder: GlobalStateHolder): RoomState {
            // 逆序
            val stateEvents = globalStateHolder
                    .mongoStore.findRoomStateEvents(roomId).sortedByDescending { it.streamId }
            val stateMap = toStateMap(stateEvents)

            return RoomState().apply {
                descStateEvent = stateEvents
                descStateMap = stateMap
            }

        }

        fun fromEvents(list: List<AbstractRoomStateEvent>): RoomState {
            val handledStateEvents = list.sortedByDescending { it.streamId }
            val stateMap = toStateMap(handledStateEvents)
            return RoomState().apply {
                descStateEvent = handledStateEvents
                descStateMap = stateMap
            }
        }

        private fun toStateMap(handledStateEvents: List<AbstractRoomStateEvent>): Map<String, AbstractRoomStateEvent> {
            // 新的替换旧的
            return handledStateEvents.groupingBy {
                it.type + it.stateKey
            }.reduce { _, acc, e -> if (acc.streamId > e.streamId) acc else e }
        }

    }

    /**
     * 查询状态事件.(替换式) , 返回的结果是asc
     */
    fun distinctStateEvents(): List<AbstractRoomStateEvent> {
        return descStateMap.values.sortedBy { it.streamId }
    }


    /**
     * 查询用户最新的membership状态
     */
    fun latestMembershipType(userId: String): MembershipType? {
        val membershipEvent = descStateMap[userId]
        return membershipEvent?.let {
            if (it is MembershipEvent) {
                return@let MembershipType.find(it.content.membership)
            }
            null
        }
    }

    fun latestMembershipEvent(userId: String): AbstractRoomStateEvent? {
        return descStateMap[userId]
    }

    /**
     * 查询最新的房间权限定义
     */
    fun lastPowerDefEvent(): PowerLevelEvent {
        return descStateMap[EventType.PowerLevel.id] as PowerLevelEvent
    }

}