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

/**
 * ServiceDiscovery lifecycle, similar to the default lifecycle, also add Announce and Unannounce.
 */
public class ServiceDiscoveryLifecycle extends AbstractLifecycle implements Lifecycle
{
    public ServiceDiscoveryLifecycle()
    {
        this(false);
    }

    public ServiceDiscoveryLifecycle(final boolean verbose)
    {
        super(new LifecycleDriver(LifecycleStage.CONFIGURE_STAGE, LifecycleStage.START_STAGE, LifecycleStage.ANNOUNCE_STAGE, LifecycleStage.UNANNOUNCE_STAGE, LifecycleStage.STOP_STAGE), verbose);
    }

    /**
     * Register a shutdown hook to fire to the Stop stage on JVM shutdown, and
     * join against the current thread.
     */
    public void join() throws InterruptedException
    {
        super.join(LifecycleStage.STOP_STAGE, true);
    }
}
