package edu.wpi.first.deployutils.deploy.artifact;

import java.io.File;

import javax.inject.Inject;

import org.gradle.api.provider.Property;

import edu.wpi.first.deployutils.deploy.cache.CacheMethod;
import edu.wpi.first.deployutils.deploy.context.DeployContext;
import edu.wpi.first.deployutils.deploy.target.RemoteTarget;
import edu.wpi.first.deployutils.log.ETLogger;

public class FileArtifact extends AbstractArtifact implements CacheableArtifact {

    private final Property<CacheMethod> cacheMethod;

    @Inject
    public FileArtifact(String name, RemoteTarget target) {
        super(name, target);

        file = target.getProject().getObjects().property(File.class);
        filename = target.getProject().getObjects().property(String.class);
        cacheMethod = target.getProject().getObjects().property(CacheMethod.class);
    }

    private final Property<File> file;

    public Property<File> getFile() {
        return file;
    }

    private final Property<String> filename;

    public Property<String> getFilename() {
        return filename;
    }

    @Override
    public Property<CacheMethod> getCacheMethod() {
        return cacheMethod;
    }

    @Override
    public void deploy(DeployContext context) {
        if (file.isPresent()) {
            File f = file.get();
            context.put(f, filename.getOrElse(f.getName()), cacheMethod.getOrElse(null));
        } else {
            ETLogger logger = context.getLogger();
            if (logger != null) {
                logger.log("No file provided for " + toString());
            }
        }
    }
}
