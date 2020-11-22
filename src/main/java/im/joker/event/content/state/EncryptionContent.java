package im.joker.event.content.state;

import com.fasterxml.jackson.annotation.JsonProperty;
import im.joker.event.content.AbstractStateContent;

public class EncryptionContent extends AbstractStateContent {

    /**
     * 加密算法
     */
    private String algorithm;

    private Integer rotationPeriodMs;

    @JsonProperty("rotation_period_msgs")
    private Integer rotationPeriodMsg;

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public Integer getRotationPeriodMs() {
        return rotationPeriodMs;
    }

    public void setRotationPeriodMs(Integer rotationPeriodMs) {
        this.rotationPeriodMs = rotationPeriodMs;
    }

    public Integer getRotationPeriodMsg() {
        return rotationPeriodMsg;
    }

    public void setRotationPeriodMsg(Integer rotationPeriodMsg) {
        this.rotationPeriodMsg = rotationPeriodMsg;
    }
}
