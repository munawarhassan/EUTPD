package com.pmi.tpd.core.avatar.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.atlassian.cache.CacheFactory;
import com.atlassian.cache.memory.MemoryCacheManager;
import com.pmi.tpd.api.user.IUser;
import com.pmi.tpd.core.avatar.INavBuilder;
import com.pmi.tpd.core.avatar.spi.AvatarType;
import com.pmi.tpd.core.avatar.spi.IAvatarRepository;
import com.pmi.tpd.testing.junit5.MockitoTestCase;

public class VersionAwareAvatarUrlDecoratorTest extends MockitoTestCase {

    private IAvatarRepository repository;

    private CacheFactory cacheFactory;

    private INavBuilder.Builder<?> builder;

    private final long versionId = 1234567890L;

    @BeforeEach
    public void setUp() throws Exception {
        repository = mock(IAvatarRepository.class);
        cacheFactory = new MemoryCacheManager();
        builder = mock(INavBuilder.Builder.class);
    }

    @Test
    public void testDecorateUser() throws Exception {
        final IUser user = mock(IUser.class);
        when(user.getId()).thenReturn(10L);
        when(repository.getVersionId(AvatarType.USER, 10L)).thenReturn(versionId);

        final VersionAwareAvatarUrlDecorator decorator = new VersionAwareAvatarUrlDecorator(repository, cacheFactory);
        decorator.decorate(builder, user);
        verify(builder).withParam(eq("v"), eq(Long.toString(versionId)));
        verifyNoMoreInteractions(builder);

        // test cache invalidation
        decorator.invalidate(user);
        final long newVersionId = versionId + 1000;
        when(repository.getVersionId(AvatarType.USER, 10L)).thenReturn(newVersionId);
        decorator.decorate(builder, user);
        verify(builder).withParam(eq("v"), eq(Long.toString(newVersionId)));
    }

}
