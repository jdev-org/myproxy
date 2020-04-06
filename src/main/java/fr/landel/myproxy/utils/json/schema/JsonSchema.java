package fr.landel.myproxy.utils.json.schema;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import fr.landel.myproxy.utils.CastUtils;
import fr.landel.myproxy.utils.InternalException;
import fr.landel.myproxy.utils.json.JsonArray;
import fr.landel.myproxy.utils.json.JsonNode;
import fr.landel.myproxy.utils.json.JsonParser;
import fr.landel.myproxy.utils.json.JsonType;

public class JsonSchema {

    private static final List<JsonType> VALUE_TYPES = Arrays.asList(JsonType.STRING, JsonType.BOOLEAN, JsonType.DOUBLE, JsonType.LONG);

    private static final String ID = "id";
    private static final String SCHEMA = "schema";
    private static final String CHILDREN = "children";
    private static final String TYPE = "type";
    private static final String REQUIRED = "required";
    private static final String DEFAULT = "default";
    private static final String LIST = "list";

    public static Optional<JsonSchemaNode> load(final byte[] content) throws InternalException {
        final var json = JsonParser.load(content);
        if (json.isPresent()) {
            return Optional.of(map(json.get()));
        } else {
            return Optional.empty();
        }
    }

    private static JsonSchemaNode map(final JsonNode jsonNode) throws InternalException {
        final var map = jsonNode.getChildren();
        final var id = map.get(ID);
        final var schema = map.get(SCHEMA);
        final var children = map.get(CHILDREN);

        if (id == null) {
            throw new InternalException("invalid schema, 'id' is not defined");
        } else if (!JsonType.STRING.equals(id.getType()) || !JsonSchemaType.REFERENCE.validates((String) id.getValue())) {
            throw new InternalException("invalid schema, incorrect 'id' expect a String with pattern '[a-zA-Z0-9_-]+'; id: {}, type: {}",
                    id.getValue(), id.getType());
        } else if (schema == null || !JsonType.NODE.equals(schema.getType())) {
            throw new InternalException("invalid schema, schema not defined or incorrect");
        } else if (children != null && !JsonType.ARRAY.equals(children.getType())) {
            throw new InternalException("invalid schema, children not defined or incorrect");
        }

        final String schemaId = (String) id.getValue();
        final JsonSchemaSchema<? extends Serializable> schemaSchema = map(schemaId, (JsonNode) schema.getValue());
        final JsonSchemaNode schemaNode = new JsonSchemaNode(schemaId, schemaSchema);

        if (children != null) {
            for (var child : (JsonArray) children.getValue()) {
                if (JsonType.NODE.equals(child.getType())) {
                    schemaNode.addChild(map((JsonNode) child.getValue()));
                }
            }
        }

        return schemaNode;
    }

    private static <T extends Serializable> JsonSchemaSchema<T> map(final String id, final JsonNode jsonNode) throws InternalException {
        final var map = jsonNode.getChildren();
        final var type = map.get(TYPE);
        final var required = map.get(REQUIRED);
        final var def = map.get(DEFAULT);
        final var list = map.get(LIST);

        final JsonSchemaType schemaType;
        if (type == null || !JsonType.STRING.equals(type.getType())) {
            throw new InternalException("invalid schema, schema type not defined or incorrect (id: {})", id);
        } else {
            var optionalSchemaType = JsonSchemaType.getType((String) type.getValue());
            if (optionalSchemaType.isEmpty()) {
                throw new InternalException("invalid schema, schema type not found: {} (id: {})", type.getValue(), id);
            } else {
                schemaType = optionalSchemaType.get();
            }
        }

        final boolean schemaRequired;
        if (required != null) {
            if (!JsonType.BOOLEAN.equals(required.getType())) {
                throw new InternalException("invalid schema, expects a boolean for 'required', but found '{}', type: '{}' (id: {})",
                        required.getValue(), required.getType(), id);
            } else {
                schemaRequired = (Boolean) required.getValue();
            }
        } else {
            schemaRequired = false;
        }

        final T schemaDefault;
        JsonType jsonType;
        if (def != null) {
            if (!VALUE_TYPES.contains(def.getType())) {
                throw new InternalException("invalid schema, default value incorrect type (id: {})", id);
            } else {
                schemaDefault = CastUtils.cast(def.getValue());
                jsonType = def.getType();
            }
        } else {
            schemaDefault = null;
            jsonType = JsonType.UNKNOWN;
        }

        if (list == null && JsonSchemaType.ENUM.equals(schemaType)) {
            throw new InternalException("invalid schema, enumeration empty (id: {})", id);
        } else if (list != null && !JsonSchemaType.ENUM.equals(schemaType)) {
            throw new InternalException("invalid schema, enumeration defined, but a bad type is defined: {} (id: {})", schemaType, id);
        }

        final JsonSchemaSchema<T> schema = new JsonSchemaSchema<>(schemaType, schemaRequired, schemaDefault);

        if (list != null) {
            for (var el : (JsonArray) list.getValue()) {
                if (JsonType.UNKNOWN.equals(jsonType)) {
                    jsonType = el.getType();
                }

                if (!jsonType.equals(el.getType())) {
                    throw new InternalException(
                            "invalid schema, all enumeration elements are not in the same type; expected: {}, actual: {} (id: {})", jsonType,
                            el.getType(), id);
                }

                schema.getList().add(CastUtils.cast(el.getValue()));
            }
        }

        return schema;
    }
}
