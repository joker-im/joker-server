package im.joker.session;

import im.joker.api.vo.LoginRequest;
import im.joker.device.DeviceManager;
import im.joker.device.IDevice;
import im.joker.exception.ErrorCode;
import im.joker.exception.ImException;
import im.joker.helper.GlobalStateHolder;
import im.joker.store.ReactiveMongodbStore;
import im.joker.user.IUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import static im.joker.constants.ImRedisKeys.TOKEN_USER;

@Component
@Slf4j
public class AuthManager {

    @Autowired
    private ReactiveMongodbStore mongodbStore;
    @Autowired
    private DeviceManager deviceManager;
    @Autowired
    private GlobalStateHolder globalStateHolder;


    public Mono<IUserSession> login(LoginRequest loginRequest) {
        String username = loginRequest.getIdentifier().getUser();
        return mongodbStore.retrieveByUsername(username)
                .switchIfEmpty(Mono.error(new ImException(ErrorCode.INVALID_USERNAME, HttpStatus.FORBIDDEN)))
                .filter(user -> user.getPassword().equals(loginRequest.getPassword()))
                .switchIfEmpty(Mono.error(new ImException(ErrorCode.CAPTCHA_INVALID, HttpStatus.FORBIDDEN)))
                .zipWhen(user -> deviceManager.findOrCreateDevice(loginRequest.getDeviceId(), username, loginRequest.getInitialDeviceDisplayName()))
                .flatMap(tuple2 -> {
                    IUser user = tuple2.getT1();
                    IDevice device = tuple2.getT2();
                    return Mono.just(new UserSession(device, user, globalStateHolder));
                });

    }

}
