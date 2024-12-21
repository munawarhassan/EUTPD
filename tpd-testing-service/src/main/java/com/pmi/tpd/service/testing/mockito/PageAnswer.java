package com.pmi.tpd.service.testing.mockito;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Arrays.asList;

import java.util.Collections;

import javax.annotation.Nonnull;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.google.common.collect.Iterables;
import com.pmi.tpd.api.paging.PageUtils;

/**
 * Useful for stubbing methods that accept a {@link Pageable} and return a
 * {@link Page} of elements. This answer will
 * collect the original page request from invocation and use it to construct the
 * page in response. The first argument in
 * invocation that is of type {@link Pageable} will be used.:
 *
 * @since 2.0
 */
public class PageAnswer<E> implements Answer<Page<E>> {

  private final Iterable<E> answer;

  private final long totalEmements;

  private PageAnswer(final Iterable<E> answer, final long totalEmements) {
    this.answer = checkNotNull(answer, "answer");
    this.totalEmements = totalEmements;
  }

  @Nonnull
  public static <F> PageAnswer<F> withPageOf(@Nonnull final Iterable<F> elements) {
    return new PageAnswer<>(elements, Iterables.size(elements));
  }

  @Nonnull
  @SafeVarargs
  public static <F> PageAnswer<F> withPageOf(@Nonnull final F... elements) {
    return withPageOf(asList(elements));
  }

  @Nonnull
  public static <F> PageAnswer<F> withPageOf(final Long totalEmements, @Nonnull final Iterable<F> elements) {
    return new PageAnswer<>(elements, totalEmements);
  }

  @Nonnull
  @SafeVarargs
  public static <F> PageAnswer<F> withPageOf(final Long totalEmements, @Nonnull final F... elements) {
    return withPageOf(totalEmements, asList(elements));
  }

  @Nonnull
  public static <F> PageAnswer<F> withEmptyPageOf(@Nonnull final Class<F> elementType) {
    checkNotNull(elementType);
    return new PageAnswer<>(Collections.<F>emptyList(), 0);
  }

  @Nonnull
  @Override
  public Page<E> answer(final InvocationOnMock invocation) throws Throwable {
    return PageUtils.createPage(answer, findPageRequest(invocation), totalEmements);
  }

  /**
   * @param elements
   *                 elements in the page
   * @param <E>
   *                 component type
   * @return answer returning a page of elements
   * @since 2.0
   * @see PageAnswer
   * @deprecated use {@link #withPageOf(Object...)}
   */
  @Deprecated
  public static <E> Answer<Page<E>> returnPageOf(@Nonnull final Iterable<E> elements) {
    return withPageOf(elements);
  }

  static Pageable findPageRequest(final InvocationOnMock invocation) {
    for (final Object arg : invocation.getArguments()) {
      if (arg instanceof Pageable) {
        return (Pageable) arg;
      }
    }
    throw new IllegalStateException(
        "This answer must be used only for invocations containing PageRequest, was: " + invocation);
  }
}
