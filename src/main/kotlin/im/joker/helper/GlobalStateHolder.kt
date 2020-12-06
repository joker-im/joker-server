package im.joker.helper

import im.joker.device.DeviceManager
import im.joker.repository.MongoStore
import org.redisson.api.RedissonReactiveClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * @Author: mkCen
 * @Date: 2020/11/21
 * @Time: 18:01
 * @Desc:
 */
@Component
class GlobalStateHolder {
    @Autowired
    lateinit var deviceManager: DeviceManager

    @Autowired
    lateinit var redissonClient: RedissonReactiveClient

    @Autowired
    lateinit var mongoStore: MongoStore

    @Autowired
    lateinit var roomSubscribeManager: RoomSubscribeManager

    @Autowired
    lateinit var eventAuthorizationValidator: EventAuthorizationValidator

    @Autowired
    lateinit var syncEventManager: SyncEventManager

    @Autowired
    lateinit var imCache: ImCache

    @Autowired
    lateinit var longPollingHelper: LongPollingHelper


}