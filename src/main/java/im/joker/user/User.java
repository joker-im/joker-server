package im.joker.user;

import im.joker.helper.GlobalStateHolder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class User implements IUser {

    @Id
    private String id;

    private String userId;

    private String username;

    private transient GlobalStateHolder globalStateHolder;

    private String displayName;

    private String avatarUrl;

    private LocalDateTime createTime;

    private String registerDeviceId;

    private String password;

}
