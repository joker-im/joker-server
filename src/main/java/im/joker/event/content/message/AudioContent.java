package im.joker.event.content.message;

import im.joker.event.content.AbstractMessageContent;
import im.joker.event.content.AudioInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@Data
@NoArgsConstructor
@SuperBuilder
public class AudioContent extends AbstractMessageContent {


    private String url;

    private AudioInfo info;


}
