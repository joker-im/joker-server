package im.joker.event.room;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public abstract class AbstractRoomStateEvent extends AbstractRoomEvent implements IRoomStateEvent {

    protected String stateKey;
}
