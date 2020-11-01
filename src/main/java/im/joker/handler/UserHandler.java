package im.joker.handler;

import im.joker.api.vo.account.*;
import im.joker.device.DeviceManager;
import im.joker.device.IDevice;
import im.joker.exception.ImException;
import im.joker.helper.IdGenerator;
import im.joker.helper.PasswordEncoder;
import im.joker.helper.RequestProcessor;
import im.joker.session.AuthManager;
import im.joker.store.ReactiveMongodbStore;
import im.joker.user.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static im.joker.exception.ErrorCode.INVALID_PARAM;

@Service
@Slf4j
public class UserHandler {

    @Autowired
    private RequestProcessor requestProcessor;
    @Autowired
    private ReactiveMongodbStore mongodbStore;
    @Autowired
    private DeviceManager deviceManager;
    @Autowired
    private AuthManager authManager;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private IdGenerator idGenerator;

    public Mono<RegisterResponse> register(RegisterRequest registerRequest) {
        String s = requestProcessor.validateMessage(registerRequest);
        if (StringUtils.isNoneBlank(s)) {
            return Mono.error(new ImException(INVALID_PARAM, HttpStatus.BAD_REQUEST, s));
        }
        User user = new User();
        user.setRegisterDeviceId(StringUtils.defaultIfBlank(registerRequest.getDeviceId(), UUID.randomUUID().toString()));
        BeanUtils.copyProperties(registerRequest, user);
        user.setUserId(idGenerator.userId(user.getUsername()));
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setCreateTime(LocalDateTime.now());
        return mongodbStore.addUser(user)
                .flatMap(e -> deviceManager.findOrCreateDevice(user.getRegisterDeviceId(),
                        user.getUsername(), user.getUserId(),
                        registerRequest.getInitialDeviceDisplayName()))
                .map(e -> RegisterResponse.builder()
                        .deviceId(e.getDeviceId())
                        .accessToken(e.getAccessToken())
                        .userId(user.getUserId())
                        .build());
    }


    /**
     * 查询支持的登录类型
     *
     * @return
     */
    public Mono<LoginFlowResponse> queryLoginFlows() {
        LoginFlowResponse.LoginFlow flow = LoginFlowResponse.LoginFlow.builder().type("m.login.password").build();
        return Mono.just(LoginFlowResponse.builder()
                .flows(List.of(flow)).build());
    }


    /**
     * 登录处理
     *
     * @param loginRequest
     * @return
     */
    public Mono<LoginResponse> login(LoginRequest loginRequest) {
        requestProcessor.validate(loginRequest);
        return authManager.login(loginRequest)
                .map(userSession -> LoginResponse.builder()
                        .accessToken(userSession.getDevice().getAccessToken())
                        .userId(userSession.getUser().getUsername())
                        .deviceId(userSession.getDevice().getDeviceId())
                        .build());

    }

    public Mono<Void> logout(IDevice e) {
        return authManager.logout(e);
    }

    public Mono<Void> logoutAll(IDevice e) {
        return authManager.logoutAll(e);
    }
}
