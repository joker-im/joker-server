package im.joker.api.vo.room;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * @Author: mkCen
 * @Date: 2020/11/2
 * @Time: 23:42
 * @Desc:
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class KickRequest {

    @NotNull
    private String userId;

    private String reason;

}
