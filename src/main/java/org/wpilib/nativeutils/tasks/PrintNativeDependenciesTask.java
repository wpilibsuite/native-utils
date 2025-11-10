package org.wpilib.nativeutils.tasks;

import javax.inject.Inject;

import org.gradle.api.DefaultTask;
import org.gradle.api.NamedDomainObjectSet;
import org.gradle.api.tasks.TaskAction;

import org.wpilib.nativeutils.NativeUtilsExtension;
//import org.wpilib.nativeutils.NativeUtilsExtension.NamedNativeDependencyList;

public class PrintNativeDependenciesTask extends DefaultTask {

    @Inject
    public PrintNativeDependenciesTask() {
        setGroup("NativeUtils");
        setDescription("Prints the native dependency graph");
    }

    // @TaskAction
    // public void execute() {
    //     NativeUtilsExtension nue = getProject().getExtensions().getByType(NativeUtilsExtension.class);
    //     NamedDomainObjectSet<NativeUtilsExtension.NamedNativeDependencyList> sets = nue
    //             .getNativeLibraryDependencySets();

    //     for (NativeUtilsExtension.NamedNativeDependencyList setList : sets) {
    //         printDependencies("", setList, sets);
    //     }
    // }

    // private void printDependencies(String printBase, NamedNativeDependencyList depList,
    //         NamedDomainObjectSet<NativeUtilsExtension.NamedNativeDependencyList> sets) {
    //     System.out.println(printBase + depList.getName());

    //     for (BaseLibraryDependencySet base : depList.getDeps()) {

    //         if (base instanceof CombinedLibraryDependencySet) {
    //             CombinedLibraryDependencySet combined = (CombinedLibraryDependencySet) base;
    //             for (String inner : combined.getLibs()) {
    //                 NamedNativeDependencyList innerDep = sets.findByName(inner);
    //                 if (innerDep == null) {
    //                     System.out.println(printBase + "    Missing dep " + inner);
    //                     continue;
    //                 }
    //                 printDependencies(printBase + "    ", innerDep, sets);
    //             }
    //         }
    //     }
    // }
}
