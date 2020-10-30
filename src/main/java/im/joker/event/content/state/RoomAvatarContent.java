package im.joker.event.content.state;

import im.joker.event.ImageInfo;
import im.joker.event.content.AbstractStateContent;
import im.joker.event.content.IContent;
import lombok.*;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class RoomAvatarContent extends AbstractStateContent {

    private ImageInfo info;

    private String url;


}
