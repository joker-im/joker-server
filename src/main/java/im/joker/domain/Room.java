package im.joker.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document
public class Room {
    @Id
    private Integer id;

    private String creator;

    private LocalDateTime createTime;

    private String type;



}
