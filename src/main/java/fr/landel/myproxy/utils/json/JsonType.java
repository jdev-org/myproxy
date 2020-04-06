package fr.landel.myproxy.utils.json;

import java.util.function.Function;

import fr.landel.myproxy.utils.CastUtils;

public enum JsonType {
    NULL,
    STRING,
    BOOLEAN(s -> {
        return "true".equals(s.toLowerCase());
    }),
    LONG(Long::parseLong),
    DOUBLE(Double::parseDouble),
    ARRAY,
    NODE,
    UNKNOWN;

    private final Function<String, ?> parser;

    private <T> JsonType() {
        this.parser = Function.identity();
    }

    private <T> JsonType(final Function<String, T> parser) {
        this.parser = parser;
    }

    public <T> T parse(final String value) {
        return CastUtils.cast(this.parser.apply(value));
    }
}
