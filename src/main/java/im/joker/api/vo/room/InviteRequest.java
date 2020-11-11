package im.joker.api.vo.room;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class InviteRequest {

    @NotNull(message = "userId不可为空")
    private String userId;
}
