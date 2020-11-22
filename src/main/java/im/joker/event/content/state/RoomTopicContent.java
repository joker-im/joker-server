package im.joker.event.content.state;

import im.joker.event.content.AbstractStateContent;

public class RoomTopicContent extends AbstractStateContent {

    private String topic;

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }
}
