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


import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.google.inject.Inject;
import com.nesscomputing.lifecycle.Lifecycle;
import com.nesscomputing.lifecycle.LifecycleListener;
import com.nesscomputing.lifecycle.LifecycleStage;

/**
 * Base class for providers that want to hook the objects they provide into the Lifecyle. Any class extending this base class
 * needs to implement internalGet() instead of get(). It should also register all desired lifecycle events using the addAction()
 * method. See e.g. the HttpClientProvider for an example.
 */
public abstract class AbstractLifecycleProvider<T> implements LifecycleProvider<T>
{
    private Lifecycle lifecycle = null;

    private final List<StageEvent> stageEvents = new ArrayList<StageEvent>();

    /**
     * Add a lifecycle Action to this provider. The action will called back when the lifecycle stage is
     * hit and contain an object that was created by the provider.
     */
    @Override
    public void addAction(final LifecycleStage stage, final LifecycleAction<T> action)
    {
        stageEvents.add(new StageEvent(stage, action));
    }

    /**
     * Called by Guice. Don't touch.
     */
    @Inject
    public final void setLifecycle(final Lifecycle lifecycle)
    {
        this.lifecycle = lifecycle;
    }

    @Override
    public final T get()
    {
        final T result = internalGet();

        if (lifecycle != null) {
            for (final StageEvent stageEvent : stageEvents) {
                lifecycle.addListener(stageEvent.getLifecycleStage(), new ActionLifecycleListener<T>(stageEvent.getLifecycleAction(), result));
            }
        }

        return result;
    }

    /**
     * Implemented instead of {@link Provider#get()} to provide the new instance.
     */
    protected abstract T internalGet();

    private class StageEvent
    {
        private final LifecycleStage stage;

        private final LifecycleAction<T> action;

        private StageEvent(@Nonnull final LifecycleStage stage, @Nonnull final LifecycleAction<T> action)
        {
            this.stage = stage;
            this.action = action;
        }

        private LifecycleStage getLifecycleStage()
        {
            return stage;
        }

        private LifecycleAction<T> getLifecycleAction()
        {
            return action;
        }

        private transient String toString;

        @Override
        public String toString() {
            if (toString == null) {
                toString = new ToStringBuilder(this).append("stage", stage)
                        .append("action", action).toString();
            }
            return toString;
        }
    }

    /**
     *  Listener that performs a given LifecycleAction on an instance object.
     */
    public static class ActionLifecycleListener<T> implements LifecycleListener
    {
        private final LifecycleAction<T> action;
        private final T obj;

        private ActionLifecycleListener(@Nonnull final LifecycleAction<T> action, @Nonnull final T obj)
        {
            this.action = action;
            this.obj = obj;
        }

        @Override
        public void onStage(@Nonnull final LifecycleStage stage)
        {
            action.performAction(obj);
        }
    }
}
