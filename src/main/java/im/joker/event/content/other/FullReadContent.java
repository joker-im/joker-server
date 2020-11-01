package im.joker.event.content.other;

import im.joker.event.content.IContent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: mkCen
 * @Date: 2020/10/31
 * @Time: 10:09
 * @Desc:
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class FullReadContent implements IContent {

    private String eventId;
}
