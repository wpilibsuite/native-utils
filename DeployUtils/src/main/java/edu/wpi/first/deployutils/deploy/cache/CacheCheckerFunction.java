package edu.wpi.first.deployutils.deploy.cache;

import java.io.File;

import edu.wpi.first.deployutils.deploy.context.DeployContext;

@FunctionalInterface
public interface CacheCheckerFunction {
  boolean check(DeployContext ctx, String filename, File localFile);
}
