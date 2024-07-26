package edu.wpi.first.deployutils.deploy.cache;

import java.io.File;
import java.util.Map;
import java.util.Set;

import org.gradle.api.Named;

import edu.wpi.first.deployutils.deploy.context.DeployContext;

public interface CacheMethod extends Named {
    // Returns false if something can't be found (e.g. md5sum). In this case, cache checking is skipped.
    boolean compatible(DeployContext context);
    Set<String> needsUpdate(DeployContext context, Map<String, File> files);
}
