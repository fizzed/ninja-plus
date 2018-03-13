package com.fizzed.ninja.executors;

import com.google.inject.Injector;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

abstract public class NinjaProcessors extends NinjaExecutors {

    private class WrappedNinjaExecutor implements Runnable {
        
        private final Long identifier;
        private final NinjaExecutor executor;

        public WrappedNinjaExecutor(Long identifier, NinjaExecutor executor) {
            this.identifier = identifier;
            this.executor = executor;
        }

        @Override
        public void run() {
            try {
                this.executor.run();
            } catch (Exception e) {
                NinjaProcessors.this.uncaughtException(Thread.currentThread(), e);
            } finally {
                processors.remove(this.identifier);
            }
        }

    }
    
    private final AtomicLong identifiers;
    private final ConcurrentHashMap<Long,WrappedNinjaExecutor> processors;
    
    public NinjaProcessors(Injector injector) {
        super(injector);
        this.identifiers = new AtomicLong();
        this.processors = new ConcurrentHashMap<>();
    }

    abstract public Class<? extends NinjaExecutor> getExecutorClass();
    
    protected void createProcessor() {
        final Class<? extends NinjaExecutor> executorClass = this.getExecutorClass();
        final Long identifier = identifiers.incrementAndGet();
        final NinjaExecutor executor = this.injector.getInstance(executorClass);
        // wrap it so we can monitor when it finishes
        final WrappedNinjaExecutor  wrappedExecutor = new WrappedNinjaExecutor(identifier, executor);
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
        List<WrappedNinjaExecutor> wrappedExecutors = new ArrayList<>(this.processors.values());
        wrappedExecutors.forEach(wrappedExecutor -> {
            wrappedExecutor.executor.shutdown();
        });
    }
    
}