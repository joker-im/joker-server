package im.joker.util;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.stream.JsonWriter;
import im.joker.exception.InvalidJsonException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class GsonUtils {
    private static final Gson INSTANCE = build(true);
    private static final Gson INSTANCE_PRETTY = buildPretty(true);
    private static final Gson INSTANCE_CAMEL = build(false);
    private static final Gson INSTANCE_CAMEL_PRETTY = buildPretty(false);

    private static GsonBuilder buildImpl(boolean underScore) {
        JsonSerializer<Date> ser = new JsonSerializer<Date>() {
            @Override
            public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext
                    context) {
                return src == null ? null : new JsonPrimitive(src.getTime());
            }
        };

        JsonDeserializer<Date> deser = new JsonDeserializer<Date>() {
            @Override
            public Date deserialize(JsonElement json, Type typeOfT,
                                    JsonDeserializationContext context) throws JsonParseException {
                return json == null ? null : new Date(json.getAsLong());
            }
        };

        return new GsonBuilder()
                .registerTypeAdapter(Date.class, ser)
                .registerTypeAdapter(Date.class, deser)
                .registerTypeAdapter(Serializable.class, new Gson().getAdapter(Object.class))
                .enableComplexMapKeySerialization()
                .setFieldNamingPolicy(underScore? FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES: FieldNamingPolicy.IDENTITY)
                .disableHtmlEscaping();
    }

    public static Gson buildPretty(boolean underScore) {
        return buildImpl(underScore).setPrettyPrinting().create();
    }

    public static Gson build(boolean underScore) {
        return buildImpl(underScore).create();
    }

    public static JsonArray asArray(List<JsonElement> elements) {
        JsonArray a = new JsonArray();
        elements.forEach(a::add);
        return a;
    }

    public static <T> Map<String, T> toMap(JsonObject obj) {
        return get().fromJson(get().toJson(obj), Map.class);
    }

    public static JsonArray asArrayObj(List<? extends Object> elements) {
        return asArray(elements.stream().map(e -> get().toJsonTree(e)).collect(Collectors.toList()));
    }

    public static JsonArray asArray(String... elements) {
        return asArray(Arrays.stream(elements).map(JsonPrimitive::new).collect(Collectors.toList()));
    }

    public static JsonArray asArray(Collection<String> elements) {
        JsonArray a = new JsonArray();
        elements.forEach(a::add);
        return a;
    }

    public static <T> List<T> asList(JsonArray a, Class<T> c) {
        List<T> l = new ArrayList<>();
        a.forEach(v -> l.add(GsonUtils.get().fromJson(v, c)));
        return l;
    }

    public static <T> List<T> asList(JsonObject obj, String member, Class<T> c) {
        return asList(getArrayOrThrow(obj, member), c);
    }

    public static JsonObject makeObj(Object o) {
        return INSTANCE.toJsonTree(o).getAsJsonObject();
    }

    public static JsonObject makeObj(String key, Object value) {
        return makeObj(key, INSTANCE.toJsonTree(value));
    }

    public static JsonObject makeObj(String key, JsonElement el) {
        JsonObject obj = new JsonObject();
        obj.add(key, el);
        return obj;
    }

    public static Gson get() {
        return INSTANCE;
    }

    public static Gson get(boolean underScore) {
        if (underScore) {
            return INSTANCE;
        }
        return INSTANCE_CAMEL;
    }

    public static Gson getPretty() {
        return INSTANCE_PRETTY;
    }

    public static Gson getPretty(boolean underScore) {
        if (underScore) {
            return INSTANCE_PRETTY;
        }
        return INSTANCE_CAMEL_PRETTY;
    }

    public static String getPrettyForLog(Object o) {
        return System.lineSeparator() + getPretty().toJson(o);
    }

    public static JsonElement parse(String s) {
        try {
            return new JsonParser().parse(s);
        } catch (JsonParseException e) {
            throw new InvalidJsonException(e);
        }
    }

    public static JsonObject parseObj(String s) {
        try {
            return parse(s).getAsJsonObject();
        } catch (IllegalStateException e) {
            throw new InvalidJsonException("Not an object");
        }
    }

    public static Map parseMap(String s) {
        try {
            return get().fromJson(s, Map.class);
        } catch (IllegalStateException e) {
            throw new InvalidJsonException("Not an object");
        }
    }

    public static String getOrThrow(JsonObject obj, String member) {
        if (!obj.has(member)) {
            throw new InvalidJsonException(member + " key is missing");
        }

        return obj.get(member).getAsString();
    }

    public static JsonArray getArrayOrThrow(JsonObject obj, String member) {
        return findArray(obj, member).orElseThrow(() -> new InvalidJsonException("Not an array"));
    }

    public static JsonObject getObjOrThrow(JsonObject obj, String member) {
        return findObj(obj, member).orElseThrow(() -> new InvalidJsonException("No object for member " + member));
    }

    public static String getString(JsonObject o, String path) {
        String[] keys = path.split("\\.");
        JsonElement e = o;
        for (String k : keys) {
            JsonElement el = e.getAsJsonObject().get(k);
            if (el != null) {
                e = el;
            } else {
                e = null;
                break;
            }
        }
        if (e != null && e.isJsonPrimitive()) {
            return e.getAsString();
        }
        return null;
    }

    public static Optional<JsonElement> findElement(JsonObject o, String key) {
        return Optional.ofNullable(o.get(key));
    }

    public static Optional<Long> findLong(JsonObject o, String key) {
        return findElement(o, key).map(el -> o.get(key)).map(JsonElement::getAsLong);
    }

    public static Optional<JsonObject> findObj(JsonObject o, String key) {
        if (!o.has(key)) {
            return Optional.empty();
        }

        return Optional.ofNullable(o.getAsJsonObject(key));
    }

    public static Optional<JsonArray> findArray(JsonObject o, String key) {
        return findElement(o, key).filter(JsonElement::isJsonArray).map(JsonElement::getAsJsonArray);
    }

    public static Optional<String> findString(JsonObject o, String key) {
        return Optional.ofNullable(getString(o, key));
    }

    // Needed to avoid silly try/catch block in lambdas
    // We only use ByteArray streams, so IOException will not happen (unless irrecoverable situation like OOM)
    private static class JsonWriterUnchecked extends JsonWriter {

        public JsonWriterUnchecked(Writer out) {
            super(out);
        }

        @Override
        public JsonWriter name(String value) {
            try {
                return super.name(value);
            } catch (IOException e) {
                throw new InvalidJsonException(e);
            }
        }
    }

    private static JsonParser parser = new JsonParser();

    private static void encodeCanonical(JsonObject el, JsonWriterUnchecked writer) throws IOException {
        writer.beginObject();
        el.entrySet().stream().sorted(Comparator.comparing(Map.Entry::getKey)).forEachOrdered(entry -> {
            writer.name(entry.getKey());
            encodeCanonicalElement(entry.getValue(), writer);
        });
        writer.endObject();
    }

    private static void encodeCanonicalArray(JsonArray array, JsonWriterUnchecked writer) throws IOException {
        writer.beginArray();
        array.forEach(el -> encodeCanonicalElement(el, writer));
        writer.endArray();
    }

    private static void encodeCanonicalElement(JsonElement el, JsonWriterUnchecked writer) {
        try {
            if (el.isJsonObject()) {
                encodeCanonical(el.getAsJsonObject(), writer);
            } else if (el.isJsonPrimitive()) {
                writer.jsonValue(el.toString());
            } else if (el.isJsonArray()) {
                encodeCanonicalArray(el.getAsJsonArray(), writer);
            } else if (el.isJsonNull()) {
                writer.nullValue();
            } else {
                throw new InvalidJsonException("Unexpected JSON type, this is a bug, report!");
            }
        } catch (IOException e) {
            throw new InvalidJsonException(e);
        }
    }

    public static String encodeCanonical(JsonObject obj) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            JsonWriterUnchecked writer = new JsonWriterUnchecked(new OutputStreamWriter(out, StandardCharsets.UTF_8));
            writer.setIndent("");
            writer.setHtmlSafe(false);
            writer.setLenient(false);

            encodeCanonical(obj, writer);
            writer.close();
            return out.toString(StandardCharsets.UTF_8.name());
        } catch (IOException e) {
            throw new InvalidJsonException(e);
        }
    }

    public static String encodeCanonical(String data) {
        JsonElement el = parser.parse(data);
        if (!el.isJsonObject()) {
            throw new InvalidJsonException("Not a JSON object, cannot encode canonical");
        }
        return encodeCanonical(el.getAsJsonObject());
    }
}
