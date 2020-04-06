package fr.landel.myproxy.utils.json.schema;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import fr.landel.myproxy.utils.CastUtils;
import fr.landel.myproxy.utils.json.JsonType;

public enum JsonSchemaType {
    STRING("string", JsonType.STRING),
    BOOLEAN("boolean", JsonType.BOOLEAN, "^(true|false)$", Pattern.CASE_INSENSITIVE, Boolean::parseBoolean, Boolean.class),
    INTEGER("integer", JsonType.LONG, "^-?(0|[1-9][0-9]*)$", Integer::parseInt, Integer.class),
    LONG("long", JsonType.LONG, "^-?(0|[1-9][0-9]*)$", Long::parseLong, Long.class),
    FLOAT("float", JsonType.DOUBLE, "^-?[0-9]+\\.[0-9]+$", Float::parseFloat, Float.class),
    DOUBLE("double", JsonType.DOUBLE, "^-?[0-9]+\\.[0-9]+(e[+-]?[0-9]+)?$", Double::parseDouble, Double.class),
    DURATION(
            "duration",
            JsonType.STRING, // from: #Duration.Lazy.PATTERN
            "^([-+]?)P(?:([-+]?[0-9]+)D)?(T(?:([-+]?[0-9]+)H)?(?:([-+]?[0-9]+)M)?(?:([-+]?[0-9]+)(?:[.,]([0-9]{0,9}))?S)?)?$",
            Pattern.CASE_INSENSITIVE,
            Duration::parse,
            Duration.class),
    PATTERN("pattern", JsonType.STRING, null, s -> true, Pattern::compile, Pattern.class),
    FILE("file", null, s -> {
        return Paths.get(s).toFile().exists();
    }, Paths::get, Path.class),
    URL(
            "url",
            JsonType.STRING,
            "^(?:(?:ht|f)tps?://[\\w.-]+(?:\\.[\\w\\.-]+)+[\\w\\-\\._~:/?#\\[\\]@!\\$&'\\(\\)\\*\\+,;=.]+|file:///[\\w]:?/(?:[^<>:\"|?*]+/)*)[^<>:\"|?*\\p{Cntrl}]+$",
            Pattern.CASE_INSENSITIVE,
            s -> {
                try {
                    return new URI(s);
                } catch (URISyntaxException e) {
                    return null;
                }
            },
            URI.class),
    REFERENCE("ref", JsonType.STRING, "[a-zA-Z0-9_-]+", String::toLowerCase, String.class),
    ENUM("enum", JsonType.ARRAY),
    ARRAY("array", JsonType.ARRAY),
    NODE("node", JsonType.NODE),
    UNKNOWN("unknown", JsonType.UNKNOWN);

    private static final Map<String, JsonSchemaType> MAP_KEYS;
    static {
        var map = new HashMap<String, JsonSchemaType>();
        for (JsonSchemaType el : JsonSchemaType.values()) {
            map.put(el.getKey(), el);
        }
        MAP_KEYS = Collections.unmodifiableMap(map);
    }

    private final String key;
    private final JsonType compatibleType;
    private final Predicate<String> validator;
    private final Function<String, ?> parser;
    private final Class<?> outputType;

    private JsonSchemaType() {
        this(null, JsonType.UNKNOWN);
    }

    private JsonSchemaType(final String key, final JsonType compatibleType) {
        this.key = key;
        this.compatibleType = compatibleType;
        this.validator = s -> true;
        this.outputType = String.class;
        this.parser = Function.identity();
    }

    private <T> JsonSchemaType(final String key, final JsonType compatibleType, final String pattern, final int flags,
            final Function<String, T> parser, final Class<T> outputType) {
        this(key, compatibleType, pattern, flags, null, parser, outputType);
    }

    private <T> JsonSchemaType(final String key, final JsonType compatibleType, final String pattern, final Function<String, T> parser,
            final Class<T> outputType) {
        this(key, compatibleType, pattern, 0, null, parser, outputType);
    }

    private <T> JsonSchemaType(final String key, final JsonType compatibleType, final String pattern, final Predicate<String> predicate,
            final Function<String, T> parser, final Class<T> outputType) {
        this(key, compatibleType, pattern, 0, predicate, parser, outputType);
    }

    private <T> JsonSchemaType(final String key, final JsonType compatibleType, final String pattern, final int flags,
            final Predicate<String> predicate, final Function<String, T> parser, final Class<T> outputType) {

        this.key = key;
        this.compatibleType = compatibleType;
        this.outputType = outputType;
        this.parser = parser;

        if (pattern != null) {
            final Pattern compiledPattern = Pattern.compile(pattern, flags);
            final Predicate<String> validator = s -> compiledPattern.matcher(s).matches();
            if (predicate != null) {
                this.validator = validator.and(predicate);
            } else {
                this.validator = validator;
            }
        } else if (predicate != null) {
            this.validator = predicate;
        } else {
            this.validator = s -> true;
        }
    }

    private <T> JsonSchemaType(final String key, final JsonType compatibleType, final Predicate<String> predicate, final Function<String, T> parser,
            final Class<T> outputType) {
        this.key = key;
        this.compatibleType = compatibleType;
        this.validator = predicate;
        this.outputType = outputType;
        this.parser = parser;
    }

    public boolean validates(final String value) {
        return this.validator.test(value);
    }

    public JsonType getCompatibleType() {
        return this.compatibleType;
    }

    public <T> Class<T> getOutputType() {
        return CastUtils.cast(this.outputType);
    }

    public <T> T parse(final String value) {
        return CastUtils.cast(this.parser.apply(value));
    }

    public String getKey() {
        return this.key;
    }

    public static Optional<JsonSchemaType> getType(final String type) {
        if (type != null) {
            return Optional.ofNullable(MAP_KEYS.get(type.toLowerCase()));
        } else {
            return Optional.empty();
        }
    }
}
