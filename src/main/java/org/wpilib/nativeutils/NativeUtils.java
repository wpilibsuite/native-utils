package org.wpilib.nativeutils;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.attributes.Attribute;
import org.gradle.internal.os.OperatingSystem;

import org.wpilib.deployutils.DeployUtils;
import org.wpilib.nativeutils.exports.ExportsConfigPlugin;
import org.wpilib.nativeutils.exports.PrivateExportsConfigRules;
import org.wpilib.nativeutils.pdb.PdbPlugin;
import org.wpilib.nativeutils.platforms.PlatformRules;
import org.wpilib.toolchain.ToolchainExtension;
import org.wpilib.toolchain.ToolchainPlugin;
import org.wpilib.vscode.GradleVsCode;

public class NativeUtils implements Plugin<Project> {
  public static final Attribute<String> NATIVE_ARTIFACT_FORMAT = Attribute.of("artifactType", String.class);
  public static final String NATIVE_ARTIFACT_ZIP_TYPE = "zip";
  public static final String NATIVE_ARTIFACT_DIRECTORY_TYPE = "nu-directory";

  @Override
  public void apply(Project project) {
    // Apply transformation for native artifacts
    project.getDependencies().registerTransform(UnzipTransform.class, variantTransform -> {
      variantTransform.getFrom().attribute(NATIVE_ARTIFACT_FORMAT, NATIVE_ARTIFACT_ZIP_TYPE);
      variantTransform.getTo().attribute(NATIVE_ARTIFACT_FORMAT, NATIVE_ARTIFACT_DIRECTORY_TYPE);
    });

    project.getPluginManager().apply(ToolchainPlugin.class);
    project.getPluginManager().apply(DeployUtils.class);

    ToolchainExtension tcExt = project.getExtensions().getByType(ToolchainExtension.class);

    project.getPluginManager().apply(PlatformRules.class);

    project.getExtensions().create("nativeUtils", NativeUtilsExtension.class, project, tcExt);

    project.getPluginManager().apply(ExportsConfigPlugin.class);
    project.getPluginManager().apply(PrivateExportsConfigRules.class);

    if (OperatingSystem.current().isWindows()) {
      project.getPluginManager().apply(PdbPlugin.class);
    }

    project.getPluginManager().apply(GradleVsCode.class);
  }
}
