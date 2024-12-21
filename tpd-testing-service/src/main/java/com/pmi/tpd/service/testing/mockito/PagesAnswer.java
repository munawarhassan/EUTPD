package com.pmi.tpd.service.testing.mockito;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.function.Function;

import javax.annotation.Nonnull;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.pmi.tpd.api.paging.PageUtils;

/**
 * {@link Answer} implementation that generates pages of elements in accordance
 * with provided {@code PageRequest} up to
 * a specified total number of elements.
 * <p>
 * Example usage:
 *
 * <pre>
 * {@code private static final Function<Integer, Foo> FOO_FACTORY = new Function<Integer, Foo> {
 *     &#64;Override
 *     public Foo apply(Integer position) {
 *         return new MockFoo(position);
 *     }
 * }
 *
 * // return up to 500 Foo objects, using FOO_FACTORY to create mock Foo objects
 * when(fooService.findAllFoos(eq("foo"), any(PageRequest.class))).thenAnswer(withPagesUpTo(500, FOO_FACTORY));
 * }
 * </pre>
 * <p>
 * For best results, it should not be used with other answers/returns (for the
 * particular method invocation being
 * stubbed).
 *
 * @since 2.0
 */
public class PagesAnswer<T> implements Answer<Page<T>> {

  private final long elementCount;

  private final Function<Long, T> generator;

  private PagesAnswer(final int elementCount, final Function<Long, T> generator) {
    this.elementCount = elementCount;
    this.generator = restrictedElementGenerator(checkNotNull(generator, "generator"));
  }

  @Nonnull
  public static <T> PagesAnswer<T> withPagesUpTo(final int maxElementCount,
      @Nonnull final Function<Long, T> generator) {
    checkArgument(maxElementCount > 0, "maxElementCount must be >0");

    return new PagesAnswer<>(maxElementCount, generator);
  }

  @Nonnull
  @Override
  public Page<T> answer(@Nonnull final InvocationOnMock invocation) throws Throwable {
    final Pageable request = PageAnswer.findPageRequest(invocation);

    return PageUtils.createPage(request, generator, elementCount);
  }

  private Function<Long, T> restrictedElementGenerator(final Function<Long, T> original) {
    return i -> {
      if (i >= elementCount) {
        return null;
      }

      return original.apply(i);
    };
  }
}
