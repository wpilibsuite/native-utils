package edu.wpi.first.deployutils.deploy.target;

import java.util.Set;

import org.gradle.api.DefaultTask;
import org.gradle.api.internal.PolymorphicDomainObjectContainerInternal;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.UntrackedTask;

import edu.wpi.first.deployutils.deploy.artifact.Artifact;
import edu.wpi.first.deployutils.deploy.target.location.DeployLocation;

@UntrackedTask(because = "Helper task")
public class ListTypeClassesTask extends DefaultTask {
    private RemoteTarget target;

    public void setTarget(RemoteTarget target) {
        this.target = target;
    }

    @SuppressWarnings("unchecked")
    @TaskAction
    public void execute() {

        getLogger().lifecycle("Type classes for {}", target.getName());

        getLogger().lifecycle("Artifact Type Classes (getArtifactTypeClass):");

        PolymorphicDomainObjectContainerInternal<Artifact> internalArtifacts =
            (PolymorphicDomainObjectContainerInternal<Artifact>) target.getArtifacts();
        Set<? extends java.lang.Class<? extends Artifact>> artifactTypeTes = internalArtifacts.getCreateableTypes();
        for (Class<? extends Artifact> artifactType : artifactTypeTes) {
            getLogger().lifecycle("\t{}", artifactType.getSimpleName());
        }

        getLogger().lifecycle("");
        getLogger().lifecycle("Location Type Classes (getLocationTypeClass):");

        PolymorphicDomainObjectContainerInternal<DeployLocation> internalLocations =
            (PolymorphicDomainObjectContainerInternal<DeployLocation>) target.getLocations();
        Set<? extends java.lang.Class<? extends DeployLocation>> locationTypeSet = internalLocations.getCreateableTypes();
        for (Class<? extends DeployLocation> locationType : locationTypeSet) {
            getLogger().lifecycle("\t{}", locationType.getSimpleName());
        }

    }
}
