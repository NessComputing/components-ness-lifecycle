package com.nesscomputing.lifecycle.executor;

import org.weakref.jmx.Managed;

public interface ExecutorServiceManagementBean
{
    @Managed boolean isShutdown();
    @Managed boolean isTerminated();
    @Managed boolean isTerminating();
    @Managed String getRejectedExecutionHandler();
    @Managed int getCorePoolSize();
    @Managed void setCorePoolSize(int corePoolSize);
    @Managed int getMaximumPoolSize();
    @Managed void setMaximumPoolSize(int maximumPoolSize);
    @Managed long getKeepAliveTime();
    @Managed void setKeepAliveTime(String keepAliveTime);
    @Managed void setKeepAliveTime(long keepAliveTimeMs);
    @Managed int getQueueCurrentSize();
    @Managed int getQueueRemainingSize();
    @Managed int getCurrentPoolSize();
    @Managed int getCurrentlyActiveThreads();
    @Managed int getLargestPoolSize();
    @Managed long getEnqueuedTaskCount();
    @Managed long getCompletedTaskCount();
}
