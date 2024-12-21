/**
 * Copyright 2015 Christophe Friederich
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.pmi.tpd.spring.context;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.util.StringUtils;

/**
 * Generates relaxed name variations from a given source.
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public final class RelaxedNames implements Iterable<String> {

    /** regex pattern used to find camelcase. */
    private static final Pattern CAMEL_CASE_PATTERN = Pattern.compile("([^A-Z-])([A-Z])");

    /** the source property name. */
    private final String name;

    /** list of variations of {@code name}. */
    private final Set<String> values = new LinkedHashSet<String>();

    /**
     * Create a new {@link com.pmi.tpd.spring.context.RelaxedNames} instance.
     *
     * @param name
     *            the source name. For the maximum number of variations specify the name using dashed notation (e.g.
     *            {@literal my-property-name}
     */
    public RelaxedNames(final String name) {
        this.name = name == null ? "" : name;
        initialize(this.name, this.values);
    }

    /** {@inheritDoc} */
    @Override
    public Iterator<String> iterator() {
        return this.values.iterator();
    }

    private void initialize(final String name, final Set<String> values) {
        if (values.contains(name)) {
            return;
        }
        for (final Variation variation : Variation.values()) {
            for (final Manipulation manipulation : Manipulation.values()) {
                String result = name;
                result = manipulation.apply(result);
                result = variation.apply(result);
                values.add(result);
                initialize(result, values);
            }
        }
    }

    /**
     * Return a {@link RelaxedNames} for the given source camelCase source name.
     *
     * @param name
     *            the source name in camelCase
     * @return the relaxed names
     */
    public static RelaxedNames forCamelCase(final String name) {
        return new RelaxedNames(Manipulation.CAMELCASE_TO_HYPHEN.apply(name));
    }

    /**
     * Define a list of different possible variations on {@link Manipulation}.
     *
     * @author Christophe Friederich
     * @since 1.0
     */
    private enum Variation {

        /**
         * none change.
         */
        NONE {

            @Override
            public String apply(final String value) {
                return value;
            }
        },

        /**
         * name converted to lower case.
         */
        LOWERCASE {

            @Override
            public String apply(final String value) {
                return value.toLowerCase();
            }
        },

        /**
         * name converted to upper case.
         */
        UPPERCASE {

            @Override
            public String apply(final String value) {
                return value.toUpperCase();
            }
        };

        public abstract String apply(String value);

    }

    /**
     * @author Christophe Friederich
     * @since 1.0
     */
    private enum Manipulation {

        /**
         * As-is.
         */
        NONE {

            @Override
            public String apply(final String value) {
                return value;
            }
        },

        /**
         * Replaces all '-' to '_', ie {@literal my-property-name} -> {@literal my_property_name}.
         */
        HYPHEN_TO_UNDERSCORE {

            @Override
            public String apply(final String value) {
                return value.replace("-", "_");
            }
        },

        /**
         * Replaces all '_' to '.', ie {@literal my_property_name} -> {@literal my.property.name}.
         */
        UNDERSCORE_TO_PERIOD {

            @Override
            public String apply(final String value) {
                return value.replace("_", ".");
            }
        },

        /**
         * Replaces all '.' to '_', ie {@literal my.property.name} -> {@literal my_property_name}.
         */
        PERIOD_TO_UNDERSCORE {

            @Override
            public String apply(final String value) {
                return value.replace(".", "_");
            }
        },

        /**
         * Replaces camelcase ot '_' , ie {@literal MyPropertyName} -> {@literal my_property_name}.
         */
        CAMELCASE_TO_UNDERSCORE {

            @Override
            public String apply(final String value) {
                final Matcher matcher = CAMEL_CASE_PATTERN.matcher(value);
                final StringBuffer result = new StringBuffer();
                while (matcher.find()) {
                    matcher.appendReplacement(result,
                        matcher.group(1) + '_' + StringUtils.uncapitalize(matcher.group(2)));
                }
                matcher.appendTail(result);
                return result.toString();
            }
        },

        /**
         * Replaces CamelCase to '-' , ie {@literal MyPropertyName} -> {@literal my-property-name}.
         */
        CAMELCASE_TO_HYPHEN {

            @Override
            public String apply(final String value) {
                final Matcher matcher = CAMEL_CASE_PATTERN.matcher(value);
                final StringBuffer result = new StringBuffer();
                while (matcher.find()) {
                    matcher.appendReplacement(result,
                        matcher.group(1) + '-' + StringUtils.uncapitalize(matcher.group(2)));
                }
                matcher.appendTail(result);
                return result.toString();
            }
        },

        /**
         * Replaces separators ('.','-','_') to CamelCase , ie {@literal my-property-NAME} -> {@literal MyPropertyNAME}.
         */
        SEPARATED_TO_CAMELCASE {

            @Override
            public String apply(final String value) {
                return separatedToCamelCase(value, false);
            }
        },

        /**
         * Replaces separators ('.','-','_') to CamelCase , ie {@literal my-PROPERTY-name} -> {@literal MyPropertyName}.
         */
        CASE_INSENSITIVE_SEPARATED_TO_CAMELCASE {

            @Override
            public String apply(final String value) {
                return separatedToCamelCase(value, true);
            }
        };

        /**
         * @param value
         * @return
         */
        public abstract String apply(String value);

        private static String separatedToCamelCase(final String value, final boolean caseInsensitive) {
            final StringBuilder builder = new StringBuilder();
            for (String field : value.split("[_\\-.]")) {
                field = caseInsensitive ? field.toLowerCase() : field;
                builder.append(builder.length() == 0 ? field : StringUtils.capitalize(field));
            }
            for (final String suffix : new String[] { "_", "-", "." }) {
                if (value.endsWith(suffix)) {
                    builder.append(suffix);
                }
            }
            return builder.toString();

        }
    }

}
