package im.joker.helper;

import im.joker.event.ImEvent;
import im.joker.event.MembershipType;
import im.joker.event.content.state.MembershipContent;
import im.joker.event.content.state.PowerLevelContent;
import im.joker.event.room.AbstractRoomStateEvent;
import im.joker.event.room.state.MembershipEvent;
import im.joker.event.room.state.PowerLevelEvent;
import im.joker.room.RoomState;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;


/**
 * 用户在房间里,或者不在房间里有5种状态
 * Unrelated (初始值)
 * Leaved  : 用户的当前状态为Joined,但是后来主动走了(或者被T)
 * Invited : 用户可能在Unrelated/Leaved 状态时候, 被人邀请
 * Joined  : 用户在Invited时接受了邀请
 * Banned  : 用户在Joined 才能被Ban, 进入到此状态
 */
@Component
@Slf4j
public class EventAuthorizationValidator {


    /**
     * 查看sender是否能够加到对应的房间.invite时间必须要在最后一次leave之后
     *
     * @param roomState
     * @param sender
     * @return
     */
    public boolean canJoin(RoomState roomState, String sender) {
        List<ImEvent> stateEvents = roomState.getStateEvents();
        LocalDateTime latestInviteTime = null;
        LocalDateTime latestLeaveTime = null;
        if (CollectionUtils.isEmpty(stateEvents)) {
            return false;
        }


        for (ImEvent e : stateEvents) {
            if (!(e instanceof MembershipEvent)) {
                continue;
            }
            MembershipContent content = (MembershipContent) e.getContent();
            String stateKey = ((MembershipEvent) e).getStateKey();
            if (MembershipType.Invite.is(content.getMembership()) && StringUtils.equals(stateKey, sender) && latestInviteTime == null) {
                latestInviteTime = ((MembershipEvent) e).getOriginServerTs();
            }
            if (MembershipType.Leave.is(content.getMembership()) && latestLeaveTime == null && StringUtils.equals(sender, stateKey)) {
                latestLeaveTime = ((MembershipEvent) e).getOriginServerTs();
            }
            if (latestInviteTime != null && latestLeaveTime != null) {
                break;
            }
        }
        return latestInviteTime != null && latestLeaveTime != null && latestInviteTime.isAfter(latestLeaveTime);
    }


    /**
     * 查看sender是否能够邀请人,那么sender必须在Joined状态,并且查看最后的leave时间要在join之前
     *
     * @param roomState
     * @param sender
     * @return
     */
    public boolean canInvite(RoomState roomState, String sender) {
        List<AbstractRoomStateEvent> senderStateEvents = roomState.getUserStateEvents().get(sender);
        if (CollectionUtils.isEmpty(senderStateEvents)) {
            return false;
        }
        // 检测该sender是否有邀请的权限
        PowerLevelContent powerLevelContent = (PowerLevelContent) findRoomPowerLevelEvent(roomState).getContent();
        int senderInvite = powerLevelContent.getUserDefault();
        if (!CollectionUtils.isEmpty(powerLevelContent.getUsers()) && powerLevelContent.getUsers().get(sender) != null) {
            senderInvite = powerLevelContent.getUsers().get(sender);
        }
        if (senderInvite < powerLevelContent.getInvite()) {
            return false;
        }
        LocalDateTime latestLeaveTime = null;
        LocalDateTime latestJoinTime = null;
        LocalDateTime latestBanTime = null;

        for (AbstractRoomStateEvent e : senderStateEvents) {
            if (!(e instanceof MembershipEvent)) {
                continue;
            }
            MembershipContent content = (MembershipContent) e.getContent();
            if (MembershipType.Join.is(content.getMembership()) && latestJoinTime == null) {
                latestJoinTime = e.getOriginServerTs();
            }
            if (MembershipType.Leave.is(content.getMembership()) && latestLeaveTime == null) {
                latestLeaveTime = e.getOriginServerTs();
            }
            if (MembershipType.Ban.is(content.getMembership()) && latestBanTime == null) {
                latestBanTime = e.getOriginServerTs();
            }
            if (latestJoinTime != null && latestLeaveTime != null && latestBanTime != null) {
                break;
            }

        }
        if (latestJoinTime == null) {
            log.info("当前房间id:{}没有该sender:{}的加入事件,无权限邀请", roomState.getRoom().getRoomId(), sender);
            return false;
        }
        if (latestBanTime != null && latestLeaveTime == null) {
            log.info("当前sender:{} 在当前房间id:{}中被ban,无权限邀请", sender, roomState.getRoom().getRoomId());
            return false;
        }
        if (latestBanTime != null && latestLeaveTime.isAfter(latestBanTime) && latestLeaveTime.isAfter(latestJoinTime)) {
            log.info("当前房间id:{}该sender:{}被解封了,但是还未join当前房间,无权限邀请", roomState.getRoom().getRoomId(), sender);
            return false;
        }

        if (latestLeaveTime == null) {
            return true;
        }

        return latestJoinTime.isAfter(latestLeaveTime);
    }

    private PowerLevelEvent findRoomPowerLevelEvent(RoomState roomState) {
        return (PowerLevelEvent) roomState.getStateEvents().stream().filter(e -> e instanceof PowerLevelEvent).findFirst().orElse(null);
    }


}
