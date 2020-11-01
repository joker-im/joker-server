package im.joker.event.content.state;

import im.joker.event.content.AbstractStateContent;
import lombok.*;

/**
 * @Author: mkCen
 * @Date: 2020/10/31
 * @Time: 11:24
 * @Desc:
 */
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class RoomJoinRuleContent extends AbstractStateContent {

    // ["public", "knock", "invite", "private"] 只能为其一
    private String joinRule;
}
