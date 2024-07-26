package edu.wpi.first.deployutils;

import java.util.function.Predicate;

import groovy.lang.Closure;

public class PredicateWrapper<T> implements Predicate<T> {
    private final Closure<T> closure;

    public PredicateWrapper(Closure<T> closure) {
        this.closure = closure;
    }

    @Override
    public boolean test(T t) {
        return ((Boolean)ClosureUtils.delegateCall(t, closure)).booleanValue();
    }
}

