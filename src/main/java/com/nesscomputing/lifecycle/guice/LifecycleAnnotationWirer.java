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

import com.google.inject.Inject;
import com.nesscomputing.lifecycle.Lifecycle;


/**
 * Once the Lifecycle is available, present it to the {@link LifecycleAnnotationFinder}
 */
class LifecycleAnnotationWirer {
    @Inject
    LifecycleAnnotationWirer(Lifecycle lifecycle, LifecycleAnnotationFinder finder) {
        finder.lifecycleAvailable(lifecycle);
    }
}
