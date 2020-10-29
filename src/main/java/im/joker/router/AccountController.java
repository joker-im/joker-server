package im.joker.router;


import im.joker.api.vo.account.*;
import im.joker.config.filter.AuthFilter;
import im.joker.device.IDevice;
import im.joker.handler.UserHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@Slf4j
@RequestMapping(path = "/_matrix/client/r0", produces = MediaType.APPLICATION_JSON_VALUE)
public class AccountController {

    @Autowired
    private UserHandler userHandler;

    /**
     * 注册一个用户,会返回access_token和device_id #https://matrix.org/docs/spec/client_server/r0.6.1#post-matrix-client-r0-register
     *
     * @param registerRequest
     * @return
     */
    @PostMapping("/register")
    public Mono<ResponseEntity<?>> register(@RequestBody RegisterRequest registerRequest) {
        return userHandler.register(registerRequest)
                .onErrorResume(throwable -> Mono.just(RegisterResponse.builder().build()))
                .map(e -> {
                    if (StringUtils.isBlank(e.getAccessToken())) {
                        return buildRegisterError();
                    } else {
                        return ResponseEntity.ok(e);
                    }
                });
    }

    public ResponseEntity<String> buildRegisterError() {
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
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(result);
    }

    @GetMapping("/login")
    public Mono<LoginFlowResponse> queryLoginFlows() {
        return userHandler.queryLoginFlows();
    }

    @PostMapping("/login")
    public Mono<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        return userHandler.login(loginRequest);
    }

    @PostMapping("/logout")
    public Mono<Void> logout() {
        Mono<IDevice> loginDevice = Mono.subscriberContext().flatMap(context -> Mono.just(context.get(AuthFilter.getLoginDevice())));
        return loginDevice.flatMap(e -> userHandler.logout(e));
    }

    @PostMapping("/logout/all")
    public Mono<Void> logoutAll() {
        Mono<IDevice> loginDevice = Mono.subscriberContext().flatMap(context -> Mono.just(context.get(AuthFilter.getLoginDevice())));
        return loginDevice.flatMap(e -> userHandler.logoutAll(e));
    }


}
