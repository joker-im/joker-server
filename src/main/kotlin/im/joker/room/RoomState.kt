package im.joker.room

import im.joker.event.MembershipType
import im.joker.event.room.AbstractRoomEvent
import im.joker.event.room.state.MembershipEvent
import im.joker.helper.GlobalStateHolder

class RoomState {

    lateinit var descStateEvent: List<AbstractRoomEvent>

    private lateinit var descStateMap: Map<String, AbstractRoomEvent>

    companion object {
        /**
         * 从数据库里面查询最新的状态事件
         */
        suspend fun from(roomId: String, globalStateHolder: GlobalStateHolder): RoomState {
            // 逆序
            val stateEvents = globalStateHolder
                    .mongoStore.findRoomStateEvents(roomId).sortedByDescending { it.streamId }
            val stateMap = stateEvents.groupingBy {
                it.type + it.stateKey
            }.reduce { _, acc, e -> if (acc.streamId > e.streamId) acc else e }

            return RoomState().apply {
                descStateEvent = stateEvents
                descStateMap = stateMap
            }

        }
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

}