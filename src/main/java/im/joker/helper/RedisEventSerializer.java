package im.joker.helper;

import com.google.gson.JsonObject;
import im.joker.util.GsonUtils;
import org.springframework.stereotype.Component;

/**
 * redis事件序列化和反序列化工具
 *
 * @Author LinYuRong
 * @Date 2020/10/29 17:32
 * @Version 1.0
 */
@Component
public class RedisEventSerializer<T> {

    public String serialize(T event) {
        JsonObject jsonObject = GsonUtils.makeObj(event);
        jsonObject.addProperty("_class", event.getClass().getName());
        return GsonUtils.get().toJson(jsonObject);
    }

    public T deserialize(String evtJson) {
        try {
            JsonObject jsonObject = GsonUtils.parseObj(evtJson);
            String className = jsonObject.get("_class").getAsString();
            T result = (T) GsonUtils.get().fromJson(jsonObject, Class.forName(className));
            return result;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
