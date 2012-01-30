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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;

import java.util.List;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

import com.nesscomputing.lifecycle.LifecycleDriver;
import com.nesscomputing.lifecycle.LifecycleStage;


public class TestLifecycleDriver
{
    private static final LifecycleStage FOO_STAGE = new LifecycleStage("foo");
    private static final LifecycleStage BAR_STAGE = new LifecycleStage("bar");


    @Test
    public void testBasicDriver()
    {
        LifecycleDriver driver = new LifecycleDriver(
            FOO_STAGE,
            BAR_STAGE
            );

        List<LifecycleStage> stages = driver.getStages();
        Assert.assertThat(stages, Matchers.hasItem(FOO_STAGE));
        Assert.assertThat(stages, Matchers.hasItem(BAR_STAGE));

        Assert.assertThat(driver.getNextStage(), is(FOO_STAGE));

        driver.onStage(FOO_STAGE);

        Assert.assertThat(driver.getNextStage(), is(BAR_STAGE));

        driver.onStage(BAR_STAGE);

        Assert.assertThat(driver.getNextStage(), is(nullValue()));
    }

    @Test(expected = IllegalStateException.class)
    public void testWrongStage()
    {
        LifecycleDriver driver = new LifecycleDriver(
            FOO_STAGE,
            BAR_STAGE
            );

        driver.onStage(new LifecycleStage("baz"));
    }
}

