/**
 * Copyright (C) 2011 Ness Computing, Inc.
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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;

import org.junit.Assert;
import org.junit.Test;

import com.likeness.lifecycle.LifecycleStage;

public class TestLifecycleStage
{
    @Test
    public void testEquality()
    {
        LifecycleStage l1 = LifecycleStage.START_STAGE;
        LifecycleStage l2 = new LifecycleStage("start");

        Assert.assertEquals(l1, l2);
    }

    @Test
    public void testEqualityIgnoreCase()
    {
        LifecycleStage l1 = new LifecycleStage("START");
        LifecycleStage l2 = new LifecycleStage("start");

        Assert.assertEquals(l1, l2);
    }

    @Test
    public void testNonEquality()
    {
        LifecycleStage l1 = new LifecycleStage("START-1");
        LifecycleStage l2 = new LifecycleStage("START-2");

        Assert.assertThat(l1, is(not(l2)));
    }
}
