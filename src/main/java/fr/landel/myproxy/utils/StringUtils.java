package fr.landel.myproxy.utils;

public final class StringUtils {

    public static final String EMPTY = "";
    public static final String NULL = "null";
    public static final String TRUE = "true";
    public static final String FALSE = "false";
    public static final String MARKER = "{}";
    public static final String MARKER_START = "{";
    public static final String MARKER_STOP = "}";

    private StringUtils() {
        throw new UnsupportedOperationException("utility class, not implemented");
    }

    public static String replace(final String text, final Object... args) {
        String content = text;
        if (args != null && args.length > 0) {
            final int markerLength = MARKER.length();
            int index = 0;
            int pos = -1;
            String arg;
            while ((pos = content.indexOf(MARKER)) > -1) {
                if (index < args.length) {
                    arg = String.valueOf(args[index]);
                } else {
                    arg = "";
                }
                ++index;
                if (pos > 0 && pos + markerLength < content.length()) {
                    content = content.substring(0, pos).concat(arg).concat(content.substring(pos + markerLength));
                } else if (pos > 0) {
                    content = content.substring(0, pos).concat(arg);
                } else if (pos + markerLength < content.length()) {
                    content = arg.concat(content.substring(pos + markerLength));
                } else {
                    content = arg;
                }
            }
            for (index = 0; index < args.length && content.indexOf(MARKER_START) > -1; ++index) {
                content = content.replace(MARKER_START.concat(String.valueOf(index + 1)).concat(MARKER_STOP), String.valueOf(args[index]));
            }
        } else {
            content = content.replace(MARKER, EMPTY);
        }
        return content;
    }
}
