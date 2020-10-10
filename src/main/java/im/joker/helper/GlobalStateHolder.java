package im.joker.helper;

import im.joker.device.DeviceManager;
import im.joker.session.AuthManager;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Data
public class GlobalStateHolder {

    @Autowired
    private AuthManager authManager;
    @Autowired
    private DeviceManager deviceManager;


}
