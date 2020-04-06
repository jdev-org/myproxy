package fr.landel.myproxy.utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

public final class IOUtils {

    private static final Logger LOG = new Logger(IOUtils.class);

    private IOUtils() {
        throw new UnsupportedOperationException("utility class, not implemented");
    }

    public static String load(final Path path) throws InternalException {
        try {
            return Files.lines(path, StandardCharsets.UTF_8).collect(Collectors.joining(System.lineSeparator()));
        } catch (IOException e) {
            LOG.error(e, "Cannot load path: {}", path);
            throw new InternalException("Cannot load path");
        }
    }
}
