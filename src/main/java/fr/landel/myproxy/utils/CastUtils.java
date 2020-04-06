package fr.landel.myproxy.utils;

public final class CastUtils {

    private CastUtils() {
        throw new UnsupportedOperationException("utility class, not implemented");
    }

    /**
     * Auto cast an object.
     * 
     * @param object
     *            The object (required)
     * @param <T>
     *            The object type
     * @return The casted object
     * @throws ClassCastException
     *             on cast failure
     */
    @SuppressWarnings("unchecked")
    public static <T> T cast(final Object object) {
        return (T) object;
    }
}
