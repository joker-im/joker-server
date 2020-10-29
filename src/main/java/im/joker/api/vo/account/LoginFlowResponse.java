package im.joker.api.vo.account;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginFlowResponse {


    private List<LoginFlow> flows;

    @Builder
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LoginFlow {
        private String type;

    }


}

