package com.fizzed.ninja.executors;

import com.google.inject.Injector;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

abstract public class NinjaProcessors extends NinjaExecutors {

    
    private final AtomicLong identifiers;
    final ConcurrentHashMap<Long,NinjaProcessor> processors;
    protected final long restartTimeout;
    
    public NinjaProcessors(Injector injector) {
        super(injector);
        this.identifiers = new AtomicLong();
        this.processors = new ConcurrentHashMap<>();
        this.restartTimeout = ninjaProperties.getIntegerWithDefault(
            this.getConfigKey("restart_timeout"), 3000);
    }

    public Collection<
    
    abstract public Class<? extends NinjaExecutor> getExecutorClass();
    
    protected void createProcessor() {
        final Class<? extends NinjaExecutor> executorClass = this.getExecutorClass();
        final Long identifier = identifiers.incrementAndGet();
        final NinjaExecutor executor = this.injector.getInstance(executorClass);
        // wrap it so we can monitor when it finishes
        final NinjaProcessor  wrappedExecutor = new NinjaProcessor(identifier, executor, this);
        this.processors.put(identifier, wrappedExecutor);
        this.executors.submit(wrappedExecutor);
    }
    
    @Override
    protected void postStart() {
        for (int i = 0; i < this.threads; i++) {
            this.createProcessor();
        }
    }
    
    @Override
    protected void stopCurrentTasks() {
        List<NinjaProcessor> wrappedExecutors = new ArrayList<>(this.processors.values());
        wrappedExecutors.forEach(wrappedExecutor -> {
            wrappedExecutor.executor.shutdown();
        });
    }

}