package edu.wpi.first.nativeutils.dependencysets;

import java.util.ArrayList;
import java.util.List;

import org.gradle.api.Project;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileTree;
import org.gradle.nativeplatform.NativeBinarySpec;

import edu.wpi.first.nativeutils.NativeUtilsExtension;

public class StaticDependencySet extends WPINativeDependencySet {

    public StaticDependencySet(NativeBinarySpec binarySpec, NativeUtilsExtension nativeExtension, String headers, String libConfigName, String src, Project project, List<String> linkExcludes) {
        super(binarySpec, nativeExtension, headers, libConfigName, src, project, linkExcludes);
    }

    @Override
    protected FileCollection getFiles(boolean isRuntime) {
        if (isRuntime) {
            return m_project.files();
        }

        String platformPath = m_nativeExtension.getPlatformPath(m_binarySpec);
        String dirPath = "static";

        List<String> matchers = new ArrayList<>();
        List<String> excludes = new ArrayList<>();

        excludes.addAll(m_linkExcludes);

        if (m_binarySpec.getTargetPlatform().getOperatingSystem().isWindows()) {
            matchers.add("**/*" + platformPath + "/" + dirPath + "/*.lib");
        } else {
            matchers.add("**/*" + platformPath + "/" + dirPath + "/*.a");
        }

        FileTree staticFiles = m_libs.getAsFileTree().matching(pat -> {
            pat.include(matchers);
            pat.exclude(excludes);
        });

        return staticFiles;
    }
}
