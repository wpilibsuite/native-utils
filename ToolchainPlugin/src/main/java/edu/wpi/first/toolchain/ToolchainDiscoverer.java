package edu.wpi.first.toolchain;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.gradle.api.Named;
import org.gradle.api.Project;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.internal.logging.text.DiagnosticsVisitor;
import org.gradle.internal.os.OperatingSystem;
import org.gradle.nativeplatform.toolchain.internal.SystemLibraries;
import org.gradle.nativeplatform.toolchain.internal.gcc.metadata.GccMetadata;
import org.gradle.nativeplatform.toolchain.internal.metadata.CompilerMetaDataProvider;
import org.gradle.nativeplatform.toolchain.internal.metadata.CompilerMetaDataProviderFactory;
import org.gradle.platform.base.internal.toolchain.SearchResult;
import org.gradle.process.ExecSpec;
import org.gradle.util.internal.VersionNumber;

public abstract class ToolchainDiscoverer implements Named {

    private final String name;
    private final CompilerMetaDataProvider<GccMetadata> metadataProvider;
    private Optional<GccMetadata> metadataLazy;
    private final Provider<File> rootDir;
    private final Function<String, String> composer;
    private final ToolchainDescriptorBase descriptor;

    public static ToolchainDiscoverer createDiscoverer(String name, ToolchainDescriptorBase descriptor, Provider<File> rootDir, Function<String, String> composer, Project project) {
        return project.getObjects().newInstance(ToolchainDiscoverer.class, name, descriptor, rootDir, composer);
    }

    public static ToolchainDiscovererProperty createProperty(String name, ToolchainDescriptorBase descriptor, Provider<File> rootDir, Function<String, String> composer, Project project) {
        ToolchainDiscovererProperty prop = project.getObjects().newInstance(ToolchainDiscovererProperty.class, name);
        Provider<ToolchainDiscoverer> disc = project.provider(() -> project.getObjects().newInstance(ToolchainDiscoverer.class, name, descriptor, rootDir, composer));
        prop.getDiscoverers().add(disc);
        return prop;
    }

    @Inject
    public ToolchainDiscoverer(String name, ToolchainDescriptorBase descriptor, Provider<File> rootDir, Function<String, String> composer, CompilerMetaDataProviderFactory metaDataProviderFactory, ProviderFactory providers) {
        this.name = name;
        this.rootDir = rootDir;
        this.composer = composer;
        this.metadataLazy = Optional.empty();
        this.descriptor = descriptor;

        this.metadataProvider = metaDataProviderFactory.gcc();
    }

    private Optional<File> rootDirFile;

    private Optional<File> rootDir() {
        if (rootDirFile != null) {
            return rootDirFile;
        }

        File f = rootDir.getOrNull();
        rootDirFile = optFile(f);
        return rootDirFile;
    }

    public boolean exists() {
        return metadata(null).isPresent();
    }

    public boolean versionValid() {
        if (!exists()) return false;

        VersionNumber v = metadata(null).get().getVersion();
        String versionLo = descriptor.getVersionLow().get();
        String versionHi = descriptor.getVersionHigh().get();
        boolean loValid = versionLo == null || v.compareTo(VersionNumber.parse(versionLo)) >= 0;
        boolean hiValid = versionHi == null || v.compareTo(VersionNumber.parse(versionHi)) <= 0;

        return loValid && hiValid;
    }

    public boolean valid() {
        return exists() && versionValid();
    }

    public Optional<File> binDir() {
        return join(rootDir(), "bin");
    }

    public Optional<File> libDir() {
        return join(rootDir(), "lib");
    }

    public Optional<File> includeDir() {
        return join(rootDir(), "include");
    }

    public Optional<File> tool(String tool) {
        return join(binDir(), toolName(tool));
    }

    public String toolName(String tool) {
        return composer == null ? tool : composer.apply(tool);
    }

    public Optional<File> gccFile() {
        return tool("g++");
    }

    public Optional<File> gdbFile() {
        return tool("gdb");
    }

    public Optional<File> sysroot() {
        return rootDir();
    }

    public Optional<GccMetadata> metadata(DiagnosticsVisitor visitor) {
        if (!metadataLazy.isPresent()) {
            metadataLazy = metadata(gccFile().orElse(null), visitor);
        }
        return metadataLazy;
    }

    public void explain(DiagnosticsVisitor visitor) {
        visitor.node("Valid?: " + valid());
        visitor.node("Found?: " + exists());
        visitor.node("Version Range");
        visitor.startChildren();
        String versionLo = descriptor.getVersionLow().get();
        String versionHi = descriptor.getVersionHigh().get();
        visitor.node("Low: " + versionLo);
        visitor.node("High: " + versionHi);
        visitor.node("Is Valid?: " + versionValid());
        visitor.endChildren();

        visitor.node("Root: " + rootDir().orElse(null));
        visitor.node("Bin: " + binDir().orElse(null));
        visitor.node("Lib: " + libDir().orElse(null));
        visitor.node("Include: " + includeDir().orElse(null));
        visitor.node("Gcc: " + gccFile().orElse(null));
        visitor.node("Gdb: " + gdbFile().orElse(null));

        if (exists()) {
            GccMetadata meta = metadata(null).get();

            visitor.node("Metadata");
            visitor.startChildren();
            visitor.node("Version: " + meta.getVersion().toString());
            visitor.node("Vendor: " + meta.getVendor());
            visitor.node("Default Arch: " + meta.getDefaultArchitecture().toString());

            visitor.node("System Libraries");
            visitor.startChildren();
            SystemLibraries syslib = meta.getSystemLibraries();
            visitor.node("Include");
            visitor.startChildren();
            for (File f : syslib.getIncludeDirs()) {
                visitor.node(f.getAbsolutePath());
            }
            visitor.endChildren();

            visitor.node("Lib Dirs");
            visitor.startChildren();
            for (File f : syslib.getLibDirs()) {
                visitor.node(f.getAbsolutePath());
            }
            visitor.endChildren();

            visitor.node("Macros");
            visitor.startChildren();
            for (Map.Entry<String, String> e : syslib.getPreprocessorMacros().entrySet()) {
                visitor.node(e.getKey() + " = " + e.getValue());
            }
            visitor.endChildren();
            visitor.endChildren(); // System Libraries
            visitor.endChildren(); // Metadata
        } else {
            if (gccFile().isPresent()) {
                visitor.node("Metadata Explain: ");
                visitor.startChildren();
                metadata(visitor);
                visitor.endChildren();
            }
        }
    }

    @Override
    public String getName() {
        return name;
    }

    private Optional<GccMetadata> metadata(File file, DiagnosticsVisitor visitor) {
        if (file == null || !file.exists())
            return Optional.empty();
        SearchResult<GccMetadata> searchresult = metadataProvider.getCompilerMetaData(new ArrayList<File>(), compilerSpec -> {
            compilerSpec.executable(file);
        });
        if (visitor != null)
            searchresult.explain(visitor);
        return Optional.ofNullable(searchresult.getComponent());
    }

    public static List<File> systemPath(Project project, ToolchainRootExtension tce, Function<String, String> composer) {
        String tool = composer == null ? "g++" : composer.apply("gcc");
        String whichResult = tce.getWhichResult(tool);
        if (whichResult == null) {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ByteArrayOutputStream errStr = new ByteArrayOutputStream();

            project.exec((ExecSpec spec) -> {
                spec.commandLine(OperatingSystem.current().isWindows() ? "where.exe" : "which", tool);
                spec.setStandardOutput(os);
                spec.setErrorOutput(errStr);
                spec.setIgnoreExitValue(true);
            });

            whichResult = os.toString().trim();
            tce.addWhichResult(tool, whichResult);
        } else {
            System.out.println("Using cache for " + tool);
        }

        return Arrays.stream(whichResult.split("\n"))
                .map(String::trim)
                .filter(((Predicate<String>)String::isEmpty).negate())
                .map((String path) -> { return new File(path).getParentFile().getParentFile(); })
                .collect(Collectors.toList());
    }

    public static ToolchainDiscovererProperty forSystemPath(Project project, ToolchainRootExtension tce, ToolchainDescriptorBase descriptor, Function<String, String> composer) {
        ToolchainDiscovererProperty prop = project.getObjects().newInstance(ToolchainDiscovererProperty.class, "PathList");

        Provider<List<ToolchainDiscoverer>> p = project.provider(() -> {
            List<ToolchainDiscoverer> disc = new ArrayList<>();
            int i = 0;
            for (File f : systemPath(project, tce, composer)) {
                Provider<File> fp = project.provider(() -> f);
                disc.add(ToolchainDiscoverer.createDiscoverer("Path" + (i++), descriptor, fp, composer, project));
            }
            return disc;
        });
        prop.getDiscoverers().addAll(p);
        return prop;
    }

    private static Optional<File> join(Optional<File> f, String join) {
        return optFile((File)(f.map((File file) -> { return new File(file, join); }).orElse(null)));
    }

    private static Optional<File> optFile(File f) {
        return (f == null || !f.exists()) ? Optional.empty() : Optional.of(f);
    }
}
