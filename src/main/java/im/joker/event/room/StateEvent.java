package im.joker.event.room;

public interface StateEvent extends IRoomEvent {
    /**
     * 状态的key, 同一种状态和同一个key在房间内只有一个最新的事件。
     *
     * @return stateKey
     */
    String getStateKey();
}
