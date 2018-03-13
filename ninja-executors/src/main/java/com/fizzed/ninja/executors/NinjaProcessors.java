package com.fizzed.ninja.executors;

import com.google.inject.Injector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

abstract public class NinjaProcessors extends NinjaExecutors {

    private final AtomicLong identifiers;
    private final ConcurrentHashMap<Long,NinjaProcessor> processors;
    protected final long initialDelay;
    protected final long restartDelay;
    
    public NinjaProcessors(Injector injector) {
        super(injector);
        this.identifiers = new AtomicLong();
        this.processors = new ConcurrentHashMap<>();
        this.initialDelay = ninjaProperties.getIntegerWithDefault(
            this.getConfigKey("initial_delay"), 0);
        this.restartDelay = ninjaProperties.getIntegerWithDefault(
            this.getConfigKey("restart_delay"), 3000);
    }

    @Override
    public String getName() {
        return this.getRunnableClass().getSimpleName();
    }
    
    abstract public Class<? extends Runnable> getRunnableClass();
    
    protected NinjaProcessor createProcessor(long delayMillis) {
        final Class<? extends Runnable> runnableClass = this.getRunnableClass();
        final Long identifier = identifiers.incrementAndGet();
        final Runnable runnable = this.injector.getInstance(runnableClass);
        return new NinjaProcessor(identifier, runnable, delayMillis, this, () -> {
            this.processors.remove(identifier);
            // if not shutting down then restart the processor
            if (!this.executors.isShutdown()) {
                log.warn("{} restarting processor in {} ms after uncaught exception",
                    this.getName(), this.restartDelay);
                NinjaProcessor restartedProcessor = this.createProcessor(this.restartDelay);
                this.submitProcessor(restartedProcessor);
            }
        });
    }
    
    protected void submitProcessor(NinjaProcessor processor) {
        this.processors.put(processor.getIdentifier(), processor);
        this.executors.submit(processor);
    }
    
    @Override
    protected void postStart() {
        for (int i = 0; i < this.threads; i++) {
            NinjaProcessor processor = this.createProcessor(this.initialDelay);
            this.submitProcessor(processor);
        }
    }
    
    @Override
    protected void stopExecuting() {
        this.processors.values().forEach(NinjaProcessor::shutdown);
    }

}