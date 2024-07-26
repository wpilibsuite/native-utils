package edu.wpi.first.deployutils.deploy.cache;

import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import edu.wpi.first.deployutils.deploy.context.DeployContext;

public class DefaultCacheMethod extends AbstractCacheMethod {
    private CacheCheckerFunction needsUpdate = (ctx, fn, lf) -> true;
    private CompatibleFunction compatible = ctx -> true;

    public DefaultCacheMethod(String name) {
        super(name);

    }

    public CacheCheckerFunction getNeedsUpdate() {
        return needsUpdate;
    }

    public void setNeedsUpdate(CacheCheckerFunction needsUpdate) {
        this.needsUpdate = needsUpdate;
    }

    public CompatibleFunction getCompatible() {
        return compatible;
    }

    public void setCompatible(CompatibleFunction compatible) {
        this.compatible = compatible;
    }

    @Override
    public boolean compatible(DeployContext context) {
        return compatible.check(context);
    }

    @Override
    public Set<String> needsUpdate(DeployContext context, Map<String, File> files) {
        return files.entrySet().stream()
            .filter(entry -> needsUpdate.check(context, entry.getKey(), entry.getValue()))
            .map(entry -> entry.getKey())
            .collect(Collectors.toSet());
    }
}
