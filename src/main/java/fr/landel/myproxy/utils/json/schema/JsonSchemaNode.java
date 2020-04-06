package fr.landel.myproxy.utils.json.schema;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class JsonSchemaNode implements Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -397775021351976719L;

    public static final String ROOT = "root";

    private final String id;
    private JsonSchemaSchema<? extends Serializable> schema;
    private final Set<JsonSchemaNode> children = new HashSet<>();

    /**
     * Constructor
     *
     * @category constructor
     */
    public JsonSchemaNode() {
        this(ROOT, null);
    }

    /**
     * Constructor
     *
     * @param id
     *            the node id
     * @param schema
     *            the schema
     * @category constructor
     */
    public <T extends Serializable> JsonSchemaNode(final String id, final JsonSchemaSchema<T> schema) {
        this.id = id;
        this.schema = schema;
    }

    public String getId() {
        return this.id;
    }

    public <T extends Serializable> void setSchema(final JsonSchemaSchema<T> schema) {
        this.schema = schema;
    }

    public JsonSchemaSchema<? extends Serializable> getSchema() {
        return this.schema;
    }

    public Set<JsonSchemaNode> getChildren() {
        return this.children;
    }

    public <T extends Serializable> JsonSchemaNode addChild(final JsonSchemaNode node) {
        this.getChildren().add(node);
        return this;
    }

    @Override
    public String toString() {
        final var builder = new StringBuilder("{");
        builder.append("\"id\": \"").append(this.id);
        builder.append("\", \"schema\": ").append(this.schema);
        if (!this.children.isEmpty()) {
            builder.append(", \"children\": ").append(this.children);
        }
        return builder.append('}').toString();
    }
}
