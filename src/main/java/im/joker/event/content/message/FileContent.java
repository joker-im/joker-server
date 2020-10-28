package im.joker.event.content.message;

import im.joker.event.content.AbstractMessageContent;
import im.joker.event.content.FileInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@Data
@NoArgsConstructor
public class FileContent extends AbstractMessageContent {


    private String filename;

    private FileInfo info;

    private String url;



}
