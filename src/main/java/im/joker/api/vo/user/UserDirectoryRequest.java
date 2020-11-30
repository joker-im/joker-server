package im.joker.api.vo.user;

import javax.validation.constraints.NotBlank;

public class UserDirectoryRequest {

    @NotBlank(message = "搜索项不可为空")
    private String searchTerm;

    private Integer limit;

    public String getSearchTerm() {
        return searchTerm;
    }

    public void setSearchTerm(String searchTerm) {
        this.searchTerm = searchTerm;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }
}
