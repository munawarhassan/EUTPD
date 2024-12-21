package ch.qos.logback.classic.spi;

/**
 * This class exists to allow us to call the package-private method.
 *
 * @link ch.qos.logback.classic.pattern.PublicExtendedThrowableProxyConverter
 * @link ch.qos.logback.classic.spi.ThrowableProxyUtil
 */
public class PublicThrowableProxyUtil extends ThrowableProxyUtil {

    public static int findNumberOfCommonFrames(final StackTraceElement[] steArray,
        final StackTraceElementProxy[] parentSTEPArray) {
        return ThrowableProxyUtil.findNumberOfCommonFrames(steArray, parentSTEPArray);
    }

}
