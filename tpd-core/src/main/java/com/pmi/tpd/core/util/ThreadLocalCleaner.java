package com.pmi.tpd.core.util;

import java.lang.reflect.Method;
import java.util.Set;

/**
 * @author Christophe Friederich
 * @since 1.0
 */
public final class ThreadLocalCleaner {

    private ThreadLocalCleaner() {
    }

    /**
     * @param threadLocal
     * @throws Exception
     */
    public static void clean(final ThreadLocal<?> threadLocal) throws Exception {
        final Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
        final Class<?> localMap = Class.forName("java.lang.ThreadLocal$ThreadLocalMap");
        final Method remove = localMap.getDeclaredMethod("remove", ThreadLocal.class);
        remove.setAccessible(true);
        final Method getMap = ThreadLocal.class.getDeclaredMethod("getMap", Thread.class);
        getMap.setAccessible(true);
        for (final Thread thread : threadSet) {
            final Object obj = getMap.invoke(threadLocal, thread);
            try {
                if (obj != null) {
                    remove.invoke(obj, threadLocal);
                }
            } catch (final Throwable e) {

            }
        }

    }
}
