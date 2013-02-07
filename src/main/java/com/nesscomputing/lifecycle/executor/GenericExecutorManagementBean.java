package com.nesscomputing.lifecycle.executor;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;

public class GenericExecutorManagementBean implements ExecutorServiceManagementBean
{
    private final ExecutorService service;
    private final BlockingQueue<?> queue;

    GenericExecutorManagementBean(ExecutorService service, BlockingQueue<?> queue)
    {
        this.service = service;
        this.queue = queue;
    }

    @Override
    public boolean isShutdown()
    {
        return service.isShutdown();
    }

    @Override
    public boolean isTerminated()
    {
        return service.isTerminated();
    }

    @Override
    public boolean isTerminating()
    {
        return false;
    }

    @Override
    public String getRejectedExecutionHandler()
    {
        return "null";
    }

    @Override
    public int getCorePoolSize()
    {
        return 0;
    }

    @Override
    public void setCorePoolSize(int corePoolSize)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getMaximumPoolSize()
    {
        return 0;
    }

    @Override
    public void setMaximumPoolSize(int maxPoolSize)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getKeepAliveTime()
    {
        return 0;
    }

    @Override
    public void setKeepAliveTime(String keepAliveTime)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setKeepAliveTime(long keepAliveTimeMs)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getQueueCurrentSize()
    {
        return queue.size();
    }

    @Override
    public int getQueueRemainingSize()
    {
        return queue.remainingCapacity();
    }

    @Override
    public int getCurrentPoolSize()
    {
        return 0;
    }

    @Override
    public int getCurrentlyActiveThreads()
    {
        return 0;
    }

    @Override
    public int getLargestPoolSize()
    {
        return 0;
    }

    @Override
    public long getEnqueuedTaskCount()
    {
        return 0;
    }

    @Override
    public long getCompletedTaskCount()
    {
        return 0;
    }
}
