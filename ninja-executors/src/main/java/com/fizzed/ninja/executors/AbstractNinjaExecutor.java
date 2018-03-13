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
import java.util.concurrent.atomic.AtomicReference;

abstract public class AbstractNinjaExecutor implements NinjaExecutor {

    protected final AtomicReference<Thread> executingThreadRef;
    protected final AtomicBoolean shutdown;
    protected final boolean interruptOnShutdown;

    public AbstractNinjaExecutor() {
        this(false);
    }
    
    public AbstractNinjaExecutor(boolean interruptOnShutdown) {
        this.executingThreadRef = new AtomicReference<>();
        this.shutdown = new AtomicBoolean(false);
        this.interruptOnShutdown = interruptOnShutdown;
    }

    @Override
    public Thread getExecutingThread() {
        return this.executingThreadRef.get();
    }
    
    @Override
    public void shutdown() {
        this.shutdown.set(true);
        if (this.interruptOnShutdown) {
            this.interrupt();
        }
    }
    
    public void interrupt() {
        final Thread executingThread = this.executingThreadRef.get();
        if (executingThread != null) {
            executingThread.interrupt();
        }
    }

    @Override
    public void run() {
        this.executingThreadRef.set(Thread.currentThread());
        try {
            this.execute();
        } finally {
            this.executingThreadRef.set(null);
        }
    }
    
    abstract public void execute();
    
}