package edu.wpi.first.nativeutils.configs.internal;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import org.gradle.api.Project;
import org.gradle.api.file.FileCollection;
import org.gradle.nativeplatform.NativeDependencySet;

import edu.wpi.first.deployutils.files.IDirectoryTree;

public class NativeLibraryDependencySet extends BaseLibraryDependencySet implements NativeDependencySet {

    private final Project         project;
    private boolean         resolvedDebug;
    private final FileCollection  linkLibs, dynamicLibs, debugLibs;
    private final IDirectoryTree  headers, sources;
    private final List<String>    systemLibs;
    private final Set<String>  targetPlatforms;
    private final Set<String>          flavors;
    private final Set<String>       buildTypes;

    public Project getProject() {
        return project;
    }

    public boolean isResolvedDebug() {
        return resolvedDebug;
    }

    public FileCollection getLinkLibs() {
        return linkLibs;
    }

    public FileCollection getDynamicLibs() {
        return dynamicLibs;
    }

    public FileCollection getDebugLibs() {
        return debugLibs;
    }

    public IDirectoryTree getHeaders() {
        return headers;
    }

    public IDirectoryTree getSources() {
        return sources;
    }

    public List<String> getSystemLibs() {
        return systemLibs;
    }

    @Override
    public Set<String> getTargetPlatforms() {
        return targetPlatforms;
    }

    @Override
    public Set<String> getFlavors() {
        return flavors;
    }

    @Override
    public Set<String> getBuildTypes() {
        return buildTypes;
    }

    @Inject
    public NativeLibraryDependencySet(Project project, String name,
                          IDirectoryTree headers, IDirectoryTree sources,
                          FileCollection linkLibs, FileCollection dynamicLibs,
                          FileCollection debugLibs, List<String> systemLibs,
                          Set<String> targetPlatforms, Set<String> flavors,
                          Set<String> buildTypes) {
        super(name);
        this.project = project;

        this.headers = headers;
        this.sources = sources;

        this.linkLibs = linkLibs;
        this.dynamicLibs = dynamicLibs;
        this.debugLibs = debugLibs;
        this.systemLibs = systemLibs;

        this.targetPlatforms = targetPlatforms;
        this.flavors = flavors;
        this.buildTypes = buildTypes;
    }

    @Override
    public FileCollection getIncludeRoots() {
        Callable<Set<File>> cbl = () -> headers.getDirectories();
        return project.files(cbl);
    }

    public FileCollection getSourceRoots() {
        Callable<Set<File>> cbl = () -> sources.getDirectories();
        return project.files(cbl);
    }

    @Override
    public FileCollection getLinkFiles() {
        Callable<FileCollection> cbl = () -> {
            if (!resolvedDebug) {
                debugLibs.getFiles();
                resolvedDebug = true;
            }
            return linkLibs;
        };
        return project.files(cbl);
    }

    @Override
    public FileCollection getRuntimeFiles() {
        // Needed to have a flat set, as otherwise the install tasks do not work
        // properly
        Callable<Set<File>> cbl = () -> dynamicLibs.getFiles();
        return project.files(cbl);
    }

    public FileCollection getDebugFiles() {
        return debugLibs;
    }

    // public boolean appliesTo(Flavor flav, BuildType btype, NativePlatform plat) {
    //     if (flavor != null && !flavor.equals(flav))
    //         return false;
    //     if (buildType != null && !buildType.equals(btype))
    //         return false;
    //     if (targetPlatform == null || !targetPlatform.equals(plat))
    //         return false;

    //     return true;
    // }

    // public boolean appliesTo(String flavorName, String buildTypeName, String platformName) {
    //     if (flavors != null && !flavors.isEmpty() && !flavors.contains(flavorName))
    //         return false;
    //     if (buildTypes != null && !buildTypes.isEmpty() && !buildTypes.contains(buildTypeName))
    //         return false;
    //     if (targetPlatforms != null && !targetPlatforms.isEmpty() && !targetPlatforms.contains(platformName))
    //         return false;

    //     return true;
    // }

    @Override
    public String toString() {
        return "ETNativeDepSet[" + getName() + "]";
    }

}
