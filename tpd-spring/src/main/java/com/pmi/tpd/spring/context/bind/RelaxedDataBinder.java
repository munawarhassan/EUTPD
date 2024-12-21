package com.pmi.tpd.spring.context.bind;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.BeansException;
import org.springframework.beans.InvalidPropertyException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.NotWritablePropertyException;
import org.springframework.beans.PropertyValue;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.validation.AbstractPropertyBindingResult;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.DataBinder;

import com.pmi.tpd.spring.context.RelaxedNames;

/**
 * Binder implementation that allows caller to bind to maps and also allows property names to match a bit loosely (if
 * underscores or dashes are removed and replaced with camel case for example).
 *
 * @author Dave Syer
 * @author Phillip Webb
 * @author Stephane Nicoll
 * @author Andy Wilkinson
 * @see RelaxedNames
 */
public class RelaxedDataBinder extends DataBinder {

    /** */
    private static final Object BLANK = new Object();

    /** */
    private String namePrefix;

    /** */
    private boolean ignoreNestedProperties;

    /** */
    private MultiValueMap<String, String> nameAliases = new LinkedMultiValueMap<>();

    /**
     * Create a new {@link RelaxedDataBinder} instance.
     *
     * @param target
     *            the target into which properties are bound
     */
    public RelaxedDataBinder(final Object target) {
        super(wrapTarget(target));
    }

    /**
     * Create a new {@link RelaxedDataBinder} instance.
     *
     * @param target
     *            the target into which properties are bound
     * @param namePrefix
     *            An optional prefix to be used when reading properties
     */
    public RelaxedDataBinder(final Object target, final String namePrefix) {
        super(wrapTarget(target), StringUtils.hasLength(namePrefix) ? namePrefix : DEFAULT_OBJECT_NAME);
        this.namePrefix = cleanNamePrefix(namePrefix);
    }

    private String cleanNamePrefix(final String namePrefix) {
        if (!StringUtils.hasLength(namePrefix)) {
            return null;
        }
        return namePrefix.endsWith(".") ? namePrefix : namePrefix + ".";
    }

    /**
     * Flag to disable binding of nested properties (i.e. those with period separators in their paths). Can be useful to
     * disable this if the name prefix is empty and you don't want to ignore unknown fields.
     *
     * @param ignoreNestedProperties
     *            the flag to set (default false)
     */
    public void setIgnoreNestedProperties(final boolean ignoreNestedProperties) {
        this.ignoreNestedProperties = ignoreNestedProperties;
    }

    /**
     * Set name aliases.
     *
     * @param aliases
     *            a map of property name to aliases
     */
    public void setNameAliases(final Map<String, List<String>> aliases) {
        this.nameAliases = new LinkedMultiValueMap<>(aliases);
    }

    /**
     * Add aliases to the {@link DataBinder}.
     *
     * @param name
     *            the property name to alias
     * @param alias
     *            aliases for the property names
     * @return this instance
     */
    public RelaxedDataBinder withAlias(final String name, final String... alias) {
        for (final String value : alias) {
            this.nameAliases.add(name, value);
        }
        return this;
    }

    @Override
    protected void doBind(final MutablePropertyValues propertyValues) {
        super.doBind(modifyProperties(propertyValues, getTarget()));
    }

    /**
     * Modify the property values so that period separated property paths are valid for map keys. Also creates new maps
     * for properties of map type that are null (assuming all maps are potentially nested). The standard bracket
     * {@code[...]} dereferencing is also accepted.
     *
     * @param propertyValues
     *            the property values
     * @param target
     *            the target object
     * @return modified property values
     */
    private MutablePropertyValues modifyProperties(MutablePropertyValues propertyValues, final Object target) {
        propertyValues = getPropertyValuesForNamePrefix(propertyValues);
        if (target instanceof MapHolder) {
            propertyValues = addMapPrefix(propertyValues);
        }
        final BeanWrapper wrapper = new BeanWrapperImpl(target);
        wrapper.setConversionService(new RelaxedConversionService(getConversionService()));
        wrapper.setAutoGrowNestedPaths(true);
        final List<PropertyValue> sortedValues = new ArrayList<>();
        final Set<String> modifiedNames = new HashSet<>();
        final List<String> sortedNames = getSortedPropertyNames(propertyValues);
        for (final String name : sortedNames) {
            final PropertyValue propertyValue = propertyValues.getPropertyValue(name);
            final PropertyValue modifiedProperty = modifyProperty(wrapper, propertyValue);
            if (modifiedNames.add(modifiedProperty.getName())) {
                sortedValues.add(modifiedProperty);
            }
        }
        return new MutablePropertyValues(sortedValues);
    }

    private List<String> getSortedPropertyNames(final MutablePropertyValues propertyValues) {
        final List<String> names = new LinkedList<>();
        for (final PropertyValue propertyValue : propertyValues.getPropertyValueList()) {
            names.add(propertyValue.getName());
        }
        sortPropertyNames(names);
        return names;
    }

    /**
     * Sort by name so that parent properties get processed first (e.g. 'foo.bar' before 'foo.bar.spam'). Don't use
     * Collections.sort() because the order might be significant for other property names (it shouldn't be but who knows
     * what people might be relying on, e.g. HSQL has a JDBCXADataSource where "databaseName" is a synonym for "url").
     *
     * @param names
     *            the names to sort
     */
    private void sortPropertyNames(final List<String> names) {
        for (final String name : new ArrayList<>(names)) {
            final int propertyIndex = names.indexOf(name);
            final BeanPath path = new BeanPath(name);
            for (final String prefix : path.prefixes()) {
                final int prefixIndex = names.indexOf(prefix);
                if (prefixIndex >= propertyIndex) {
                    // The child property has a parent in the list in the wrong order
                    names.remove(name);
                    names.add(prefixIndex, name);
                }
            }
        }
    }

    private MutablePropertyValues addMapPrefix(final MutablePropertyValues propertyValues) {
        final MutablePropertyValues rtn = new MutablePropertyValues();
        for (final PropertyValue pv : propertyValues.getPropertyValues()) {
            rtn.add("map." + pv.getName(), pv.getValue());
        }
        return rtn;
    }

    private MutablePropertyValues getPropertyValuesForNamePrefix(final MutablePropertyValues propertyValues) {
        if (!StringUtils.hasText(this.namePrefix) && !this.ignoreNestedProperties) {
            return propertyValues;
        }
        final MutablePropertyValues rtn = new MutablePropertyValues();
        for (final PropertyValue value : propertyValues.getPropertyValues()) {
            String name = value.getName();
            for (final String prefix : new RelaxedNames(stripLastDot(this.namePrefix))) {
                for (final String separator : new String[] { ".", "_" }) {
                    final String candidate = StringUtils.hasLength(prefix) ? prefix + separator : prefix;
                    if (name.startsWith(candidate)) {
                        name = name.substring(candidate.length());
                        if (!(this.ignoreNestedProperties && name.contains("."))) {
                            final PropertyOrigin propertyOrigin = OriginCapablePropertyValue.getOrigin(value);
                            rtn.addPropertyValue(
                                new OriginCapablePropertyValue(name, value.getValue(), propertyOrigin));
                        }
                    }
                }
            }
        }
        return rtn;
    }

    private String stripLastDot(String string) {
        if (StringUtils.hasLength(string) && string.endsWith(".")) {
            string = string.substring(0, string.length() - 1);
        }
        return string;
    }

    private PropertyValue modifyProperty(final BeanWrapper target, final PropertyValue propertyValue) {
        final String name = propertyValue.getName();
        final String normalizedName = normalizePath(target, name);
        if (!normalizedName.equals(name)) {
            return new PropertyValue(normalizedName, propertyValue.getValue());
        }
        return propertyValue;
    }

    /**
     * Normalize a bean property path to a format understood by a BeanWrapper. This is used so that
     * <ul>
     * <li>Fuzzy matching can be employed for bean property names</li>
     * <li>Period separators can be used instead of indexing ([...]) for map keys</li>
     * </ul>
     *
     * @param wrapper
     *            a bean wrapper for the object to bind
     * @param path
     *            the bean path to bind
     * @return a transformed path with correct bean wrapper syntax
     */
    protected String normalizePath(final BeanWrapper wrapper, final String path) {
        return initializePath(wrapper, new BeanPath(path), 0);
    }

    @Override
    protected AbstractPropertyBindingResult createBeanPropertyBindingResult() {
        return new RelaxedBeanPropertyBindingResult(getTarget(), getObjectName(), isAutoGrowNestedPaths(),
                getAutoGrowCollectionLimit(), getConversionService());
    }

    private String initializePath(final BeanWrapper wrapper, final BeanPath path, int index) {
        final String prefix = path.prefix(index);
        String key = path.name(index);
        if (path.isProperty(index)) {
            key = getActualPropertyName(wrapper, prefix, key);
            path.rename(index, key);
        }
        if (path.name(++index) == null) {
            return path.toString();
        }

        final String name = path.prefix(index);
        final TypeDescriptor descriptor = wrapper.getPropertyTypeDescriptor(name);
        if (descriptor == null || descriptor.isMap()) {
            if (isMapValueStringType(descriptor) || isBlanked(wrapper, name, path.name(index))) {
                path.collapseKeys(index);
            }
            path.mapIndex(index);
            extendMapIfNecessary(wrapper, path, index);
        } else if (descriptor.isCollection()) {
            extendCollectionIfNecessary(wrapper, path, index);
        } else if (descriptor.getType().equals(Object.class)) {
            if (isBlanked(wrapper, name, path.name(index))) {
                path.collapseKeys(index);
            }
            path.mapIndex(index);
            if (path.isLastNode(index)) {
                wrapper.setPropertyValue(path.toString(), BLANK);
            } else {
                final String next = path.prefix(index + 1);
                if (wrapper.getPropertyValue(next) == null) {
                    wrapper.setPropertyValue(next, new LinkedHashMap<String, Object>());
                }
            }
        }
        return initializePath(wrapper, path, index);
    }

    private boolean isMapValueStringType(final TypeDescriptor descriptor) {
        if (descriptor == null || descriptor.getMapValueTypeDescriptor() == null) {
            return false;
        }
        if (Properties.class.isAssignableFrom(descriptor.getObjectType())) {
            // Properties is declared as Map<Object,Object> but we know it's really
            // Map<String,String>
            return true;
        }
        final Class<?> valueType = descriptor.getMapValueTypeDescriptor().getObjectType();
        return valueType != null && CharSequence.class.isAssignableFrom(valueType);
    }

    @SuppressWarnings("rawtypes")
    private boolean isBlanked(final BeanWrapper wrapper, final String propertyName, final String key) {
        final Object value = wrapper.isReadableProperty(propertyName) ? wrapper.getPropertyValue(propertyName) : null;
        if (value instanceof Map) {
            if (((Map) value).get(key) == BLANK) {
                return true;
            }
        }
        return false;
    }

    private void extendCollectionIfNecessary(final BeanWrapper wrapper, final BeanPath path, final int index) {
        final String name = path.prefix(index);
        final TypeDescriptor elementDescriptor = wrapper.getPropertyTypeDescriptor(name).getElementTypeDescriptor();
        if (!elementDescriptor.isMap() && !elementDescriptor.isCollection()
                && !elementDescriptor.getType().equals(Object.class)) {
            return;
        }
        Object extend = new LinkedHashMap<String, Object>();
        if (!elementDescriptor.isMap() && path.isArrayIndex(index)) {
            extend = new ArrayList<>();
        }
        wrapper.setPropertyValue(path.prefix(index + 1), extend);
    }

    private void extendMapIfNecessary(final BeanWrapper wrapper, final BeanPath path, final int index) {
        final String name = path.prefix(index);
        final TypeDescriptor parent = wrapper.getPropertyTypeDescriptor(name);
        if (parent == null) {
            return;
        }
        TypeDescriptor descriptor = parent.getMapValueTypeDescriptor();
        if (descriptor == null) {
            descriptor = TypeDescriptor.valueOf(Object.class);
        }
        if (!descriptor.isMap() && !descriptor.isCollection() && !descriptor.getType().equals(Object.class)) {
            return;
        }
        final String extensionName = path.prefix(index + 1);
        if (wrapper.isReadableProperty(extensionName)) {
            final Object currentValue = wrapper.getPropertyValue(extensionName);
            if (descriptor.isCollection() && currentValue instanceof Collection
                    || !descriptor.isCollection() && currentValue instanceof Map) {
                return;
            }
        }
        Object extend = new LinkedHashMap<String, Object>();
        if (descriptor.isCollection()) {
            extend = new ArrayList<>();
        }
        if (descriptor.getType().equals(Object.class) && path.isLastNode(index)) {
            extend = BLANK;
        }
        wrapper.setPropertyValue(extensionName, extend);
    }

    private String getActualPropertyName(final BeanWrapper target, final String prefix, final String name) {
        String propertyName = resolvePropertyName(target, prefix, name);
        if (propertyName == null) {
            propertyName = resolveNestedPropertyName(target, prefix, name);
        }
        return propertyName == null ? name : propertyName;
    }

    private String resolveNestedPropertyName(final BeanWrapper target, final String prefix, final String name) {
        final StringBuilder candidate = new StringBuilder();
        for (final String field : name.split("[_\\-\\.]")) {
            candidate.append(candidate.length() > 0 ? "." : "");
            candidate.append(field);
            final String nested = resolvePropertyName(target, prefix, candidate.toString());
            if (nested != null) {
                final Class<?> type = target.getPropertyType(nested);
                if (type != null && Map.class.isAssignableFrom(type)) {
                    // Special case for map property (gh-3836).
                    return nested + "[" + name.substring(candidate.length() + 1) + "]";
                }
                final String propertyName = resolvePropertyName(target,
                    joinString(prefix, nested),
                    name.substring(candidate.length() + 1));
                if (propertyName != null) {
                    return joinString(nested, propertyName);
                }
            }
        }
        return null;
    }

    private String resolvePropertyName(final BeanWrapper target, final String prefix, final String name) {
        final Iterable<String> names = getNameAndAliases(name);
        for (final String nameOrAlias : names) {
            for (final String candidate : new RelaxedNames(nameOrAlias)) {
                try {
                    if (target.getPropertyType(joinString(prefix, candidate)) != null) {
                        return candidate;
                    }
                } catch (final InvalidPropertyException ex) {
                    // swallow and continue
                }
            }
        }
        return null;
    }

    private String joinString(final String prefix, final String name) {
        return StringUtils.hasLength(prefix) ? prefix + "." + name : name;
    }

    private Iterable<String> getNameAndAliases(final String name) {
        final List<String> aliases = this.nameAliases.get(name);
        if (aliases == null) {
            return Collections.singleton(name);
        }
        final List<String> nameAndAliases = new ArrayList<>(aliases.size() + 1);
        nameAndAliases.add(name);
        nameAndAliases.addAll(aliases);
        return nameAndAliases;
    }

    private static Object wrapTarget(Object target) {
        if (target instanceof Map) {
            @SuppressWarnings("unchecked")
            final Map<String, Object> map = (Map<String, Object>) target;
            target = new MapHolder(map);
        }
        return target;
    }

    /**
     * Holder to allow Map targets to be bound.
     */
    static class MapHolder {

        /** */
        private Map<String, Object> map;

        MapHolder(final Map<String, Object> map) {
            this.map = map;
        }

        public void setMap(final Map<String, Object> map) {
            this.map = map;
        }

        public Map<String, Object> getMap() {
            return this.map;
        }

    }

    /**
     * A path though properties of a bean.
     */
    private static class BeanPath {

        /** */
        private List<PathNode> nodes;

        BeanPath(final String path) {
            this.nodes = splitPath(path);
        }

        public List<String> prefixes() {
            final List<String> prefixes = new ArrayList<>();
            for (int index = 1; index < this.nodes.size(); index++) {
                prefixes.add(prefix(index));
            }
            return prefixes;
        }

        public boolean isLastNode(final int index) {
            return index >= this.nodes.size() - 1;
        }

        private List<PathNode> splitPath(final String path) {
            final List<PathNode> nodes = new ArrayList<>();
            final String current = extractIndexedPaths(path, nodes);
            for (final String name : StringUtils.delimitedListToStringArray(current, ".")) {
                if (StringUtils.hasText(name)) {
                    nodes.add(new PropertyNode(name));
                }
            }
            return nodes;
        }

        private String extractIndexedPaths(final String path, final List<PathNode> nodes) {
            int startRef = path.indexOf("[");
            String current = path;
            while (startRef >= 0) {
                if (startRef > 0) {
                    nodes.addAll(splitPath(current.substring(0, startRef)));
                }
                final int endRef = current.indexOf("]", startRef);
                if (endRef > 0) {
                    final String sub = current.substring(startRef + 1, endRef);
                    if (sub.matches("[0-9]+")) {
                        nodes.add(new ArrayIndexNode(sub));
                    } else {
                        nodes.add(new MapIndexNode(sub));
                    }
                }
                current = current.substring(endRef + 1);
                startRef = current.indexOf("[");
            }
            return current;
        }

        public void collapseKeys(final int index) {
            final List<PathNode> revised = new ArrayList<>();
            for (int i = 0; i < index; i++) {
                revised.add(this.nodes.get(i));
            }
            final StringBuilder builder = new StringBuilder();
            for (int i = index; i < this.nodes.size(); i++) {
                if (i > index) {
                    builder.append(".");
                }
                builder.append(this.nodes.get(i).name);
            }
            revised.add(new PropertyNode(builder.toString()));
            this.nodes = revised;
        }

        public void mapIndex(final int index) {
            PathNode node = this.nodes.get(index);
            if (node instanceof PropertyNode) {
                node = ((PropertyNode) node).mapIndex();
            }
            this.nodes.set(index, node);
        }

        public String prefix(final int index) {
            return range(0, index);
        }

        public void rename(final int index, final String name) {
            this.nodes.get(index).name = name;
        }

        public String name(final int index) {
            if (index < this.nodes.size()) {
                return this.nodes.get(index).name;
            }
            return null;
        }

        private String range(final int start, final int end) {
            final StringBuilder builder = new StringBuilder();
            for (int i = start; i < end; i++) {
                final PathNode node = this.nodes.get(i);
                builder.append(node);
            }
            if (builder.toString().startsWith(".")) {
                builder.replace(0, 1, "");
            }
            return builder.toString();
        }

        public boolean isArrayIndex(final int index) {
            return this.nodes.get(index) instanceof ArrayIndexNode;
        }

        public boolean isProperty(final int index) {
            return this.nodes.get(index) instanceof PropertyNode;
        }

        @Override
        public String toString() {
            return prefix(this.nodes.size());
        }

        /**
         * @author Christophe Friederich
         */
        private static class PathNode {

            /** */
            protected String name;

            PathNode(final String name) {
                this.name = name;
            }

        }

        /**
         * @author Christophe Friederich
         */
        private static class ArrayIndexNode extends PathNode {

            ArrayIndexNode(final String name) {
                super(name);
            }

            @Override
            public String toString() {
                return "[" + this.name + "]";
            }

        }

        /**
         * @author Christophe Friederich
         */
        private static class MapIndexNode extends PathNode {

            MapIndexNode(final String name) {
                super(name);
            }

            @Override
            public String toString() {
                return "[" + this.name + "]";
            }

        }

        /**
         * @author Christophe Friederich
         */
        private static class PropertyNode extends PathNode {

            PropertyNode(final String name) {
                super(name);
            }

            public MapIndexNode mapIndex() {
                return new MapIndexNode(this.name);
            }

            @Override
            public String toString() {
                return "." + this.name;
            }

        }

    }

    /**
     * Extended version of {@link BeanPropertyBindingResult} to support relaxed binding.
     */
    private static class RelaxedBeanPropertyBindingResult extends BeanPropertyBindingResult {

        /**
         *
         */
        private static final long serialVersionUID = 1L;

        /** */
        private final RelaxedConversionService conversionService;

        RelaxedBeanPropertyBindingResult(final Object target, final String objectName,
                final boolean autoGrowNestedPaths, final int autoGrowCollectionLimit,
                final ConversionService conversionService) {
            super(target, objectName, autoGrowNestedPaths, autoGrowCollectionLimit);
            this.conversionService = new RelaxedConversionService(conversionService);
        }

        @Override
        protected BeanWrapper createBeanWrapper() {
            final BeanWrapper beanWrapper = new RelaxedBeanWrapper(getTarget());
            beanWrapper.setConversionService(this.conversionService);
            beanWrapper.registerCustomEditor(InetAddress.class, new InetAddressEditor());
            return beanWrapper;
        }

    }

    /**
     * Extended version of {@link BeanWrapperImpl} to support relaxed binding.
     */
    private static class RelaxedBeanWrapper extends BeanWrapperImpl {

        /** */
        private static final Set<String> BENIGN_PROPERTY_SOURCE_NAMES;

        static {
            final Set<String> names = new HashSet<>();
            names.add(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME);
            names.add(StandardEnvironment.SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME);
            BENIGN_PROPERTY_SOURCE_NAMES = Collections.unmodifiableSet(names);
        }

        RelaxedBeanWrapper(final Object target) {
            super(target);
        }

        @Override
        public void setPropertyValue(final PropertyValue pv) throws BeansException {
            try {
                super.setPropertyValue(pv);
            } catch (final NotWritablePropertyException ex) {
                final PropertyOrigin origin = OriginCapablePropertyValue.getOrigin(pv);
                if (isBenign(origin)) {
                    logger.debug("Ignoring benign property binding failure", ex);
                    return;
                }
                if (origin == null) {
                    throw ex;
                }
                throw new RelaxedBindingNotWritablePropertyException(ex, origin);
            }
        }

        private boolean isBenign(final PropertyOrigin origin) {
            final String name = origin == null ? null : origin.getSource().getName();
            return BENIGN_PROPERTY_SOURCE_NAMES.contains(name);
        }

    }

}
