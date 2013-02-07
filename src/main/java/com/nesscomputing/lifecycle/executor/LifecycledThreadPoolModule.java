package com.nesscomputing.lifecycle.executor;

import java.lang.annotation.Annotation;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.google.inject.name.Names;
import com.mogwee.executors.LoggingExecutor;

import org.apache.commons.lang3.time.StopWatch;
import org.skife.config.TimeSpan;
import org.weakref.jmx.guice.MBeanModule;

import com.nesscomputing.config.Config;
import com.nesscomputing.lifecycle.Lifecycle;
import com.nesscomputing.lifecycle.LifecycleListener;
import com.nesscomputing.lifecycle.LifecycleStage;
import com.nesscomputing.lifecycle.executor.ThreadPoolConfiguration.RejectedHandler;
import com.nesscomputing.logging.Log;
import com.nesscomputing.scopes.threaddelegate.concurrent.ThreadDelegatingDecorator;

/**
 * Guice bindings for a configurable, lifecycled {@link ExecutorService}.
 * The executor service is bound as {@code @Named(threadPoolName) ExecutorService myService}.
 * The service will be shut down during {@link LifecycleStage#STOP_STAGE}.  Configuration
 * has the prefix {@code ness.thread-pool.[pool-name]}.
 * @see ThreadPoolConfiguration Thread pool configuration options
 */
public class LifecycledThreadPoolModule extends AbstractModule
{
    private static final Log LOG = Log.findLog();
    private final String threadPoolName;
    private final Annotation annotation;

    private int defaultMinThreads = ThreadPoolConfiguration.DEFAULT_MIN_THREADS;
    private int defaultMaxThreads = ThreadPoolConfiguration.DEFAULT_MAX_THREADS;
    private TimeSpan defaultTimeout = ThreadPoolConfiguration.DEFAULT_TIMEOUT;
    private int defaultQueueSize = ThreadPoolConfiguration.DEFAULT_QUEUE_SIZE;
    private RejectedExecutionHandler defaultRejectedHandler = ThreadPoolConfiguration.DEFAULT_REJECTED_HANDLER.getHandler();

    public LifecycledThreadPoolModule(String threadPoolName) {
        this.threadPoolName = threadPoolName;
        this.annotation = Names.named(threadPoolName);
    }

    @Override
    protected void configure() {
        PoolProvider poolProvider = new PoolProvider();

        bind (ExecutorService.class).annotatedWith(annotation).toProvider(poolProvider).in(Scopes.SINGLETON);
        bind (ExecutorServiceManagementBean.class).annotatedWith(annotation).toProvider(poolProvider.getManagementProvider());
        MBeanModule.newExporter(binder()).export(ExecutorServiceManagementBean.class).annotatedWith(annotation).as(createMBeanName());
    }

    /**
     * Set the default pool core thread count.
     */
    public LifecycledThreadPoolModule withDefaultMinThreads(int defaultMinThreads)
    {
        this.defaultMinThreads = defaultMinThreads;
        return this;
    }

    /**
     * Set the default pool max thread count.  May be 0, in which case the executor will be a
     * {@link MoreExecutors#sameThreadExecutor()}.
     */
    public LifecycledThreadPoolModule withDefaultMaxThreads(int defaultMaxThreads)
    {
        this.defaultMaxThreads = defaultMaxThreads;
        return this;
    }

    /**
     * Set the default worker thread idle timeout.
     */
    public LifecycledThreadPoolModule withDefaultThreadTimeout(long duration, TimeUnit units)
    {
        defaultTimeout = new TimeSpan(duration, units);
        return this;
    }

    /**
     * Set the default queue length.  May be 0, in which case the queue will be a
     * {@link SynchronousQueue}.
     */
    public LifecycledThreadPoolModule withDefaultQueueSize(int defaultQueueSize)
    {
        this.defaultQueueSize = defaultQueueSize;
        return this;
    }

    /**
     * Set the default rejected execution handler.
     */
    public LifecycledThreadPoolModule withDefaultRejectedHandler(RejectedExecutionHandler defaultRejectedHandler)
    {
        this.defaultRejectedHandler = defaultRejectedHandler;
        return this;
    }

    private String createMBeanName()
    {
        return "com.nesscomputing.thread-pool:name=" + threadPoolName;
    }

    @Singleton
    class PoolProvider implements Provider<ExecutorService>
    {
        private ThreadPoolConfiguration config;
        private volatile ExecutorService service;
        private volatile ExecutorServiceManagementBean management;

        @Inject
        public void inject(Config config, Lifecycle lifecycle) {
            this.config = config.getBean("ness.thread-pool." + threadPoolName, ThreadPoolConfiguration.class);

            service = create();

            lifecycle.addListener(LifecycleStage.STOP_STAGE, new LifecycleListener() {
                @Override
                public void onStage(LifecycleStage lifecycleStage)
                {
                    stopExecutor();
                }
            });
        }

        @Override
        public ExecutorService get()
        {
            ExecutorService myService = service;
            Preconditions.checkState(myService != null, "Thread pool %s was injected before lifecycle start or after lifecycle stop.  " +
            		"You might consider injecting a Provider instead, or maybe you forgot a Lifecycle entirely.", threadPoolName);
            return myService;
        }

        void stopExecutor()
        {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();

            ExecutorService myService = service;
            Preconditions.checkState(myService != null, "no service was ever started?");

            myService.shutdown();
            try {
                if (!myService.awaitTermination(20, TimeUnit.SECONDS))
                {
                    LOG.error("Executor service %s did not shut down after 20 seconds of waiting!", threadPoolName);
                    myService.shutdownNow();
                }
            } catch (InterruptedException e) {
                LOG.warn(e, "While awaiting executor %s termination", threadPoolName);
                Thread.currentThread().interrupt();
            }

            LOG.info("Executor service %s shutdown after %s", threadPoolName, stopWatch);
        }

        private ExecutorService create() {
            Preconditions.checkArgument(config != null, "no config injected");

            Integer queueSize = Objects.firstNonNull(config.getQueueSize(), defaultQueueSize);
            Integer minThreads = Objects.firstNonNull(config.getMinThreads(), defaultMinThreads);
            Integer maxThreads = Objects.firstNonNull(config.getMaxThreads(), defaultMaxThreads);
            TimeSpan threadTimeout = Objects.firstNonNull(config.getThreadTimeout(), defaultTimeout);
            RejectedHandler rejectedHandlerEnum = config.getRejectedHandler();
            RejectedExecutionHandler rejectedHandler = rejectedHandlerEnum != null ? rejectedHandlerEnum.getHandler() : defaultRejectedHandler;

            final BlockingQueue<Runnable> queue;
            final ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat(threadPoolName + "-%d").build();

            if (queueSize == 0) {
                queue = new SynchronousQueue<Runnable>();
            } else {
                queue = new LinkedBlockingQueue<Runnable>(queueSize);
            }

            final ExecutorService result;

            if (maxThreads <= 0) {
                result = MoreExecutors.sameThreadExecutor();
                management = new GenericExecutorManagementBean(result, new SynchronousQueue<>());
            } else {
                ThreadPoolExecutor executor = new LoggingExecutor(
                        minThreads,
                        maxThreads,
                        threadTimeout.getMillis(),
                        TimeUnit.MILLISECONDS,
                        queue,
                        threadFactory,
                        rejectedHandler);
                management = new ThreadPoolExecutorManagementBean(executor);
                result = executor;
            }

            return ThreadDelegatingDecorator.wrapExecutorService(result);
        }

        Provider<ExecutorServiceManagementBean> getManagementProvider()
        {
            return new ManagementProvider();
        }

        class ManagementProvider implements Provider<ExecutorServiceManagementBean>
        {
            @Inject
            void setInjector(Injector injector)
            {
                // Ensure that create() has been called so that management is set.
                injector.getInstance(Key.get(ExecutorService.class, annotation));
            }

            @Override
            public ExecutorServiceManagementBean get()
            {
                return management;
            }
        }
    }
}
