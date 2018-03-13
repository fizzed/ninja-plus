/*
 * Copyright 2018 Fizzed, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.fizzed.ninja.executors;

import java.util.concurrent.atomic.AtomicBoolean;

abstract public class AbstractNinjaExecutor implements NinjaExecutor {
 
   protected final AtomicBoolean shutdown;
   
   public AbstractNinjaExecutor() {
       this.shutdown = new AtomicBoolean(false);
   }

    @Override
    public void shutdown() {
        this.shutdown.set(true);
    }
    
}