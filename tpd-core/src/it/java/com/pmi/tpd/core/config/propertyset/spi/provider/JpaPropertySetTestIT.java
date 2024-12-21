package com.pmi.tpd.core.config.propertyset.spi.provider;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.google.common.collect.ImmutableMap;
import com.opensymphony.module.propertyset.IllegalPropertyException;
import com.opensymphony.module.propertyset.PropertyException;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.module.propertyset.PropertySetManager;
import com.opensymphony.util.XMLUtils;
import com.pmi.tpd.core.BaseDaoTestIT;
import com.pmi.tpd.core.context.propertyset.IPropertySetDAO;
import com.pmi.tpd.core.context.propertyset.TestObject;
import com.pmi.tpd.core.context.propertyset.spi.provider.DefaultJpaConfigurationProvider;
import com.pmi.tpd.core.context.propertyset.spi.provider.IJpaConfigurationProvider;
import com.pmi.tpd.core.context.propertyset.spi.provider.JpaPropertySet;
import com.pmi.tpd.core.context.propertyset.spi.provider.JpaPropertySetDAOImpl;

@Configuration
@ContextConfiguration(classes = { JpaPropertySetTestIT.class })
public class JpaPropertySetTestIT extends BaseDaoTestIT {

    /** */
    private final static String entityName = "foo";

    /** */
    private final static long entityId = 1;

    /** */
    private PropertySet ps;

    /** */
    private IJpaConfigurationProvider provider;

    /** */
    @Inject
    private IPropertySetDAO propertySetDao;

    @Bean
    public static IPropertySetDAO jpaPropertySetDAOImpl(final EntityManager entityManager) {
        return new JpaPropertySetDAOImpl(entityManager);
    }

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        provider = new DefaultJpaConfigurationProvider(propertySetDao);
        final Map<String, Object> props = new ImmutableMap.Builder<String, Object>()
                .put(JpaPropertySet.ENTITY_ID_PROPERTY, entityId)
                .put(JpaPropertySet.ENTITY_NAME_PROPERTY, entityName)
                .put(IJpaConfigurationProvider.PROVIDER_NAME_PROPERTY, provider)
                .build();
        ps = PropertySetManager.getInstance("jpa", props);
        assertEquals(JpaPropertySet.class, ps.getClass());
    }

    @Test
    public void exists() {
        ps.remove();
        assertEquals(false, ps.exists("foo"));
        ps.setString("foo", "nothing");
        assertEquals(true, ps.exists("foo"));
    }

    @Test
    public void removeKeys() {
        ps.remove();
        ps.setString("foo", "nothing");
        assertEquals(true, ps.exists("foo"));
        ps.remove("foo");
        assertEquals(false, ps.exists("foo"));
    }

    @Test
    public void supportsTypes() {
        assertEquals(true, ps.supportsTypes());
        assertEquals(true, ps.supportsType(PropertySet.BOOLEAN));
        assertEquals(true, ps.supportsType(PropertySet.DATA));
        assertEquals(true, ps.supportsType(PropertySet.DATE));
        assertEquals(true, ps.supportsType(PropertySet.DOUBLE));
        assertEquals(true, ps.supportsType(PropertySet.INT));
        assertEquals(true, ps.supportsType(PropertySet.LONG));
        assertEquals(false, ps.supportsType(PropertySet.OBJECT));
        assertEquals(false, ps.supportsType(PropertySet.PROPERTIES));
        assertEquals(true, ps.supportsType(PropertySet.STRING));
        assertEquals(true, ps.supportsType(PropertySet.TEXT));
        assertEquals(true, ps.supportsType(PropertySet.XML));
    }

    @Test
    public void globalTest() throws SAXException, IOException, ParserConfigurationException {
        ps.remove();
        final String string = "nothing";
        final Boolean bool = Boolean.TRUE;
        final byte[] data = "normal".getBytes();
        final Date date = new Date();
        final long l = Long.MAX_VALUE;
        final int i = Integer.MAX_VALUE;
        final double d = 0.000000046546548578554654f;
        final String text = "blalablbalblablaabal";
        final Document xml = XMLUtils.parse("<root>\n<item>toto1</item>\n<item>toto2</item>\n</root>");

        assertEquals(true, ps.getKeys().isEmpty());
        ps.setString("foo.string", string);
        ps.setBoolean("foo.boolean", bool);

        ps.setData("foo.data", data);
        ps.setDate("foo.date", date);
        ps.setLong("foo.long", l);
        ps.setInt("foo.int", i);
        ps.setDouble("foo.double", d);
        ps.setText("foo.text", text);

        ps.setXML("foo.xml", xml);
        assertEquals(9, ps.getKeys().size());

        assertEquals(string, ps.getString("foo.string"));
        assertEquals(bool, ps.getBoolean("foo.boolean"));
        assertEquals(new String(data), new String(ps.getData("foo.data")));
        assertEquals(date, ps.getDate("foo.date"));
        assertEquals(l, ps.getLong("foo.long"));
        assertEquals(i, ps.getInt("foo.int"));
        assertEquals(d, ps.getDouble("foo.double"), 0.001f);
        assertEquals(text, ps.getText("foo.text"));
        assertEquals(XMLUtils.print(xml), XMLUtils.print(ps.getXML("foo.xml")));

        assertEquals(PropertySet.STRING, ps.getType("foo.string"));
    }

    @Test
    public void setPropertyWithWrongType() {
        assertThrows(PropertyException.class, () -> {
            ps.remove();
            ps.setText("foo", "toto");
            ps.setLong("foo", 11);
        });
    }

    @Test
    public void getPropertyWithWrongType() {
        assertThrows(PropertyException.class, () -> {
            ps.remove();
            ps.setText("foo", "toto");
            ps.getLong("foo");
        });
    }

    @Test
    public void setKeyWithUnsupporedType() {
        assertThrows(PropertyException.class, () -> {
            ps.remove();
            ps.setObject("foo", "toto");
        });
    }

    @Test
    public void getExistingKeyWithUnsupporedType() {
        assertThrows(PropertyException.class, () -> {
            ps.remove();
            ps.setText("foo", "toto");
            ps.getObject("foo");
        });
    }

    // extended test copy from PropertySet test

    protected final String TEXT_300 = "12345678901234567890123456789012345678901234567890"
            + "12345678901234567890123456789012345678901234567890"
            + "12345678901234567890123456789012345678901234567890"
            + "12345678901234567890123456789012345678901234567890"
            + "12345678901234567890123456789012345678901234567890"
            + "12345678901234567890123456789012345678901234567890";

    @Test
    public void testExistsOnPropertyInPropertySet() {
        ps.setString("test1", "value1");
        assertTrue(ps.exists("test1"));
    }

    @Test
    public void testExistsOnPropertyNotInPropertySet() {
        assertFalse(ps.exists("test425"));
    }

    @Test
    public void testGetKeys() {
        ps.setString("test1", "value1");
        ps.setString("test2", "value2");
        ps.setString("test3", "value3");
        assertEquals(3, ps.getKeys().size());
    }

    @Test
    public void testGetKeysOfType() {
        if (ps.supportsTypes()) {
            ps.setString("test1", "value1");
            ps.setString("test2", "value2");
            ps.setInt("testInt", 14);
            assertEquals(2, ps.getKeys(PropertySet.STRING).size());
            assertEquals(1, ps.getKeys(PropertySet.INT).size());
        }
    }

    @Test
    public void testGetKeysWithPrefix() {
        ps.setString("test1", "value1");
        ps.setString("test2", "value2");
        ps.setString("username", "user1");
        final Collection<?> test = ps.getKeys("test");
        assertEquals(2, test.size());
        final Collection<?> user = ps.getKeys("user");
        assertEquals(1, user.size());
    }

    @Test
    public void testGetKeysWithPrefixOfType() {
        if (ps.supportsTypes()) {
            ps.setString("test1", "value1");
            ps.setString("test2", "value2");
            ps.setString("username", "user1");
            ps.setInt("testInt", 32);
            ps.setInt("usernum", 18);
            assertEquals(2, ps.getKeys("test", PropertySet.STRING).size());
            assertEquals(1, ps.getKeys("user", PropertySet.STRING).size());
            assertEquals(1, ps.getKeys("test", PropertySet.INT).size());
            assertEquals(1, ps.getKeys("user", PropertySet.INT).size());
        }
    }

    @Test
    public void testGetStringNotInPropertySet() {
        assertNull(ps.getString("test555"));
    }

    @Test
    public void testGetTypeForBoolean() {
        if (ps.supportsType(PropertySet.BOOLEAN)) {
            ps.setBoolean("testBoolean", true);
            assertEquals(PropertySet.BOOLEAN, ps.getType("testBoolean"));
        }
    }

    @Test
    public void testGetTypeForData() {
        if (ps.supportsType(PropertySet.DATA)) {
            ps.setData("testData", "value2".getBytes());
            assertEquals(PropertySet.DATA, ps.getType("testData"));
        }
    }

    @Test
    public void testGetTypeForDate() {
        if (ps.supportsType(PropertySet.DATE)) {
            ps.setDate("testDate", new Date());
            assertEquals(PropertySet.DATE, ps.getType("testDate"));
        }
    }

    @Test
    public void testGetTypeForDouble() {
        if (ps.supportsType(PropertySet.DOUBLE)) {
            ps.setDouble("testDouble", 10.456D);
            assertEquals(PropertySet.DOUBLE, ps.getType("testDouble"));
        }
    }

    @Test
    public void testGetTypeForInt() {
        if (ps.supportsType(PropertySet.INT)) {
            ps.setInt("testInt", 7);
            assertEquals(PropertySet.INT, ps.getType("testInt"));
        }
    }

    @Test
    public void testGetTypeForLong() {
        if (ps.supportsType(PropertySet.LONG)) {
            ps.setLong("testLong", 7L);
            assertEquals(PropertySet.LONG, ps.getType("testLong"));
        }
    }

    @Test
    public void testGetTypeForObject() {
        if (ps.supportsType(PropertySet.OBJECT)) {
            ps.setObject("testObject", new StringBuffer());
            assertEquals(PropertySet.OBJECT, ps.getType("testObject"));
        }
    }

    @Test
    public void testGetTypeForProperties() {
        if (ps.supportsType(PropertySet.PROPERTIES)) {
            ps.setProperties("testProperties", new Properties());
            assertEquals(PropertySet.PROPERTIES, ps.getType("testProperties"));
        }
    }

    @Test
    public void testGetTypeForString() {
        if (ps.supportsType(PropertySet.STRING)) {
            ps.setString("testString", "value7");
            assertEquals(PropertySet.STRING, ps.getType("testString"));
        }
    }

    @Test
    public void testGetTypeForText() {
        if (ps.supportsType(PropertySet.TEXT)) {
            ps.setText("testText", TEXT_300);
            assertEquals(PropertySet.TEXT, ps.getType("testText"));
        }
    }

    @Test
    public void testGetTypeForXml() throws ParserConfigurationException {
        if (ps.supportsType(PropertySet.XML)) {
            final Document doc = XMLUtils.newDocument();
            doc.appendChild(doc.createElement("root"));
            ps.setXML("testXml", doc);
            assertEquals(doc.getFirstChild().getNodeName(), ps.getXML("testXml").getFirstChild().getNodeName());
        }
    }

    @Test
    public void testRemoveAllKeys() {
        ps.setString("test1", "value1");
        assertEquals(1, ps.getKeys().size());

        try {
            ps.remove();
            assertEquals(0, ps.getKeys().size());
        } catch (final PropertyException e) {
            // this is ok too for read only PropertySets
        }
    }

    @Test
    public void testRemoveSingleKey() {
        ps.setString("test1", "value1");
        assertEquals(1, ps.getKeys().size());

        try {
            ps.remove("test1");
            assertEquals(0, ps.getKeys().size());
        } catch (final PropertyException e) {
            // this is ok too for read only PropertySets
        }
    }

    @Test
    public void testSetAsActualTypeGetAsActualTypeForBoolean() {
        if (ps.supportsType(PropertySet.BOOLEAN)) {
            ps.setAsActualType("testBoolean", Boolean.TRUE);
            assertEquals(Boolean.TRUE, ps.getAsActualType("testBoolean"));
        }
    }

    @Test
    public void testSetAsActualTypeGetAsActualTypeForData() {
        if (ps.supportsType(PropertySet.DATA)) {
            ps.setAsActualType("testData", "value1".getBytes());
            assertEquals(new String("value1".getBytes()), new String((byte[]) ps.getAsActualType("testData")));
        }
    }

    @Test
    public void testSetAsActualTypeGetAsActualTypeForDate() {
        if (ps.supportsType(PropertySet.DATE)) {
            final DateFormat df = DateFormat.getInstance();
            final Date now = new Date();
            ps.setAsActualType("testDate", now);
            assertEquals(df.format(now), df.format(ps.getAsActualType("testDate")));
        }
    }

    @Test
    public void testSetAsActualTypeGetAsActualTypeForDouble() {
        if (ps.supportsType(PropertySet.DOUBLE)) {
            ps.setAsActualType("testDouble", Double.valueOf(10.234));
            assertEquals(Double.valueOf(10.234), ps.getAsActualType("testDouble"));
        }
    }

    @Test
    public void testSetAsActualTypeGetAsActualTypeForInt() {
        if (ps.supportsType(PropertySet.INT)) {
            ps.setAsActualType("testInt", Integer.valueOf(7));
            assertEquals(Integer.valueOf(7), ps.getAsActualType("testInt"));
        }
    }

    @Test
    public void testSetAsActualTypeGetAsActualTypeForLong() {
        if (ps.supportsType(PropertySet.LONG)) {
            ps.setAsActualType("testLong", Long.valueOf(70000));
            assertEquals(Long.valueOf(70000), ps.getAsActualType("testLong"));
        }
    }

    @Test
    public void testSetAsActualTypeGetAsActualTypeForObject() {
        if (ps.supportsType(PropertySet.OBJECT)) {
            final TestObject testObject = new TestObject(2);
            ps.setAsActualType("testObject", testObject);
            assertEquals(testObject, ps.getAsActualType("testObject"));
        }
    }

    @Test
    public void testSetAsActualTypeGetAsActualTypeForProperties() {
        if (ps.supportsType(PropertySet.PROPERTIES)) {
            final Properties props = new Properties();
            props.setProperty("prop1", "value1");
            ps.setAsActualType("testProperties", props);
            assertEquals(props, ps.getAsActualType("testProperties"));
        }
    }

    @Test
    public void testSetAsActualTypeGetAsActualTypeForString() {
        if (ps.supportsType(PropertySet.STRING)) {
            ps.setAsActualType("testString", "value1");
            assertEquals("value1", ps.getAsActualType("testString"));
        }
    }

    @Test
    public void testSetAsActualTypeGetAsActualTypeForText() {
        if (ps.supportsType(PropertySet.TEXT)) {
            ps.setAsActualType("testText", TEXT_300);
            assertEquals(TEXT_300, ps.getAsActualType("testText"));
        }
    }

    @Test
    public void testSetAsActualTypeGetAsActualTypeForXml() throws ParserConfigurationException {
        if (ps.supportsType(PropertySet.XML)) {
            final Document doc = XMLUtils.newDocument();
            doc.appendChild(doc.createElement("root"));
            ps.setAsActualType("testXml", doc);
            assertEquals(doc.getFirstChild().getNodeName(), ps.getXML("testXml").getFirstChild().getNodeName());
        }
    }

    @Test
    public void testSetBooleanGetBoolean() {
        if (ps.supportsType(PropertySet.BOOLEAN)) {
            ps.setBoolean("testBoolean", true);
            assertTrue(ps.getBoolean("testBoolean"));
            ps.setBoolean("testBoolean", false);
            assertFalse(ps.getBoolean("testBoolean"));
        }
    }

    @Test
    public void testSetDataGetData() {
        if (ps.supportsType(PropertySet.DATA)) {
            ps.setData("testData", "value1".getBytes());
            assertEquals(new String("value1".getBytes()), new String(ps.getData("testData")));
            ps.setData("testData", "value2".getBytes());
            assertEquals(new String("value2".getBytes()), new String(ps.getData("testData")));
        }
    }

    @Test
    public void testSetDateGetDate() {
        if (ps.supportsType(PropertySet.DATE)) {
            final DateFormat df = DateFormat.getInstance();
            final Date now = new Date();
            ps.setDate("testDate", now);
            assertEquals(df.format(now), df.format(ps.getDate("testDate")));
            ps.setDate("testDate", new Date());
            assertEquals(df.format(now), df.format(ps.getDate("testDate")));
        }
    }

    @Test
    public void testSetDoubleGetDouble() {
        if (ps.supportsType(PropertySet.DOUBLE)) {
            ps.setDouble("testDouble", 1D);
            assertEquals(1D, ps.getDouble("testDouble"), 0);
            ps.setDouble("testDouble", 100000D);
            assertEquals(100000D, ps.getDouble("testDouble"), 0);
        }
    }

    @Test
    public void testSetIntGetInt() {
        if (ps.supportsType(PropertySet.INT)) {
            ps.setInt("testInt", 7);
            assertEquals(7, ps.getInt("testInt"));
            ps.setInt("testInt", 11);
            assertEquals(11, ps.getInt("testInt"));
        }
    }

    @Test
    public void testSetLongGetLong() {
        if (ps.supportsType(PropertySet.LONG)) {
            ps.setLong("testLong", 1L);
            assertEquals(1L, ps.getLong("testLong"));
            ps.setLong("testLong", 100000);
            assertEquals(100000, ps.getLong("testLong"));
        }
    }

    @Test
    public void testSetObjectGetObject() {
        if (ps.supportsType(PropertySet.OBJECT)) {
            final TestObject testObject = new TestObject(1);
            ps.setObject("testObject", testObject);
            assertEquals(testObject, ps.getObject("testObject"));
        }
    }

    @Test
    public void testSetPropertiesGetProperties() {
        if (ps.supportsType(PropertySet.PROPERTIES)) {
            final Properties props = new Properties();
            props.setProperty("prop1", "propValue1");
            ps.setProperties("testProperties", props);
            assertEquals(props, ps.getProperties("testProperties"));
            props.setProperty("prop2", "propValue2");
            ps.setProperties("testProperties", props);
            assertEquals(props, ps.getProperties("testProperties"));
        }
    }

    @Test
    public void testSetStringGetStringLengthGreaterThan255() {
        try {
            ps.setString("testString", TEXT_300);
            fail("Should not be able to setString() with a String longer than 255 chars.");
        } catch (final IllegalPropertyException e) {
            // expected
        }
    }

    @Test
    public void testSetStringGetStringLengthLessThan255() {
        ps.setString("testString", "value1");
        assertTrue("value1".equals(ps.getString("testString")));
        ps.setString("testString", "value2");
        assertTrue("value2".equals(ps.getString("testString")));
    }

    @Test
    public void testSetTextGetText() {
        if (ps.supportsType(PropertySet.TEXT)) {
            ps.setText("testText", TEXT_300);
            assertEquals(TEXT_300, ps.getText("testText"));
            ps.setText("testText", TEXT_300 + "A");
            assertEquals(TEXT_300 + "A", ps.getText("testText"));
        }
    }

    @Test
    public void testSetXmlGetXml() throws ParserConfigurationException {
        if (ps.supportsType(PropertySet.XML)) {
            final Document doc = XMLUtils.newDocument();
            doc.appendChild(doc.createElement("root"));
            ps.setXML("testXml", doc);
            assertEquals(doc.getFirstChild().getNodeName(), ps.getXML("testXml").getFirstChild().getNodeName());
        }
    }
}
