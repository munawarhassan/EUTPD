package com.pmi.tpd.core.util.path;

import java.util.Collection;

/**
 * The PathMapper is used to map file patterns to keys, and find an approriate key for a given file path. The pattern
 * rules are consistent with those defined in the Servlet 2.3 API on the whole. Wildcard patterns are also supported,
 * using any combination of * and ?.
 * <h3>Example</h3> <blockquote><code>
 * PathMapper pm = new PathMapper();<br>
 * <br>
 * pm.put("one","/");<br>
 * pm.put("two","/mydir/*");<br>
 * pm.put("three","*.xml");<br>
 * pm.put("four","/myexactfile.html");<br>
 * pm.put("five","/*\/admin/*.??ml");<br>
 * <br>
 * String result1 = pm.get("/mydir/myfile.xml"); // returns "two";<br>
 * String result2 = pm.get("/mydir/otherdir/admin/myfile.html"); // returns "five";<br>
 * </code> </blockquote>
 *
 * @since 1.0
 * @author Christophe Friederich
 */
public interface PathMapper {

    /**
     * Retrieve appropriate key by matching patterns with supplied path.
     *
     * @param path
     *            a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    String get(String path);

    /**
     * Retrieve all mappings which match a supplied path.
     *
     * @param path
     *            a {@link java.lang.String} object.
     * @return a {@link java.util.Collection} object.
     */
    Collection<String> getAll(String path);

    /**
     * Add a key and appropriate matching pattern.
     *
     * @param key
     *            a {@link java.lang.String} object.
     * @param pattern
     *            a {@link java.lang.String} object.
     */
    void put(final String key, final String pattern);
}
