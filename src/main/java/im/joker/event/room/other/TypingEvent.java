package im.joker.event.room.other;

import im.joker.event.content.IContent;
import im.joker.event.content.other.TypingContent;
import im.joker.event.room.AbstractRoomEvent;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * @Author: mkCen
 * @Date: 2020/10/31
 * @Time: 0:14
 * @Desc:
 */
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
@SuperBuilder
public class TypingEvent extends AbstractRoomEvent {



    private TypingContent content;


    @Override
    public IContent getContent() {
        return content;
    }
}
