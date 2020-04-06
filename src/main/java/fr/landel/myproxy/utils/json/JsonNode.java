package fr.landel.myproxy.utils.json;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import fr.landel.myproxy.utils.CastUtils;
import fr.landel.myproxy.utils.InternalException;

public class JsonNode implements Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -3313446202833491202L;

    private final JsonNode parent;

    private final Map<String, JsonObject<? extends Serializable>> children = new HashMap<>();

    private String lastKey;

    private boolean expectArray;

    /**
     * Constructor
     *
     * @param parent
     *            the parent node
     * @category constructor
     */
    public JsonNode(final JsonNode parent) {
        this.parent = parent;
    }

    public void setExpectArray(boolean expectArray) {
        this.expectArray = expectArray;
    }

    public boolean isExpectArray() {
        return this.expectArray;
    }

    /**
     * Constructor
     *
     * @category constructor
     */
    public JsonNode() {
        this(null);
    }

    public boolean hasParent() {
        return this.parent != null;
    }

    public JsonNode getParent() {
        return this.parent;
    }

    public String getLastKey() {
        return this.lastKey;
    }

    public JsonType getLastChildType() {
        return this.getChildType(this.lastKey);
    }

    public Map<String, JsonObject<? extends Serializable>> getChildren() {
        return this.children;
    }

    public <T extends Serializable> JsonNode addChild(final String key) throws InternalException {
        if (!this.children.containsKey(key)) {
            this.children.put(this.lastKey = key, new JsonObject<>(this, JsonType.UNKNOWN, null));
        } else {
            throw new InternalException("Key already exists");
        }
        return this;
    }

    public <T extends Serializable> JsonNode setChild(final String key, final JsonType type, final T value) {
        this.children.put(key, new JsonObject<>(this, type, value));
        return this;
    }

    private JsonObject<? extends Serializable> addArray(final String key) throws InternalException {
        JsonObject<? extends Serializable> array = this.children.get(key);
        if (array == null || JsonType.UNKNOWN.equals(array.getType())) {
            this.children.put(key, array = new JsonObject<>(this, JsonType.ARRAY, new JsonArray()));
        } else {
            throw new InternalException("Key already exists");
        }
        return array;
    }

    private JsonObject<? extends Serializable> getArray(final String key) {
        JsonObject<? extends Serializable> array = this.children.get(key);
        if (array == null) {
            this.children.put(key, array = new JsonObject<>(this, JsonType.ARRAY, new JsonArray()));
        }
        return array;
    }

    public <T extends Serializable> JsonNode openArray() throws InternalException {
        addArray(this.lastKey);
        this.expectArray = true;
        return this;
    }

    public <T extends Serializable> JsonNode closeArray() throws InternalException {
        if (this.expectArray) {
            final JsonArray set = CastUtils.cast(getArray(this.lastKey).getValue());
            set.close();
            this.expectArray = false;
        } else {
            throw new InternalException("Cannot close the array");
        }
        return this;
    }

    public <T extends Serializable> JsonNode addArrayChild(final String key, final JsonType type, final T value) throws InternalException {
        if (this.expectArray) {
            final JsonArray set = CastUtils.cast(getArray(key).getValue());
            if (set.isOpened()) {
                set.add(new JsonObject<T>(this, type, value));
            } else {
                throw new InternalException("Cannot add an element in closed array");
            }
        } else {
            throw new InternalException("Not in array");
        }
        return this;
    }

    public JsonType getChildType(final String key) {
        final JsonObject<? extends Serializable> child = this.children.get(key);
        return child != null ? child.getType() : JsonType.UNKNOWN;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder().append('{');
        int c = 0;
        for (Entry<String, JsonObject<? extends Serializable>> child : children.entrySet()) {
            if (c++ > 0) {
                builder.append(", ");
            }
            builder.append('"').append(child.getKey()).append("\": ").append(child.getValue());
        }
        return builder.append('}').toString();
    }
}
