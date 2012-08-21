/**
 * Copyright (C) 2012 Ness Computing, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nesscomputing.lifecycle.guice;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import javax.annotation.concurrent.NotThreadSafe;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import com.nesscomputing.lifecycle.Lifecycle;
import com.nesscomputing.lifecycle.LifecycleListener;
import com.nesscomputing.lifecycle.LifecycleStage;
import com.nesscomputing.logging.Log;

/**
 * Visit all Guice injections.  For each declared method in the class (and superclasses),
 * inspect for {@link OnStage} annotations.  Register such annotated methods with the injected
 * {@link Lifecycle} so that they are run.
 */
@NotThreadSafe
class LifecycleAnnotationFinder implements TypeListener {
    private static final Log LOG = Log.findLog();

    /** Store all invocations found <b>before</b> the Lifecycle is available.  Null after lifecycle is available */
    private List<LifecycleInvocation> foundInvocations = Lists.newArrayList();

    /** The lifecycle, which is null before it is available (i.e. before Guice creates it) */
    private Lifecycle lifecycle;

    @Override
    public <I> void hear(TypeLiteral<I> type, TypeEncounter<I> encounter) {
        LOG.trace("Found new injectable type %s", type);

        Class<? super I> klass = type.getRawType();
        // Loop over the class and superclasses
        do {
            for (final Method m : klass.getDeclaredMethods()) {

                // Inspect declared methods for @OnStage

                final OnStage onStage = m.getAnnotation(OnStage.class);
                if (onStage == null) {
                    continue;
                }

                LOG.trace("Will invoke %s on %s", m, onStage.value());

                encounter.register(new InjectionListener<I>() {
                    @Override
                    public void afterInjection(I injectee) {
                        LifecycleInvocation invocation = new LifecycleInvocation(new LifecycleStage(onStage.value()), injectee, m);

                        if (lifecycle != null) { // If the lifecycle is available, register now
                            addListener(invocation);
                        } else { // Otherwise, do it later, when the lifecycle is injected
                            Preconditions.checkState(foundInvocations != null, "Injection after lifecycle start!");
                            foundInvocations.add(invocation);
                        }
                    }
                });

            }
            klass = klass.getSuperclass();
        } while (klass != null);
    }

    /**
     * Called once Guice has created our Lifecycle, so we can start registering callbacks
     */
    void lifecycleAvailable(Lifecycle lifecycle) {
        LOG.debug("Lifecycle now available, draining queue");

        // First, make sure we will not let any more listeners be added once the lifecycle starts going.

        lifecycle.addListener(LifecycleStage.CONFIGURE_STAGE, new LifecycleListener() {
            @Override
            public void onStage(LifecycleStage lifecycleStage) {
                LOG.debug("Lifecycle started, further injections disallowed");
                LifecycleAnnotationFinder.this.lifecycle = null; // Now both lifecycle and foundInvocations are null, triggering ISE on further discoveries
            }
        });

        // Now direct further visits to the lifecycle directly

        this.lifecycle = lifecycle;

        // Now drain out all the previous ones into the lifecycle and remove the list itself

        for (LifecycleInvocation invocation : foundInvocations) {
            addListener(invocation);
        }
        foundInvocations = null;
    }

    void addListener(LifecycleInvocation invocation) {
        Preconditions.checkState(lifecycle != null, "no lifecycle");
        invocation.visit(lifecycle);
    }

    /**
     * Stored lifecycle listener (stage, object, method)
     */
    static class LifecycleInvocation {
        private final LifecycleStage stage;
        private final Method method;
        private final Object object;

        private LifecycleInvocation(LifecycleStage stage, Object object, Method method) {
            this.stage = stage;
            this.object = object;
            this.method = method;

            Preconditions.checkState(method.getParameterTypes().length == 0, "Method '%s' must have no arguments to be a @OnStage method", method);
        }

        void visit(Lifecycle lifecycle) {
            method.setAccessible(true);
            lifecycle.addListener(stage, new LifecycleListener() {
                @Override
                public void onStage(LifecycleStage lifecycleStage) {
                    try {
                        method.invoke(object);
                    } catch (IllegalAccessException e) {
                        Throwables.propagate(e);
                    } catch (InvocationTargetException e) {
                        if (e.getCause() == null) {
                            Throwables.propagate(e);
                        }
                        Throwables.propagate(e.getCause());
                    }
                }
            });
        }
    }
}
