package org.wpilib.toolchain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.gradle.api.GradleException;
import org.gradle.api.invocation.Gradle;
import org.gradle.api.services.BuildService;
import org.gradle.api.services.BuildServiceParameters;
import org.gradle.internal.logging.text.TreeFormatter;
import org.gradle.internal.logging.text.StyledTextOutput;

import org.wpilib.deployutils.log.ETLogger;
import org.wpilib.deployutils.log.ETLoggerFactory;

public abstract class ToolchainGraphBuildService implements BuildService<BuildServiceParameters.None> {

    private boolean singlePrintPerPlatform = false;
    private boolean configured = false;

    private final List<String> registeredInstallTasks = new ArrayList<>();

    public boolean registerInstallTask(String name) {
        if (registeredInstallTasks.contains(name)) {
            return false;
        }
        registeredInstallTasks.add(name);
        return true;
    }

    private final Map<String, String> whichResults = new HashMap<>();

    public String getWhichResult(String tool) {
        return whichResults.getOrDefault(tool, null);
    }

    public void addWhichResult(String tool, String value) {
        whichResults.put(tool, value);
    }

    public void setSinglePrintPerPlatform() {
        singlePrintPerPlatform = true;
    }

    private final List<GccExtension> missingToolChains = new ArrayList<>();

    public void addMissingToolchain(GccExtension toolchain) {
        missingToolChains.add(toolchain);
    }

    public void configure(Gradle gradle) {
        if (configured) {
            return;
        }
        configured = true;
        gradle.getTaskGraph().whenReady(graph -> {
            List<String> skippedPlatforms = new ArrayList<>();
            for (GccExtension tcExt : missingToolChains) {
                ToolchainDescriptorBase descriptor = tcExt.getDescriptor();
                boolean installing = graph.getAllTasks().stream().anyMatch(t -> t instanceof InstallToolchainTask && ((InstallToolchainTask) t).getDescriptorName().equals(descriptor.getName()));
                if (!installing) {
                    ETLogger logger = ETLoggerFactory.INSTANCE.create(this.getClass().getSimpleName());
                    TreeFormatter formatter = new TreeFormatter();
                    descriptor.explain(formatter);
                    logger.info(formatter.toString());

                    boolean optional = descriptor.getOptional().get() ||tcExt.getProject().hasProperty("toolchain-optional-" + descriptor.getName());;
                    if (optional) {
                        if (!singlePrintPerPlatform || !skippedPlatforms.contains(descriptor.getName())) {
                            skippedPlatforms.add(descriptor.getName());
                            logger.logStyle("Skipping builds for " + descriptor.getName() + " (toolchain is marked optional)", StyledTextOutput.Style.Description);
                        }
                    } else if (tcExt.isUsed()) {
                        logger.logError("=============================");
                        logger.logErrorHead("No Toolchain Found for " + descriptor.getName());
                        logger.logErrorHead("Run `./gradlew " + descriptor.getInstallTaskName() + "` to install one!");
                        logger.logErrorHead("");
                        logger.logErrorHead("You can ignore this error with -Ptoolchain-optional-" + descriptor.getName());
                        logger.logErrorHead("For more information, run with `--info`");
                        logger.logError("=============================");

                        throw new GradleException("No Toolchain Found! Scroll up for more information.");
                    }
                }
            }
        });
    }

    @Inject
    public ToolchainGraphBuildService() {
    }
}
