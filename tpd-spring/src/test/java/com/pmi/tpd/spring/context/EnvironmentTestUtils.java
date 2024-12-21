package com.pmi.tpd.spring.context;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;

/**
 * Test utilities for setting environment values.
 *
 * @author Christophe Friederich
 */
public class EnvironmentTestUtils {

    /**
     * Add additional (high priority) values to an {@link org.springframework.core.env.Environment} owned by an
     * {@link org.springframework.context.ApplicationContext}. Name-value pairs can be specified with colon (":") or
     * equals ("=") separators.
     *
     * @param context
     *            the context with an environment to modify
     * @param pairs
     *            the name:value pairs
     */
    public static void addEnvironment(final ConfigurableApplicationContext context, final String... pairs) {
        addEnvironment(context.getEnvironment(), pairs);
    }

    /**
     * Add additional (high priority) values to an {@link org.springframework.core.env.Environment}. Name-value pairs
     * can be specified with colon (":") or equals ("=") separators.
     *
     * @param environment
     *            the environment to modify
     * @param pairs
     *            the name:value pairs
     */
    public static void addEnvironment(final ConfigurableEnvironment environment, final String... pairs) {
        addEnvironment("test", environment, pairs);
    }

    /**
     * Add additional (high priority) values to an {@link org.springframework.core.env.Environment}. Name-value pairs
     * can be specified with colon (":") or equals ("=") separators.
     *
     * @param environment
     *            the environment to modify
     * @param name
     *            the property source name
     * @param pairs
     *            the name:value pairs
     */
    public static void addEnvironment(final String name,
        final ConfigurableEnvironment environment,
        final String... pairs) {
        final MutablePropertySources sources = environment.getPropertySources();
        Map<String, Object> map;
        if (!sources.contains(name)) {
            map = new HashMap<String, Object>();
            final MapPropertySource source = new MapPropertySource(name, map);
            sources.addFirst(source);
        } else {
            @SuppressWarnings("unchecked")
            final Map<String, Object> value = (Map<String, Object>) sources.get(name).getSource();
            map = value;
        }
        for (final String pair : pairs) {
            int index = pair.indexOf(":");
            index = index < 0 ? index = pair.indexOf("=") : index;
            final String key = pair.substring(0, index > 0 ? index : pair.length());
            final String value = index > 0 ? pair.substring(index + 1) : "";
            map.put(key.trim(), value.trim());
        }
    }

}
