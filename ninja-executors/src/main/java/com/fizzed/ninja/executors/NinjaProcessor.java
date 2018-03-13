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

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.function.Consumer;

class NinjaProcessor implements Runnable {
    
    private final Long identifier;
    private final NinjaExecutor executor;
    private final long delayMillis;
    private 

    public NinjaProcessor(
            Long identifier,
            NinjaExecutor executor,
            long delayMillis,
            UncaughtExceptionHandler uncaughtExceptionHandler,
            Consumer<Long> onExitHandler) {
        this.identifier = identifier;
        this.executor = executor;
        this.delayMillis = delayMillis;
    }
    
    

    @Override
    public void run() {
        try {
            if (this.delayMillis > 0) {
                Thread.sleep(this.delayMillis);
            }
            this.executor.run();
        } catch (Throwable t) {
            processors.uncaughtException(Thread.currentThread(), t);
        } finally {
            processors.processors.remove(this.identifier);
            if (!executors.isShutdown()) {
                // restart it
            }
        }
    }
    
}
