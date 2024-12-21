package com.pmi.tpd.api.paging;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;

import java.util.Arrays;
import java.util.Date;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;

import com.pmi.tpd.testing.junit5.TestCase;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.StringPath;

import lombok.Data;

public class DslPagingHelperTest extends TestCase {

    @Test
    public void testCreateLikeIgnoreCaseWithNullArguments() {

        assertThrows(IllegalArgumentException.class, () -> {
            DslPagingHelper.createLikeIgnoreCase(null, "");
        });
        assertThrows(IllegalArgumentException.class, () -> {
            DslPagingHelper.createLikeIgnoreCase(QEntity.entity.action, null);
        });
    }

    @Test
    public void shouldCreateEndsWithIgnoreCaseExpression() {

        final BooleanExpression exp = DslPagingHelper.createLikeIgnoreCase(QEntity.entity.action, "*value");

        assertNotNull(exp);
        assertEquals("endsWithIgnoreCase(entity.action,value)", exp.toString());
    }

    @Test
    public void shouldCreateStartsWithIgnoreCaseExpression() {

        final BooleanExpression exp = DslPagingHelper.createLikeIgnoreCase(QEntity.entity.action, "value*");

        assertNotNull(exp);
        assertEquals("startsWithIgnoreCase(entity.action,value)", exp.toString());
    }

    @Test
    public void shouldCreateContainsIgnoreCaseExpression() {

        BooleanExpression exp = DslPagingHelper.createLikeIgnoreCase(QEntity.entity.action, "*value*");

        assertNotNull(exp);
        assertEquals("containsIc(entity.action,value)", exp.toString());

        exp = DslPagingHelper.createLikeIgnoreCase(QEntity.entity.action, "value");

        assertNotNull(exp);
        assertEquals("containsIc(entity.action,value)", exp.toString());
    }

    @Test
    public void testCreateLikeWithNullArguments() {

        assertThrows(IllegalArgumentException.class, () -> {
            DslPagingHelper.createLike(null, "");
        });
        assertThrows(IllegalArgumentException.class, () -> {
            DslPagingHelper.createLike(QEntity.entity.action, null);
        });
    }

    @Test
    public void shouldCreateEndsExpression() {

        final BooleanExpression exp = DslPagingHelper.createLike(QEntity.entity.action, "*value");

        assertNotNull(exp);
        assertEquals("endsWith(entity.action,value)", exp.toString());
    }

    @Test
    public void shouldCreateStartsExpression() {

        final BooleanExpression exp = DslPagingHelper.createLike(QEntity.entity.action, "value*");

        assertNotNull(exp);
        assertEquals("startsWith(entity.action,value)", exp.toString());
    }

    @Test
    public void shouldCreateContainsExpression() {

        BooleanExpression exp = DslPagingHelper.createLike(QEntity.entity.action, "*value*");

        assertNotNull(exp);
        assertEquals("contains(entity.action,value)", exp.toString());

        exp = DslPagingHelper.createLike(QEntity.entity.action, "value");

        assertNotNull(exp);
        assertEquals("entity.action = value", exp.toString());
    }

    @Test
    public void testCreateEmptyPredicate() {
        BooleanBuilder predicate = DslPagingHelper.createPredicates(PageUtils.newRequest(0, 5), QEntity.entity);
        assertNotNull(predicate);
        assertEquals(false, predicate.hasValue());

        predicate = DslPagingHelper.createPredicates(PageUtils.newRequest(0, 5, Sort.unsorted(), Filters.empty(), null),
            QEntity.entity);
        assertNotNull(predicate);
        assertEquals(false, predicate.hasValue());
    }

    @Test
    public void shouldFailedCreatePredicatesWithWrongProperty() {
        assertThrows(RuntimeException.class,
            () -> DslPagingHelper.createPredicates(
                PageUtils.newRequest(0, 5, Sort.unsorted(), Filter.eq("wrongProperty", "value")),
                QEntity.entity));

    }

    @Test
    public void shouldAddExisitinCriteria() {
        final BooleanBuilder predicate = DslPagingHelper
                .createPredicates(PageUtils.newRequest(0, 5), QEntity.entity, QEntity.entity.action.eq("value"));
        assertEquals(true, predicate.hasValue());
        assertEquals("entity.action = value", predicate.getValue().toString());

    }

    @Test
    public void shouldAddEqualFilter() {
        final BooleanBuilder predicate = DslPagingHelper.createPredicates(
            PageUtils.newRequest(0, 5, Sort.unsorted(), Filter.eq("action", "value")),
            QEntity.entity);
        assertEquals(true, predicate.hasValue());
        assertEquals("entity.action = value", predicate.getValue().toString());
    }

    @Test
    public void shouldAddInFilter() {
        final BooleanBuilder predicate = DslPagingHelper.createPredicates(PageUtils
                .newRequest(0, 5, Sort.unsorted(), Filter.in("action", Arrays.asList("value1", "value2", "value3"))),
            QEntity.entity);
        assertEquals(true, predicate.hasValue());
        assertEquals("entity.action in [value1, value2, value3]", predicate.getValue().toString());
    }

    @Test
    public void shouldAddNotInFilter() {
        final BooleanBuilder predicate = DslPagingHelper.createPredicates(PageUtils
                .newRequest(0, 5, Sort.unsorted(), Filter.notIn("action", Arrays.asList("value1", "value2", "value3"))),
            QEntity.entity);
        assertEquals(true, predicate.hasValue());
        assertEquals("entity.action not in [value1, value2, value3]", predicate.getValue().toString());
    }

    @Test
    public void shouldAddInFilterWithListWithOne() {
        final BooleanBuilder predicate = DslPagingHelper.createPredicates(
            PageUtils.newRequest(0, 5, Sort.unsorted(), Filter.in("action", Arrays.asList("value1"))),
            QEntity.entity);
        assertEquals(true, predicate.hasValue());
        assertEquals("entity.action in [value1]", predicate.getValue().toString());
    }

    @Test
    public void testShouldAddContainFilter() {
        final BooleanBuilder predicate = DslPagingHelper.createPredicates(
            PageUtils.newRequest(0, 5, Sort.unsorted(), Filter.contains("action", "*value")),
            QEntity.entity);
        assertEquals(true, predicate.hasValue());
        assertEquals("endsWithIgnoreCase(entity.action,value)", predicate.getValue().toString());
    }

    @Data
    public static class Entity {

        private String action;

        private Date timestamp;

    }

    public static class QEntity extends EntityPathBase<Entity> {

        private static final long serialVersionUID = -1357315884L;

        public static final QEntity entity = new QEntity("entity");

        public final StringPath action = createString("action");

        public final DateTimePath<java.util.Date> timestamp = createDateTime("timestamp", java.util.Date.class);

        public QEntity(final String variable) {
            super(Entity.class, forVariable(variable));
        }

        public QEntity(final Path<? extends Entity> path) {
            super(path.getType(), path.getMetadata());
        }

        public QEntity(final PathMetadata metadata) {
            super(Entity.class, metadata);
        }

    }

}
