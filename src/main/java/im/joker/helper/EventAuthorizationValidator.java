package im.joker.helper;

import im.joker.event.MembershipType;
import im.joker.event.content.state.MembershipContent;
import im.joker.event.content.state.PowerLevelContent;
import im.joker.event.room.AbstractRoomEvent;
import im.joker.event.room.AbstractRoomStateEvent;
import im.joker.event.room.state.MembershipEvent;
import im.joker.event.room.state.PowerLevelEvent;
import im.joker.room.RoomState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;


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
        MembershipEvent latestSenderMembershipEvent = roomState.getLatestMembershipEventMap().get(sender);
        if (latestSenderMembershipEvent == null) {
            log.info("当前房间id:{},该sender:{} 从未在房间出现过membership事件", roomState.getRoom().getRoomId(), sender);
            return false;
        }
        MembershipContent membershipContent = (MembershipContent) latestSenderMembershipEvent.getContent();
        return MembershipType.Invite.is(membershipContent.getMembership());
    }


    /**
     * 查看sender是否能够发送邀请人事件,那么sender在房间之中,并且需要足够的邀请权限
     *
     * @param roomState
     * @param sender
     * @return
     */
    public boolean canPostInviteEvent(RoomState roomState, String sender) {
        // 检测该sender是否有邀请的权限
        PowerLevelEvent roomPowerLevelEvent = findRoomPowerLevelEvent(roomState);
        if (roomPowerLevelEvent != null) {
            PowerLevelContent powerLevelContent = (PowerLevelContent) roomPowerLevelEvent.getContent();
            int senderInvitePower = powerLevelContent.getUserDefault();
            int inviteNeedPower = powerLevelContent.getStateDefault();
            if (!CollectionUtils.isEmpty(powerLevelContent.getUsers()) && powerLevelContent.getUsers().get(sender) != null) {
                senderInvitePower = powerLevelContent.getUsers().get(sender);
            }
            if (powerLevelContent.getInvite() != null) {
                inviteNeedPower = powerLevelContent.getInvite();
            }
            if (senderInvitePower < inviteNeedPower) {
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
        MembershipEvent membershipEvents = roomState.getLatestMembershipEventMap().get(sender);
        return membershipEvents != null;
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
        if (!CollectionUtils.isEmpty(plc.getUsers()) && plc.getUsers().get(sender) != null) {
            senderKickPower = plc.getUsers().get(sender);
        }
        if (plc.getKick() != null) {
            kickNeedPower = plc.getKick();
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
        MembershipEvent membershipEvent = roomState.getLatestMembershipEventMap().get(target);
        if (membershipEvent == null) {
            return false;
        }
        MembershipContent membershipContent = (MembershipContent) membershipEvent.getContent();
        return MembershipType.Join.is(membershipContent.getMembership());
    }

    public boolean canPostMessageEvent(RoomState roomState, AbstractRoomEvent messageEvent) {
        // todo 检测该用户发消息的权限是否足够
        return noBanInRoom(roomState, messageEvent.getSender());
    }
}
