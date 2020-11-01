package im.joker.event.room.other;

import im.joker.event.content.IContent;
import im.joker.event.content.other.FullReadContent;
import im.joker.event.room.AbstractRoomEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * @Author: mkCen
 * @Date: 2020/10/31
 * @Time: 10:08
 * @Desc:
 */
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
@SuperBuilder
public class FullReadEvent extends AbstractRoomEvent {


    private FullReadContent content;

    @Override
    public IContent getContent() {
        return content;
    }
}
