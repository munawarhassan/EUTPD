package com.pmi.tpd.spring.env.yaml;

import java.util.Properties;

import org.springframework.beans.factory.config.YamlProcessor.DocumentMatcher;
import org.springframework.beans.factory.config.YamlProcessor.MatchStatus;

/**
 * A {@link DocumentMatcher} that matches the default profile implicitly but not explicitly (i.e. matches if
 * "spring.profiles" is not found and not otherwise).
 *
 * @author Dave Syer
 */
public class DefaultProfileDocumentMatcher implements DocumentMatcher {

    @Override
    public MatchStatus matches(final Properties properties) {
        if (!properties.containsKey("spring.profiles")) {
            return MatchStatus.FOUND;
        }
        return MatchStatus.NOT_FOUND;
    }

}
