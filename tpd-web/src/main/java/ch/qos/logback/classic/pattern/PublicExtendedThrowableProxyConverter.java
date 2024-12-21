package ch.qos.logback.classic.pattern;

import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import ch.qos.logback.core.CoreConstants;

/**
 * This class exists as a hack to allow us to call the package-private method. It has no extra functionality.
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public class PublicExtendedThrowableProxyConverter extends ExtendedThrowableProxyConverter {

    /**
     * @param buf
     * @param tp
     */
    public void subjoinThrowableProxy(final StringBuilder buf, final IThrowableProxy tp) {
        ThrowableProxyUtil.subjoinFirstLine(buf, tp);
        buf.append(CoreConstants.LINE_SEPARATOR);
        final StackTraceElementProxy[] stepArray = tp.getStackTraceElementProxyArray();
        final int commonFrames = tp.getCommonFrames();

        final boolean unrestrictedPrinting = lengthOption > stepArray.length;

        int maxIndex = unrestrictedPrinting ? stepArray.length : lengthOption;
        if (commonFrames > 0 && unrestrictedPrinting) {
            maxIndex -= commonFrames;
        }

        for (int i = 0; i < maxIndex; i++) {
            final String string = stepArray[i].toString();
            buf.append(CoreConstants.TAB);
            buf.append(string);
            extraData(buf, stepArray[i]); // allow other data to be added
            buf.append(CoreConstants.LINE_SEPARATOR);
        }

        if (commonFrames > 0 && unrestrictedPrinting) {
            buf.append("\t... ")
                    .append(tp.getCommonFrames())
                    .append(" common frames omitted")
                    .append(CoreConstants.LINE_SEPARATOR);
        }
    }
}
