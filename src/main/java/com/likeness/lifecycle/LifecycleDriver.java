/**
 * Copyright (C) 2011 Ness Computing, Inc.
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
package com.likeness.lifecycle;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

/**
 * A simple LifecycleDriver. Controls switching between lifecycle stages, allows stages to be fired multiple times and does not enforce the sequence.
 */
public class LifecycleDriver implements LifecycleListener
{
    /** List of all stages in this cycle. */
    private final LinkedList<LifecycleStage> lifecycleStages = new LinkedList<LifecycleStage>();

    /** Map to find the next stage. */
    private final Map<LifecycleStage, LifecycleStage> stageMap = new HashMap<LifecycleStage, LifecycleStage>();

    /** The next stage to fire */
    private LifecycleStage nextStage = null;

    /**
     * Builds a new Lifecycler. Stages can be passed as C'tor arguments.
     * @param stages Stages to add to the Lifecycle.
     */
    public LifecycleDriver(@Nonnull final LifecycleStage ...stages)
    {
    	for (LifecycleStage stage : stages) {
    		if (lifecycleStages.size() > 0) {
    			stageMap.put(lifecycleStages.getLast(), stage);
    		}

    		stageMap.put(stage, null);
    		lifecycleStages.add(stage);
    	}

        nextStage = lifecycleStages.getFirst();
    }

    /**
     * Returns a list of stages in this lifecycle driver.
     */
    public List<LifecycleStage> getStages()
    {
        return Collections.unmodifiableList(lifecycleStages);
    }

    /**
     * Returns the next stage that will be executed.
     */
    public LifecycleStage getNextStage()
    {
        return nextStage;
    }

    @Override
    public void onStage(@Nonnull final LifecycleStage stage)
    {
        if (stageMap.containsKey(stage)) {
            nextStage = stageMap.get(stage);
        }
        else {
            throw new IllegalStateException("Stage '" + stage.getName() + "' is not in this lifecycle driver!");
       }
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder(this.getClass().getName()).append('[');

        for (Iterator<LifecycleStage> it = lifecycleStages.iterator(); it.hasNext(); ) {
            LifecycleStage le = it.next();
            boolean ne = nextStage != null && le.equals(nextStage);
            if (ne) {
                sb.append("*");
            }

            sb.append(le.getName());

            if (ne) {
                sb.append("*");
            }

            if (it.hasNext()) {
                sb.append(", ");
            }
        }
        sb.append(']');
        return sb.toString();
    }
}
