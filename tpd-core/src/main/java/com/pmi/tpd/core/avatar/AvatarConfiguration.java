package com.pmi.tpd.core.avatar;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.atlassian.cache.CacheFactory;
import com.atlassian.cache.memory.MemoryCacheManager;
import com.pmi.tpd.api.config.IApplicationConfiguration;
import com.pmi.tpd.api.context.IApplicationProperties;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.api.user.avatar.AvatarSourceType;
import com.pmi.tpd.core.IGlobalApplicationProperties;
import com.pmi.tpd.core.avatar.impl.ConfigurableAvatarService;
import com.pmi.tpd.core.avatar.impl.DiskAvatarRepository;
import com.pmi.tpd.core.avatar.impl.GravatarSource;
import com.pmi.tpd.core.avatar.impl.NavBuilderImpl;
import com.pmi.tpd.core.avatar.impl.OutlookSource;
import com.pmi.tpd.core.avatar.impl.VersionAwareAvatarUrlDecorator;
import com.pmi.tpd.core.avatar.spi.AvatarUrlDecorator;
import com.pmi.tpd.core.avatar.spi.IAvatarRepository;
import com.pmi.tpd.core.avatar.spi.IAvatarSource;
import com.pmi.tpd.core.avatar.spi.IInternalAvatarService;
import com.pmi.tpd.web.core.request.spi.IRequestContext;

@Configuration
public class AvatarConfiguration {

    @Value("${avatar.max.dimension}")
    private int maxDimension;

    @Value("${avatar.max.size}")
    private int maxSize;

    @Value("${avatar.source}")
    private String defaultSource;

    @Bean
    public INavBuilder navBuilder(final IApplicationProperties applicationProperties) {
        return new NavBuilderImpl(applicationProperties);
    }

    @Bean
    public IAvatarRepository avatarRepository(final I18nService i18nService,
        final IApplicationConfiguration applicationConfiguration,
        final IGlobalApplicationProperties propertiesService) {
        final DiskAvatarRepository repository = new DiskAvatarRepository(i18nService, applicationConfiguration,
                propertiesService);
        repository.setMaxDimension(maxDimension);
        repository.setMaxSize(maxSize);
        return repository;
    }

    @Bean
    public CacheFactory cacheFactory() {
        return new MemoryCacheManager();
    }

    @Bean
    public AvatarUrlDecorator avatarUrlDecorator(final IAvatarRepository repository, final CacheFactory cacheFactory) {
        return new VersionAwareAvatarUrlDecorator(repository, cacheFactory);
    }

    @Bean("gravatarSource")
    public IAvatarSource gravatarSource(@Value("${avatar.gravatar.format.http}") final String httpUrlFormat,
        @Value("${avatar.gravatar.format.https}") final String httpsUrlFormat,
        @Value("${avatar.gravatar.default}") final String defaultFallbackUrl) {
        return new GravatarSource(httpUrlFormat, httpsUrlFormat, defaultFallbackUrl);
    }

    @Bean("outlookSource")
    public IAvatarSource outlookSource(@Value("${avatar.outlook.format.https}") final String httpsUrlFormat) {
        return new OutlookSource(httpsUrlFormat);
    }

    @Bean
    public IInternalAvatarService avatarService(final I18nService i18nService,
        final INavBuilder navBuilder,
        final IApplicationProperties applicationProperties,
        final IAvatarRepository repository,
        final AvatarUrlDecorator urlDecorator,
        final List<IAvatarSource> sources,
        final IRequestContext requestContext) {
        final ConfigurableAvatarService avatarService = new ConfigurableAvatarService(i18nService, navBuilder,
                applicationProperties, repository, urlDecorator, sources, requestContext);
        avatarService.setDefaultSource(AvatarSourceType.valueOf(this.defaultSource));
        return avatarService;
    }
}
