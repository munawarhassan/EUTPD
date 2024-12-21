package com.pmi.tpd.core.config.propertyset.spi.provider;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ContextConfiguration;

import com.opensymphony.module.propertyset.PropertySet;
import com.pmi.tpd.api.paging.PageUtils;
import com.pmi.tpd.core.BaseDaoTestIT;
import com.pmi.tpd.core.context.propertyset.IPropertySetDAO;
import com.pmi.tpd.core.context.propertyset.spi.provider.JpaPropertySetDAOImpl;
import com.pmi.tpd.core.model.propertyset.PropertySetItem;

@Configuration
@ContextConfiguration(classes = { JpaPropertySetDAOImplTestIT.class })
public class JpaPropertySetDAOImplTestIT extends BaseDaoTestIT {

    @Inject
    private IPropertySetDAO propertySetDao;

    @Bean
    public static IPropertySetDAO jpaPropertySetDAOImpl(final EntityManager entityManager) {
        return new JpaPropertySetDAOImpl(entityManager);
    }

    @Test
    public void createPropertySetItemAndSave() {
        final PropertySetItem expected = PropertySetItem.builder("Foo", 1L, "foo.name").build();
        propertySetDao.persist(expected);

        // verify is stored
        Optional<PropertySetItem> actual = propertySetDao.findByKey("Foo", 1L, "foo.name");
        assertTrue(actual.isPresent());
        assertEquals(expected, actual.get());

        actual = propertySetDao.findByKey("Foo", 1L, "foo");
        assertFalse(actual.isPresent());
    }

    @Test
    public void getAllKeys() {
        final String entityName = "Foo";
        final Long entityId = 1L;
        creator().create(entityName, entityId, "foo.name");
        creator().create(entityName, entityId, "foo.login");
        creator().create(entityName, entityId, "foo.firstname");
        final Collection<String> keys = propertySetDao.getKeys(entityName, entityId, null, -1);
        assertEquals(3, keys.size());
    }

    @Test
    public void getAllKeysWithType() {
        final String entityName = "Foo";
        final Long entityId = 1L;
        creator().create(entityName, entityId, "foo.name", PropertySet.STRING);
        creator().create(entityName, entityId, "foo.login", PropertySet.STRING);
        creator().create(entityName, entityId, "foo.firstname", PropertySet.STRING);
        creator().create(entityName, entityId, "foo.activated", PropertySet.BOOLEAN);
        Collection<String> keys = propertySetDao.getKeys(entityName, entityId, null, PropertySet.STRING);
        assertEquals(3, keys.size());

        keys = propertySetDao.getKeys(entityName, entityId, null, PropertySet.BOOLEAN);
        assertEquals(1, keys.size());
    }

    @Test
    public void getAllKeysWithTypeLike() {
        final String entityName = "Foo";
        final Long entityId = 1L;
        creator().create(entityName, entityId, "foo.name", PropertySet.STRING);
        creator().create(entityName, entityId, "foo.login", PropertySet.STRING);
        creator().create(entityName, entityId, "foo.firstname", PropertySet.STRING);
        creator().create(entityName, entityId, "foo.activated", PropertySet.BOOLEAN);
        creator().create(entityName, 2L, "foo.activated", PropertySet.BOOLEAN);

        final Collection<String> keys = propertySetDao.getKeys(entityName, entityId, "foo", PropertySet.STRING);
        assertEquals(3, keys.size());
    }

    @Test
    public void getAllKeysLike() {
        final String entityName = "Foo";
        final Long entityId = 1L;
        creator().create(entityName, entityId, "foo.name", PropertySet.STRING);
        creator().create(entityName, entityId, "foo.login", PropertySet.STRING);
        creator().create(entityName, entityId, "foo.firstname", PropertySet.STRING);
        creator().create(entityName, entityId, "foo.activated", PropertySet.BOOLEAN);
        creator().create(entityName, 2L, "foo.activated", PropertySet.BOOLEAN);

        final Collection<String> keys = propertySetDao.getKeys(entityName, entityId, "foo", -1);
        assertEquals(4, keys.size());
    }

    @Test
    public void findOneByValue() {
        final String entityName = "Foo";
        for (long entityId = 1; entityId < 10; entityId++) {
            creator().create(entityName, entityId, "foo.name", PropertySet.STRING, "value_" + entityId);
        }

        final Optional<PropertySetItem> optional = propertySetDao
                .findOneByValue(entityName, "foo.name", PropertySet.STRING, "value_5");
        assertTrue(optional.isPresent());
        final PropertySetItem key = optional.get();
        assertEquals((long) key.getId().getEntityId(), 5L);

        assertFalse(propertySetDao.findOneByValue(entityName, "foo.name", PropertySet.STRING, "value_0").isPresent());
    }

    @Test
    public void findAllByValue() {
        final String entityName = "Foo";
        for (long entityId = 1; entityId < 10; entityId++) {
            creator().create(entityName, entityId, "foo.name", PropertySet.STRING, "value_" + entityId % 3);
        }

        List<PropertySetItem> keys = propertySetDao
                .findAllByValue(entityName, "foo.name", PropertySet.STRING, "value_2");
        assertNotNull(keys);
        assertEquals(3, keys.size());

        keys = propertySetDao.findAllByValue(entityName, "foo.name", PropertySet.STRING, "value_3");

        assertNotNull(keys);
        assertEquals(0, keys.size());
    }

    @Test
    public void findAllByValueWithPage() {
        final String entityName = "Foo";
        final Pageable pageRequest = PageUtils.newRequest(0, 10);
        for (long entityId = 1; entityId < 10; entityId++) {
            creator().create(entityName, entityId, "foo.name", PropertySet.STRING, "value_" + entityId % 3);
        }

        Page<PropertySetItem> keys = propertySetDao
                .findAllByValue(entityName, "foo.name", PropertySet.STRING, "value_2", pageRequest);
        assertNotNull(keys);
        assertEquals(3, keys.getTotalElements());

        keys = propertySetDao.findAllByValue(entityName, "foo.name", PropertySet.STRING, "value_3", pageRequest);

        assertNotNull(keys);
        assertEquals(0, keys.getTotalElements());
    }

    @Test
    public void removeAllOfEntity() {
        final String entityName = "Foo";
        final Long entityId = 1L;
        creator().create(entityName, entityId, "foo.name", PropertySet.STRING);
        creator().create(entityName, entityId, "foo.login", PropertySet.STRING);
        creator().create(entityName, entityId, "foo.firstname", PropertySet.STRING);
        creator().create(entityName, entityId, "foo.activated", PropertySet.BOOLEAN);
        propertySetDao.remove(entityName, entityId);
        final Collection<String> keys = propertySetDao.getKeys(entityName, entityId, null, -1);
        assertEquals(0, keys.size());
    }

    @Test
    public void removeAllOfEntityByKey() {
        final String entityName = "Foo";
        final Long entityId = 1L;
        creator().create(entityName, entityId, "foo.name", PropertySet.STRING);
        creator().create(entityName, entityId, "foo1.activated", PropertySet.BOOLEAN);
        propertySetDao.remove(entityName, entityId, "foo.name");
        final Collection<String> keys = propertySetDao.getKeys(entityName, entityId, null, -1);
        assertEquals(1, keys.size());
    }

    public Creator creator() {
        return new Creator();
    }

    public class Creator extends PropertySetItem.Builder {

        public PropertySetItem create(final String entityName, final long entityId, final String key) {
            return ((Creator) new Creator().id(entityName, entityId, key)).create();
        }

        public PropertySetItem create(final String entityName, final long entityId, final String key, final int type) {
            return create(entityName, entityId, key, type, null);
        }

        public PropertySetItem create(final String entityName,
            final long entityId,
            final String key,
            final int type,
            final Object value) {
            return ((Creator) new Creator().id(entityName, entityId, key).value(type, value)).create();
        }

        public PropertySetItem create() {
            final PropertySetItem item = build();
            propertySetDao.persist(item);
            return item;
        }
    }
}
