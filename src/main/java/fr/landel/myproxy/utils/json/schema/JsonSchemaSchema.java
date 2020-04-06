package fr.landel.myproxy.utils.json.schema;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class JsonSchemaSchema<T extends Serializable> implements Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -229839153681068952L;

    private final JsonSchemaType type;
    private final boolean required;
    private final T def;
    private final Set<T> list = new HashSet<>();

    public JsonSchemaSchema(final JsonSchemaType type, final boolean required, final T def) {
        this.type = type;
        this.required = required;
        this.def = def;
    }

    public JsonSchemaType getType() {
        return this.type;
    }

    public boolean isRequired() {
        return this.required;
    }

    public T getDef() {
        return this.def;
    }

    public Set<T> getList() {
        return this.list;
    }

    @Override
    public String toString() {
        final var builder = new StringBuilder("{");
        builder.append("\"type\": ").append(this.type);
        builder.append(", \"required\": ").append(this.required);
        if (this.def != null) {
            builder.append(", \"default\": \"").append(this.def).append('"');
        }
        if (!this.list.isEmpty()) {
            builder.append(", \"list\": [");
            int c = 0;
            for (T el : this.list) {
                if (c++ > 0) {
                    builder.append("\", \"");
                }
                builder.append(el);
            }
            builder.append("\"]");
        }
        return builder.append('}').toString();
    }
}
