package im.joker.event;

import im.joker.exception.MalformedEventException;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;

/**
 * @author linyurong
 */
public enum EventKey {

    Content("content"),
    Id("event_id"),
    Timestamp("origin_server_ts"),
    PreviousEvents("prev_events"),
    PreviousState("prev_state"),
    RoomId("room_id"),
    Sender("sender"),
    StateKey("state_key"),
    Type("type"),
    TransactionId("transaction_id"),
    Membership("membership"),
    StreamId("stream_id");

    private String key;

    EventKey(String key) {
        this.key = key;
    }

    public String get() {
        return key;
    }

    public Optional<Serializable> find(Map<String, Serializable> o) {
        return o.containsKey(key)? Optional.of(o.get(key)) : Optional.empty();
    }

    public Serializable get(Map<String, Serializable> o) {
        return find(o).orElseThrow(() -> new MalformedEventException(key + " not found"));
    }

    public Optional<String> findString(Map<String, Serializable> o) {
        if (o.containsKey(key)) {
            return Optional.ofNullable(o.get(key).toString());
        }

        return Optional.empty();
    }

    public String getStringOrThrow(Map<String, Serializable> o) {
        return findString(o).orElseThrow(() -> new MalformedEventException(key + " not found"));
    }

    public String getString(Map<String, Serializable> o) {
        return findString(o).isPresent()? findString(o).get() : null;
    }

    @Override
    public String toString() {
        return key;
    }

}
