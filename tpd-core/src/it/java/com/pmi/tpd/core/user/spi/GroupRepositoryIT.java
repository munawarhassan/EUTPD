package com.pmi.tpd.core.user.spi;

import static com.pmi.tpd.api.paging.PageUtils.newRequest;

import java.util.Iterator;

import javax.inject.Inject;

import org.joda.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ContextConfiguration;

import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.pmi.tpd.api.paging.Filter;
import com.pmi.tpd.api.paging.Filters;
import com.pmi.tpd.core.BaseDaoTestIT;
import com.pmi.tpd.core.DaoCoreConfig;
import com.pmi.tpd.core.DataSets;
import com.pmi.tpd.core.model.user.GroupEntity;

@Configuration
@ContextConfiguration(classes = { DaoCoreConfig.class, GroupRepositoryIT.class })
@DatabaseSetup(value = { DataSets.USERS })
public class GroupRepositoryIT extends BaseDaoTestIT {

    @Inject
    private IGroupRepository repository;

    @Test
    public void testFindByName() {
        final GroupEntity group = repository.findByName("grp-user");
        assertNotNull(group);
    }

    @Test
    public void testFindByNameEmptyOrNull() {
        assertNull(repository.findByName("\n\t"));
    }

    @Test
    public void testfindGroups() {
        final Page<GroupEntity> page = repository.findGroups(newRequest(0, 10));
        assertNotNull(page);
        assertEquals(5, page.getNumberOfElements());
    }

    @Test
    public void testFindGroupsWithFiltering() {
        final Page<GroupEntity> page = repository
                .findGroups(newRequest(0, 10, (Sort) null, new Filters(Filter.contains("name", "grp*")), null));
        assertNotNull(page);
        assertEquals(3, page.getNumberOfElements());

        final Iterator<GroupEntity> groups = page.getContent().iterator();
        assertEquals(1, groups.next().getId().longValue());
        assertEquals(2, groups.next().getId().longValue());
        assertEquals(3, groups.next().getId().longValue());
        assertFalse(groups.hasNext());
    }

    @Test
    public void testFindGroupsWithQuery() {
        final Page<GroupEntity> page = repository.findGroups(newRequest(0, 10, (Sort) null, null, "grp*"));
        assertNotNull(page);
        assertEquals(3, page.getNumberOfElements());

        final Iterator<GroupEntity> groups = page.getContent().iterator();
        assertEquals(1, groups.next().getId().longValue());
        assertEquals(2, groups.next().getId().longValue());
        assertEquals(3, groups.next().getId().longValue());
        assertFalse(groups.hasNext());
    }

    @Test
    public void testFindGroupsByName() {
        final Page<GroupEntity> page = repository.findGroupsByName("grp", newRequest(0, 10));
        assertNotNull(page);
        assertEquals(3, page.getNumberOfElements());

        final Iterator<GroupEntity> groups = page.getContent().iterator();
        assertEquals(1, groups.next().getId().longValue());
        assertEquals(2, groups.next().getId().longValue());
        assertEquals(3, groups.next().getId().longValue());
        assertFalse(groups.hasNext());
    }

    @Test
    public void testFindGroupsByNameWithEmptyName() {
        final Page<GroupEntity> page = repository.findGroupsByName("", newRequest(0, 10));
        assertNotNull(page);
        // return all
        assertEquals(5, page.getNumberOfElements());

    }

    @Test
    public void testFindGroupsByUser() {
        final Page<GroupEntity> page = repository.findGroupsByUser("admin", "grp", newRequest(0, 10));
        assertNotNull(page);
        assertEquals(2, page.getNumberOfElements());

        final Iterator<GroupEntity> groups = page.getContent().iterator();
        assertEquals(2, groups.next().getId().longValue());
        assertEquals(3, groups.next().getId().longValue());
        assertFalse(groups.hasNext());
    }

    @Test
    public void testFindGroupsByUserWithEmptyGroupName() {
        final Page<GroupEntity> page = repository.findGroupsByUser("admin", "", newRequest(0, 10));
        assertNotNull(page);
        assertEquals(2, page.getNumberOfElements());

        final Iterator<GroupEntity> groups = page.getContent().iterator();
        assertEquals(2, groups.next().getId().longValue());
        assertEquals(3, groups.next().getId().longValue());
        assertFalse(groups.hasNext());
    }

    @Test
    public void testFindGroupsWithoutUser() {
        final Page<GroupEntity> page = repository.findGroupsWithoutUser("admin", "grp-", newRequest(0, 10));
        assertNotNull(page);
        assertEquals(1, page.getNumberOfElements());

        final Iterator<GroupEntity> groups = page.getContent().iterator();
        assertEquals(1, groups.next().getId().longValue());
        assertFalse(groups.hasNext());
    }

    @Test
    public void testFindDeletedGroupByName() throws Exception {
        final GroupEntity deletedGroup = repository.findByName("test-group-to-delete");

        assertNotNull(deletedGroup);
        assertEquals(10L, deletedGroup.getId().longValue());
        assertEquals("test-group-to-delete", deletedGroup.getName());
        assertEquals(Instant.parse("2015-04-07T12:00:00.000").toDate(), deletedGroup.getDeletedDate());
    }

    @Test
    public void testFindDeletedGroupByNameCaseInsensitive() throws Exception {
        final GroupEntity deletedGroup = repository.findByName("TeSt-GrOuP-tO-dElEtE");

        assertNotNull(deletedGroup);
        assertEquals(10L, deletedGroup.getId().longValue());
        assertEquals("test-group-to-delete", deletedGroup.getName());
        assertEquals(Instant.parse("2015-04-07T12:00:00.000").toDate(), deletedGroup.getDeletedDate());
    }

    @Test
    public void testFindByDeletedDateEarlierThan() throws Exception {
        final Pageable request = newRequest(0, 100);

        final Page<GroupEntity> allDeletedGroups = repository.findAll(request);
        assertEquals(5, allDeletedGroups.getTotalElements());

        final Page<GroupEntity> deletedGroups = repository
                .findByDeletedDateEarlierThan(Instant.parse("2015-04-08T12:00:00").toDate(), request);

        assertEquals(1, deletedGroups.getTotalElements());

        final GroupEntity deletedGroup = deletedGroups.getContent().iterator().next();
        assertNotNull(deletedGroup);
        assertEquals("test-group-to-delete", deletedGroup.getName());
    }

}
