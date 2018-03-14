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
package com.fizzed.ninja.util;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.ConsoleAppender;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.LoggerFactory;

public class Logback {
    
    static public void detachConsoleAppenders(Logger log, long delayMillis) {
        // delay detaching console appenders
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            try {
                Thread.sleep(delayMillis);
                detachConsoleAppenders(log);
            } catch (InterruptedException e) {
                // do nothing
            } finally {
                executor.shutdownNow();
            }
        });
    }
    
    static public void detachConsoleAppenders(Logger log) {
        Logger logger = (Logger)LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        List<Appender> appendersToRemove = new ArrayList<>();
        logger.iteratorForAppenders().forEachRemaining(appender -> {
            if (appender instanceof ConsoleAppender) {
                appendersToRemove.add(appender);
                log.info("Will detach console logging appender {}", appender.getName());
            }
        });
        appendersToRemove.forEach(appender -> {
            logger.detachAppender(appender);
        });
    }
    
}