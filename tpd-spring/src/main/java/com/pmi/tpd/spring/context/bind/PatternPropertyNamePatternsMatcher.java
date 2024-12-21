package com.pmi.tpd.spring.context.bind;

import java.util.Collection;

import org.springframework.util.PatternMatchUtils;

/**
 * {@link PropertyNamePatternsMatcher} that delegates to {@link PatternMatchUtils#simpleMatch(String[], String)}.
 *
 * @author Phillip Webb
 * @since 1.2.0
 */
class PatternPropertyNamePatternsMatcher implements PropertyNamePatternsMatcher {

    /** */
    private final String[] patterns;

    PatternPropertyNamePatternsMatcher(final Collection<String> patterns) {
        this.patterns = patterns == null ? new String[] {} : patterns.toArray(new String[patterns.size()]);
    }

    @Override
    public boolean matches(final String propertyName) {
        return PatternMatchUtils.simpleMatch(this.patterns, propertyName);
    }

}
