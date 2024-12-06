package edu.wpi.first.nativeutils.vendordeps;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.gradle.api.Named;
import org.gradle.api.NamedDomainObjectSet;
import org.gradle.api.Project;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.provider.Property;
import org.gradle.nativeplatform.plugins.NativeComponentPlugin;
import org.gradle.tooling.BuildException;

import com.google.gson.Gson;

import edu.wpi.first.deployutils.log.ETLogger;
import edu.wpi.first.deployutils.log.ETLoggerFactory;
import edu.wpi.first.toolchain.NativePlatforms;

public abstract class WPIVendorDepsExtension {

    private final Property<String> frcYear;

    public Property<String> getFrcYear() {
        return frcYear;
    }

    private final DirectoryProperty frcHome;

    public DirectoryProperty getFrcHome() {
        return frcHome;
    }

    private final NamedDomainObjectSet<NamedJsonDependency> dependencySet;

    public NamedDomainObjectSet<NamedJsonDependency> getDependencySet() {
        return dependencySet;
    }

    private final ETLogger log;
    private final Gson gson = new Gson();

    public static final String DEFAULT_VENDORDEPS_FOLDER_NAME = "vendordeps";
    public static final String NATIVEUTILS_VENDOR_FOLDER_PROPERTY = "nativeutils.vendordep.folder.path";
    public static final String HW_SIM_SWITCH_PROPERTY = "hwSim";

    private final Project project;

    private WPINativeVendorDepsExtension nativeVendor;

    public WPINativeVendorDepsExtension getNativeVendor() {
        return nativeVendor;
    }

    private WPIJavaVendorDepsExtension javaVendor;

    public WPIJavaVendorDepsExtension getJavaVendor() {
        return javaVendor;
    }

    @Inject
    public WPIVendorDepsExtension(Project project) {

        frcYear = project.getObjects().property(String.class);
        frcHome = project.getObjects().directoryProperty();
        this.log = ETLoggerFactory.INSTANCE.create("WPIVendorDeps");
        this.project = project;
        hwSimulation = project.hasProperty(HW_SIM_SWITCH_PROPERTY);
        dependencySet = project.getObjects().namedDomainObjectSet(NamedJsonDependency.class);
        vendorRepos = project.getObjects().namedDomainObjectSet(VendorMavenRepo.class);
        getFixedVersion().convention("0.0.0");

        ObjectFactory objects = project.getObjects();

        project.getPlugins().withType(NativeComponentPlugin.class, p -> {
            nativeVendor = objects.newInstance(WPINativeVendorDepsExtension.class, this, project);
        });

        project.getPlugins().withType(JavaPlugin.class, p -> {
            javaVendor = objects.newInstance(WPIJavaVendorDepsExtension.class, this, project);
        });
    }

    private File vendorFolder(Project project) {
        Object prop = project.findProperty(NATIVEUTILS_VENDOR_FOLDER_PROPERTY);
        String filepath = DEFAULT_VENDORDEPS_FOLDER_NAME;
        if (prop != null && !prop.equals(DEFAULT_VENDORDEPS_FOLDER_NAME)) {
            log.logErrorHead(
                    "Warning! You have the property " + NATIVEUTILS_VENDOR_FOLDER_PROPERTY
                            + " set to a non-default value: " + prop);
            log.logError("The default path (from the project root) is " + DEFAULT_VENDORDEPS_FOLDER_NAME);
            log.logError(
                    "This can cause NativeUtils/GradleRIO to not be able to find the vendordep JSON files, and the dependencies not being loaded.");
            log.logError("This can result in compilation errors and you not being able to deploy code.");
            log.logError("Remove this from your gradle.properties file unless you know what you're doing.");
            filepath = (String) prop;
        }
        return project.file(filepath);
    }

    private boolean hwSimulation;

    public boolean isHwSimulation() {
        return hwSimulation;
    }

    public void setHwSimulation(boolean value) {
        hwSimulation = value;
    }

    public static List<File> vendorFiles(File directory) {
        if (directory.exists()) {
            return List.of(directory.listFiles(pathname -> {
                return pathname.getName().endsWith(".json") && !pathname.isHidden();
            }));
        } else {
            return List.of();
        }
    }

    public void loadAll() {
        loadFrom(vendorFolder(project));
    }

    public void validateDependencies() {
        String requiredFrcYear = frcYear.getOrNull();
        for (NamedJsonDependency jsonDep : dependencySet) {
            if (requiredFrcYear != null) {
                if (!requiredFrcYear.equals(jsonDep.dependency.frcYear)) {
                    throw new InvalidVendorDepYearException(jsonDep.dependency, requiredFrcYear);
                }
            }

            VendorDependency[] requiredDependencies = jsonDep.dependency.requires;
            if (requiredDependencies != null) {
                for (VendorDependency requiredDep : requiredDependencies) {
                    if (dependencySet.findByName(requiredDep.uuid) == null) {
                        throw new ConflictingVendorDependencyException(jsonDep.name, requiredDep.uuid,
                                requiredDep.errorMessage);
                    }
                }
            }
            VendorDependency[] conflictsWithDependencies = jsonDep.dependency.conflictsWith;
            if (conflictsWithDependencies != null) {
                for (VendorDependency conflictsWithDep : conflictsWithDependencies) {
                    if (dependencySet.findByName(conflictsWithDep.uuid) != null) {
                        throw new ConflictingVendorDependencyException(jsonDep.name, conflictsWithDep.uuid,
                                conflictsWithDep.errorMessage);
                    }
                }
            }
        }
    }

    public void loadFrom(File directory) {
        for (File f : vendorFiles(directory)) {
            JsonDependency dep = parse(f);
            if (dep != null) {
                try {
                    load(dep);
                } catch(Exception e) {
                    throw new BuildException("Failed to load vendor dependency: " + f.getName(), e);
                }
            }
        }
    }

    public void loadFrom(Project project) {
        loadFrom(vendorFolder(project));
    }

    private JsonDependency parse(File f) {
        try (BufferedReader reader = Files.newBufferedReader(f.toPath())) {
            return gson.fromJson(reader, JsonDependency.class);
        } catch (Exception e) {
            throw new BuildException("Failed to parse vendor dependency: " + f.getName(), e);
        }
    }

    private void load(JsonDependency dep) throws VendorParsingException {
        // Don"t double-add a dependency!
        if (dependencySet.findByName(dep.uuid) != null) {
            return;
        }

        NamedJsonDependency namedDep = new NamedJsonDependency(dep.uuid, dep);
        dependencySet.add(namedDep);

        if (dep.name == null) {
            throw new VendorParsingException(VendorParsingError.MissingName);
        }
        String filename = dep.name;

        if (dep.mavenUrls == null) {
            throw new VendorParsingException(filename, VendorParsingError.NoMavenUrl);
        }

        // Enumerate all group ids
        Set<String> groupIds = new HashSet<>();
        if (dep.cppDependencies == null) {
            throw new VendorParsingException(filename, VendorParsingError.MissingCppDeps);
        }
        for (CppArtifact cpp : dep.cppDependencies) {
            groupIds.add(cpp.groupId);
        }
        if (dep.jniDependencies == null) {
            throw new VendorParsingException(filename, VendorParsingError.MissingJniDeps);
        }
        for (JniArtifact jni : dep.jniDependencies) {
            groupIds.add(jni.groupId);
        }
        if (dep.javaDependencies == null) {
            throw new VendorParsingException(filename, VendorParsingError.MissingJavaDeps);
        }
        for (JavaArtifact java : dep.javaDependencies) {
            groupIds.add(java.groupId);
        }
        if (dep.extraGroupIds != null) {
            for (String groupId : dep.extraGroupIds) {
                groupIds.add(groupId);
            }
        }

        int i = 0;
        for (String url : dep.mavenUrls) {
            boolean found = false;

            for (VendorMavenRepo machingRepo : vendorRepos.matching(x -> x.getUrl().equals(url))) {
                found = true;
                machingRepo.getAllowedGroupIds().addAll(groupIds);
            }

            // // Only add if the maven doesn"t yet exist.
            if (!found) {
                String name = dep.uuid + "_" + i++;
                log.info("Registering vendor dep maven: " + name + " on project " + project.getPath());
                VendorMavenRepo repo = project.getObjects().newInstance(VendorMavenRepo.class, name, url, groupIds);
                vendorRepos.add(repo);
            }
        }
    }

    public abstract Property<String> getFixedVersion();

    public String getVersion(String inputVersion) {
        return inputVersion.equals("wpilib") ? getFixedVersion().get() : inputVersion;
    }

    public boolean isIgnored(String[] ignore, JsonDependency dep) {
        for (String i : ignore) {
            if (i.equals(dep.name) || i.equals(dep.uuid)) {
                return true;
            }
        }
        return false;
    }

    public static final String HW_SIM_FLAG = "hwsim";
    public static final String SW_SIM_FLAG = "swsim";

    public static class JavaArtifact {
        public String groupId;
        public String artifactId;
        public String version;
    }

    public static class JniArtifact {
        public boolean useInHwSim() {
            return !SW_SIM_FLAG.equals(simMode);
        }

        public boolean useInSwSim() {
            return !HW_SIM_FLAG.equals(simMode);
        }

        public String groupId;
        public String artifactId;
        public String version;
        public String simMode;

        public boolean isJar;

        public String[] validPlatforms;
        public boolean skipInvalidPlatforms;
    }

    public static class CppArtifact {
        public boolean useInHwSim() {
            return !SW_SIM_FLAG.equals(simMode);
        }

        public boolean useInSwSim() {
            return !HW_SIM_FLAG.equals(simMode);
        }

        public boolean useInRio() {
            return Arrays.asList(binaryPlatforms).contains(NativePlatforms.roborio);
        }

        public String groupId;
        public String artifactId;
        public String version;
        public String libName;
        public String simMode;

        public String headerClassifier;
        public String sourcesClassifier;
        public String[] binaryPlatforms;
        public boolean skipInvalidPlatforms;

        public boolean sharedLibrary;
    }

    public static class VendorDependency {
        public String uuid;
        public String errorMessage;
        public String offlineFileName;
        public String onlineUrl;
    }

    public static class JsonDependency {
        public String name;
        public String version;
        public String uuid;
        public VendorDependency[] requires;
        public VendorDependency[] conflictsWith;
        public String[] mavenUrls;
        public String[] extraGroupIds;
        public String jsonUrl = "";
        public String fileName;
        public String frcYear;
        public JavaArtifact[] javaDependencies;
        public JniArtifact[] jniDependencies;
        public CppArtifact[] cppDependencies;
    }

    public static class NamedJsonDependency implements Named {
        private final JsonDependency dependency;
        private final String name;

        public NamedJsonDependency(String name, JsonDependency dependency) {
            this.name = name;
            this.dependency = dependency;
        }

        public String getName() {
            return name;
        }

        public JsonDependency getDependency() {
            return dependency;
        }
    }

    private final NamedDomainObjectSet<VendorMavenRepo> vendorRepos;

    public NamedDomainObjectSet<VendorMavenRepo> getVendorRepos() {
        return vendorRepos;
    }

    public static class VendorMavenRepo implements Named {
        private final Set<String> allowedGroupIds;
        private final String url;

        private final String name;

        @Inject
        public VendorMavenRepo(String name, String url, Set<String> allowedGroupIds) {
            this.name = name;
            this.url = url;
            this.allowedGroupIds = new HashSet<>();
            if (allowedGroupIds != null) {
                this.allowedGroupIds.addAll(allowedGroupIds);
            }
        }

        public Set<String> getAllowedGroupIds() {
            return allowedGroupIds;
        }

        public String getUrl() {
            return url;
        }

        @Override
        public String getName() {
            return name;
        }
    }

    public void addVendorReposToMaven(boolean enableGroupLimits) {
        for (VendorMavenRepo vRepo : vendorRepos) {
            project.getRepositories().maven(repo -> {
                repo.setName("WPI" + vRepo.getName() + "Vendor");
                repo.setUrl(vRepo.getUrl());
                if (enableGroupLimits) {
                    repo.content(desc -> {
                        for (String group : vRepo.allowedGroupIds) {
                            desc.includeGroup(group);
                        }
                    });
                }
            });
        }
    }
}
