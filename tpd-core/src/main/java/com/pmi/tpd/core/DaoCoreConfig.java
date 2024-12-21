package com.pmi.tpd.core;

import javax.persistence.EntityManager;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.pmi.tpd.core.user.impl.JpaGroupRepository;
import com.pmi.tpd.core.user.impl.JpaUserRepository;
import com.pmi.tpd.core.user.permission.impl.JpaEffectivePermissionRepository;
import com.pmi.tpd.core.user.permission.impl.JpaGlobalPermissionRepository;
import com.pmi.tpd.core.user.permission.spi.IEffectivePermissionRepository;
import com.pmi.tpd.core.user.permission.spi.IGlobalPermissionRepository;
import com.pmi.tpd.core.user.spi.IGroupRepository;
import com.pmi.tpd.core.user.spi.IUserRepository;

/**
 * <p>
 * UserCoreConfig class.
 * </p>
 *
 * @author Christophe Friederich
 * @since 1.0
 */
@Configuration
public class DaoCoreConfig {

    /**
     * @param entityManagerFactory
     * @return
     */
    @Bean
    public IUserRepository userRepository(final EntityManager entityManager) {
        return new JpaUserRepository(entityManager);
    }

    /**
     * @param entityManagerFactory
     * @return
     */
    @Bean
    public IGroupRepository groupRepository(final EntityManager entityManager) {
        return new JpaGroupRepository(entityManager);
    }

    @Bean
    public IEffectivePermissionRepository effectivePermissionRepository(final EntityManager entityManager) {
        return new JpaEffectivePermissionRepository(entityManager);
    }

    @Bean
    public IGlobalPermissionRepository globalPermissionRepository(final EntityManager entityManager) {
        return new JpaGlobalPermissionRepository(entityManager);
    }

}
