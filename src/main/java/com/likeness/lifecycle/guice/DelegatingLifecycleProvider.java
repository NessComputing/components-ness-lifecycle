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

import javax.annotation.Nonnull;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;

/**
 * Augments an existing provider or provider class with the Lifecycle.
 */
public class DelegatingLifecycleProvider<T> extends AbstractLifecycleProvider<T>
{
    private final Class<? extends Provider<T>> providerClass;

    private Provider<T> delegate = null;

    /**
     * Returns a LifecycleProvider that delegates to an existing provider. The provider is managed by Guice
     * and injected.
     */
    public static final <U> LifecycleProvider<U> of(@Nonnull final Class<? extends Provider<U>> providerClass)
    {
        return new DelegatingLifecycleProvider<U>(providerClass, null);
    }

    /**
     * Returns a LifecycleProvider that delegates to an existing provider instance.
     */
    public static final <U> LifecycleProvider<U> of(@Nonnull final Provider<U> delegate)
    {
        return new DelegatingLifecycleProvider<U>(null, delegate);
    }

    private DelegatingLifecycleProvider(final Class<? extends Provider<T>> providerClass, Provider<T> delegate)
    {
        this.providerClass = providerClass;
        this.delegate = delegate;
    }

    @Inject
    public void setInjector(@Nonnull final Injector injector)
    {
        if (delegate == null) {
            delegate = injector.getInstance(providerClass);
        }
    }

    @Override
    public T internalGet()
    {
        return delegate.get();
    }
}
