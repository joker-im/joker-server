package im.joker.room;

import im.joker.event.ImEvent;
import im.joker.helper.GlobalStateHolder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class Room implements IRoom {
    @Id
    private Integer id;

    private String roomId;

    private String creator;

    private LocalDateTime createTime;

    private String type;

    private GlobalStateHolder globalStateHolder;


    @Override
    public String inject(ImEvent ev) {
        return null;
    }

    @Override
    public IRoomState getCurrentState() {
        return null;
    }

    @Override
    public IRoomState getStateFor(String eventId) {
        return null;
    }
}
