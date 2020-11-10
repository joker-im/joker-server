package im.joker.room;

import im.joker.event.ImEvent;
import im.joker.event.room.AbstractRoomEvent;
import im.joker.helper.GlobalStateHolder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

@Document
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class Room implements IRoom {
    @Id
    private String id;

    private String roomId;

    private String creator;

    private LocalDateTime createTime;

    private transient GlobalStateHolder globalStateHolder;

    private Boolean direct;

    private String visibility;


    @Override
    public Mono<ImEvent> injectEvent(ImEvent ev) {
        return globalStateHolder.getMongodbStore().addEvent(ev);
    }

    @Override
    public Flux<ImEvent> injectEvents(List<ImEvent> evs) {
        return globalStateHolder.getMongodbStore().addEvents(evs);
    }

    @Override
    public RoomState getCurrentState() {
        return null;
    }

    @Override
    public RoomState getStateFor(String eventId) {
        return null;
    }
}
