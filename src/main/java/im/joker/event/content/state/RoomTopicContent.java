package im.joker.event.content.state;

import im.joker.event.content.AbstractStateContent;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class RoomTopicContent extends AbstractStateContent {

    private String topic;
}
