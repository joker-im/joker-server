package im.joker.event.content;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CreateContent implements IContent {

    private String creator;

    private String roomVersion;

    private PreviousRoom predecessor;


    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class PreviousRoom {

        private String roomId;

        private String eventId;
    }
}
