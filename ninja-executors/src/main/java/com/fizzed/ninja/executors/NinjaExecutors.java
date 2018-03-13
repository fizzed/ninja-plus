package com.fizzed.ninja.executors;

import com.fizzed.crux.util.StopWatch;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Injector;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import ninja.lifecycle.Dispose;
import ninja.lifecycle.Start;
import ninja.utils.NinjaProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * service.enabled = 
 * service.name
 * service.threads
 * 
 * 
 * @author jjlauer
 */
abstract public class NinjaExecutors implements UncaughtExceptionHandler {

    protected final Injector injector;
    protected final Logger log = LoggerFactory.getLogger(this.getClass());
    protected final boolean enabled;
    protected final int threads;
    protected final boolean gracefulStop;
    protected final long gracefulStopTimeout;
    protected ExecutorService executors;

    public NinjaExecutors(
            Injector injector) {
        
        this.injector = injector;
        NinjaProperties ninjaProperties = injector.getInstance(NinjaProperties.class);
        this.enabled = ninjaProperties.getBooleanWithDefault(
            this.getConfigKey("enabled"), Boolean.TRUE);
        this.threads = ninjaProperties.getIntegerWithDefault(
            this.getConfigKey("threads"), 1);
        this.gracefulStop = ninjaProperties.getBooleanWithDefault(
            this.getConfigKey("graceful_stop"), Boolean.TRUE);
        this.gracefulStopTimeout = ninjaProperties.getIntegerWithDefault(
            this.getConfigKey("graceful_stop_timeout"), 10000);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getThreads() {
        return threads;
    }

    public boolean isGracefulStop() {
        return gracefulStop;
    }

    abstract public String getName();
    
    abstract public String getConfigPrefix();
    
    final protected String getConfigKey(String key) {
        return this.getConfigPrefix() + "." + key;
    }
    
    public void validate() {
        if (this.threads < 1) {
            throw new IllegalArgumentException(this.getConfigKey("threads") + " was < 1");
        }
    }
    
    public void preStart() {
        // do nothing by default
    }
    
    @Start(order = 91)
    public void start() {
        if (!this.enabled) {
            log.info("{} disabled (will not start)", this.getName());
            return;
        }
        
        // already started?
        if (this.executors != null) {
            log.warn("{} already started (will not start multiple times", this.getName());
            return;
        }
        
        this.validate();
        
        this.preStart();

        log.info("{} starting {} threads", this.getName(), this.threads);
        this.executors = Executors.newFixedThreadPool(
            this.threads, new ThreadFactoryBuilder()
                .setDaemon(true)
                .setNameFormat(this.getName() + "-%d")
                .setUncaughtExceptionHandler(this)
                .build());
        
        this.postStart();
    }

    protected void postStart() {
        // do nothing by default
    }

    @Dispose
    public void stop() {
        if (!this.enabled) {
            // nothing to do
            return;
        }
        
        // already stopped?
        if (this.executors == null) {
            log.warn("{} already stopped (will not stop multiple times", this.getName());
            return;
        }
        
        final StopWatch timer = StopWatch.timeMillis();
        try {
            if (this.gracefulStop) {
                this.stopGracefully();
            } else {
                this.stopNow();
            }
        } catch (InterruptedException e) {
            log.warn("{} interrupted while shutting down...", e);
        } finally {
            this.executors = null;
            log.info("{} stopped (in {})", this.getName(), timer);
        }
    }

    protected void stopNow() {
        log.info("{} immediately stopping...", this.getName());
        this.executors.shutdownNow();
    }

    protected void stopGracefully() throws InterruptedException {
        log.info("{} gracefully stopping... (will wait up to {} ms)", this.getName(), this.gracefulStopTimeout);
        this.executors.shutdown();
        
        this.stopCurrentTasks();
        
        if (!this.executors.awaitTermination(this.gracefulStopTimeout, TimeUnit.MILLISECONDS)) {
            log.warn("{} timed out gracefully stopping. Forcing stop now!", this.getName());
            this.executors.shutdownNow();
        }
    }
    
    protected void stopCurrentTasks() {
        // do nothing by default
    }
    
    @Override
    public void uncaughtException(Thread thread, Throwable e) {
        log.error("{} had thread {} with an uncaught exception", thread.getName(), e);
    }

}