package im.joker.event.room.state;

import im.joker.event.content.IContent;
import im.joker.event.content.state.MembershipContent;
import im.joker.event.room.AbstractRoomStateEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
@SuperBuilder
public class MembershipEvent extends AbstractRoomStateEvent {



    private MembershipContent content;


    @Override
    public IContent getContent() {
        return content;
    }


}
