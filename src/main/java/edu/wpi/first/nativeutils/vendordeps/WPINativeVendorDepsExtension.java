package edu.wpi.first.nativeutils.vendordeps;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.gradle.api.ExtensiblePolymorphicDomainObjectContainer;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.nativeplatform.NativeBinarySpec;
import org.gradle.platform.base.VariantComponentSpec;

import edu.wpi.first.nativeutils.NativeUtilsExtension;
import edu.wpi.first.nativeutils.dependencies.AllPlatformsCombinedNativeDependency;
import edu.wpi.first.nativeutils.dependencies.NativeDependency;
import edu.wpi.first.nativeutils.dependencies.WPIVendorMavenDependency;
import edu.wpi.first.nativeutils.vendordeps.WPIVendorDepsExtension.CppArtifact;
import edu.wpi.first.nativeutils.vendordeps.WPIVendorDepsExtension.JsonDependency;
import edu.wpi.first.nativeutils.vendordeps.WPIVendorDepsExtension.NamedJsonDependency;
import edu.wpi.first.toolchain.NativePlatforms;

public class WPINativeVendorDepsExtension {
    private final WPIVendorDepsExtension vendorDeps;
    private final Project project;
    private NativeUtilsExtension nte;

    public static final String SW_SIM_PREFIX = "swsim";
    public static final String HW_SIM_PREFIX = "hwsim";
    public static final String RIO_PREFIX = "rio";

    @Inject
    public WPINativeVendorDepsExtension(WPIVendorDepsExtension vendorDeps, Project project) {
        this.vendorDeps = vendorDeps;
        this.project = project;
    }

    private void initializeJsonDep(String uuid, String jsonName, List<CppArtifact> cppDeps, String prefix, ExtensiblePolymorphicDomainObjectContainer<NativeDependency> dependencyContainer) {

        String depName = prefix + "_" + uuid + "_" + jsonName;

        AllPlatformsCombinedNativeDependency combinedDep = dependencyContainer.create(depName,
                AllPlatformsCombinedNativeDependency.class);

        if (cppDeps.isEmpty()) {
            return;
        }

        for (CppArtifact cpp : cppDeps) {
            String name = depName + "_" + cpp.libName;
            combinedDep.getDependencies().add(name);
            WPIVendorMavenDependency vendorDep = dependencyContainer.create(name, WPIVendorMavenDependency.class);
            vendorDep.setArtifact(cpp);
        }
    }

    public void initializeNativeDependencies() {
        nte = project.getExtensions().getByType(NativeUtilsExtension.class);
        var dependencyContainer = nte.getNativeDependencyContainer();
        dependencyContainer.registerFactory(WPIVendorMavenDependency.class, name -> {
            return project.getObjects().newInstance(WPIVendorMavenDependency.class, name, project);
        });

        vendorDeps.getDependencySet().all(d -> {
            JsonDependency dep = d.getDependency();

            List<CppArtifact> swSimDeps = new ArrayList<>();
            List<CppArtifact> hwSimDeps = new ArrayList<>();
            List<CppArtifact> rioDeps = new ArrayList<>();

            for (CppArtifact art : dep.cppDependencies) {
                if (art.useInHwSim()) {
                    hwSimDeps.add(art);
                }
                if (art.useInSwSim()) {
                    swSimDeps.add(art);
                }
                if (art.useInRio()) {
                    rioDeps.add(art);
                }
            }

            initializeJsonDep(dep.uuid, dep.name, hwSimDeps, HW_SIM_PREFIX, dependencyContainer);
            initializeJsonDep(dep.uuid, dep.name, swSimDeps, SW_SIM_PREFIX, dependencyContainer);
            initializeJsonDep(dep.uuid, dep.name, rioDeps, RIO_PREFIX, dependencyContainer);
        });
    }

    public void cpp(Object scope, String... ignore) {
        if (scope instanceof VariantComponentSpec) {
            ((VariantComponentSpec) scope).getBinaries().withType(NativeBinarySpec.class).all(bin -> {
                cppVendorLibForBin(bin, ignore);
            });
        } else if (scope instanceof NativeBinarySpec) {
            cppVendorLibForBin((NativeBinarySpec) scope, ignore);
        } else {
            throw new GradleException(
                    "Unknown type for useVendorLibraries target. You put this declaration in a weird place.");
        }
    }

    private void cppVendorLibForBin(NativeBinarySpec bin, String[] ignore) {
        String prefix = vendorDeps.isHwSimulation() ? HW_SIM_PREFIX : SW_SIM_PREFIX;
        if (bin.getTargetPlatform().getName().equals(NativePlatforms.roborio)) {
            prefix = RIO_PREFIX;
        }
        for (NamedJsonDependency namedDep : vendorDeps.getDependencySet()) {
            JsonDependency dep = namedDep.getDependency();
            if (vendorDeps.isIgnored(ignore, dep)) {
                continue;
            }
            nte.useRequiredLibrary(bin, prefix + "_" + dep.uuid + "_" + dep.name);
        }
    }
}
