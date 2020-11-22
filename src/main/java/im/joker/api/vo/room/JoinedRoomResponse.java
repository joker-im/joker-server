package im.joker.api.vo.room;

import java.util.List;

public class JoinedRoomResponse {

    private List<String> joinedRooms;


    public List<String> getJoinedRooms() {
        return joinedRooms;
    }

    public void setJoinedRooms(List<String> joinedRooms) {
        this.joinedRooms = joinedRooms;
    }
}
