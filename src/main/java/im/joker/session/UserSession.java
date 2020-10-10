package im.joker.session;

import im.joker.device.IDevice;
import im.joker.helper.GlobalStateHolder;
import im.joker.user.IUser;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserSession implements IUserSession {

    private IDevice device;
    private IUser user;
    private GlobalStateHolder globalStateHolder;
}
