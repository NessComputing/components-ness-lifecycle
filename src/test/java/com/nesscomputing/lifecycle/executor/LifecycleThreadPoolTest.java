package com.nesscomputing.lifecycle.executor;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.management.ManagementFactory;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import javax.management.MBeanServer;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import org.junit.Test;
import org.weakref.jmx.guice.MBeanModule;

import com.nesscomputing.config.ConfigModule;
import com.nesscomputing.lifecycle.Lifecycle;
import com.nesscomputing.lifecycle.LifecycleStage;
import com.nesscomputing.lifecycle.guice.LifecycleModule;

public class LifecycleThreadPoolTest
{
    @Inject
    Lifecycle lifecycle;

    @Inject
    @Named("test")
    ExecutorService service;

    @Test
    public void testSameThread() throws Exception
    {
        Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure()
            {
                install (ConfigModule.forTesting());
                install (new LifecycleModule());
                install (new LifecycledThreadPoolModule("test").withDefaultMaxThreads(0));
            }
        }).injectMembers(this);

        lifecycle.executeTo(LifecycleStage.START_STAGE);

        final Thread currentThread = Thread.currentThread();

        Future<Boolean> future = service.submit(new Callable<Boolean>() {
            @Override
            public Boolean call()
            {
                return currentThread == Thread.currentThread();
            }
        });

        assertTrue("must be on same thread", future.get());

        lifecycle.executeTo(LifecycleStage.STOP_STAGE);
        assertTrue(service.isShutdown());
        assertTrue(service.isTerminated());
    }

    @Test
    public void testThreadPool() throws Exception
    {
        Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure()
            {
                install (ConfigModule.forTesting());
                install (new LifecycleModule());
                install (new LifecycledThreadPoolModule("test"));

                bind (MBeanServer.class).toInstance(ManagementFactory.getPlatformMBeanServer());
                install (new MBeanModule());
            }
        }).injectMembers(this);

        lifecycle.executeTo(LifecycleStage.START_STAGE);

        final Thread currentThread = Thread.currentThread();

        Future<Boolean> future = service.submit(new Callable<Boolean>() {
            @Override
            public Boolean call()
            {
                return currentThread == Thread.currentThread();
            }
        });

        assertFalse("must not be on same thread", future.get());

        lifecycle.executeTo(LifecycleStage.STOP_STAGE);
        assertTrue(service.isShutdown());
        assertTrue(service.isTerminated());
    }
}
