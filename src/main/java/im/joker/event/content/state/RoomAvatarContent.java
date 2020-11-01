package im.joker.event.content.state;

import im.joker.event.ImageInfo;
import im.joker.event.content.AbstractStateContent;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class RoomAvatarContent extends AbstractStateContent {

    private ImageInfo info;

    private String url;


}
