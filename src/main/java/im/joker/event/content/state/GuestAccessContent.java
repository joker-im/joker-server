package im.joker.event.content.state;

import im.joker.event.content.AbstractStateContent;
import lombok.*;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class GuestAccessContent extends AbstractStateContent {

    /**
     * 只能为以下["can_join", "forbidden"]
     */
    private String guestAccess;

}
