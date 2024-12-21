package com.pmi.tpd.spring.i18n;

import static com.google.common.base.Predicates.notNull;
import static com.google.common.collect.FluentIterable.from;

import java.io.IOException;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import com.google.common.annotations.VisibleForTesting;

/**
 * A {@link org.springframework.context.MessageSource message source} that looks for application message bundles on the
 * classpath to calculate the {@link #setBasenames(String...) basenames} used for message lookups.
 */
public class ClasspathI18nMessageSource extends ResourceBundleMessageSource {

    /** */
    private static Logger LOGGER = LoggerFactory.getLogger(ClasspathI18nMessageSource.class);

    public ClasspathI18nMessageSource() throws IOException {
        this(true);
    }

    /**
     * @throws IOException
     */
    public ClasspathI18nMessageSource(final boolean allClasspath) throws IOException {
        final String[] baseNames = getResolveResource(allClasspath);

        if (baseNames.length > 0) {
            setBasenames(baseNames);
        }
    }

    @VisibleForTesting
    static String[] getResolveResource(final boolean allClasspath) throws IOException {
        final Set<String> baseNames = from(new PathMatchingResourcePatternResolver()
                .getResources((allClasspath ? "classpath*" : "classpath") + ":/i18n/*/app-*.properties"))
                        .transform(resource -> getI18nPath(resource) + "/" + getShortName(resource.getFilename()))
                        .filter(notNull())
                        .toSet();
        baseNames.forEach(ClasspathI18nMessageSource::logBaseName);
        return from(baseNames).toArray(String.class);
    }

    private static String getI18nPath(final Resource resource) {
        String path = null;
        try {
            path = resource.getURI().toString();
            final int indexStart = path.indexOf("i18n/");
            final int indexEnd = path.lastIndexOf('/');
            path = path.substring(indexStart, indexEnd);
        } catch (final IOException e) {
        }
        return path;
    }

    private static String getShortName(final String filename) {
        String shortName = filename;
        final int underscoreIndex = filename.indexOf('_');
        if (underscoreIndex > 0) {
            shortName = filename.substring(0, underscoreIndex);
        } else {
            shortName = filename.substring(0, filename.length() - ".properties".length());
        }
        return shortName;
    }

    private static void logBaseName(final String path) {
        if (path.contains(".")) {
            LOGGER.warn(
                "The I18n basename {} can not use dot character. See javadoc of "
                        + "org.springframework.context.support.ResourceBundleMessageSource class",
                path);
        }

    }

}
