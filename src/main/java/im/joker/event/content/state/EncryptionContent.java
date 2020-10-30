package im.joker.event.content.state;

import com.fasterxml.jackson.annotation.JsonProperty;
import im.joker.event.content.AbstractStateContent;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class EncryptionContent extends AbstractStateContent {

    /**
     * 加密算法
     */
    private String algorithm;

    private Integer rotationPeriodMs;

    @JsonProperty("rotation_period_msgs")
    private Integer rotationPeriodMsg;
}
