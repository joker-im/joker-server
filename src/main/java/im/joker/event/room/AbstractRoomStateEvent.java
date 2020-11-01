package im.joker.event.room;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
@SuperBuilder

public abstract class AbstractRoomStateEvent extends AbstractRoomEvent implements IRoomStateEvent {

    protected String stateKey;
}
