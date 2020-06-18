package fr.landel.myproxy.utils;

import java.util.Map;

public final class StringUtils {

    public static final String EMPTY = "";

    public static final String NULL = "null";

    public static final String TRUE = "true";
    public static final String FALSE = "false";

    public static final String MARKER = "{}";
    public static final char MARKER_START_CHAR = '{';
    public static final char MARKER_STOP_CHAR = '}';

    private StringUtils() {
        throw new UnsupportedOperationException("utility class, not implemented");
    }

    public static String inject(final String text, final Object... args) {
        return inject(text, null, args);
    }

    public static <T> String inject(final String text, final Map<String, T> map, final Object... args) {
        if (text == null || text.indexOf(MARKER_START_CHAR) < 0) {
            return text;
        }

        final int argsLen = args == null ? 0 : args.length;
        final boolean mapEmpty = map == null || map.isEmpty();

        if (argsLen > 0 || !mapEmpty) {
            final int len = text.length();
            int index = 0;
            int previousClose = -1;
            int posOpen = 0;
            int posClose = -1;
            int argIndex;
            String key;
            Object value = null;
            final StringBuilder output = new StringBuilder();
            while ((posOpen = text.indexOf(MARKER_START_CHAR, posOpen)) > -1 && (posClose = text.indexOf(MARKER_STOP_CHAR, posOpen + 1)) > -1) {

                // manages \{
                if (posOpen > 0 && text.charAt(posOpen - 1) == '\\') {
                    if (previousClose < posOpen - 2) {
                        output.append(text.substring(previousClose + 1, posOpen - 1));
                    }
                    output.append(MARKER_START_CHAR);
                    previousClose = posOpen++;

                    // manages {}
                } else if (posOpen + 1 == posClose) {
                    if ((previousClose == -1 && posOpen > 0) || (previousClose > 0 && previousClose < posOpen)) {
                        output.append(text.substring(previousClose + 1, posOpen));
                    }
                    if (index < argsLen) {
                        output.append(String.valueOf(args[index++]));
                    }
                    previousClose = posClose;
                    posOpen = previousClose + 1;

                    // manages {0} and {key}
                } else {
                    key = text.substring(posOpen + 1, posClose);

                    // manages {0}
                    if (mapEmpty || (value = map.get(key)) == null) {
                        argIndex = parsePositiveInt(key);
                        if (argIndex > -1) {
                            if ((previousClose == -1 && posOpen > 0) || (previousClose > 0 && previousClose < posOpen)) {
                                output.append(text.substring(previousClose + 1, posOpen));
                            }
                            if (argIndex < argsLen) {
                                output.append(String.valueOf(args[argIndex]));
                            }
                            previousClose = posClose;
                            posOpen = previousClose + 1;
                        } else {
                            ++posOpen;
                        }

                        // manages {key}
                    } else {
                        if ((previousClose == -1 && posOpen > 0) || (previousClose > 0 && previousClose < posOpen)) {
                            output.append(text.substring(previousClose + 1, posOpen));
                        }
                        output.append(String.valueOf(value));
                        previousClose = posClose;
                        posOpen = previousClose + 1;
                    }
                }
            }
            if (previousClose < len - 1) {
                output.append(text.substring(previousClose + 1));
            }
            return output.toString();
        } else {
            return text.replace(MARKER, EMPTY);
        }
    }

    public static int parsePositiveInt(final String input) {
        final char[] array = input.toCharArray();
        final int len = array.length;
        if (len > 10) {
            return -1;
        }
        long value = 0;
        char c;
        int middle = len / 2;
        boolean odd = len % 2 != 0;
        for (int i = 0; (odd && i <= middle) || i < middle; ++i) {
            c = array[i];
            if (c > 48 && c < 58) {
                value += (c - 48) * Math.pow(10, len - i - 1);
            } else if (c != 48) {
                return -1;
            }
            if (middle > i) {
                c = array[len - i - 1];
                if (c > 48 && c < 58) {
                    value += (c - 48) * Math.pow(10, i);
                } else if (c != 48) {
                    return -1;
                }
            }
        }
        if (value <= (long) Integer.MAX_VALUE) {
            return (int) value;
        } else {
            return -1;
        }
    }
}
