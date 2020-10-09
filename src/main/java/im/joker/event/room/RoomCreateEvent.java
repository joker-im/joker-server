package im.joker.event.room;

import im.joker.event.EventType;

import java.io.Serializable;

/**
 * @author linyurong
 */
public class RoomCreateEvent extends AbstractRoomEvent<RoomCreateEvent.Content> {
    public RoomCreateEvent() {}

    public static class Content implements Serializable {
        private String creator;

        public Content() {}

        public Content(String creator) {
            this.creator = creator;
        }

        public String getCreator() {
            return creator;
        }

        public void setCreator(String creator) {
            this.creator = creator;
        }
    }

    public RoomCreateEvent(String creator) {
        this(creator, creator);
    }

    public RoomCreateEvent(String sender, String creator) {
        super(EventType.Creation.get(), sender, System.currentTimeMillis(), new Content(creator));
        this.stateKey = "";
    }

    public String getCreator() {
        return getContent().getCreator();
    }
}
