package controllers;

import javax.inject.Inject;
import javax.inject.Singleton;
import ninja.Result;
import ninja.Results;
import services.Consumers;

@Singleton
public class ApplicationController {
    
    private final Consumers consumers;
    
    @Inject
    public ApplicationController(Consumers consumers) {
        this.consumers = consumers;
    }
    
    public Result home() {
        return Results.ok()
            .renderRaw("Consumers: " + consumers.getThreads());
    }
    
}