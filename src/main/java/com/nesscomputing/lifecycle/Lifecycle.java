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
package com.nesscomputing.lifecycle;

/**
 * Provides a lifecycle that can be run inside an IoC container such as guice.
 */
public interface Lifecycle
{
   /**
     * Adds a listener to a lifecycle stage.
     *
     * @param stage    The Lifecycle stage on which to be notified.
     * @param listener Callback to be invoked when the lifecycle stage is executed.
     */
    void addListener(LifecycleStage stage, LifecycleListener listener);

    /**
     * Return the next stage in the lifecycle.
     */
    LifecycleStage getNextStage();

    /**
     * Execute the next stage in the cycle.
     */
    void executeNext();

    /**
     * Executes the next stage until the stage requested has been reached.
     */
    void executeTo(final LifecycleStage stage);

    /**
     * Execute a lifecycle stage.
     */
    void execute(final LifecycleStage stage);

    /**
     * Register a shutdown hook to stop on JVM shutdown, and
     * join against the current thread.
     *
     * @throws InterruptedException if the Thread.currentThread.join() is interrupted
     */
    void join() throws InterruptedException;
}
