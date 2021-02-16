package edu.wpi.first.nativeutils.dependencies.configs;

import javax.annotation.Nullable;
import javax.inject.Inject;

import org.gradle.api.Action;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.internal.CollectionCallbackActionDecorator;
import org.gradle.api.internal.DefaultNamedDomainObjectSet;
import org.gradle.api.model.ObjectFactory;
import org.gradle.internal.Cast;
import org.gradle.internal.reflect.DirectInstantiator;

public class NativeDependencyContainer extends DefaultNamedDomainObjectSet<NativeDependency> {

    private final ObjectFactory objects;

    @Inject
    public NativeDependencyContainer(ObjectFactory objects) {
        super(NativeDependency.class, DirectInstantiator.INSTANCE, CollectionCallbackActionDecorator.NOOP);
        this.objects = objects;
    }

    public <T extends NativeDependency> NamedDomainObjectProvider<T> dependency(String name, Class<T> type, final Action<? super T> config) throws InvalidUserDataException {
        assertMutable("dependency(String, Class, Action)");
        return createDomainObjectProvider(name, type, config);
    }

    public NamedDomainObjectProvider<WPIMavenDependency> wpiDependency(String name, final Action<? super WPIMavenDependency> config) throws InvalidUserDataException {
        return dependency(name, WPIMavenDependency.class, config);
    }

    private <T extends NativeDependency> NamedDomainObjectProvider<T> createDomainObjectProvider(String name, Class<T> type, @Nullable Action<? super T> configurationAction) {
        assertCanAdd(name);
        NamedDomainObjectProvider<T> provider = Cast.uncheckedCast(
            getInstantiator().newInstance(NamedDomainObjectCreatingProvider.class, NativeDependencyContainer.this, name, type, configurationAction)
        );
        addLater(provider);
        return provider;
    }

    private <T extends NativeDependency> T doCreate(String name, Class<T> type) {
        return objects.newInstance(type, name);
    }

    public class NamedDomainObjectCreatingProvider<I extends NativeDependency> extends AbstractDomainObjectCreatingProvider<I> {

        public NamedDomainObjectCreatingProvider(String name, Class<I> type, @Nullable Action<? super I> configureAction) {
            super(name, type, configureAction);
        }

        @Override
        protected I createDomainObject() {
            return Cast.uncheckedCast(doCreate(getName(), getType()));
        }
    }
}
