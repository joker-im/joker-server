package im.joker.constants

class ImConstants {
    companion object {
        /**
         * token键,用来看哪个用户申请了token
         * %s  : userId
         * hashKey : device_id , token
         */
        const val USER_DEVICES_TOKENS_HASH = "im.joker:devices_tokens_hash:%s"

        /**
         * token键,用来看token是哪个用户的
         * %s是token, value有很多key
         */
        const val TOKEN_USER_HASH = "im.joker:token_user:%s"

        const val TOKEN_USER_HASH_KEY_USERNAME = "username"

        const val TOKEN_USER_HASH_KEY_DEVICE_ID = "device_id"

        const val TOKEN_USER_HASH_KEY_USER_ID = "user_id"

        const val TOKEN_USER_HASH_KEY_DEVICE_NAME = "device_name"

        const val TOKEN_USER_HASH_KEY_USER_AVATAR = "user_avatar"

        const val TOKEN_USER_HASH_KEY_USER_DISPLAY_NAME = "user_display_name"

        /**
         * 状态键,用来定位当前用户的登录状态
         * %s是user, value是 ["online", "offline", "unavailable"] 其中之一
         */
        const val USER_PRESENCE = "im.joker:user_presence:%s"


        /**
         * stream的自增序列
         */
        const val EVENT_STREAM_SEQUENCE = "im.joker:event_stream_sequence"

        /**
         * 用户名序列
         */
        const val USER_DISPLAY_NAME_SEQUENCE = "im.joker:user_display_name_sequence"


        /**
         * 活动房间最新的事件集合
         * %s是room_id
         */
        const val ACTIVE_ROOM_LATEST_EVENTS = "im.joker:active_room_latest_events:%s"


        /**
         * 房间的订阅者, 是一个set集合,%s是roomId,value是deviceIds
         */
        const val ROOM_SUBSCRIBERS_OF_DEVICE = "im.joker:room_subscribers_of_device:%s"


        /**
         * 发消息的分布式锁
         * %s是roomId
         */
        const val EVENT_LOCK = "im.joker:room_event_lock:%s"

        /**
         * 房间状态变更通知的topic_key
         */
        const val ROOM_STATE_TOPIC = "im.joker:room_state_topic"

        /**
         * 正在sync的设备
         */
        const val ROOM_SYNC_DEVICE = "im.joker:sync_device_topic"

        /**
         * 每个userId在某个房间中能读的最大maxStreamId,
         * 其key是roomId,其value是streamId
         */
        const val ROOM_MESSAGE_LIMIT_HASH = "im.joker:room_message_limit:%s"
    }
}

