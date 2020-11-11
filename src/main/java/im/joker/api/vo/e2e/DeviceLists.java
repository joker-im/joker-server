package im.joker.api.vo.e2e;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@Data
@Builder
@NoArgsConstructor
public class DeviceLists {

    private List<String> changed;

    private List<String> left;

}
