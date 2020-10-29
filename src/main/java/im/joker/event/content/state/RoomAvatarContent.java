package im.joker.event.content.state;

import im.joker.event.ImageInfo;
import im.joker.event.content.AbstractStateContent;
import im.joker.event.content.IContent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class RoomAvatarContent extends AbstractStateContent {

    private ImageInfo info;

    private String url;


}
