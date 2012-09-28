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

import java.util.List;

import org.junit.internal.runners.statements.InvokeMethod;
import org.junit.internal.runners.statements.RunBefores;
import org.junit.rules.RunRules;
import org.junit.rules.TestRule;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

/**
 * Runs an unit test and looks for {@link \#0064LifecycleRule} annotations to hook up the lifecycle around a method.
 */
public class LifecycleRunner extends BlockJUnit4ClassRunner
{
    public LifecycleRunner(Class<?> klass) throws InitializationError
    {
        super(klass);
    }

    private Statement withLifecycleRules(final FrameworkMethod method, final Object target, final Statement statement)
    {
        List<TestRule> testRules = getLifecycleRules(target);
        return testRules.isEmpty() ? statement : new RunRules(statement, testRules, describeChild(method));
    }

    private Statement withLifecycleBefore(final FrameworkMethod method, final Object target, final Statement statement)
    {
        List<FrameworkMethod> methods = getTestClass().getAnnotatedMethods(LifecycleBefore.class);
        return methods.isEmpty() ? statement : new RunBefores(statement, methods, target);
    }

    private Statement withLifecycleAfter(final FrameworkMethod method, final Object target, final Statement statement)
    {
        List<FrameworkMethod> methods = getTestClass().getAnnotatedMethods(LifecycleAfter.class);
        return methods.isEmpty() ? statement : new RunBefores(statement, methods, target);
    }

    protected List<TestRule> getLifecycleRules(final Object target)
    {
        return getTestClass().getAnnotatedFieldValues(target, LifecycleRule.class, TestRule.class);
    }

    @Override
    protected Statement methodInvoker(final FrameworkMethod method, final Object test)
    {
        Statement s = new InvokeMethod(method, test);
        s = withLifecycleBefore(method, test, s);
        s = withLifecycleAfter(method, test, s);
        s = withLifecycleRules(method, test, s);
        return s;
    }
}
