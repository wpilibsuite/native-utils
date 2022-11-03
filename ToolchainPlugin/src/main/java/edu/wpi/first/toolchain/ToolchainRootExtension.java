package edu.wpi.first.toolchain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.gradle.api.GradleException;
import org.gradle.api.invocation.Gradle;
import org.gradle.internal.logging.text.TreeFormatter;
import org.gradle.internal.logging.text.StyledTextOutput;

import edu.wpi.first.deployutils.log.ETLogger;
import edu.wpi.first.deployutils.log.ETLoggerFactory;

public class ToolchainRootExtension {
    private boolean singlePrintPerPlatform = false;

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

    private final List<GccToolChain> missingToolChains = new ArrayList<>();

    public void addMissingToolchain(GccToolChain toolchain) {
        missingToolChains.add(toolchain);
    }

    @Inject
    public ToolchainRootExtension(Gradle gradle) {
        gradle.getTaskGraph().whenReady(graph -> {
            List<String> skippedPlatforms = new ArrayList<>();
            for (GccToolChain toolchain : missingToolChains) {
                ToolchainDescriptorBase descriptor = toolchain.getDescriptor();
                boolean installing = graph.getAllTasks().stream().anyMatch(t -> t instanceof InstallToolchainTask && ((InstallToolchainTask) t).getDescriptorName().equals(descriptor.getName()));
                if (!installing) {
                    ETLogger logger = ETLoggerFactory.INSTANCE.create(this.getClass().getSimpleName());
                    TreeFormatter formatter = new TreeFormatter();
                    descriptor.explain(formatter);
                    logger.info(formatter.toString());

                    boolean optional = descriptor.getOptional().get();
                    if (optional) {
                        if (!singlePrintPerPlatform || !skippedPlatforms.contains(descriptor.getName())) {
                            skippedPlatforms.add(descriptor.getName());
                            logger.logStyle("Skipping builds for " + descriptor.getName() + " (toolchain is marked optional)", StyledTextOutput.Style.Description);
                        }
                    } else if (toolchain.isUsed()) {
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
}
