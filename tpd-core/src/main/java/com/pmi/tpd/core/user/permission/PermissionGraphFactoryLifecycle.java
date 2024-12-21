package com.pmi.tpd.core.user.permission;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.Lifecycle;
import org.springframework.stereotype.Component;

/**
 * Controls the lifecycle of the {@link CachingPermissionGraphFactory}. This is used instead of a PostConstruct as
 * hazelcast requires a fully initialised spring context to avoid deadlocking
 */
@Component
public class PermissionGraphFactoryLifecycle implements Lifecycle {

    private final CachingPermissionGraphFactory permissionGraphFactory;

    private volatile boolean running;

    @Autowired
    public PermissionGraphFactoryLifecycle(final CachingPermissionGraphFactory permissionGraphFactory) {
        this.permissionGraphFactory = permissionGraphFactory;
    }

    @Override
    public void start() {
        permissionGraphFactory.warmCaches();
        running = true;
    }

    @Override
    public void stop() {
        running = false;
    }

    @Override
    public boolean isRunning() {
        return running;
    }

}