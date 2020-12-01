package im.joker.event.content.other;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import im.joker.event.content.IContent;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;


/**
 * @Author: mkCen
 * @Date: 2020/10/31
 * @Time: 0:22
 * @Desc: 此类特殊
 * {
 *     "content": {
 *         "$1435641916114394fHBLK:matrix.org": {
 *             "m.read": {
 *                 "@rikj:jki.re": {
 *                     "ts": 1436451550453
 *                 }
 *             }
 *         }
 *     }
 * }
 */
public class ReceiptContent implements IContent {


    private Map<String, Receipts> properties = new HashMap<>();

    @JsonAnySetter
    public void add(String key, Receipts value) {
        properties.put(key, value);
    }

    @JsonAnyGetter
    public Map<String, Receipts> getProperties() {
        return properties;
    }


    public void setProperties(Map<String, Receipts> properties) {
        this.properties = properties;
    }

    public static class Receipts {
        @JsonProperty("m.read")
        private Map<String, Receipt> read = new HashMap<>();

        public Map<String, Receipt> getRead() {
            return read;
        }

        public void setRead(Map<String, Receipt> read) {
            this.read = read;
        }
    }

    public static class Receipt {
        private LocalDateTime ts;

        public LocalDateTime getTs() {
            return ts;
        }

        public void setTs(LocalDateTime ts) {
            this.ts = ts;
        }
    }


}
