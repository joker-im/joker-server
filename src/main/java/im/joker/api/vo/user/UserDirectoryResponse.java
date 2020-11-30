package im.joker.api.vo.user;

import java.util.List;

public class UserDirectoryResponse {

    private List<UserInfo> results;

    private Boolean limited;

    public List<UserInfo> getResults() {
        return results;
    }

    public void setResults(List<UserInfo> results) {
        this.results = results;
    }

    public Boolean getLimited() {
        return limited;
    }

    public void setLimited(Boolean limited) {
        this.limited = limited;
    }

    public static class UserInfo {


        private String userId;

        private String displayName;

        private String avatarUrl;

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public String getAvatarUrl() {
            return avatarUrl;
        }

        public void setAvatarUrl(String avatarUrl) {
            this.avatarUrl = avatarUrl;
        }
    }
}
