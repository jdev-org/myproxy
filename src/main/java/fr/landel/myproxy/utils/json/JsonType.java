package fr.landel.myproxy.utils.json;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import fr.landel.myproxy.utils.CastUtils;

public enum JsonType {
    NULL(null),
    STRING(String.class),
    BOOLEAN(Boolean.class, s -> {
        return "true".equals(s.toLowerCase());
    }),
    LONG(Long.class, Long::parseLong),
    DOUBLE(Double.class, Double::parseDouble),
    ARRAY(Set.class),
    NODE(JsonObject.class),
    UNKNOWN(Void.class);

    private static final JsonType[] CLASSES;
    static {
        final Set<JsonType> classes = new HashSet<>();
        for (JsonType type : JsonType.values()) {
            if (type.clazz != null && type.clazz != Void.class) {
                classes.add(type);
            }
        }
        CLASSES = classes.toArray(JsonType[]::new);
    }

    private final Function<String, ?> parser;
    private final Class<?> clazz;

    private <T> JsonType(final Class<T> clazz) {
        this.clazz = clazz;
        this.parser = Function.identity();
    }

    private <T> JsonType(final Class<T> clazz, final Function<String, T> parser) {
        this.clazz = clazz;
        this.parser = parser;
    }

    public <T> boolean is(final T object) {
        if (object == null && this.clazz == null) {
            return true;
        } else {
            return object != null && this.clazz != null && this.clazz.equals(object.getClass());
        }
    }

    public <T> T parse(final String value) {
        return CastUtils.cast(this.parser.apply(value));
    }

    public static JsonType get(final Object object) {
        if (object == null) {
            return NULL;
        } else {
            final Class<?> clazz = object.getClass();
            for (JsonType type : CLASSES) {
                if (type.clazz.equals(clazz)) {
                    return type;
                }
            }
            return UNKNOWN;
        }
    }
}
