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
package com.likeness.lifecycle.guice;


import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Maps;
import com.likeness.lifecycle.DefaultLifecycle;
import com.likeness.lifecycle.Lifecycle;
import com.likeness.lifecycle.LifecycleStage;
import com.likeness.lifecycle.guice.AbstractLifecycleProvider;
import com.likeness.lifecycle.guice.LifecycleAction;

public class TestCustomLifecycledProvider
{
    private static final LifecycleStage [] stages = new LifecycleStage [] {
        LifecycleStage.CONFIGURE_STAGE,
        LifecycleStage.START_STAGE,
        LifecycleStage.STOP_STAGE
    };

    private static final Foo FOO = new Foo("foo");

    @Test
    public void testCustomLifecycledProvider()
    {
        // Build a new Lifecycle
        final Lifecycle lifecycle = new DefaultLifecycle();

        // Set up the provider. This should register all the actions with the abstract provider.
        CustomLifecycledProvider customProvider = new CustomLifecycledProvider();

        // Insert the injector. This will make the provider grab the lifecycle.
        customProvider.setLifecycle(lifecycle);

        // Now get the instance object. This will register all the actions with the
        // lifecycle for this specific instance.
        final Foo obj = customProvider.get();

        // did we get the right object?
        Assert.assertEquals(FOO, obj);

        // Run the lifecycle. This must trigger all the actions one by one.
        lifecycle.executeTo(LifecycleStage.STOP_STAGE);

        final Map<LifecycleStage, CustomLifecycledProvider.CustomAction> actions = customProvider.getActions();

        // one action for each stage.
        Assert.assertEquals(stages.length, actions.size());

        // Every action was hit once.
        for (final Map.Entry<LifecycleStage, CustomLifecycledProvider.CustomAction> entry : actions.entrySet()) {
            Assert.assertEquals(1, entry.getValue().getCount());
        }
    }

    private static class CustomLifecycledProvider extends AbstractLifecycleProvider<Foo>
    {
        private Map<LifecycleStage, CustomAction> actions = Maps.newHashMap();

        public CustomLifecycledProvider()
        {
            for (LifecycleStage stage : stages) {
                final CustomAction action = new CustomAction();
                addAction(stage, action);
                actions.put(stage, action);
            }
        }

        public Map<LifecycleStage, CustomAction> getActions()
        {
        	return actions;
        }

        @Override
        public Foo internalGet()
        {
            return FOO;
        }

        private static class CustomAction implements LifecycleAction<Foo>
        {
            private int count = 0;

            @Override
            public void performAction(final Foo obj)
            {
                Assert.assertEquals(FOO, obj);
                count++;
            }

            public int getCount()
            {
                return count;
            }
        }
    }

    private static class Foo
    {
        private final String name;

        private Foo(final String name)
        {
            this.name = name;
        }

		@Override
		public boolean equals(final Object other) {
			if (!(other instanceof Foo))
				return false;
			Foo castOther = (Foo) other;
			return new EqualsBuilder().append(name, castOther.name).isEquals();
		}

		private transient int hashCode;

		@Override
		public int hashCode() {
			if (hashCode == 0) {
				hashCode = new HashCodeBuilder().append(name).toHashCode();
			}
			return hashCode;
		}

    }
}
