package fr.landel.myproxy.utils.json;

import java.io.Serializable;

public class JsonObject<T extends Serializable> implements Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -6803386158825447957L;

    private final JsonNode parent;

    private final JsonType type;

    // String, Boolean, Integer, Long, Float, Double, Array, Node, ISO8601-Duration, Enumeration, File, URL, Reference
    private final T value;

    /**
     * Constructor
     *
     * @param parent
     *            the parent
     * @param type
     *            the value type
     * @param value
     *            the value
     * @category constructor
     */
    public JsonObject(final JsonNode parent, final JsonType type, final T value) {
        this.parent = parent;
        this.type = type;
        this.value = value;
    }

    public JsonNode getParent() {
        return this.parent;
    }

    public JsonType getType() {
        return this.type;
    }

    public T getValue() {
        return this.value;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        if (JsonType.STRING.equals(this.type)) {
            builder.append('"').append(this.value).append('"');
        } else {
            builder.append(this.value);
        }
        if (!JsonType.ARRAY.equals(this.type) && !JsonType.NODE.equals(this.type)) {
            builder.append(" <").append(this.type.name()).append('>');
        }
        return builder.toString();
    }
}
