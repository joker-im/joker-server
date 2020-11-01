package im.joker.event.room.state;

import im.joker.event.content.IContent;
import im.joker.event.content.state.RoomJoinRuleContent;
import im.joker.event.room.AbstractRoomStateEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * @Author: mkCen
 * @Date: 2020/10/31
 * @Time: 11:21
 * @Desc:
 */

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
@SuperBuilder
public class RoomJoinRuleEvent extends AbstractRoomStateEvent {

    private RoomJoinRuleContent content;

    @Override
    public IContent getContent() {
        return content;
    }
}
