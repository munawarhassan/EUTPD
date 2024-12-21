package com.pmi.tpd.core;

import javax.annotation.PostConstruct;

import com.pmi.tpd.api.lifecycle.IStartable;

public class AutoStarter {

    private final IStartable startable;

    public AutoStarter(final IStartable startable) {
        this.startable = startable;
    }

    @PostConstruct
    public void start() throws Exception {
        startable.start();
    }
}
