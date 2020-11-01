package im.joker.session;

import im.joker.api.vo.account.LoginRequest;
import im.joker.api.vo.presence.PresenceRequest;
import im.joker.device.DeviceManager;
import im.joker.device.IDevice;
import im.joker.exception.ErrorCode;
import im.joker.exception.ImException;
import im.joker.handler.PresenceHandler;
import im.joker.helper.GlobalStateHolder;
import im.joker.helper.PasswordEncoder;
import im.joker.presence.PresenceType;
import im.joker.store.ReactiveMongodbStore;
import im.joker.user.IUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@Slf4j
public class AuthManager {

    @Autowired
    private ReactiveMongodbStore mongodbStore;
    @Autowired
    private DeviceManager deviceManager;
    @Autowired
    private GlobalStateHolder globalStateHolder;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private PresenceHandler presenceHandler;


    public Mono<IUserSession> login(LoginRequest loginRequest) {
        String username = loginRequest.getIdentifier().getUser();
        if (StringUtils.isBlank(loginRequest.getDeviceId())) {
            loginRequest.setDeviceId(UUID.randomUUID().toString());
        }
        return mongodbStore.findUserByUsername(username)
                .switchIfEmpty(Mono.error(new ImException(ErrorCode.INVALID_USERNAME, HttpStatus.FORBIDDEN)))
                .filter(user -> passwordEncoder.matches(loginRequest.getPassword(), user.getPassword()))
                .switchIfEmpty(Mono.error(new ImException(ErrorCode.CAPTCHA_INVALID, HttpStatus.FORBIDDEN)))
                .zipWhen(user -> deviceManager.findOrCreateDevice(loginRequest.getDeviceId(), username,
                        user.getUserId(), loginRequest.getInitialDeviceDisplayName()))
                .flatMap(tuple2 -> {
                    IUser user = tuple2.getT1();
                    IDevice device = tuple2.getT2();
                    return Mono.just(new UserSession(device, user, globalStateHolder));
                });

    }

    public Mono<Void> logout(IDevice device) {
        presenceHandler.setPresence(PresenceRequest.builder().presence(PresenceType.offline.name()).statusMsg("登出操作").build(), device);
        Mono<Void> logoutPresence = presenceHandler.deletePresence(device);
        return deviceManager.removeDevice(device).then(logoutPresence);
    }

    public Mono<Void> logoutAll(IDevice device) {
        Flux<IDevice> devices = deviceManager.findDevices(device.getUsername());
        return devices.flatMap(this::logout)
                .then(deviceManager.deleteAllDevices(device.getUsername()));
    }
}
