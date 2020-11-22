package im.joker.api.vo.room;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import im.joker.event.content.state.PowerLevelContent;
import im.joker.event.room.AbstractRoomStateEvent;

import java.util.List;

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
     * 创房的时候可以放一堆事件到这里,成为附加事件,根据事件里面的type字段选择指定的子类
     */
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", visible = true)
    private List<AbstractRoomStateEvent> initialState;

    /**
     * 房间是否公有可见, 默认是私有
     * public, private
     */
    private String visibility;

    private PowerLevelContent powerLevelContentOverride;

    public String getPreset() {
        return preset;
    }

    public void setPreset(String preset) {
        this.preset = preset;
    }

    public String getRoomAliasName() {
        return roomAliasName;
    }

    public void setRoomAliasName(String roomAliasName) {
        this.roomAliasName = roomAliasName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public List<String> getInvite() {
        return invite;
    }

    public void setInvite(List<String> invite) {
        this.invite = invite;
    }

    public Boolean getDirect() {
        return direct;
    }

    public void setDirect(Boolean direct) {
        this.direct = direct;
    }

    public List<AbstractRoomStateEvent> getInitialState() {
        return initialState;
    }

    public void setInitialState(List<AbstractRoomStateEvent> initialState) {
        this.initialState = initialState;
    }

    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    public PowerLevelContent getPowerLevelContentOverride() {
        return powerLevelContentOverride;
    }

    public void setPowerLevelContentOverride(PowerLevelContent powerLevelContentOverride) {
        this.powerLevelContentOverride = powerLevelContentOverride;
    }
}
