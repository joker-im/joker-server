package im.joker.api.vo.room;

import im.joker.event.room.AbstractRoomEvent;
import im.joker.event.room.AbstractRoomStateEvent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class MessageResponse {

    private Integer start;

    private Integer end;

    private List<AbstractRoomEvent> chunk;

    private List<AbstractRoomStateEvent> state;

}
