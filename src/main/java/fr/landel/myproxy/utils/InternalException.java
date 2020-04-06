package fr.landel.myproxy.utils;

public class InternalException extends Exception {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 7735932050919212481L;

    public InternalException() {
        super();
    }

    public InternalException(final String text, final Object... args) {
        super(StringUtils.replace(text, args));
    }

    public InternalException(final Throwable throwable, final String text, final Object... args) {
        super(StringUtils.replace(text, args), throwable);
    }
}
