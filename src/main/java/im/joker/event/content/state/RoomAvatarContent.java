package im.joker.event.content.state;

import im.joker.event.ImageInfo;
import im.joker.event.content.IContent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class RoomAvatarContent implements IContent {

    private ImageInfo info;

    private String url;


}
