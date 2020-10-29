package im.joker.api.vo.presence;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PresenceRequest {

    /**
     * ["online", "offline", "unavailable"]  只能为其中一种
     */
    private String presence;

    private String statusMsg;
}
