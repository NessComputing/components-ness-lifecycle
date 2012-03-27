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
package com.nesscomputing.lifecycle.junit;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Stage;
import com.nesscomputing.lifecycle.Lifecycle;
import com.nesscomputing.lifecycle.LifecycleStage;

@RunWith(LifecycleRunner.class)
public class TestLifecycleStatement
{
    @LifecycleRule
    public final LifecycleStatement lifecycleRule = LifecycleStatement.defaultLifecycle();

    @Inject
    private Lifecycle guiceLifecycle;

    @Before
    public void startUp()
    {
        final Lifecycle lifecycle = lifecycleRule.getLifecycle();
        Assert.assertEquals(LifecycleStage.CONFIGURE_STAGE, lifecycle.getNextStage());

        final Injector inj = Guice.createInjector(Stage.PRODUCTION,
                                                  lifecycleRule.getLifecycleModule());

        inj.injectMembers(this);
        Assert.assertNotNull(guiceLifecycle);
    }

    @After
    public void tearDown()
    {
        final Lifecycle lifecycle = lifecycleRule.getLifecycle();
        Assert.assertNull(lifecycle.getNextStage());
    }

    @Test
    public void testSimple()
    {
        final Lifecycle lifecycle = lifecycleRule.getLifecycle();
        Assert.assertEquals(LifecycleStage.STOP_STAGE, lifecycle.getNextStage());
    }

    @Test
    public void testGuice()
    {
        final Lifecycle lifecycle = lifecycleRule.getLifecycle();
        Assert.assertSame(lifecycle, guiceLifecycle);
    }
}
