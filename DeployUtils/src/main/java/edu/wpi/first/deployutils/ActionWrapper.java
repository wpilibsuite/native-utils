package edu.wpi.first.deployutils;

import org.gradle.api.Action;

import groovy.lang.Closure;

public class ActionWrapper<T> implements Action<T> {
    private final Closure<T> closure;

    public ActionWrapper(Closure<T> closure) {
        this.closure = closure;
    }

    @Override
    public void execute(T t) {
        ClosureUtils.delegateCall(t, closure);
    }
}
