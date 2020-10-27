package im.joker.event.room;

import im.joker.event.content.IContent;
import im.joker.event.content.MembershipContent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class MembershipEvent extends AbsStateEvent {

    private MembershipContent content;



    @Override
    public IContent getContent() {
        return content;
    }
}
