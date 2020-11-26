package im.joker.api.vo.room;


import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

public class MessageRequest {

    private String roomId;

    @NotNull(message = "from不可为空")
    private Long from;

    private Long to;

    @NotNull(message = "dir不可为空,并且必须为b或者f")
    @Pattern(regexp = "[b,f]$", message = "dir必须为b或者f")
    private String dir;

    private Integer limit;

    private String filter;

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public Long getFrom() {
        return from;
    }

    public void setFrom(Long from) {
        this.from = from;
    }

    public Long getTo() {
        return to;
    }

    public void setTo(Long to) {
        this.to = to;
    }

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }
}
