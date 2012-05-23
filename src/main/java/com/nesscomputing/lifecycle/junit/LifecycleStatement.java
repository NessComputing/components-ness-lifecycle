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

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Module;
import com.nesscomputing.lifecycle.DefaultLifecycle;
import com.nesscomputing.lifecycle.Lifecycle;
import com.nesscomputing.lifecycle.LifecycleStage;
import com.nesscomputing.lifecycle.guice.AbstractLifecycleModule;
import com.nesscomputing.testing.lessio.AllowAll;

/**
 * Test rule to run lifecycle start and stop for unit tests. Unfortunately, this must be run around a method (after \#0064Before and before \#0064After annotations), so
 * it can only be used in conjunction with the {@link LifecycleRunner}.
 */
@AllowAll
public final class LifecycleStatement implements TestRule
{
    public static LifecycleStatement defaultLifecycle()
    {
        return new LifecycleStatement(new DefaultLifecycle(), LifecycleStage.START_STAGE, LifecycleStage.STOP_STAGE);
    }

    public static LifecycleStatement serviceDiscoveryLifecycle()
    {
        return new LifecycleStatement(new DefaultLifecycle(), LifecycleStage.ANNOUNCE_STAGE, LifecycleStage.STOP_STAGE);
    }

    private final Lifecycle lifecycle;

    private final LifecycleStage startStage;
    private final LifecycleStage stopStage;

    public LifecycleStatement(final Lifecycle lifecycle, final LifecycleStage startStage, final LifecycleStage stopStage)
    {
        this.lifecycle = lifecycle;
        this.startStage = startStage;
        this.stopStage = stopStage;
    }

    public Module getLifecycleModule()
    {
        return new AbstractLifecycleModule() {
            @Override
            public void configureLifecycle() {
                bind(Lifecycle.class).toInstance(lifecycle);
            }
        };
    }

    @VisibleForTesting
    Lifecycle getLifecycle()
    {
        return lifecycle;
    }

    @Override
    public Statement apply(Statement base, final Description description)
    {
        return new LifecycleStatementWrapper(base);
    }

    public class LifecycleStatementWrapper extends Statement
    {
        private final Statement delegate;

        LifecycleStatementWrapper(final Statement delegate)
        {
            this.delegate = delegate;
        }

        @Override
        public void evaluate() throws Throwable {
            try {
                lifecycle.executeTo(startStage);
                delegate.evaluate();
            }
            finally {
                lifecycle.executeTo(stopStage);
            }
        }
    }
}
