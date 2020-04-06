package fr.landel.myproxy.conf;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

import fr.landel.myproxy.utils.IOUtils;
import fr.landel.myproxy.utils.InternalException;
import fr.landel.myproxy.utils.Logger;
import fr.landel.myproxy.utils.json.JsonNode;
import fr.landel.myproxy.utils.json.JsonParser;
import fr.landel.myproxy.utils.json.schema.JsonSchema;
import fr.landel.myproxy.utils.json.schema.JsonSchemaNode;
import fr.landel.myproxy.utils.json.schema.JsonSchemaValidator;

public class Loader {

    // TODO
    // analysis proxy.pac
    // XML, parsing + schema + validation + XPath
    // JSON, parsing + schema + validation + XPath
    // YAML ???
    // proxy, manages connection, 302, retry...
    // engine, rules, references
    // perf, UT, DOC...
    // https://github.com/hkoosha/pac-vole

    private static final Logger LOG = new Logger(Loader.class);

    public static final void main(String[] args) throws InternalException, FileNotFoundException, IOException {
        long start1 = System.currentTimeMillis();

        final Path pathSchema = Paths.get("src/main/resources/configuration-schema.json");
        final String jsonSchema = IOUtils.load(pathSchema);

        final Path path = Paths.get("src/main/resources/configuration.json");
        final String jsonData = IOUtils.load(path);

        long start2 = System.currentTimeMillis();

        final JsonSchemaNode schema = JsonSchema.load(jsonSchema.getBytes(StandardCharsets.UTF_8)).orElse(null);

        final JsonNode json = JsonParser.load(jsonData.getBytes(StandardCharsets.UTF_8)).orElse(null);

        long start3 = System.currentTimeMillis();

        final boolean valid = JsonSchemaValidator.validates(schema, json);

        LOG.info("Loaded in: {} ms, run in: {} ms, validated in: {} ms", start2 - start1, start3 - start2, System.currentTimeMillis() - start3);

        LOG.info(schema);
        LOG.info(json);
        LOG.info(valid);
    }
}
