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

    public Mono<ServerResponse> register(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(RegisterRequest.class)
                .doOnNext(e -> requestProcessor.validate(e))
                .zipWhen(registerRequest -> {
                    User user = new User();
                    user.setRegisterDeviceId(StringUtils.defaultIfBlank(registerRequest.getDeviceId(), UUID.randomUUID().toString()));
                    BeanUtils.copyProperties(registerRequest, user);
                    user.setPassword(passwordEncoder.encode(user.getPassword()));
                    return mongodbStore.addUser(user);
                })
                .flatMap(tuple2 -> {
                    RegisterRequest registerRequest = tuple2.getT1();
                    IUser user = tuple2.getT2();
                    return deviceManager.findOrCreateDevice(user.getRegisterDeviceId(),
                            user.getUsername(),
                            registerRequest.getInitialDeviceDisplayName())
                            .flatMap(e -> {
                                RegisterResponse registerResponse = RegisterResponse.builder()
                                        .deviceId(e.getDeviceId())
                                        .accessToken(e.getAccessToken())
                                        .userId(user.getId())
                                        .build();
                                return ServerResponse.ok().bodyValue(registerResponse);
                            });
                })
                .onErrorResume(throwable -> {
                    String result = """
                                {
                                            "session": "FsfiufEOEvnjQXJRBSyTTdNr",
                                            "flows": [
                                                {
                                                    "stages": [
                                                        "m.login.recaptcha",
                                                        "m.login.terms",
                                                        "m.login.dummy"
                                                    ]
                                                },
                                                {
                                                    "stages": [
                                                        "m.login.recaptcha",
                                                        "m.login.terms",
                                                        "m.login.email.identity"
                                                    ]
                                                }
                                            ],
                                            "params": {
                                                "m.login.recaptcha": {
                                                    "public_key": "6LcgI54UAAAAABGdGmruw6DdOocFpYVdjYBRe4zb"
                                                },
                                                "m.login.terms": {
                                                    "policies": {
                                                        "privacy_policy": {
                                                            "version": "1.0",
                                                            "en": {
                                                                "name": "Terms and Conditions",
                                                                "url": "https://matrix-client.matrix.org/_matrix/consent?v=1.0"
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                            """;
                    return ServerResponse.status(HttpStatus.UNAUTHORIZED)
                            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                            .bodyValue(result);
                });
    }


    /**
     * 查询支持的登录类型
     *
     * @param serverRequest
     * @return
     */
    public Mono<ServerResponse> queryLoginFlows(ServerRequest serverRequest) {
        LoginFlowResponse.LoginFlow flow = LoginFlowResponse.LoginFlow.builder().type("m.login.password").build();
        LoginFlowResponse response = LoginFlowResponse.builder()
                .flows(List.of(flow)).build();
        return Mono.just(response)
                .flatMap(e -> ServerResponse.ok().bodyValue(e));
    }


    /**
     * 登录处理
     *
     * @param serverRequest
     * @return
     */
    public Mono<ServerResponse> login(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(LoginRequest.class)
                .doOnNext(e -> requestProcessor.validate(e))
                .flatMap(e -> authManager.login(e)
                        .flatMap(tuple2 -> {
                            LoginResponse loginResponse = LoginResponse.builder()
                                    .accessToken(tuple2.getDevice().getAccessToken())
                                    .userId(tuple2.getUser().getUsername())
                                    .deviceId(tuple2.getDevice().getDeviceId())
                                    .build();
                            return ServerResponse.ok().bodyValue(loginResponse);
                        })
                );

    }
}
