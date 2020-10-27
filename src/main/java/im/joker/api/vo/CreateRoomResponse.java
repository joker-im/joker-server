package im.joker.api.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateRoomResponse {

    private String roomId;
}
