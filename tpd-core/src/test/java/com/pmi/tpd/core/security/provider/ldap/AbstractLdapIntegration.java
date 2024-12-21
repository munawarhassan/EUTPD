package com.pmi.tpd.core.security.provider.ldap;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.ldap.DefaultSpringSecurityContextSource;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;

import com.pmi.tpd.testing.junit5.TestCase;

/**
 * @author Luke Taylor
 */
@Configuration
@ExtendWith(SpringExtension.class)
@TestExecutionListeners(
        value = { DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class },
        inheritListeners = false)
public abstract class AbstractLdapIntegration extends TestCase {

    private static LdapContextSource contextSource;

    @Bean
    public static LdapServerIntegration ldapServerIntegration(final ApplicationContext context) {
        final LdapServerIntegration ldapServer = new LdapServerIntegration("classpath:ldif/test-apache-server.ldif");
        return ldapServer;
    }

    @Bean
    public static LdapContextSource ldapContextSource(final LdapServerIntegration ldapServer) {
        final int serverPort = ldapServer.getServerPort();
        contextSource = new DefaultSpringSecurityContextSource("ldap://127.0.0.1:" + serverPort);
        return contextSource;
    }

    public static LdapContextSource getContextSource() {
        return contextSource;
    }

}
