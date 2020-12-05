package im.joker.event.room;

import com.fasterxml.jackson.annotation.JsonInclude;
import im.joker.event.content.IContent;


public abstract class AbstractRoomStateEvent extends AbstractRoomEvent implements IRoomStateEvent {

    protected String stateKey;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    protected IContent prevContent;


    @Override
    public String getStateKey() {
        return stateKey;
    }

    public void setStateKey(String stateKey) {
        this.stateKey = stateKey;
    }

    public IContent getPrevContent() {
        return prevContent;
    }

    public void setPrevContent(IContent prevContent) {
        this.prevContent = prevContent;
    }
}
