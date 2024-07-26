package edu.wpi.first.deployutils.deploy.cache;

import edu.wpi.first.deployutils.deploy.context.DeployContext;

@FunctionalInterface
public interface CompatibleFunction {
  boolean check(DeployContext ctx);
}
