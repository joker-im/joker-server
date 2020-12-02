package im.joker.room

import im.joker.event.EventType
import im.joker.event.MembershipType
import im.joker.event.content.state.MembershipContent
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

        fun build(): RoomState {
            return RoomState().apply {
                descStateEvent = ArrayList()
                descStateMap = HashMap()
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
        val membershipEvent = descStateMap[EventType.Membership.id + userId]
        return membershipEvent?.let {
            if (it is MembershipEvent) {
                return@let MembershipType.find(it.content.membership)
            }
            null
        }
    }

    /**
     * 查询某个人最新的membership事件
     */
    fun latestMembershipEvent(userId: String): AbstractRoomStateEvent? {
        return descStateMap[EventType.Membership.id + userId]
    }

    /**
     * 查询最新的房间权限定义
     */
    fun lastPowerDefEvent(): PowerLevelEvent {
        return descStateMap[EventType.PowerLevel.id] as PowerLevelEvent
    }

    /**
     * 查询所有在此房间指定状态的userIds
     */
    fun findSpecificStateMembers(membershipType: MembershipType): List<String> {
        return descStateMap.filter { it.value is MembershipEvent && membershipType.`is`((it.value.content as MembershipContent).membership) }
                .map { it.value.stateKey }
    }

    fun findMembershipEvents(membershipType: MembershipType): List<MembershipEvent> {
        return descStateMap.filter { it.value is MembershipEvent && membershipType.`is`((it.value.content as MembershipContent).membership) }
                .map { it.value as MembershipEvent }
    }

}