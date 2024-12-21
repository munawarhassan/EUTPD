/*
 * This code was written by Bear Giles <bgiles@coyotesong.com> and he
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Any contributions made by others are licensed to this project under
 * one or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 * Copyright (c) 2013 Bear Giles <bgiles@coyotesong.com>
 */
package com.pmi.tpd.database.hibernate;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.persistence.Entity;
import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;

import org.hibernate.HibernateException;
import org.hibernate.event.spi.DeleteEvent;
import org.hibernate.event.spi.DeleteEventListener;
import org.hibernate.event.spi.MergeEvent;
import org.hibernate.event.spi.MergeEventListener;
import org.hibernate.event.spi.PersistEvent;
import org.hibernate.event.spi.PersistEventListener;
import org.hibernate.event.spi.PostCommitDeleteEventListener;
import org.hibernate.event.spi.PostCommitInsertEventListener;
import org.hibernate.event.spi.PostCommitUpdateEventListener;
import org.hibernate.event.spi.PostDeleteEvent;
import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.event.spi.PostLoadEvent;
import org.hibernate.event.spi.PostLoadEventListener;
import org.hibernate.event.spi.PostUpdateEvent;
import org.hibernate.event.spi.PreDeleteEvent;
import org.hibernate.event.spi.PreDeleteEventListener;
import org.hibernate.event.spi.PreInsertEvent;
import org.hibernate.event.spi.PreInsertEventListener;
import org.hibernate.event.spi.PreUpdateEvent;
import org.hibernate.event.spi.PreUpdateEventListener;
import org.hibernate.event.spi.SaveOrUpdateEvent;
import org.hibernate.event.spi.SaveOrUpdateEventListener;
import org.hibernate.persister.entity.EntityPersister;

import com.google.common.collect.Lists;

/**
 * Adapter that allows a Hibernate event listener to call a standard JPA
 * EntityListener. For simplicity only a single bean of each class is supported.
 * It is not difficult to support multiple beans, just messy. Each listener can
 * have multiple methods with the same annotation.
 *
 * @author Bear Giles <bgiles@coyotesong.com>
 * @author Christophe Friederich
 */
public class HibernateEntityListenersAdapter
        implements PreInsertEventListener, PreUpdateEventListener, PersistEventListener, MergeEventListener,
        SaveOrUpdateEventListener, DeleteEventListener, PreDeleteEventListener, PostLoadEventListener,
        PostCommitInsertEventListener, PostCommitUpdateEventListener, PostCommitDeleteEventListener {

    /** */
    private static final long serialVersionUID = 1L;

    /** */
    private final List<Object> listeners = Lists.newArrayList();

    /** */
    private final Map<Class<?>, Map<Method, Object>> preInsert = new LinkedHashMap<>();

    /** */
    private final Map<Class<?>, Map<Method, Object>> postInsert = new LinkedHashMap<>();

    /** */
    private final Map<Class<?>, Map<Method, Object>> preUpdate = new LinkedHashMap<>();

    /** */
    private final Map<Class<?>, Map<Method, Object>> postUpdate = new LinkedHashMap<>();

    /** */
    private final Map<Class<?>, Map<Method, Object>> preRemove = new LinkedHashMap<>();

    /** */
    private final Map<Class<?>, Map<Method, Object>> postRemove = new LinkedHashMap<>();

    /** */
    private final Map<Class<?>, Map<Method, Object>> postLoad = new LinkedHashMap<>();

    /**
     * Default constructor.
     */
    public HibernateEntityListenersAdapter() {
    }

    /**
     * Constructor taking arguments.
     */
    public HibernateEntityListenersAdapter(final List<Object> listeners) {
        addListeners(listeners);
    }

    @PostConstruct
    public void findMethods() {
        for (final Object listener : listeners) {
            findMethodsForListener(listener);
        }

    }

    public void addListeners(final Iterable<Object> listeners) {
        for (final Object obj : listeners) {
            addListener(obj);
        }
    }

    public void addListener(final Object listener) {
        this.listeners.add(listener);
        findMethodsForListener(listener);
    }

    public void findMethodsForListener(final Object listener) {
        final Class<?> c = listener.getClass();
        for (final Method m : c.getMethods()) {
            if (Void.TYPE.equals(m.getReturnType())) {
                final Class<?>[] types = m.getParameterTypes();
                if (types.length == 1) {
                    // check for all annotations now...
                    if (m.getAnnotation(PrePersist.class) != null) {
                        if (!preInsert.containsKey(types[0])) {
                            preInsert.put(types[0], new LinkedHashMap<Method, Object>());
                        }
                        preInsert.get(types[0]).put(m, listener);
                    }

                    if (m.getAnnotation(PostPersist.class) != null) {
                        if (!postInsert.containsKey(types[0])) {
                            postInsert.put(types[0], new LinkedHashMap<Method, Object>());
                        }
                        postInsert.get(types[0]).put(m, listener);
                    }

                    if (m.getAnnotation(PreUpdate.class) != null) {
                        if (!preUpdate.containsKey(types[0])) {
                            preUpdate.put(types[0], new LinkedHashMap<Method, Object>());
                        }
                        preUpdate.get(types[0]).put(m, listener);
                    }

                    if (m.getAnnotation(PostUpdate.class) != null) {
                        if (!postUpdate.containsKey(types[0])) {
                            postUpdate.put(types[0], new LinkedHashMap<Method, Object>());
                        }
                        postUpdate.get(types[0]).put(m, listener);
                    }

                    if (m.getAnnotation(PreRemove.class) != null) {
                        if (!preRemove.containsKey(types[0])) {
                            preRemove.put(types[0], new LinkedHashMap<Method, Object>());
                        }
                        preRemove.get(types[0]).put(m, listener);
                    }

                    if (m.getAnnotation(PostRemove.class) != null) {
                        if (!postRemove.containsKey(types[0])) {
                            postRemove.put(types[0], new LinkedHashMap<Method, Object>());
                        }
                        postRemove.get(types[0]).put(m, listener);
                    }

                    if (m.getAnnotation(PostLoad.class) != null) {
                        if (!postLoad.containsKey(types[0])) {
                            postLoad.put(types[0], new LinkedHashMap<Method, Object>());
                        }
                        postLoad.get(types[0]).put(m, listener);
                    }
                }
            }
        }
    }

    /**
     * Execute the listeners. We need to check the entity's class, parent classes,
     * and interfaces.
     *
     * @param map
     * @param entity
     */
    private void execute(final Map<Class<?>, Map<Method, Object>> map, final Object entity) {
        if (entity == null) {
            return;
        }
        if (entity.getClass().isAnnotationPresent(Entity.class)) {

            // check for hits on this class or its superclasses.
            for (Class<?> c = entity.getClass(); c != null && c != Object.class; c = c.getSuperclass()) {
                if (map.containsKey(c)) {
                    for (final Map.Entry<Method, Object> entry : map.get(c).entrySet()) {
                        try {
                            entry.getKey().invoke(entry.getValue(), entity);
                        } catch (final InvocationTargetException e) {
                            // log it
                        } catch (final IllegalAccessException e) {
                            // log it
                        }
                    }
                }
            }

            // check for hits on interfaces.
            for (final Class<?> c : entity.getClass().getInterfaces()) {
                if (map.containsKey(c)) {
                    for (final Map.Entry<Method, Object> entry : map.get(c).entrySet()) {
                        try {
                            entry.getKey().invoke(entry.getValue(), entity);
                        } catch (final InvocationTargetException e) {
                            // log it
                        } catch (final IllegalAccessException e) {
                            // log it
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onDelete(final DeleteEvent event) throws HibernateException {
        execute(preRemove, event.getObject());

    }

    @Override
    public void onDelete(final DeleteEvent event, @SuppressWarnings("rawtypes") final Set transientEntities)
            throws HibernateException {
        execute(preRemove, event.getObject());

    }

    /**
     * @see org.hibernate.event.spi.PostDeleteEventListener#onPostDelete(org.hibernate
     *      .event.spi.PostDeleteEvent)
     */
    @Override
    public void onPostDelete(final PostDeleteEvent event) {
        execute(postRemove, event.getEntity());
    }

    /**
     * @see org.hibernate.event.spi.PreDeleteEventListener#onPreDelete(org.hibernate
     *      .event.spi.PreDeleteEvent)
     */
    @Override
    public boolean onPreDelete(final PreDeleteEvent event) {
        execute(preRemove, event.getEntity());
        return false;
    }

    /**
     * @see org.hibernate.event.spi.PreInsertEventListener#onPreInsert(org.hibernate
     *      .event.spi.PreInsertEvent)
     */
    @Override
    public boolean onPreInsert(final PreInsertEvent event) {
        execute(preInsert, event.getEntity());
        return false;
    }

    @Override
    public void onMerge(final MergeEvent event) throws HibernateException {
        execute(preInsert, event.getEntity());
    }

    @Override
    public void onMerge(final MergeEvent event, @SuppressWarnings("rawtypes") final Map copiedAlready)
            throws HibernateException {
        execute(preInsert, event.getEntity());
    }

    /**
     * @see org.hibernate.event.spi.PostInsertEventListener#onPostInsert(org.hibernate
     *      .event.spi.PostInsertEvent)
     */
    @Override
    public void onPostInsert(final PostInsertEvent event) {
        execute(postInsert, event.getEntity());
    }

    @Override
    public void onPersist(final PersistEvent event) throws HibernateException {
        execute(preInsert, event.getObject());

    }

    @Override
    public void onPersist(final PersistEvent event, @SuppressWarnings("rawtypes") final Map createdAlready)
            throws HibernateException {
        execute(preInsert, event.getObject());
    }

    /**
     * @see org.hibernate.event.spi.PreUpdateEventListener#onPreUpdate(org.hibernate
     *      .event.spi.PreUpdateEvent)
     */
    @Override
    public boolean onPreUpdate(final PreUpdateEvent event) {
        execute(preUpdate, event.getEntity());
        return false;
    }

    /**
     * @see org.hibernate.event.spi.PostUpdateEventListener#onPostUpdate(org.hibernate
     *      .event.spi.PostUpdateEvent)
     */
    @Override
    public void onPostUpdate(final PostUpdateEvent event) {
        execute(postUpdate, event.getEntity());
    }

    @Override
    public void onSaveOrUpdate(final SaveOrUpdateEvent event) throws HibernateException {
        execute(preInsert, event.getEntity());
    }

    /**
     * @see org.hibernate.event.spi.PostLoadEventListener#onPostLoad(org.hibernate
     *      .event.spi.PostLoadEvent)
     */
    @Override
    public void onPostLoad(final PostLoadEvent event) {
        execute(postLoad, event.getEntity());
    }

    @Override
    public boolean requiresPostCommitHanding(final EntityPersister persister) {
        return true;
    }

    @Override
    public void onPostUpdateCommitFailed(final PostUpdateEvent event) {

    }

    @Override
    public void onPostInsertCommitFailed(final PostInsertEvent event) {

    }

    @Override
    public void onPostDeleteCommitFailed(final PostDeleteEvent event) {

    }

}
