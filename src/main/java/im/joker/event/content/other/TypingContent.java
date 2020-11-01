package im.joker.event.content.other;

import im.joker.event.content.IContent;
import lombok.*;

import java.util.List;

/**
 * @Author: mkCen
 * @Date: 2020/10/31
 * @Time: 0:17
 * @Desc:
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class TypingContent implements IContent {

    private List<String> userIds;
}
