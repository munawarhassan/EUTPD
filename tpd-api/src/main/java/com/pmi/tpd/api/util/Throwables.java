package com.pmi.tpd.api.util;

/**
 * @author Christophe Friederich
 * @since 1.0
 */
public final class Throwables {

    private Throwables() {
        throw new UnsupportedOperationException(
                getClass().getName() + " is a utility class and should not be instantiated");
    }

    /**
     * Part A of a little compiler trick to throw checked exceptions without declaring them.
     * 
     * @param e
     */
    public static void throwUnchecked(final Throwable e) {
        Throwables.<RuntimeException> throwAny(e);
    }

    /**
     * Part B of a little compiler trick to throw checked exceptions without declaring them.
     * 
     * @param e
     */
    @SuppressWarnings("unchecked")
    private static <E extends Throwable> void throwAny(final Throwable e) throws E {
        throw (E) e;
    }

}
