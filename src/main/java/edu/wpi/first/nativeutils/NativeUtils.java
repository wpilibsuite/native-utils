package edu.wpi.first.nativeutils;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.attributes.Attribute;
import org.gradle.api.attributes.Usage;
import org.gradle.api.model.ObjectFactory;

import edu.wpi.first.nativeutils.configs.ExportsConfig;
import edu.wpi.first.nativeutils.rules.PdbRules;
import edu.wpi.first.nativeutils.rules.ExportsConfigRules;
import edu.wpi.first.nativeutils.rules.PlatformRules;
import edu.wpi.first.nativeutils.rules.PrivateExportsConfigRules;
import edu.wpi.first.nativeutils.tasks.ExportsGenerationTask;
import edu.wpi.first.toolchain.ToolchainExtension;
import edu.wpi.first.toolchain.ToolchainPlugin;
import edu.wpi.first.deployutils.DeployUtils;

public class NativeUtils implements Plugin<Project> {
  public static final Attribute<String> NATIVE_ARTIFACT_FORMAT = Attribute.of("artifactType", String.class);
  public static final String NATIVE_ARTIFACT_ZIP_TYPE = "zip";
  public static final String NATIVE_ARTIFACT_DIRECTORY_TYPE = "directory";

  @Override
  public void apply(Project project) {

    project.getDependencies().registerTransform(UnzipTransform.class, variantTransform -> {
      variantTransform.getFrom().attribute(NATIVE_ARTIFACT_FORMAT, NATIVE_ARTIFACT_ZIP_TYPE);
      variantTransform.getTo().attribute(NATIVE_ARTIFACT_FORMAT, NATIVE_ARTIFACT_DIRECTORY_TYPE);
    });

    project.getPluginManager().apply(ToolchainPlugin.class);
    project.getPluginManager().apply(DeployUtils.class);

    ToolchainExtension tcExt = project.getExtensions().getByType(ToolchainExtension.class);

    project.getPluginManager().apply(PlatformRules.class);

    project.getExtensions().create("nativeUtils", NativeUtilsExtension.class, project, tcExt);

    project.getExtensions().add(NativeUtils.class.getSimpleName(), NativeUtils.class.getName());
    project.getExtensions().add(ExportsConfig.class.getSimpleName(), ExportsConfig.class.getName());
    project.getExtensions().add(ExportsGenerationTask.class.getSimpleName(), ExportsGenerationTask.class.getName());
    project.getPluginManager().apply(ExportsConfigRules.class);
    project.getPluginManager().apply(PrivateExportsConfigRules.class);

    project.getPluginManager().apply(PdbRules.class);
  }
}
