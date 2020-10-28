package im.joker.handler;

import im.joker.api.vo.*;
import im.joker.device.DeviceManager;
import im.joker.helper.PasswordEncoder;
import im.joker.helper.RequestProcessor;
import im.joker.session.AuthManager;
import im.joker.store.ReactiveMongodbStore;
import im.joker.user.IUser;
import im.joker.user.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

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

    public Mono<RegisterResponse> register(RegisterRequest registerRequest) {
        requestProcessor.validate(registerRequest);
        User user = new User();
        user.setRegisterDeviceId(StringUtils.defaultIfBlank(registerRequest.getDeviceId(), UUID.randomUUID().toString()));
        BeanUtils.copyProperties(registerRequest, user);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return mongodbStore.addUser(user)
                .flatMap(e -> deviceManager.findOrCreateDevice(user.getRegisterDeviceId(),
                        user.getUsername(),
                        registerRequest.getInitialDeviceDisplayName()))
                .map(e -> {
                    RegisterResponse response = RegisterResponse.builder()
                            .deviceId(e.getDeviceId())
                            .accessToken(e.getAccessToken())
                            .userId(user.getId())
                            .build();
                    return response;
                });
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
                .map(tuple2 -> LoginResponse.builder()
                        .accessToken(tuple2.getDevice().getAccessToken())
                        .userId(tuple2.getUser().getUsername())
                        .deviceId(tuple2.getDevice().getDeviceId())
                        .build());

    }
}
