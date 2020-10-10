package im.joker.room;

import im.joker.event.IContentEvent;
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

    private String creator;

    private LocalDateTime createTime;

    private String type;


    @Override
    public String inject(IContentEvent ev) {
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
