package com.pmi.tpd.spring.env.yaml;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Properties;

import com.google.common.collect.Iterables;
import com.pmi.tpd.spring.env.YamlProcessor.DocumentMatcher;
import com.pmi.tpd.spring.env.YamlProcessor.MatchStatus;

/**
 * {@link DocumentMatcher} backed by {@link org.springframework.core.env.Environment#getActiveProfiles()}. A YAML
 * document matches if it contains an element "spring.profiles" (a comma-separated list) and one of the profiles is in
 * the active list.
 *
 * @author Dave Syer
 */
public class SpringProfileDocumentMatcher implements DocumentMatcher {

    private static final String[] DEFAULT_PROFILES = new String[] { "default" };

    private String[] activeProfiles = new String[0];

    public SpringProfileDocumentMatcher() {
    }

    public SpringProfileDocumentMatcher(final String... profiles) {
        addActiveProfiles(profiles);
    }

    public void addActiveProfiles(final String... profiles) {
        final LinkedHashSet<String> set = new LinkedHashSet<String>(Arrays.asList(this.activeProfiles));
        Collections.addAll(set, profiles);
        this.activeProfiles = Iterables.toArray(set, String.class);
    }

    @Override
    public MatchStatus matches(final Properties properties) {
        String[] profiles = this.activeProfiles;
        if (profiles.length == 0) {
            profiles = DEFAULT_PROFILES;
        }
        return new ArrayDocumentMatcher("spring.profiles", profiles).matches(properties);
    }

}
