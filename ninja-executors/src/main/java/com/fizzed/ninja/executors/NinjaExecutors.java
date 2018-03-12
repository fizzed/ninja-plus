package com.fizzed.ninja.executors;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
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
abstract public class NinjaExecutors {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());
    protected final Boolean enabled;
    protected final Integer threads;
    protected final Boolean gracefulShutdown;
    protected ExecutorService executers;

    public NinjaExecutors(NinjaProperties ninjaProperties) {
        this.enabled = ninjaProperties.getBooleanWithDefault(
            this.getConfigKey("enabled"), Boolean.TRUE);
        this.threads = ninjaProperties.getIntegerWithDefault(this.getConfigKey("threads"), 1);
        this.gracefulShutdown = ninjaProperties.getBooleanWithDefault(
            this.getConfigKey("graceful_shutdown"), ninjaProperties.isProd());
    }

    abstract public String getName();
    
    abstract public String getConfigPrefix();
    
    final protected String getConfigKey(String key) {
        return this.getConfigPrefix() + "." + key;
    }
    
    public void validate() {
        if (this.threads < 0) {
            throw new IllegalArgumentException(this.getConfigKey("threads") + " was < 0");
        }
    }
    
    @Start(order = 91)
    public void start() {
        
        
        if (this.executers == null) {
            log.info("Creating fixed thread pool of {} workers for {} service", workers, name);
            this.executers = Executors.newFixedThreadPool(
                this.workers, new ThreadFactoryBuilder()
                    .setDaemon(true)
                    .setNameFormat(this.getName() + "-%d")
                    .build());
        }
        this.postStart();
    }

    protected void postStart() {
        // do nothing
    }

    @Dispose
    public void stop() {
        if (this.executers != null) {
            if (gracefulShutdown) {
                stopGracefully();           // slower process, annoying in dev/test
            } else {
                stopNow();
            }
        } else {
            log.warn("Executors was null. Nothing to stop for {} service", name);
        }
    }

    private void stopNow() {
        log.info("Immediately stopping {} service", name);
        this.executers.shutdownNow();
    }

    private void stopGracefully() {
        try {
            log.info("Gracefully stopping {} service (waiting up to 10 secs)", name);
            this.executers.shutdown();
            if (!this.executers.awaitTermination(10000L, TimeUnit.MILLISECONDS)) {
                log.warn("{} service did not shutdown gracefully, forcing stop now!", name);
                this.executers.shutdownNow();
            }
        } catch (InterruptedException e) {
            log.warn("Interrupted while shutting down {} service", name);
        }
    }

    public void submit(Runnable runnable) {
        this.executers.submit(runnable);
    }

}