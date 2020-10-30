package im.joker.event.content.state;

import im.joker.event.content.AbstractStateContent;
import lombok.*;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class RoomNameContent extends AbstractStateContent {

    private String name;
}
