package im.joker.event.content.message;

import im.joker.event.content.AbstractMessageContent;
import im.joker.event.content.LocationInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@Data
@NoArgsConstructor
public class LocationContent extends AbstractMessageContent {

    private String geoUri;

    private LocationInfo info;


}
