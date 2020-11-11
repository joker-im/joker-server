package im.joker.api.vo.e2e;

import im.joker.event.room.AbstractRoomEvent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@Data
@Builder
@NoArgsConstructor
public class ToDevice {
    private List<AbstractRoomEvent> events;
}
