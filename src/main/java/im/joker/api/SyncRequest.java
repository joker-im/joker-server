package im.joker.api;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class SyncRequest {


    private String filter;
    @NotNull(message = "since不可为空")
    private String since;

    private Boolean fullState;

    private Integer timeout;

    private String setPresence;

}
