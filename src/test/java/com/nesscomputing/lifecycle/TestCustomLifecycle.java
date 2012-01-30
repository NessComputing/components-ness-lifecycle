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

import org.junit.Assert;
import org.junit.Test;

import com.nesscomputing.lifecycle.AbstractLifecycle;
import com.nesscomputing.lifecycle.Lifecycle;
import com.nesscomputing.lifecycle.LifecycleDriver;
import com.nesscomputing.lifecycle.LifecycleListener;
import com.nesscomputing.lifecycle.LifecycleStage;


public class TestCustomLifecycle
{
    public static final LifecycleStage FIRST_STAGE = new LifecycleStage("first_stage");
    public static final LifecycleStage SECOND_STAGE = new LifecycleStage("second_stage");
    public static final LifecycleStage THIRD_STAGE = new LifecycleStage("third_stage");
    public static final LifecycleStage FOURTH_STAGE = new LifecycleStage("fourth_stage");
    public static final LifecycleStage FIFTH_STAGE = new LifecycleStage("fifth_stage");
    public static final LifecycleStage SIXTH_STAGE = new LifecycleStage("sixth_stage");
    public static final LifecycleStage SEVENTH_STAGE = new LifecycleStage("seventh_stage");
    public static final LifecycleStage EIGHTH_STAGE = new LifecycleStage("eighth_stage");
    public static final LifecycleStage NINTH_STAGE = new LifecycleStage("ninth_stage");
    public static final LifecycleStage TENTH_STAGE = new LifecycleStage("tenth_stage");

    private static final LifecycleStage [] stages = new LifecycleStage [] {
        FIRST_STAGE,
        SECOND_STAGE,
        THIRD_STAGE,
        FOURTH_STAGE,
        FIFTH_STAGE,
        SIXTH_STAGE,
        SEVENTH_STAGE,
        EIGHTH_STAGE,
        NINTH_STAGE,
        TENTH_STAGE
    };

    @Test
    public void testDefaultRun()
    {
        final Lifecycle lifecycle = new CustomLifecycle();

        TestLifecycleListener testListener = new TestLifecycleListener();

        for (final LifecycleStage stage : stages) {
            lifecycle.addListener(stage, testListener);
        }

        Assert.assertEquals(stages[0], lifecycle.getNextStage());

        for (final LifecycleStage stage : stages) {
            Assert.assertEquals(stage, lifecycle.getNextStage());
            lifecycle.executeNext();
            Assert.assertEquals(stage, testListener.getLastStageSeen());
        }

        Assert.assertNull(lifecycle.getNextStage());
    }

    @Test
    public void testUpDownRun()
    {
        final Lifecycle lifecycle = new CustomLifecycle();

        CountLifecycleListener countListener = new CountLifecycleListener();
        TestLifecycleListener testListener = new TestLifecycleListener();

        for (final LifecycleStage stage : stages) {
            lifecycle.addListener(stage, countListener);
            lifecycle.addListener(stage, testListener);
        }

        Assert.assertEquals(stages[0], lifecycle.getNextStage());

        lifecycle.executeTo(FIFTH_STAGE);

        Assert.assertEquals(FIFTH_STAGE, testListener.getLastStageSeen());
        Assert.assertEquals(5, countListener.getCount());

        countListener.reset();

        lifecycle.executeTo(TENTH_STAGE);

        Assert.assertEquals(TENTH_STAGE, testListener.getLastStageSeen());
        Assert.assertEquals(5, countListener.getCount());

        Assert.assertNull(lifecycle.getNextStage());
    }

    private static class CustomLifecycle extends AbstractLifecycle
    {
        private CustomLifecycle()
        {
            super(new LifecycleDriver(stages), false);
        }

        @Override
        public void join() throws InterruptedException
        {
        	super.join(stages[stages.length-1], true);
        }
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

