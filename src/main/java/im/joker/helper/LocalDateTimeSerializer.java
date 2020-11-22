package im.joker.helper;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * @Author: mkCen
 * @Date: 2020/11/20
 * @Time: 21:18
 * @Desc:
 */

public class LocalDateTimeSerializer extends StdSerializer<LocalDateTime> {


    public LocalDateTimeSerializer(Class<LocalDateTime> t) {
        super(t);
    }

    @Override
    public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        if (value != null) {
            gen.writeNumber(value.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        } else {
            gen.writeNull();
        }
    }
}
