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
package services;

import com.fizzed.ninja.executors.NinjaExecutor;
import com.fizzed.ninja.executors.NinjaProcessors;
import com.google.inject.Injector;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class Consumers extends NinjaProcessors {

    @Inject
    public Consumers(Injector injector) {
        super(injector);
    }

    @Override
    public String getName() {
        return "Consumer";
    }

    @Override
    public String getConfigPrefix() {
        return "consumer";
    }

    @Override
    public Class<? extends NinjaExecutor> getExecutorClass() {
        return Consumer.class;
    }
    
}