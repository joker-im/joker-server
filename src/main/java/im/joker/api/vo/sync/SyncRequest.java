package im.joker.api.vo.sync;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class SyncRequest {
    private String filter;

    private String since;

    private Boolean fullState;

    private Integer timeout;

    private String setPresence;
}
