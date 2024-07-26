package edu.wpi.first.deployutils.deploy.artifact;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.gradle.api.file.FileTree;
import org.gradle.api.provider.Property;

import edu.wpi.first.deployutils.deploy.cache.CacheMethod;
import edu.wpi.first.deployutils.deploy.context.DeployContext;
import edu.wpi.first.deployutils.deploy.target.RemoteTarget;
import edu.wpi.first.deployutils.log.ETLogger;

public class FileTreeArtifact extends AbstractArtifact implements CacheableArtifact {

    private final Property<CacheMethod> cacheMethod;

    @Inject
    public FileTreeArtifact(String name, RemoteTarget target) {
        super(name, target);
        files = target.getProject().getObjects().property(FileTree.class);
        cacheMethod = target.getProject().getObjects().property(CacheMethod.class);
    }

    private final Property<FileTree> files;

    public Property<FileTree> getFiles() {
        return files;
    }

    @Override
    public Property<CacheMethod> getCacheMethod() {
        return cacheMethod;
    }

    @Override
    public void deploy(DeployContext context) {
        if (files.isPresent()) {
            Map<String, File> f = new HashMap<>();
            Set<String> mkdirs = new HashSet<>();
            // TODO: we can probably use filevisit in dep root finding.
            files.get().visit(details -> {
                if (details.isDirectory()) {
                    mkdirs.add(details.getPath());
                } else {
                    f.put(details.getPath(), details.getFile());
                }
            });

            context.execute("mkdir -p " + String.join(" ", mkdirs));
            context.put(f, cacheMethod.getOrElse(null));
        } else {
            ETLogger logger = context.getLogger();
            if (logger != null) {
                logger.log("No file tree provided for " + toString());
            }
        }
    }
}
