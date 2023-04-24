package edu.wpi.first.nativeutils;

import edu.wpi.first.deployutils.DeployUtils;
import edu.wpi.first.nativeutils.exports.ExportsConfigPlugin;
import edu.wpi.first.nativeutils.exports.PrivateExportsConfigRules;
import edu.wpi.first.nativeutils.pdb.PdbPlugin;
import edu.wpi.first.nativeutils.platforms.PlatformRules;
import edu.wpi.first.toolchain.ToolchainExtension;
import edu.wpi.first.toolchain.ToolchainPlugin;
import edu.wpi.first.vscode.GradleVsCode;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.attributes.Attribute;
import org.gradle.internal.os.OperatingSystem;

public class NativeUtils implements Plugin<Project> {
  public static final Attribute<String> NATIVE_ARTIFACT_FORMAT =
      Attribute.of("artifactType", String.class);
  public static final String NATIVE_ARTIFACT_ZIP_TYPE = "zip";
  public static final String NATIVE_ARTIFACT_DIRECTORY_TYPE = "nu-directory";

  @Override
  public void apply(Project project) {
    // Apply transformation for native artifacts
    project
        .getDependencies()
        .registerTransform(
            UnzipTransform.class,
            variantTransform -> {
              variantTransform
                  .getFrom()
                  .attribute(NATIVE_ARTIFACT_FORMAT, NATIVE_ARTIFACT_ZIP_TYPE);
              variantTransform
                  .getTo()
                  .attribute(NATIVE_ARTIFACT_FORMAT, NATIVE_ARTIFACT_DIRECTORY_TYPE);
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
