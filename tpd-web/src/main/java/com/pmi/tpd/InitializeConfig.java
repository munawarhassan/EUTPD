package com.pmi.tpd;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;
import org.springframework.web.servlet.LocaleResolver;
import org.thymeleaf.spring4.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;

import com.pmi.tpd.api.ApplicationConstants;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.locale.AngularCookieLocaleResolver;

@Configuration
public class InitializeConfig {

    /**
     * @return
     */
    @Bean(name = "localeResolver")
    public LocaleResolver localeResolver() {
        final AngularCookieLocaleResolver cookieLocaleResolver = new AngularCookieLocaleResolver();
        cookieLocaleResolver.setCookieName("NG_TRANSLATE_LANG_KEY");
        return cookieLocaleResolver;
    }

    /**
     * @return
     */
    @Bean
    @Description("Thymeleaf template resolver serving HTML 5")
    public ITemplateResolver templateResolver() {
        final ClassLoaderTemplateResolver emailTemplateResolver = new ClassLoaderTemplateResolver();
        emailTemplateResolver.setPrefix("thymeleaf/");
        emailTemplateResolver.setSuffix(".html");
        emailTemplateResolver.setTemplateMode("HTML");
        emailTemplateResolver.setCharacterEncoding(ApplicationConstants.getDefaultCharset().name());
        emailTemplateResolver.setOrder(1);
        return emailTemplateResolver;
    }

    @Bean
    public SpringTemplateEngine templateEngine(final I18nService i18nService,
        final ITemplateResolver templateResolver) {
        final SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.setTemplateEngineMessageSource(i18nService);
        templateEngine.setTemplateResolver(templateResolver);
        return templateEngine;
    }

}
