package edu.wpi.first.nativeutils.dependencies;

import java.util.Objects;

import javax.inject.Inject;

import org.gradle.api.GradleException;
import org.gradle.api.Named;
import org.gradle.api.NamedDomainObjectCollection;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.ProjectLayout;
import org.gradle.nativeplatform.NativeBinarySpec;
import org.gradle.nativeplatform.NativeDependencySet;

public class DelegatedDependencySet implements NativeDependencySet, Named {
    private FileCollection includeRoots;
    private FileCollection linkFiles;
    private FileCollection runtimeFiles;
    private FileCollection sourceRoots;
    private final String name;
    private final boolean required;
    private final NamedDomainObjectCollection<NativeDependency> dependencyCollection;
    private boolean resolved = false;
    private final NativeBinarySpec binary;

    @Inject
    public ProjectLayout getProjectLayout() {
        throw new UnsupportedOperationException();
    }

    @Inject
    public DelegatedDependencySet(String name, NamedDomainObjectCollection<NativeDependency> dependencyCollection, boolean required, NativeBinarySpec binary) {
        this.name = name;
        this.required = required;
        this.dependencyCollection = Objects.requireNonNull(dependencyCollection, "Must have a valid depenedency collection");
        this.binary = binary;
    }

    public boolean isRequired() {
        return required;
    }

    @Override
    public String getName() {
        return name;
    }

    private void resolve() {
        if (resolved) {
            return;
        }
        resolved = true;
        NativeDependency resolvedDependency = dependencyCollection.findByName(this.name);
        if (resolvedDependency == null) {
            if (required) {
                // TODO better exceptions
                throw new GradleException("Missing Dependency " + name);
            }
            ProjectLayout layout = getProjectLayout();
            includeRoots = layout.files();
            sourceRoots = layout.files();
            linkFiles = layout.files();
            runtimeFiles = layout.files();
            return;
        }

        ResolvedNativeDependency resolvedDep = resolvedDependency.resolveNativeDependency(binary);

        if (resolvedDep == null) {
            if (required) {
                // TODO better exceptions
                throw new GradleException("Missing Dependency " + resolvedDependency.getName());
            }
            ProjectLayout layout = getProjectLayout();
            includeRoots = layout.files();
            sourceRoots = layout.files();
            linkFiles = layout.files();
            runtimeFiles = layout.files();
            return;
        }

        includeRoots = resolvedDep.getIncludeRoots();
        sourceRoots = resolvedDep.getSourceRoots();
        linkFiles = resolvedDep.getLinkFiles();
        runtimeFiles = resolvedDep.getRuntimeFiles();
    }

    public FileCollection getSourceRoots() {
        resolve();
        return sourceRoots;
    }

    @Override
    public FileCollection getIncludeRoots() {
        resolve();
        return includeRoots;
    }

    @Override
    public FileCollection getLinkFiles() {
        resolve();
        return linkFiles;
    }

    @Override
    public FileCollection getRuntimeFiles() {
        resolve();
        return runtimeFiles;
    }

}
