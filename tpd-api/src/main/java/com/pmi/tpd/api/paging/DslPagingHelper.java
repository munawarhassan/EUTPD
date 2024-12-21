package com.pmi.tpd.api.paging;

import static com.pmi.tpd.api.util.Assert.checkNotNull;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.mapping.PropertyPath;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.pmi.tpd.api.paging.Filter.Cardinality;
import com.pmi.tpd.api.paging.Filter.Operator;
import com.pmi.tpd.api.query.DefaultArgumentParser;
import com.pmi.tpd.api.query.IArgumentParser;
import com.pmi.tpd.api.util.Assert;
import com.querydsl.collections.CollQuery;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.OrderSpecifier.NullHandling;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.ComparableExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.ListPath;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.core.types.dsl.TemporalExpression;

/**
 * <p>
 * DslPagingHelper class.
 * </p>
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public final class DslPagingHelper extends PageUtils {

    private DslPagingHelper() {
    }

    /**
     * <p>
     * createLikeIgnore.
     * </p>
     *
     * @param stringPath
     *                   a {@link com.mysema.query.types.path.StringPath} object.
     * @param filter
     *                   a {@link java.lang.String} object.
     * @return a {@link com.mysema.query.types.expr.BooleanExpression} object.
     */
    @Nonnull
    public static BooleanExpression createLikeIgnoreCase(@Nonnull final StringPath stringPath,
        @Nonnull final String filter) {
        checkNotNull(stringPath, "stringPath");
        String param = checkNotNull(filter, "filter");
        BooleanExpression path = null;
        boolean start = false, end = false;
        if (param.charAt(0) == '*') {
            param = param.substring(1);
            start = true;
        }
        if (param.length() > 0 && param.charAt(param.length() - 1) == '*') {
            param = param.substring(0, param.length() - 1);
            end = true;
        }
        if (start && end) {
            path = stringPath.containsIgnoreCase(param);
        } else {
            if (start) {
                path = stringPath.endsWithIgnoreCase(param);
            }
            if (end) {
                path = stringPath.startsWithIgnoreCase(param);
            }
        }
        if (path == null) {
            return stringPath.containsIgnoreCase(param);
        }
        return path;
    }

    /**
     * <p>
     * createLike.
     * </p>
     *
     * @param stringPath
     *                   a {@link com.mysema.query.types.path.StringPath} object.
     * @param filter
     *                   a {@link java.lang.String} object.
     * @return a {@link com.mysema.query.types.expr.BooleanExpression} object.
     */
    @Nonnull
    public static BooleanExpression createLike(@Nonnull final StringPath stringPath, @Nonnull final String filter) {
        checkNotNull(stringPath, "stringPath");
        String param = checkNotNull(filter, "filter");
        BooleanExpression path = null;
        boolean start = false, end = false;
        if (param.charAt(0) == '*') {
            param = param.substring(1);
            start = true;
        }
        if (param.length() > 0 && param.charAt(param.length() - 1) == '*') {
            param = param.substring(0, param.length() - 1);
            end = true;
        }
        if (start && end) {
            path = stringPath.contains(param);
        } else {
            if (start) {
                path = stringPath.endsWith(param);
            }
            if (end) {
                path = stringPath.startsWith(param);
            }
        }
        if (path == null) {
            path = stringPath.eq(param);
        }
        return path;
    }

    /**
     * @param request
     * @param entity
     * @param predicate
     * @return
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Nonnull
    public static BooleanBuilder createPredicates(@Nonnull final Pageable request,
        @Nonnull final EntityPath<?> entity,
        @Nullable final Predicate... criterion) {
        checkNotNull(request, "request");
        final IArgumentParser argumentParser = new DefaultArgumentParser();
        final BooleanBuilder predicates = new BooleanBuilder();
        if (criterion != null) {
            predicates.and(ExpressionUtils.allOf(criterion));
        }
        if (request instanceof IFilterable) {
            final IFilterable filterable = (IFilterable) request;
            filterable.getFilterMap().values().forEach(filterList -> {
                final List<Predicate> propertyPredicate = Lists.newArrayList();
                for (final Filter<?> filter : filterList) {

                    final String property = filter.getProperty();
                    final Operator op = filter.getOperator();
                    final Path<?> path = getPath(entity, property);
                    if (!filter.getValue().isPresent() && path instanceof SimpleExpression) {
                        predicates.and(((SimpleExpression) path).isNull());
                    } else {
                        final Object filterValue = filter.size() == 1 && Cardinality.unary.equals(op.getCardinality())
                                ? filter.getValue().get() : filter.getValues();
                        Object val = null;
                        if (filterValue instanceof Collection) {
                            val = ((Collection) filterValue).stream()
                                    .map(e -> argumentParser.parse(e.toString(), path.getType()))
                                    .collect(Collectors.toList());
                        } else {
                            final String valString = filter.getValue().get().toString();
                            val = argumentParser.parse(valString, path.getType());
                        }
                        switch (op) {
                            case contains:
                                if (path instanceof StringPath) {
                                    propertyPredicate
                                            .add(DslPagingHelper.createLikeIgnoreCase((StringPath) path, (String) val));
                                } else {
                                    if (path instanceof ComparableExpression) {
                                        propertyPredicate.add(((ComparableExpression) path).eq(val));
                                    }
                                }
                                break;
                            case start:
                                if (path instanceof StringPath) {
                                    final StringPath stringPath = (StringPath) path;
                                    propertyPredicate.add(stringPath.startsWithIgnoreCase((String) val));
                                }
                                break;
                            case end:
                                if (path instanceof StringPath) {
                                    final StringPath stringPath = (StringPath) path;
                                    propertyPredicate.add(stringPath.endsWithIgnoreCase((String) val));
                                }
                                break;
                            case gte:
                                if (path instanceof ComparableExpression) {
                                    propertyPredicate.add(((ComparableExpression) path).goe(Expressions.constant(val)));
                                }
                                break;
                            case lte:
                                if (path instanceof ComparableExpression) {
                                    propertyPredicate.add(((ComparableExpression) path).loe(Expressions.constant(val)));
                                }
                                break;
                            case gt:
                                if (path instanceof ComparableExpression) {
                                    propertyPredicate.add(((ComparableExpression) path).gt(Expressions.constant(val)));
                                }
                                break;
                            case lt:
                                if (path instanceof ComparableExpression) {
                                    propertyPredicate.add(((ComparableExpression) path).lt(Expressions.constant(val)));
                                }
                                break;
                            case between:
                                if (path instanceof ComparableExpression && val instanceof List
                                        && ((List) val).size() == 2) {
                                    final List<?> vals = (List) val;
                                    propertyPredicate.add(
                                        ((ComparableExpression) path).between(Expressions.constant(vals.get(0)),
                                            Expressions.constant(vals.get(1))));
                                }
                                break;
                            case before:
                                if (path instanceof TemporalExpression) {
                                    final TemporalExpression datePath = (TemporalExpression) path;
                                    propertyPredicate.add(datePath.before(Expressions.constant(val)));
                                }
                                break;
                            case after:
                                if (path instanceof TemporalExpression) {
                                    final TemporalExpression datePath = (TemporalExpression) path;
                                    propertyPredicate.add(datePath.after(Expressions.constant(val)));
                                }
                                break;
                            case noteq: {
                                if (path instanceof SimpleExpression) {
                                    propertyPredicate.add(((SimpleExpression) path).ne(Expressions.constant(val)));
                                }
                                break;
                            }
                            case in: {
                                if (path instanceof SimpleExpression) {
                                    propertyPredicate.add(((SimpleExpression) path).in(Expressions.constant(val)));
                                }
                                break;
                            }
                            case notin: {
                                if (path instanceof SimpleExpression) {
                                    propertyPredicate.add(((SimpleExpression) path).notIn(Expressions.constant(val)));
                                }
                                break;
                            }
                            case eq:
                            default: {
                                if (path instanceof SimpleExpression) {
                                    propertyPredicate.add(((SimpleExpression) path).eq(Expressions.constant(val)));
                                }
                                break;
                            }
                        }
                    }
                }
                predicates.andAnyOf(FluentIterable.from(propertyPredicate).toArray(Predicate.class));
            });
        }
        return predicates;
    }

    public static <T> CollQuery<T> applyPagination(final Pageable pageable,
        final PathBuilder<?> pathBuilder,
        final CollQuery<T> query) {

        if (pageable == null) {
            return query;
        }

        query.offset(pageable.getOffset());
        query.limit(pageable.getPageSize());

        return applySorting(pageable.getSort(), pathBuilder, query);
    }

    /**
     * Applies sorting to the given {@link CollQuery}.
     *
     * @param sort
     * @param query
     *              must not be {@literal null}.
     * @return the Querydsl {@link CollQuery}
     */
    public static <T> CollQuery<T> applySorting(final Sort sort,
        final PathBuilder<?> pathBuilder,
        final CollQuery<T> query) {

        if (sort == null) {
            return query;
        }
        return addOrderByFrom(sort, pathBuilder, query);
    }

    /**
     * @param entity
     * @param property
     * @return
     */
    @SuppressWarnings("unchecked")
    private static <E extends Comparable<E>> Path<E> getPath(final EntityPath<?> entity, final String property) {
        Field field;
        try {
            final String[] ar = property.split("\\.");
            Class<?> cl = entity.getClass();
            Object path = entity;
            for (final String prop : ar) {
                field = cl.getField(prop);
                final Object value = field.get(path);
                if (value instanceof ListPath) {
                    path = ((ListPath) value).any();
                } else {
                    path = value;
                }
                cl = path.getClass();
            }
            return (Path<E>) path;
        } catch (NoSuchFieldException | IllegalArgumentException e) {
            throw new RuntimeException("Property '" + property + "' doesn't exist.", e);
        } catch (final IllegalAccessException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

    }

    /**
     * Converts the {@link Order} items of the given {@link Sort} into {@link OrderSpecifier} and attaches those to the
     * given {@link CollQuery}.
     *
     * @param sort
     *              must not be {@literal null}.
     * @param query
     *              must not be {@literal null}.
     * @return
     */
    private static <T> CollQuery<T> addOrderByFrom(final Sort sort,
        final PathBuilder<?> pathBuilder,
        final CollQuery<T> query) {

        Assert.notNull(sort, "Sort must not be null!");
        Assert.notNull(query, "Query must not be null!");

        for (final Order order : sort) {
            query.orderBy(toOrderSpecifier(order, pathBuilder));
        }

        return query;
    }

    /**
     * Transforms a plain {@link Order} into a QueryDsl specific {@link OrderSpecifier}.
     *
     * @param order
     *              must not be {@literal null}.
     * @return
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static OrderSpecifier<?> toOrderSpecifier(final Order order, final PathBuilder<?> pathBuilder) {

        return new OrderSpecifier(
                order.isAscending() ? com.querydsl.core.types.Order.ASC : com.querydsl.core.types.Order.DESC,
                buildOrderPropertyPathFrom(order, pathBuilder), toQueryDslNullHandling(order.getNullHandling()));
    }

    /**
     * Converts the given {@link org.springframework.data.domain.Sort.NullHandling} to the appropriate Querydsl
     * {@link NullHandling}.
     *
     * @param nullHandling
     *                     must not be {@literal null}.
     * @return
     * @since 1.6
     */
    private static NullHandling toQueryDslNullHandling(
        final org.springframework.data.domain.Sort.NullHandling nullHandling) {

        Assert.notNull(nullHandling, "NullHandling must not be null!");

        switch (nullHandling) {

            case NULLS_FIRST:
                return NullHandling.NullsFirst;

            case NULLS_LAST:
                return NullHandling.NullsLast;

            case NATIVE:
            default:
                return NullHandling.Default;
        }
    }

    /**
     * Creates an {@link Expression} for the given {@link Order} property.
     *
     * @param order
     *              must not be {@literal null}.
     * @return
     */
    private static Expression<?> buildOrderPropertyPathFrom(final Order order, final PathBuilder<?> pathBuilder) {

        Assert.notNull(order, "Order must not be null!");

        PropertyPath path = PropertyPath.from(order.getProperty(), pathBuilder.getType());
        Expression<?> sortPropertyExpression = pathBuilder;

        while (path != null) {

            if (!path.hasNext() && order.isIgnoreCase()) {
                // if order is ignore-case we have to treat the last path segment as a String.
                sortPropertyExpression = Expressions.stringPath((Path<?>) sortPropertyExpression, path.getSegment())
                        .lower();
            } else {
                sortPropertyExpression = Expressions
                        .path(path.getType(), (Path<?>) sortPropertyExpression, path.getSegment());
            }

            path = path.next();
        }

        return sortPropertyExpression;
    }
}
