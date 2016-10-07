Ness Computing Lifecycle Component
==================================

Component Charter
-----------------

* Control the startup and shutdown of all pieces of code in a larger system.
* Offer defined stages in the lifecycle of a system.
* Offer arbitrary stages and allow easy creation of custom lifecycles.

Component Level
---------------

*Foundation component*

* Allowed dependencies: logging component.
* Should minimize its dependency footprint.

Lifecycled Thread Pools
-----------------------

This component provides a `LifecycledThreadPoolModule` which makes providing
`ExecutorService` very easy.  The configuration is specified in 
`ThreadPoolConfiguration`.  The default configuration is generally reasonable
for pools that will handle medium to large size tasks.  Some tuning may be appropriate
if the pool handles many very small tasks.

The module is supplied with a pool name, and will bind `@Named(poolName) ExecutorService service`.
The executor will shut down at lifecycle stop, and will warn if tasks take too long to finish.

Executors will expose running statistics over JMX as `com.nesscomputing.thread-pool:name=poolName`.

----
Copyright (C) 2012 Ness Computing, Inc.
