package com.nesscomputing.lifecycle.junit;

import java.util.List;

import org.junit.internal.runners.statements.InvokeMethod;
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

    private Statement withLifeycleRules(final FrameworkMethod method, final Object target, final Statement statement)
    {
        List<TestRule> testRules = getLifecycleRules(target);
        return testRules.isEmpty() ? statement : new RunRules(statement, testRules, describeChild(method));
    }

    protected List<TestRule> getLifecycleRules(final Object target)
    {
        return getTestClass().getAnnotatedFieldValues(target, LifecycleRule.class, TestRule.class);
    }

    @Override
    protected Statement methodInvoker(final FrameworkMethod method, final Object test)
    {
        return withLifeycleRules(method, test, new InvokeMethod(method, test));
    }
}
