package im.joker.session;

import im.joker.device.IDevice;
import im.joker.user.IUser;

public interface IUserSession {

    IUser getUser();

    IDevice getDevice();


}
