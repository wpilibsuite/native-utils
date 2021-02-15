package edu.wpi.first.nativeutils.utils;

import javax.annotation.Nullable;
import javax.inject.Inject;

import groovy.lang.Closure;
import org.gradle.api.Action;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.Named;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.NamedDomainObjectFactory;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Namer;
import org.gradle.api.internal.CollectionCallbackActionDecorator;
import org.gradle.api.internal.DefaultNamedDomainObjectSet;
import org.gradle.api.internal.MutationGuard;
import org.gradle.api.internal.MutationGuards;
import org.gradle.api.internal.NamedDomainObjectContainerConfigureDelegate;
import org.gradle.api.internal.collections.CollectionFilter;
import org.gradle.api.reflect.HasPublicType;
import org.gradle.api.reflect.TypeOf;
import org.gradle.internal.Actions;
import org.gradle.internal.Cast;
import org.gradle.internal.metaobject.ConfigureDelegate;
import org.gradle.internal.reflect.DirectInstantiator;
import org.gradle.internal.reflect.Instantiator;
import org.gradle.util.ConfigureUtil;

import static org.gradle.api.reflect.TypeOf.parameterizedTypeOf;
import static org.gradle.api.reflect.TypeOf.typeOf;

public class AfterAddNamedDomainObjectContainer<T> extends DefaultNamedDomainObjectSet<T>
        implements NamedDomainObjectContainer<T>, HasPublicType {

    private final NamedDomainObjectFactory<T> factory;
    private final MutationGuard crossProjectConfiguratorMutationGuard;

    public AfterAddNamedDomainObjectContainer(Class<T> type, Instantiator instantiator, NamedDomainObjectFactory<T> factory, CollectionCallbackActionDecorator collectionCallbackActionDecorator) {
        this(type, instantiator, Named.Namer.forType(type), factory, MutationGuards.identity(), collectionCallbackActionDecorator);
    }

    public AfterAddNamedDomainObjectContainer(Class<T> type, Instantiator instantiator, Namer<? super T> namer, NamedDomainObjectFactory<T> factory, MutationGuard crossProjectConfiguratorMutationGuard, CollectionCallbackActionDecorator collectionCallbackActionDecorator) {
        super(type, instantiator, namer, collectionCallbackActionDecorator);
        this.factory = factory;
        this.crossProjectConfiguratorMutationGuard = crossProjectConfiguratorMutationGuard;
    }

    @Inject
    public AfterAddNamedDomainObjectContainer(Class<T> type, NamedDomainObjectFactory<T> factory) {
        this(type, DirectInstantiator.INSTANCE, Named.Namer.forType(type), factory, MutationGuards.identity(), CollectionCallbackActionDecorator.NOOP);
    }

    @Override
    protected <S extends T> DefaultNamedDomainObjectSet<S> filtered(CollectionFilter<S> filter) {
        return Cast.uncheckedNonnullCast(getInstantiator().newInstance(DefaultNamedDomainObjectSet.class, this, filter, getInstantiator(), getNamer(), crossProjectConfiguratorMutationGuard));
    }

    @Override
    protected <I extends T> Action<? super I> withMutationDisabled(Action<? super I> action) {
        return crossProjectConfiguratorMutationGuard.withMutationDisabled(super.withMutationDisabled(action));
    }

    protected T doCreate(String name) {
        return factory.create(name);
    }

    @Override
    public T create(String name) {
        assertMutable("create(String)");
        return create(name, Actions.doNothing());
    }

    @Override
    public T maybeCreate(String name) {
        T item = findByName(name);
        if (item != null) {
            return item;
        }
        return create(name);
    }

    @Override
    public T create(String name, Closure configureClosure) {
        assertMutable("create(String, Closure)");
        return create(name, ConfigureUtil.configureUsing(configureClosure));
    }

    @Override
    public T create(String name, Action<? super T> configureAction) throws InvalidUserDataException {
        assertMutable("create(String, Action)");
        assertCanAdd(name);
        T object = doCreate(name);
        configureAction.execute(object);
        add(object);
        return object;
    }

    protected ConfigureDelegate createConfigureDelegate(Closure configureClosure) {
        return new NamedDomainObjectContainerConfigureDelegate(configureClosure, this);
    }

    @Override
    public AfterAddNamedDomainObjectContainer<T> configure(Closure configureClosure) {
        ConfigureDelegate delegate = createConfigureDelegate(configureClosure);
        ConfigureUtil.configureSelf(configureClosure, this, delegate);
        return this;
    }

    @Override
    public String getDisplayName() {
        return getTypeDisplayName() + " container";
    }

    @Override
    public TypeOf<?> getPublicType() {
        return parameterizedTypeOf(new TypeOf<NamedDomainObjectContainer<?>>() {}, typeOf(getType()));
    }

    @Override
    public NamedDomainObjectProvider<T> register(String name) throws InvalidUserDataException {
        assertMutable("register(String)");
        return createDomainObjectProvider(name, null);
    }

    @Override
    public NamedDomainObjectProvider<T> register(String name, Action<? super T> configurationAction) throws InvalidUserDataException {
        assertMutable("register(String, Action)");
        return createDomainObjectProvider(name, configurationAction);
    }

    protected NamedDomainObjectProvider<T> createDomainObjectProvider(String name, @Nullable Action<? super T> configurationAction) {
        assertCanAdd(name);
        NamedDomainObjectProvider<T> provider = Cast.uncheckedCast(
            getInstantiator().newInstance(NamedDomainObjectCreatingProvider.class, AfterAddNamedDomainObjectContainer.this, name, getType(), configurationAction)
        );
        addLater(provider);
        return provider;
    }

    // Cannot be private due to reflective instantiation
    public class NamedDomainObjectCreatingProvider<I extends T> extends AbstractDomainObjectCreatingProvider<I> {
        public NamedDomainObjectCreatingProvider(String name, Class<I> type, @Nullable Action<? super I> configureAction) {
            super(name, type, configureAction);
        }

        @Override
        protected I createDomainObject() {
            return Cast.uncheckedCast(doCreate(getName()));
        }
    }
}
