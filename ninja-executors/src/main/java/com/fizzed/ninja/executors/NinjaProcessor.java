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
import java.util.concurrent.atomic.AtomicReference;

public class NinjaProcessor extends AbstractNinjaExecutor {
    
    private final Long identifier;
    private final Runnable runnable;
    private final long delayMillis;
    private volatile boolean delaying;
    private final UncaughtExceptionHandler uncaughtExceptionHandler;
    private final Runnable exitHandler;

    public NinjaProcessor(
            Long identifier,
            Runnable runnable,
            long delayMillis,
            UncaughtExceptionHandler uncaughtExceptionHandler,
            Runnable exitHandler) {
        super(false);
        this.identifier = identifier;
        this.runnable = runnable;
        this.delayMillis = delayMillis;
        this.delaying = false;
        this.uncaughtExceptionHandler = uncaughtExceptionHandler;
        this.exitHandler = exitHandler;
    }

    public Long getIdentifier() {
        return identifier;
    }

    @Override
    public void shutdown() {
        super.shutdown();
        if (this.runnable instanceof NinjaExecutor) {
            ((NinjaExecutor)this.runnable).shutdown();
        }
        if (this.delaying) {
            this.interrupt();
        }
    }
    
    protected void delay(long millis) {
        if (millis <= 0) {
            return;
        }
        this.delaying = true;
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            // ignore these
        } finally {
            this.delaying = false;
        }
    }
    
    @Override
    @SuppressWarnings("UseSpecificCatch")
    public void execute() {
        try {
            this.delay(this.delayMillis);
            if (!this.shutdown.get()) {
                this.runnable.run();
            }
        } catch (Throwable throwable) {
            this.uncaughtExceptionHandler.uncaughtException(Thread.currentThread(), throwable);
        } finally {
            this.exitHandler.run();
        }
    }
    
}