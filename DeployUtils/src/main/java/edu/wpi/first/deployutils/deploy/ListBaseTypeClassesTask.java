package edu.wpi.first.deployutils.deploy;

import java.util.Set;

import org.gradle.api.DefaultTask;
import org.gradle.api.internal.PolymorphicDomainObjectContainerInternal;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.UntrackedTask;

import edu.wpi.first.deployutils.deploy.cache.CacheMethod;
import edu.wpi.first.deployutils.deploy.target.RemoteTarget;

@UntrackedTask(because = "Helper task")
public class ListBaseTypeClassesTask extends DefaultTask {
    private DeployExtension extension;

    public void setExtension(DeployExtension extension) {
        this.extension = extension;
    }

    @SuppressWarnings("unchecked")
    @TaskAction
    public void execute() {

        getLogger().lifecycle("Type classes for targets and cache methods");

        getLogger().lifecycle("Target Type Classes (getTargetTypeClass):");

        PolymorphicDomainObjectContainerInternal<RemoteTarget> internalTargets =
            (PolymorphicDomainObjectContainerInternal<RemoteTarget>) extension.getTargets();
        Set<? extends java.lang.Class<? extends RemoteTarget>> targetTypeSet = internalTargets.getCreateableTypes();
        for (Class<? extends RemoteTarget> targetType : targetTypeSet) {
            getLogger().lifecycle("\t{}", targetType.getSimpleName());
        }

        getLogger().lifecycle("");
        getLogger().lifecycle("Cache Method Type Classes (getCacheTypeClass):");

        PolymorphicDomainObjectContainerInternal<CacheMethod> internalCache =
            (PolymorphicDomainObjectContainerInternal<CacheMethod>) extension.getCache();
        Set<? extends java.lang.Class<? extends CacheMethod>> cacheTypeSet = internalCache.getCreateableTypes();
        for (Class<? extends CacheMethod> cacheType : cacheTypeSet) {
            getLogger().lifecycle("\t{}", cacheType.getSimpleName());
        }

    }
}
