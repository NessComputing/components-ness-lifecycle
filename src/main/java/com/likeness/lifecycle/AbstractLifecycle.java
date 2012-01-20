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
package com.likeness.lifecycle;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.annotation.Nonnull;

import com.google.common.collect.Lists;
import com.likeness.logging.Log;

/**
 * Base code for lifecycling.
 */
public abstract class AbstractLifecycle implements Lifecycle
{
    private static final Log LOG = Log.findLog();

    private final ConcurrentMap<LifecycleStage, List<LifecycleListener>> listeners = new ConcurrentHashMap<LifecycleStage, List<LifecycleListener>>();

    private final LifecycleDriver lifecycleDriver;

    private final boolean verbose;

    /**
     * Builds a new Lifecycle.
     *
     * @param lifecycleDriver The lifecycleDriver to provide the available stages and the sequence in which they should be processed.
     *
     * @param verbose If true, then report each stage at info logging level.
     */
    protected AbstractLifecycle(@Nonnull final LifecycleDriver lifecycleDriver, final boolean verbose)
    {
        this.verbose = verbose;
        this.lifecycleDriver = lifecycleDriver;

        // Add all stages for that Lifecycle to the listener map.
        for (LifecycleStage lifecycleStage : lifecycleDriver.getStages()) {
            listeners.put(lifecycleStage, new CopyOnWriteArrayList<LifecycleListener>());
            addListener(lifecycleStage, lifecycleDriver);
        }
    }

    /**
     * Adds a listener to a lifecycle stage.
     *
     * @param lifecycleStage    The Lifecycle stage on which to be notified.
     * @param lifecycleListener Callback to be invoked when the lifecycle stage is executed.
     */
    @Override
    public void addListener(@Nonnull final LifecycleStage lifecycleStage, @Nonnull final LifecycleListener lifecycleListener)
    {
        if (!listeners.containsKey(lifecycleStage)) {
            throw illegalStage(lifecycleStage);
        }
        listeners.get(lifecycleStage).add(lifecycleListener);
    }

    /**
     * Return the next stage in the lifecycle.
     */
    @Override
    public LifecycleStage getNextStage()
    {
        return lifecycleDriver.getNextStage();
    }

    /**
     * Execute the next stage in the cycle.
     */
    @Override
    public void executeNext()
    {
        final LifecycleStage nextStage = getNextStage();
        if (nextStage == null) {
            throw new IllegalStateException("Lifecycle already hit the final stage!");
        }
        execute(nextStage);
    }

    /**
     * Executes stages until the stage requested has been reached.
     * @param lifecycleStage The lifecycle stage to reach.
     *
     * @throws IllegalStateException If the cycle ends before this stage is reached.
     */
    @Override
    public void executeTo(@Nonnull final LifecycleStage lifecycleStage)
    {
        boolean foundStage = false;
        do {
            final LifecycleStage nextStage = lifecycleDriver.getNextStage();

            if (nextStage == null) {
                throw new IllegalStateException("Never reached stage '" + lifecycleStage.getName() + "' before ending the lifecycle.");
            }

            foundStage = nextStage.equals(lifecycleStage);
            execute(nextStage);

        } while(!foundStage);
    }

    /**
     * Execute a lifecycle stage.
     */
    @Override
    public void execute(@Nonnull final LifecycleStage lifecycleStage)
    {
        List<LifecycleListener> lifecycleListeners = listeners.get(lifecycleStage);
        if (lifecycleListeners == null) {
            throw illegalStage(lifecycleStage);
        }

        log("Stage '%s' starting...", lifecycleStage.getName());

        // Reverse the order for the STOP stage, so that dependencies are torn down in reverse order.
        if (lifecycleStage.equals(LifecycleStage.STOP_STAGE)) {
            lifecycleListeners = Lists.reverse(lifecycleListeners);
        }

        for (final LifecycleListener listener : lifecycleListeners) {
            listener.onStage(lifecycleStage);
        }

        log("Stage '%s' complete.", lifecycleStage.getName());
    }

    /**
     * Register a shutdown hook to execute the given stage on JVM shutdown, and
     * join against the current thread. This will block, so there needs to be a another way to shut
     * the current thread down.
     *
     * @param lifecycleStage The stage to reach.
     * @param cycle If true, then cycle to the stage, otherwise, just execute the stage.
     *
     * @throws InterruptedException if the Thread.currentThread.join() is interrupted
     */
    protected void join(@Nonnull final LifecycleStage lifecycleStage, final boolean cycle) throws InterruptedException
    {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run()
            {
                if (cycle) {
                    AbstractLifecycle.this.executeTo(lifecycleStage);
                }
                else {
                    AbstractLifecycle.this.execute(lifecycleStage);
                }
            }
        });
        Thread.currentThread().join();
    }

    protected IllegalStateException illegalStage(final LifecycleStage lifecycleStage) {
        return new IllegalStateException(String.format("This lifecycle does not support the '%s' stage, only '%s' are supported", lifecycleStage.getName(), lifecycleDriver.getStages()));
    }

    protected void log(final String message, final Object ... args)
    {
        if (verbose) {
            LOG.info(message, args);
        }
        else {
            LOG.debug(message, args);
        }
    }
}
