package com.pmi.tpd.core.context.propertyset;

import org.junit.jupiter.api.BeforeEach;

import com.google.common.collect.ImmutableMap;
import com.opensymphony.module.propertyset.memory.SerializablePropertySet;

public class CachingPropertySetTest extends AbstractPropertySetTest {


    @BeforeEach
    public void setUp() throws Exception {
        final SerializablePropertySet serializablePropertySet = new SerializablePropertySet();
        serializablePropertySet.init(null, null);

        ps = new CachingPropertySet();
        ps.init(null, ImmutableMap.of("PropertySet", serializablePropertySet));
    }

}
