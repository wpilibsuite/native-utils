package edu.wpi.first.toolchain;

import java.io.File;
import java.util.List;
import java.util.Optional;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.nativeplatform.NativeBinarySpec;
import org.gradle.nativeplatform.tasks.AbstractLinkTask;

import jaci.gradle.log.ETLogger;
import jaci.gradle.log.ETLoggerFactory;

public class OrderedStripTask implements Action<Task> {

    public final ToolchainExtension tcExt;
    public final NativeBinarySpec binary;
    public final AbstractLinkTask linkTask;
    public final GccToolChain gcc;
    public final Project project;
    private boolean performStripAll = false;
    private boolean performDebugStrip = false;


    public boolean getPerformStripAll() {
        return performStripAll;
    }

    public void setPerformStripAll(boolean stripAll) {
        performStripAll = stripAll;
    }

    public boolean getPerformDebugStrip() {
        return performDebugStrip;
    }

    public void setPerformDebugStrip(boolean stripDebug) {
        performDebugStrip = stripDebug;
    }


    public OrderedStripTask(ToolchainExtension tcExt, NativeBinarySpec binary, AbstractLinkTask linkTask, GccToolChain gcc, Project project) {
        this.tcExt = tcExt;
        this.binary = binary;
        this.linkTask = linkTask;
        this.project = project;
        this.gcc = gcc;
    }

    @Override
    public void execute(Task task) {
        if (!performDebugStrip) return;
        List<String> excludeComponents = tcExt
                .getStripExcludeComponentsForPlatform(binary.getTargetPlatform().getName());
        if (excludeComponents != null && excludeComponents.contains(binary.getComponent().getName())) {
            return;
        }

        File mainFile = linkTask.getLinkedFile().get().getAsFile();

        if (mainFile.exists()) {
            String mainFileStr = mainFile.toString();
            String debugFile = mainFileStr + ".debug";

            ToolchainDiscoverer disc = gcc.getDiscoverer();

            Optional<File> objcopyOptional = disc.tool("objcopy");
            Optional<File> stripOptional = disc.tool("strip");
            if (!objcopyOptional.isPresent() || !stripOptional.isPresent()) {
                ETLogger logger = ETLoggerFactory.INSTANCE.create("NativeBinaryStrip");
                logger.logError("Failed to strip binaries because of unknown tool objcopy and strip");
                return;
            }

            String objcopy = disc.tool("objcopy").get().toString();
            String strip = disc.tool("strip").get().toString();

            project.exec((ex) -> {
                ex.commandLine(objcopy, "--only-keep-debug", mainFileStr, debugFile);
            });
            project.exec((ex) -> {
                ex.commandLine(strip, "-g", mainFileStr);
            });
            project.exec((ex) -> {
                ex.commandLine(objcopy, "--add-gnu-debuglink=" + debugFile, mainFileStr);
            });
            if (performStripAll) {
                project.exec((ex) -> {
                    ex.commandLine(strip, "--strip-all", "--discard-all", mainFileStr);
                });   
            }
        }
    }
}