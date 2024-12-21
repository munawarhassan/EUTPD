package com.pmi.tpd.util;

class ThreadLocalRandom {

    /**
     * The actual ThreadLocal
     */
    private static final ThreadLocal<ThreadLocalRandom> localRandom = new ThreadLocal<ThreadLocalRandom>() {

        @Override
        protected ThreadLocalRandom initialValue() {
            return new ThreadLocalRandom();
        }
    };

    /**
     * Constructor called only by localRandom.initialValue.
     */
    ThreadLocalRandom() {
        super();
    }

    /**
     * Returns the current thread's {@code ThreadLocalRandom}.
     *
     * @return the current thread's {@code ThreadLocalRandom}
     */
    public static ThreadLocalRandom current() {
        return localRandom.get();
    }

}
