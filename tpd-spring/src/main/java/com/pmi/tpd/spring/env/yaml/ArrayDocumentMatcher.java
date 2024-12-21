package com.pmi.tpd.spring.env.yaml;

import java.util.Properties;
import java.util.Set;

import org.springframework.util.StringUtils;

import com.pmi.tpd.spring.env.YamlProcessor.DocumentMatcher;
import com.pmi.tpd.spring.env.YamlProcessor.MatchStatus;

/**
 * Matches a document containing a given key and where the value of that key is an array containing one of the given
 * values, or where one of the values matches one of the given values (interpreted as regexes).
 *
 * @author Dave Syer
 */
public class ArrayDocumentMatcher implements DocumentMatcher {

    private final String key;

    private final String[] patterns;

    public ArrayDocumentMatcher(final String key, final String... patterns) {
        this.key = key;
        this.patterns = patterns;

    }

    @Override
    public MatchStatus matches(final Properties properties) {
        if (!properties.containsKey(this.key)) {
            return MatchStatus.ABSTAIN;
        }
        final Set<String> values = StringUtils.commaDelimitedListToSet(properties.getProperty(this.key));
        for (final String pattern : this.patterns) {
            for (final String value : values) {
                if (value.matches(pattern)) {
                    return MatchStatus.FOUND;
                }
            }
        }
        return MatchStatus.NOT_FOUND;
    }

}
