package com.pmi.tpd.euceg.backend.core;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.SmartLifecycle;

import com.pmi.tpd.api.lifecycle.IShutdown;
import com.pmi.tpd.api.lifecycle.IStartable;

public interface IReceiver extends IStartable, IShutdown, SmartLifecycle, DisposableBean {

    void setAutoStartup(final boolean autoStartup);
}
