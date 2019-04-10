package edu.wpi.first.nativeutils.dependencysets;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.gradle.api.file.FileTree;
import org.gradle.api.file.FileCollection;
import org.gradle.api.Project;
import org.gradle.nativeplatform.NativeDependencySet;

import edu.wpi.first.nativeutils.NativeUtilsExtension;

import org.gradle.api.tasks.util.PatternFilterable;
import org.gradle.nativeplatform.NativeBinarySpec;


public class SharedDependencySet extends WPINativeDependencySet {

    public SharedDependencySet(NativeBinarySpec binarySpec, NativeUtilsExtension nativeExtension, String headers, String libConfigName, String src, Project project, List<String> linkExcludes) {
        super(binarySpec, nativeExtension, headers, libConfigName, src, project, linkExcludes);
    }

    @Override
    protected FileCollection getFiles(boolean isRuntime) {
        String platformPath = m_nativeExtension.getPlatformPath(m_binarySpec);
        String dirPath = "shared";

        List<String> debugMatchers = new ArrayList<>();
        List<String> debugExcludes = new ArrayList<>();

        if (m_binarySpec.getTargetPlatform().getOperatingSystem().isWindows() && !isRuntime) {
            debugMatchers.add("**/*" + platformPath + "/" + dirPath + "/*.pdb");
        } else {
            debugMatchers.add("**/*" + platformPath + "/" + dirPath + "/*.so.debug");
            debugMatchers.add("**/*" + platformPath + "/" + dirPath + "/*.so.*.debug");
        }


        FileTree debugFiles = m_libs.getAsFileTree().matching(pat -> {
            pat.include(debugMatchers);
            pat.exclude(debugExcludes);
        });

        List<String> matchers = new ArrayList<>();
        List<String> excludes = new ArrayList<>();

        if (!isRuntime) {
            excludes.addAll(m_linkExcludes);
        }

        if (m_binarySpec.getTargetPlatform().getOperatingSystem().isWindows() && !isRuntime) {
            matchers.add("**/*" + platformPath + "/" + dirPath + "/*.lib");
        } else if (m_binarySpec.getTargetPlatform().getOperatingSystem().isWindows()) {
            matchers.add("**/*" + platformPath + "/" + dirPath + "/*.dll");
        } else {
            matchers.add("**/*" + platformPath + "/" + dirPath + "/*.dylib");
            matchers.add("**/*" + platformPath + "/" + dirPath + "/*.so");
            matchers.add("**/*" + platformPath + "/" + dirPath + "/*.so.*");
            matchers.add("**/*" + platformPath + "/" + dirPath + "/*.so.debug");
            matchers.add("**/*" + platformPath + "/" + dirPath + "/*.so.*.debug");
        }

        FileTree sharedFiles = m_libs.getAsFileTree().matching(pat -> {
            pat.include(matchers);
            pat.exclude(excludes);
        });

        if (isRuntime) {
            return sharedFiles.plus(debugFiles);
        } else {
            return sharedFiles;
        }
    }
}
