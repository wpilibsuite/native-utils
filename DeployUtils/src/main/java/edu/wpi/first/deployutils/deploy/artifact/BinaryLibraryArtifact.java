package edu.wpi.first.deployutils.deploy.artifact;

import org.gradle.api.file.FileCollection;
import org.gradle.api.provider.Property;
import org.gradle.nativeplatform.NativeBinarySpec;

import edu.wpi.first.deployutils.deploy.cache.CacheMethod;
import edu.wpi.first.deployutils.deploy.context.DeployContext;
import edu.wpi.first.deployutils.deploy.target.RemoteTarget;
import edu.wpi.first.deployutils.log.ETLogger;

import java.io.File;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;

public class BinaryLibraryArtifact extends AbstractArtifact implements CacheableArtifact {
    private Set<File> files;
    private boolean doDeploy = false;
    private final Property<CacheMethod> cacheMethod;

    private NativeBinarySpec binary;

    @Inject
    public BinaryLibraryArtifact(String name, RemoteTarget target) {
        super(name, target);

        cacheMethod = target.getProject().getObjects().property(CacheMethod.class);

        getPreWorkerThread().add(v -> {
            Optional<FileCollection> libs = binary.getLibs().stream().map(x -> x.getRuntimeFiles()).reduce((a, b) -> a.plus(b));
            if (libs.isPresent()) {
                files = libs.get().getFiles();
                doDeploy = true;
            }
        });
    }

    @Override
    public Property<CacheMethod> getCacheMethod() {
        return cacheMethod;
    }

    public NativeBinarySpec getBinary() {
        return binary;
    }

    public void setBinary(NativeBinarySpec binary) {
        this.binary = binary;
    }

    public boolean isDoDeploy() {
        return doDeploy;
    }

    public void setDoDeploy(boolean doDeploy) {
        this.doDeploy = doDeploy;
    }

    public Set<File> getFiles() {
        return files;
    }

    public void setFiles(Set<File> files) {
        this.files = files;
    }

    @Override
    public void deploy(DeployContext context) {
        if (doDeploy) {
            context.put(files, getCacheMethod().getOrElse(null));
        } else {
            ETLogger logger = context.getLogger();
            if (logger != null) {
                logger.log("No file(s) provided for " + toString());
            }
        }
    }
}
