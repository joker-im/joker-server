package im.joker.event.content.message;

import im.joker.event.ImageInfo;
import im.joker.event.content.AbstractMessageContent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@Data
@NoArgsConstructor
public class ImageContent extends AbstractMessageContent {

    private ImageInfo info;

    private String url;



}
