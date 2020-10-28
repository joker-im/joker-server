package im.joker.router;

import im.joker.config.filter.AuthFilter;
import im.joker.device.IDevice;
import im.joker.handler.RuleHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@Slf4j
@RequestMapping(path = "/_matrix/client/r0", produces = MediaType.APPLICATION_JSON_VALUE)
public class PushRuleController {

    @Autowired
    private RuleHandler ruleHandler;

    @GetMapping("/pushrules/")
    public Mono<String> searchPushRules() {
        Mono<IDevice> loginDevice = Mono.subscriberContext().flatMap(context -> context.get(AuthFilter.getLoginDevice()));
        return loginDevice.flatMap(e -> ruleHandler.retrievePushRules(e));
    }
}
