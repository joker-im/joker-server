package im.joker.api.vo;

import im.joker.event.room.AbstractRoomEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data

public class RoomEvents {

    private String roomId;

    private List<AbstractRoomEvent> sliceLastEvents;
}
