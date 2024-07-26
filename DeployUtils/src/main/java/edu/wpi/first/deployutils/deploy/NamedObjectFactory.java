package edu.wpi.first.deployutils.deploy;

import org.gradle.api.ExtensiblePolymorphicDomainObjectContainer;
import org.gradle.api.NamedDomainObjectFactory;
import org.gradle.api.model.ObjectFactory;

import edu.wpi.first.deployutils.deploy.target.RemoteTarget;

public class NamedObjectFactory<T> implements NamedDomainObjectFactory<T> {
    private final ObjectFactory objects;
    private final RemoteTarget target;
    private final Class<T> cls;

    @Override
    public T create(String name) {
        return objects.newInstance(cls, name, target);
    }

    public NamedObjectFactory(ObjectFactory objects, RemoteTarget target, Class<T> cls) {
        this.objects = objects;
        this.target = target;
        this.cls = cls;
    }

    public static <T, U extends T> void registerType(Class<U> cls, ExtensiblePolymorphicDomainObjectContainer<T> registry, RemoteTarget remote, ObjectFactory objects) {
        registry.registerFactory(cls, new NamedObjectFactory<U>(objects, remote, cls));
    }
}
