DeployUtils
====
Deploy to Embedded Targets in both Java and C++.

For all projects, you can define deployment targets and artifacts. The deploy process works over SSH/SFTP and
is extremely quick.

For the previous functionality for native library building, see edu.wpi.first.NativeUtils

Commands:
`gradlew deploy` will deploy all artifacts
`gradlew deployStandalone` will deploy all artifacts marked for standalone use with `allowStandaloneDeploy`
`gradlew deploy<artifact name><target name>` will deploy only the specified artifact to the specified target

Properties:
`gradlew deploy -Pdeploy-dirty` will skip the cache check and force redeployment of all files
`gradlew deploy -Pdeploy-dry` will do a 'dry run' (will not connect or deploy to target, instead only printing to console)

## Installing plugin
Include the following in your `build.gradle`
```gradle
plugins {
    id "edu.wpi.first.DeployUtils" version "<latest version>"
}
```

See [https://plugins.gradle.org/plugin/edu.wpi.first.DeployUtils](https://plugins.gradle.org/plugin/edu.first.wpi.DeployUtils) for the latest version

## Spec

```gradle

// DSL (all properties optional unless stated as required)
deploy {
    targets {
        myTarget(getTargetTypeClass('RemoteTarget')) { // name is first, parameter to getTargetTypeClass is type
            directory = '/home/myuser'  // The root directory to start deploying to. Default: user home
            maxChannels = 1         // The number of channels to open on the target (how many files / commands to run at the same time). Default: 1
            timeout = 3             // Timeout to use when connecting to target. Default: 3 (seconds)
            failOnMissing = true    // Should the build fail if the target can't be found? Default: true

            locations {
                ssh(getLocationTypeClass('SshDeployLocation')) {
                    address = "mytarget.local"  // Required. The address to try
                    user = 'myuser'             // Required. The user to login as
                    password = ''               // The password for the user. Default: blank (empty) string
                    ipv6 = false                // Are IPv6 addresses permitted? Default: false
                }
            }

            // Artifacts are specific per target
            artifacts {
                // COMMON PROPERTIES FOR ALL ARTIFACTS //
                all {
                    directory = 'mydir'                     // Subdirectory to use. Relative to target directory

                    onlyIf = { execute('echo Hi').result == 'Hi' }   // Check closure for artifact. Will not deploy if evaluates to false

                    predeploy << { execute 'echo Pre' }      // After onlyIf, but before deploy logic
                    postdeploy << { execute 'echo Post' }    // After this artifact's deploy logic

                    disabled = true                         // Disable this artifact. Default: false.

                    dependsOn('someTask')                   // Make this artifact depend on a task, both standalone and main deploy tasks

                    dependsOnForDeployTask('someTask')      // Make main artifact deploy task only depend on task

                    dependsOnForStandaloneDeployTask('someTask')    // Make standalone artifact deploy task only depend on task
                }
                // END COMMON //

                myFileArtifact(getArtifactTypeClass('FileArtifact)) {
                    file = file('myFile')               // Set the file to deploy. Required.
                    filename = 'myFile.dat'             // Set the filename to deploy to. Default: same name as file
                }

                // FileCollectionArtifact is a flat collection of files - directory structure is not preserved
                myFileCollectionArtifact(getArtifactTypeClass('FileCollectionArtifact)) {
                    files = fileTree(dir: 'myDir')      // Required. Set the filecollection (e.g. filetree, files, etc) to deploy
                }

                // FileTreeArtifact is like a FileCollectionArtifact, but the directory structure is preserved
                myFileTreeArtifact(getArtifactTypeClass('FileTreeArtifact)) {
                    files = fileTree(dir: 'mydir')      // Required. Set the fileTree (e.g. filetree, ziptree) to deploy
                }

                myCommandArtifact(getArtifactTypeClass('CommandArtifact)) {
                    command = 'echo Hello'              // The command to run. Required.
                    // Output will be stored in 'result' after execution
                }

                // JavaArtifact inherits from FileArtifact
                myJavaArtifact(getArtifactTypeClass('JavaArtifact)) {
                    // The binary to deploy is not configured by default. To configure,
                    // assign the exectuable property to the binary you want to run.
                    // See below for how to do this.
                    // High level plugins can provide an easier way to do this.
                }

                myNativeArtifact(getArtifactTypeClass('NativeExecutableArtifact)) {
                    // The binary to deploy is not configured by default. To configure,
                    // assign the exectuable property to the binary you want to run.
                    // See below for how to do this.
                    // High level plugins can provide an easier way to do this.
                }
            }
        }
    }
}

// For Java
deploy.targets.myTarget.artifacts.myJavaArtifact.jarTask = jar // Assuming you have a standard 'java' plugin

// For Native Code
model {
    components {
        my_program(NativeExecutableSpec) {
            binaries.all {
                // Filter to binary you want to deploy here.
                // For instace
                if (it.targetPlatform.name == 'SomeCrossBuild' && it.buildType.name == 'debug') {
                    deploy.targets.myTarget.artifacts.myNativeArtifact.binary = it
                }
            }
        }
    }
}
```
