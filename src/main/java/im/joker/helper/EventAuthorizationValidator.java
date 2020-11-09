package im.joker.helper;

import im.joker.event.EventType;
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
     * 查看sender是否能够发送join事件.那sender不在房中,并且有被邀请的事件前提
     *
     * @param roomState
     * @param sender
     * @return
     */
    public boolean canPostJoinEvent(RoomState roomState, String sender) {
        List<MembershipEvent> membershipEvents = roomState.getUserMemberEventMap().get(sender);
        LocalDateTime latestInviteTime = null;
        LocalDateTime latestLeaveTime = null;
        if (CollectionUtils.isEmpty(membershipEvents)) {
            return false;
        }
        boolean inRoom = noBanInRoom(roomState, sender);
        if (inRoom) {
            log.info("当前房间id:{},该sender:{} 已经在房中,不需要重复post", roomState.getRoom().getRoomId(), sender);
            return false;
        }
        for (MembershipEvent e : membershipEvents) {
            MembershipContent content = (MembershipContent) e.getContent();
            if (MembershipType.Invite.is(content.getMembership()) && latestInviteTime == null) {
                latestInviteTime = e.getOriginServerTs();
            }
            if (MembershipType.Leave.is(content.getMembership()) && latestLeaveTime == null) {
                latestLeaveTime = e.getOriginServerTs();
            }
            if (latestInviteTime != null && latestLeaveTime != null) {
                break;
            }
        }
        if (latestInviteTime == null) {
            log.info("当前房间id:{},该sender:{} 没有对应是Invite事件,因此不能够发送Join事件", roomState.getRoom().getRoomId(), sender);
            return false;
        }
        if (latestLeaveTime != null && latestInviteTime.isBefore(latestLeaveTime)) {
            log.info("当前房间id:{},该sender:{} 的Invite事件在Leave事件之前, 证明此人不在room中,因此不能发送Join事件", roomState.getRoom().getRoomId(), sender);
            return false;
        }

        return true;
    }


    /**
     * 查看sender是否能够发送邀请人事件,那么sender在房间之中,并且需要足够的邀请权限
     *
     * @param roomState
     * @param sender
     * @return
     */
    public boolean canPostInviteEvent(RoomState roomState, String sender) {
        List<MembershipEvent> senderMemberEvents = roomState.getUserMemberEventMap().get(sender);
        if (CollectionUtils.isEmpty(senderMemberEvents)) {
            return false;
        }
        // 检测该sender是否有邀请的权限
        PowerLevelEvent roomPowerLevelEvent = findRoomPowerLevelEvent(roomState);
        if (roomPowerLevelEvent != null) {
            PowerLevelContent powerLevelContent = (PowerLevelContent) roomPowerLevelEvent.getContent();
            int senderInvitePower = powerLevelContent.getUserDefault();
            if (!CollectionUtils.isEmpty(powerLevelContent.getUsers()) && powerLevelContent.getUsers().get(sender) != null) {
                senderInvitePower = powerLevelContent.getUsers().get(sender);
            }
            if (senderInvitePower < powerLevelContent.getInvite()) {
                return false;
            }
        }
        return noBanInRoom(roomState, sender);
    }

    private PowerLevelEvent findRoomPowerLevelEvent(RoomState roomState) {
        AbstractRoomStateEvent event = roomState.getStateEvents().stream().filter(e -> e instanceof PowerLevelEvent).findFirst().orElse(null);
        if (event != null) {
            return (PowerLevelEvent) event;
        }
        return null;
    }


    /**
     * 查询sender是否可以向房间发送离开消息
     * 1. 当前用户不接受邀请, 也会调用Leave,表示拒绝加入该房间.但是该sender其实从未在房间中出现过
     * 2. 被解BAN的时候,会发送leave事件
     * 3. 自己想走
     *
     * @param roomState
     * @param sender
     * @return
     */
    public boolean canPostLeaveEvent(RoomState roomState, String sender) {
        List<MembershipEvent> membershipEvents = roomState.getUserMemberEventMap().get(sender);
        return !CollectionUtils.isEmpty(membershipEvents);
    }

    /**
     * 查询sender是否可以将某个人T走
     *
     * @param roomState
     * @param sender
     * @param targetUserId
     * @return
     */
    public boolean canKickMember(RoomState roomState, String sender, String targetUserId) {
        PowerLevelEvent powerLevelEvent = findRoomPowerLevelEvent(roomState);
        if (powerLevelEvent == null) {
            log.info("当前房间id:{} 没有PowerLevelEvent", roomState.getRoom().getRoomId());
            return false;
        }
        boolean senderInRoom = noBanInRoom(roomState, sender);
        if (!senderInRoom) {
            log.info("当前房间id:{},该sender:{} 不在房间内,不可T走 {}", roomState.getRoom().getRoomId(), sender, targetUserId);
            return false;
        }
        PowerLevelContent plc = (PowerLevelContent) powerLevelEvent.getContent();
        int senderKickPower = plc.getUserDefault() == null ? Integer.MIN_VALUE : plc.getUserDefault();
        int kickNeedPower = plc.getStateDefault() == null ? Integer.MAX_VALUE : plc.getStateDefault();
        if (plc.getUsers().get(sender) != null) {
            senderKickPower = plc.getUsers().get(sender);
        }
        if (plc.getKick() != null) {
            kickNeedPower = plc.getKick();
        }
        if (plc.getEvents().get(EventType.Membership.getId()) != null) {
            kickNeedPower = plc.getEvents().get(EventType.Membership.getId());
        }
        if (kickNeedPower > senderKickPower) {
            log.info("当前房间id:{},该sender:{} 定义的kick值为{},而当前sender的kick值为:{}", roomState.getRoom().getRoomId(), sender, plc.getKick(), senderKickPower);
            return false;
        }
        return true;
    }

    /**
     * 目标用户是否在房间
     *
     * @param roomState
     * @param target
     * @return
     */
    private boolean noBanInRoom(RoomState roomState, String target) {
        LocalDateTime latestLeaveTime = null;
        LocalDateTime latestJoinTime = null;
        LocalDateTime latestBanTime = null;

        List<MembershipEvent> membershipEvents = roomState.getUserMemberEventMap().get(target);

        for (MembershipEvent e : membershipEvents) {
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
        // 没有加入事件, 肯定不在房间
        if (latestJoinTime == null) {
            log.info("当前房间id:{},没有该sender:{}的加入事件", roomState.getRoom().getRoomId(), target);
            return false;
        }
        // 如果有Ban事件但无Leave事件,证明被BAN
        if (latestBanTime != null && latestLeaveTime == null) {
            log.info("当前房间id:{},该sender:{} 在中被Ban", roomState.getRoom().getRoomId(), target);
            return false;
        }
        // 如果解封了
        if (latestBanTime != null && latestLeaveTime.isAfter(latestBanTime) && latestLeaveTime.isAfter(latestJoinTime)) {
            log.info("当前房间id:{},该sender:{}被解封了,但是还未Join当前房间", roomState.getRoom().getRoomId(), target);
            return false;
        }

        if (latestLeaveTime != null && latestJoinTime.isBefore(latestLeaveTime)) {
            log.info("当前房间id:{},该sender {},的Join事件在Leave事件之前,因此该sender不在房中", roomState.getRoom().getRoomId(), target);
            return false;
        }

        return true;
    }

}
