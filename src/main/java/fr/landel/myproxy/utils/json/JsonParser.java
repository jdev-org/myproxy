package fr.landel.myproxy.utils.json;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Pattern;

import fr.landel.myproxy.utils.InternalException;
import fr.landel.myproxy.utils.Logger;
import fr.landel.myproxy.utils.StringUtils;

public class JsonParser {

    private static final Logger LOG = new Logger(JsonParser.class);

    private static final char[] NULL_CHARACTERS = "nulNUL".toCharArray();
    private static final char[] BOOLEAN_CHARACTERS = "truefalsTRUEFALS".toCharArray();
    private static final char[] DECIMAL_CHARACTERS = "-+0123456789.eE".toCharArray();
    static {
        Arrays.sort(NULL_CHARACTERS);
        Arrays.sort(BOOLEAN_CHARACTERS);
        Arrays.sort(DECIMAL_CHARACTERS);
    }
    private static final Pattern PATTERN_DECIMAL = Pattern.compile("^-?(0|[1-9][0-9]*|[0-9]+\\.[0-9]+)(e[+-]?[0-9]+)?$", Pattern.CASE_INSENSITIVE);

    private static boolean isBlank(final byte[] content) {
        final int len = content.length;
        int st = -1;
        while (++st < len && Character.isWhitespace(content[st])) {
        }
        return st == len;
    }

    public static Optional<JsonNode> load(final byte[] content) {
        if (content == null || isBlank(content)) {
            LOG.error("empty json");
            return Optional.empty();
        }

        return parse(content);
    }

    private static void error(final byte[] content, final int position, final String message, final Object... args) {
        final int padding = 10;
        final int len = content.length;

        final int start = position - padding > 0 ? position - padding : 0;
        final int stop = len > position + padding ? position + padding : len;

        final Object[] arguments;
        final int argsLength;
        if (args != null && (argsLength = args.length) > 0) {
            arguments = new Object[argsLength + 1];
            System.arraycopy(args, 0, arguments, 1, argsLength);
        } else {
            arguments = new Object[1];
        }

        final StringBuilder builder = new StringBuilder();
        if (start < position - 1) {
            builder.append(extractString(content, start, position));
        }
        builder.append('<').append((char) content[position]).append('>');
        if (stop > position + 1) {
            builder.append(extractString(content, position + 1, stop));
        }
        arguments[0] = builder.toString();

        LOG.error(message, arguments);
    }

    private static String extractString(final byte[] content, final int start, final int end) {
        if (start < end) {
            return new String(Arrays.copyOfRange(content, start, end), StandardCharsets.UTF_8);
        } else {
            return StringUtils.EMPTY;
        }
    }

    private static <T extends Serializable> void addChild(final JsonNode parent, final JsonType type, final T value) throws InternalException {
        if (JsonType.ARRAY.equals(parent.getLastChildType())) {
            parent.addArrayChild(parent.getLastKey(), type, value);
        } else {
            parent.setChild(parent.getLastKey(), type, value);
        }
    }

    private static Optional<JsonNode> parse(final byte[] content) {
        int len = content.length;
        int st = -1;
        while (++st < len && Character.isWhitespace(content[st])) {
        }
        while (st < len && Character.isWhitespace(content[len - 1])) {
            --len;
        }

        if (content[st] != '{' || content[len - 1] != '}') {
            LOG.error("invalid json, doesn't start with '{' or doesn't end with '}'");
            return Optional.empty();
        }

        --len;

        try {

            final JsonNode root = new JsonNode(null);
            JsonNode parent = root;

            boolean inError = false;

            boolean expectKey = true;
            boolean expectColon = false;
            boolean expectNull = false;
            boolean expectBoolean = false;
            boolean expectDecimal = false;
            boolean expectString = false;
            boolean expectComment = false;

            boolean inString = false;
            int inStringStart = 0;
            String string;

            int inNullStart = 0;
            int inBooleanStart = 0;
            int inDecimalStart = 0;

            boolean inComment = false;
            int inCommentStart = 0;

            char lastOperator = 0;

            int nodes = 0;
            byte b;
            char c;
            while (++st < len) {
                b = content[st];
                if (!Character.isWhitespace(b) && !inComment) {
                    c = (char) b;

                    if (parent == null) {
                        error(content, st, "invalid json, end already reached, unexpected char '{2}' found at {3}, extract:\n{1}", c, st);
                        inError = true;
                        break;
                    }

                    if (expectKey && !inString) {
                        if (c != '\"') {
                            if (c == '}' && lastOperator == '{') {
                                --nodes;
                                parent = parent.getParent();
                                expectKey = false;
                            } else {
                                error(content, st, "invalid json, expects key but char '{2}' was found at {3}, extract:\n{1}", c, st);
                                inError = true;
                                break;
                            }
                        } else {
                            inString = true;
                            inStringStart = st + 1;
                        }
                    } else if (inString && c == '\"') {
                        if (content[st - 1] != '\\') {
                            inString = false;
                            string = extractString(content, inStringStart, st);
                            if (expectKey) {
                                parent.addChild(string);
                                expectKey = false;
                                expectColon = true;
                            } else if (expectString) {
                                addChild(parent, JsonType.STRING, string);
                            } else {
                                error(content, st, "invalid json, unexpected char '{2}' found at {3}, extract:\n{1}", c, st);
                                inError = true;
                                break;
                            }
                        }
                    } else if (!inString) {
                        if (c == '/') {
                            if (!expectComment) {
                                expectComment = true;
                                inCommentStart = st;
                            } else if (expectComment && inCommentStart != st - 1) {
                                error(content, st, "invalid json, expects a comment but char '{2}' was found at {3}, extract:\n{1}", c, st);
                                inError = true;
                                break;
                            } else {
                                inComment = true;
                                inCommentStart = 0;
                                expectComment = false;
                            }
                        } else if (expectComment) {
                            error(content, st, "invalid json, expects a comment but char '{2}' was found at {3}, extract:\n{1}", c, st);
                            inError = true;
                            break;
                        } else if (c == ':') {
                            if (!expectColon) {
                                error(content, st, "invalid json, expects a colon but char '{2}' was found at {3}, extract:\n{1}", c, st);
                                inError = true;
                                break;
                            }
                            expectColon = false;
                        } else if (c == ',' || c == ']' || c == '}') {
                            if (expectDecimal || expectBoolean || expectNull) {
                                if (expectDecimal) {
                                    string = extractString(content, inDecimalStart, st).trim().toLowerCase();
                                    if (!PATTERN_DECIMAL.matcher(string).matches()) {
                                        error(content, st, "invalid json, expects a decimal number at '{2}' but was: {3}, extract:\n{1}",
                                                inDecimalStart, string);
                                        inError = true;
                                        break;
                                    } else if (string.indexOf('.') > -1 || string.indexOf('e') > -1) {
                                        addChild(parent, JsonType.DOUBLE, JsonType.DOUBLE.parse(string));
                                    } else {
                                        addChild(parent, JsonType.LONG, JsonType.LONG.parse(string));
                                    }
                                    expectDecimal = false;
                                } else if (expectBoolean) {
                                    string = extractString(content, inBooleanStart, st).trim().toLowerCase();
                                    if (StringUtils.TRUE.equals(string)) {
                                        addChild(parent, JsonType.BOOLEAN, true);
                                    } else if (StringUtils.FALSE.equals(string)) {
                                        addChild(parent, JsonType.BOOLEAN, false);
                                    } else {
                                        error(content, st, "invalid json, expects a boolean at '{2}' but was: {3}, extract:\n{1}", inBooleanStart,
                                                string);
                                        inError = true;
                                        break;
                                    }
                                    expectBoolean = false;
                                } else if (expectNull) {
                                    string = extractString(content, inNullStart, st).trim().toLowerCase();
                                    if (StringUtils.NULL.equals(string)) {
                                        addChild(parent, JsonType.NULL, true);
                                    } else {
                                        error(content, st, "invalid json, expects a null at '{2}' but was: {3}, extract:\n{1}", inNullStart, string);
                                        inError = true;
                                        break;
                                    }
                                    expectNull = false;
                                }
                            }

                            if (c == ']') {
                                if (parent.isExpectArray()) {
                                    parent.closeArray();
                                } else {
                                    error(content, st, "invalid json, unexpected array end, char '{2}' found at {3}, extract:\n{1}", c, st);
                                    inError = true;
                                    break;
                                }
                            } else if (c == '}') {
                                if (lastOperator != ',' && !expectKey && !parent.isExpectArray()) {
                                    --nodes;
                                    parent = parent.getParent();
                                } else {
                                    error(content, st, "invalid json, unexpected node end, char '{2}' found at {3}, extract:\n{1}", c, st);
                                    inError = true;
                                    break;
                                }
                            } else if (c == ',') {
                                if (!parent.isExpectArray()) {
                                    expectKey = true;
                                }
                            } else {
                                error(content, st, "invalid json, unexpected char '{2}' found at {3}, extract:\n{1}", c, st);
                                inError = true;
                                break;
                            }
                        } else {
                            if (c == '{') {
                                ++nodes;
                                expectKey = true;
                                addChild(parent, JsonType.NODE, parent = new JsonNode(parent));
                            } else if (c == '[') {
                                parent.openArray();
                            } else if (lastOperator == ':' || lastOperator == '[' || lastOperator == ',') {
                                if (c == '\"') {
                                    expectString = true;
                                    inString = true;
                                    inStringStart = st + 1;
                                } else if (!expectBoolean && Arrays.binarySearch(DECIMAL_CHARACTERS, c) > -1) { // character E in both
                                    if (!expectDecimal) {
                                        expectDecimal = true;
                                        inDecimalStart = st;
                                    }
                                } else if (!expectNull && Arrays.binarySearch(BOOLEAN_CHARACTERS, c) > -1) { // character L in both
                                    if (!expectBoolean) {
                                        expectBoolean = true;
                                        inBooleanStart = st;
                                    }
                                } else if (Arrays.binarySearch(NULL_CHARACTERS, c) > -1) {
                                    if (!expectNull) {
                                        expectNull = true;
                                        inNullStart = st;
                                    }
                                } else {
                                    error(content, st, "invalid json, unexpected char '{2}' found at {3}, extract:\n{1}", c, st);
                                    inError = true;
                                    break;
                                }
                            } else {
                                error(content, st, "invalid json, unexpected char '{2}' found at {3}, extract:\n{1}", c, st);
                                inError = true;
                                break;
                            }
                        }
                    }

                    if (!inString && (c == ',' || c == '{' || c == '}' || c == '[' || c == ']' || c == ':')) {
                        lastOperator = c;
                    }
                } else if (inComment && (b == '\n' || b == '\r')) {
                    inComment = false;
                }
            }

            if (nodes != 0) {
                LOG.error("invalid json, unexpected final node end");
                inError = true;
            }

            if (!inError) {
                return Optional.of(root);
            } else {
                return Optional.empty();
            }

        } catch (InternalException e) {
            error(content, st, "invalid json, unexpected error, last character analyzed at {3}, extract:\n{1}\nError:\n{2}", e.getMessage(), st);
            return Optional.empty();
        }
    }
}
