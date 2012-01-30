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


import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Maps;
import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.Scopes;
import com.nesscomputing.lifecycle.DefaultLifecycle;
import com.nesscomputing.lifecycle.Lifecycle;
import com.nesscomputing.lifecycle.LifecycleStage;
import com.nesscomputing.lifecycle.guice.DelegatingLifecycleProvider;
import com.nesscomputing.lifecycle.guice.LifecycleAction;
import com.nesscomputing.lifecycle.guice.LifecycleProvider;

public class TestDelegatingLifecycleProvider
{
    private static final LifecycleStage [] stages = new LifecycleStage [] {
        LifecycleStage.CONFIGURE_STAGE,
        LifecycleStage.START_STAGE,
        LifecycleStage.STOP_STAGE
    };

    private static final Foo FOO = new Foo("foo");

    @Test
    public void testLifecycleProviderClass()
    {
        final Map<LifecycleStage, CustomAction> actions = Maps.newHashMap();

        final LifecycleProvider<Foo> lcProvider = DelegatingLifecycleProvider.of(FooProvider.class);

        for (LifecycleStage stage : stages) {
            final CustomAction action = new CustomAction();
            lcProvider.addAction(stage, action);
            actions.put(stage, action);
        }

        final Injector injector = Guice.createInjector(
            new Module() {
                @Override
                public void configure(final Binder binder)
                {
                    binder.bind(Lifecycle.class).to(DefaultLifecycle.class).in(Scopes.SINGLETON);
                    binder.bind(Foo.class).toProvider(lcProvider).in(Scopes.SINGLETON);
                }
            });

        final Lifecycle lifecycle = injector.getInstance(Lifecycle.class);

        final Foo foo = injector.getInstance(Foo.class);

        Assert.assertEquals(FOO, foo);

        // Run the lifecycle. This must trigger all the actions one by one.
        lifecycle.executeTo(LifecycleStage.STOP_STAGE);

        // one action for each stage.
        Assert.assertEquals(stages.length, actions.size());

        // Every action was hit once.
        for (final Map.Entry<LifecycleStage, CustomAction> entry : actions.entrySet()) {
            Assert.assertEquals(1, entry.getValue().getCount());
        }
    }


    @Test
    public void testLifecycleProvider()
    {
        final Map<LifecycleStage, CustomAction> actions = Maps.newHashMap();

        final LifecycleProvider<Foo> lcProvider = DelegatingLifecycleProvider.of(new FooProvider());

        for (LifecycleStage stage : stages) {
            final CustomAction action = new CustomAction();
            lcProvider.addAction(stage, action);
            actions.put(stage, action);
        }

        final Injector injector = Guice.createInjector(
            new Module() {
                @Override
                public void configure(final Binder binder)
                {
                    binder.bind(Lifecycle.class).to(DefaultLifecycle.class).in(Scopes.SINGLETON);
                    binder.bind(Foo.class).toProvider(lcProvider).in(Scopes.SINGLETON);
                }
            });

        final Lifecycle lifecycle = injector.getInstance(Lifecycle.class);

        final Foo foo = injector.getInstance(Foo.class);

        Assert.assertEquals(FOO, foo);

        // Run the lifecycle. This must trigger all the actions one by one.
        lifecycle.executeTo(LifecycleStage.STOP_STAGE);

        // one action for each stage.
        Assert.assertEquals(stages.length, actions.size());

        // Every action was hit once.
        for (final Map.Entry<LifecycleStage, CustomAction> entry : actions.entrySet()) {
            Assert.assertEquals(1, entry.getValue().getCount());
        }
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

    public static class FooProvider implements Provider<Foo>
    {
        @Override
        public Foo get()
        {
            return FOO;
        }
    }

    public static class Foo
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
