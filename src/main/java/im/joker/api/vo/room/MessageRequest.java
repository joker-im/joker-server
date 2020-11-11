package im.joker.api.vo.room;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class MessageRequest {

    private String roomId;

    @NotNull(message = "from不可为空")
    private Integer from;

    private Integer to;

    @NotNull(message = "dir不可为空,并且必须为b或者f")
    @Pattern(regexp = "[b,f]$", message = "dir必须为b或者f")
    private String dir;

    private Integer limit;

    private String filter;

}
