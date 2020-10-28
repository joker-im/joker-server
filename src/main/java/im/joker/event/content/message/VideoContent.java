package im.joker.event.content.message;

import im.joker.event.content.AbstractMessageContent;
import im.joker.event.content.VideoInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@Data
@NoArgsConstructor
public class VideoContent extends AbstractMessageContent {

    private VideoInfo info;

    private String url;

}
