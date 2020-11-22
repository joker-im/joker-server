package im.joker.api.vo.account;

import java.util.List;

public class LoginFlowResponse {


    private List<LoginFlow> flows;

    public List<LoginFlow> getFlows() {
        return flows;
    }

    public void setFlows(List<LoginFlow> flows) {
        this.flows = flows;
    }

    public static class LoginFlow {

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        private String type;

    }


}

