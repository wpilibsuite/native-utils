package edu.wpi.first.deployutils;

import groovy.lang.Closure;

public class ClosureUtils {
    public static <T> Object delegateCall(Object object, Closure<T> closure, Object... args) {
        closure.setResolveStrategy(Closure.DELEGATE_FIRST);
        closure.setDelegate(object);
        Object[] passArgs = new Object[args.length + 1];
        passArgs[0] = object;
        for (int i = 0; i < args.length; i++) {
            passArgs[i + 1] = args[i];
        }
        return closure.call(passArgs);
    }

    public static <T> Object delegateCall(Object object, Closure<T> closure) {
        closure.setResolveStrategy(Closure.DELEGATE_FIRST);
        closure.setDelegate(object);
        return closure.call(object);
    }
}
