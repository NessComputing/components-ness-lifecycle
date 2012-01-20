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

import org.junit.Assert;
import org.junit.Test;

import com.likeness.lifecycle.DefaultLifecycle;
import com.likeness.lifecycle.Lifecycle;
import com.likeness.lifecycle.LifecycleListener;
import com.likeness.lifecycle.LifecycleStage;


public class TestDefaultLifecycle
{
    private static final LifecycleStage [] stages = new LifecycleStage [] {
        LifecycleStage.CONFIGURE_STAGE,
        LifecycleStage.START_STAGE,
        LifecycleStage.STOP_STAGE
    };

    @Test
    public void testDefaultRun()
    {
        final Lifecycle lifecycle = new DefaultLifecycle(false);

        TestLifecycleListener testListener = new TestLifecycleListener();

        for (final LifecycleStage stage : stages) {
            lifecycle.addListener(stage, testListener);
        }

        for (final LifecycleStage stage : stages) {
            Assert.assertEquals(stage, lifecycle.getNextStage());
            lifecycle.executeNext();
            Assert.assertEquals(stage, testListener.getLastStageSeen());
        }

        Assert.assertNull(lifecycle.getNextStage());
    }

    @Test
    public void testStartStopRun()
    {
        final Lifecycle lifecycle = new DefaultLifecycle(false);

        CountLifecycleListener countListener = new CountLifecycleListener();
        TestLifecycleListener testListener = new TestLifecycleListener();

        for (final LifecycleStage stage : stages) {
            lifecycle.addListener(stage, countListener);
            lifecycle.addListener(stage, testListener);
        }

        Assert.assertEquals(stages[0], lifecycle.getNextStage());
        
        lifecycle.executeTo(LifecycleStage.START_STAGE);

        Assert.assertEquals(LifecycleStage.START_STAGE, testListener.getLastStageSeen());
        Assert.assertEquals(2, countListener.getCount());
        
        countListener.reset();

        lifecycle.executeTo(LifecycleStage.STOP_STAGE);

        Assert.assertEquals(LifecycleStage.STOP_STAGE, testListener.getLastStageSeen());
        Assert.assertEquals(1, countListener.getCount());

        Assert.assertNull(lifecycle.getNextStage());
    }

    @Test(expected = IllegalStateException.class)
    public void runPastEnd()
    {
        final Lifecycle lifecycle = new DefaultLifecycle(false);
        lifecycle.executeTo(LifecycleStage.STOP_STAGE);
        Assert.assertNull(lifecycle.getNextStage());

        lifecycle.executeNext();
    }

    @Test(expected = IllegalStateException.class)
    public void runToIllegalStage()
    {
        final Lifecycle lifecycle = new DefaultLifecycle(false);
        lifecycle.executeTo(new LifecycleStage("foo"));
    }

    @Test(expected = IllegalStateException.class)
    public void executeIllegalStage()
    {
        final Lifecycle lifecycle = new DefaultLifecycle(false);
        lifecycle.execute(new LifecycleStage("foo"));
    }

    @Test(expected = IllegalStateException.class)
    public void addIllegalListener()
    {
        final Lifecycle lifecycle = new DefaultLifecycle(false);
        lifecycle.addListener(new LifecycleStage("foo"), new TestLifecycleListener());
    }


    private static class TestLifecycleListener implements LifecycleListener
    {
        private LifecycleStage lastStageSeen = null;

        @Override
        public void onStage(final LifecycleStage stage)
        {
            this.lastStageSeen = stage;
        }

        public LifecycleStage getLastStageSeen()
        {
            return lastStageSeen;
        }
    }

    private static class CountLifecycleListener implements LifecycleListener
    {
        private int count = 0;

        @Override
        public void onStage(final LifecycleStage stage)
        {
            count++;
        }
        
        public void reset()
        {
            count = 0;
        }

        public int getCount()
        {
            return count;
        }
    }
}

