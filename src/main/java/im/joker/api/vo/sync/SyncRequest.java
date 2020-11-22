package im.joker.api.vo.sync;

public class SyncRequest {
    private String filter;

    private String since;

    private Boolean fullState;

    private Integer timeout;

    private String setPresence;

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public String getSince() {
        return since;
    }

    public void setSince(String since) {
        this.since = since;
    }

    public Boolean getFullState() {
        return fullState;
    }

    public void setFullState(Boolean fullState) {
        this.fullState = fullState;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    public String getSetPresence() {
        return setPresence;
    }

    public void setSetPresence(String setPresence) {
        this.setPresence = setPresence;
    }
}
