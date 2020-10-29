package im.joker.api.vo.room;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import im.joker.event.content.AbstractStateContent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateRoomRequest {

    /**
     * 房间预设状态, 有private_chat,trusted_private_chat,public_chat三种
     */
    private String preset;

    /**
     * 房间别名
     */
    private String roomAliasName;

    /**
     * 房间名称(有这个名称就应该发送一个 m.room.name事件)
     */
    private String name;

    /**
     * 房间主题事件, 有这个值就应该发送一个m.room.topic事件
     */
    private String topic;


    /**
     * 这里应该是一个用户列表,将会邀请这些人进房间
     */
    private List<String> invite;

    /**
     *
     */
    private Boolean direct;

    /**
     * 创房的时候可以放一堆事件到这里,成为附加事件
     */
    private List<StateEvent> initialState;

    /**
     * 房间是否公有可见, 默认是私有
     * public, private
     */
    private String visibility;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StateEvent {

        private String type;

        private String stateKey;

        @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "type", visible = true)
        private AbstractStateContent content;


    }
}
