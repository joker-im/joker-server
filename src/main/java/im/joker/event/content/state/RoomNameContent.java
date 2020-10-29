package im.joker.event.content.state;

import im.joker.event.content.AbstractStateContent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class RoomNameContent extends AbstractStateContent {

    private String name;
}
