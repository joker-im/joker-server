package im.joker.event.content.state;

import im.joker.event.content.AbstractStateContent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class GuestAccessContent extends AbstractStateContent {

    /**
     * 只能为以下["can_join", "forbidden"]
     */
    private String guestAccess;

}
