package fr.landel.myproxy.utils.json.schema;

import java.io.Serializable;
import java.util.Set;

import fr.landel.myproxy.utils.json.JsonNode;
import fr.landel.myproxy.utils.json.JsonObject;
import fr.landel.myproxy.utils.json.JsonType;

public class JsonSchemaValidator {
    // private static final Logger LOGGER = LoggerFactory.getLogger(JsonSchemaValidator.class);

    // TODO check and append data (default) to input json object

    public static boolean validates(final JsonSchemaNode schema, final JsonNode json) {
        final var schemaSchema = schema.getSchema();
        if (schemaSchema.isRequired()) {
            schemaSchema.getType();
        }

        for (var child : schema.getChildren()) {
            String key = child.getId();
            boolean required = child.getSchema().isRequired();
        }

        return false;
    }

    private static boolean checksNode(final JsonSchemaNode schema, final JsonNode json) {
        final var schemaSchema = schema.getSchema();

        final String id = schema.getId();
        final boolean required = schemaSchema.isRequired();
        final JsonSchemaType type = schemaSchema.getType();
        final JsonType jsonType = type.getCompatibleType();
        final Object def = schemaSchema.getDef();
        final Set<? extends Serializable> list = schemaSchema.getList();

        JsonObject<? extends Serializable> object = json.getChildren().get(id);
        if (object == null && required && def == null) {
            return false;
        } else if (object != null) {
            if (!jsonType.equals(object.getType())) {
                return false;
            }
        }

        return false;
    }
}
