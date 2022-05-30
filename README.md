# Native Utils and Toolchain Plugin

![CI](https://github.com/wpilibsuite/native-utils/workflows/CI/badge.svg)

## DSL Documentation for Native Utils

```
nativeUtils {
  platformConfigs {
    linuxathena {
      // The platform path for archives. Must be set
      platformPath = "linux/athena"

      cppCompiler {
        // Args for debug and release
        args << ""
        // Args for debug
        debugArgs << ""
        // Args for release
        releaseArgs << ""
      }
      // These are identical to cppCompiler
      linker {}
      cCompiler {}
      assembler {}
      objcppCompiler {}
      objcCompiler {}
    }
  }

  // Windows specific functionality to export all symbols from a binary automatically
  exportsConfigs {
    libName {
      x86ExcludeSymbols << ""
      x64ExcludeSymbols << ""
      excludeBuildTypes << ""
      x86SymbolFilter = { symbols ->
        symbols.removeIf({ !it.startsWith('HAL_') && !it.startsWith('HALSIM_') })
      }
      x64SymbolFilter = { symbols ->
        symbols.removeIf({ !it.startsWith('HAL_') && !it.startsWith('HALSIM_') })
      }
    }
  }
  // Multi platform way to expose only a limited number of symbols
  // Used to do private symbols. Can not cross libraries with exportsConfigs
  privateExportsConfigs {
    libName {
      exportsFile = project.file("path/to/symbols/files")
    }

  }
  // Add a dependency
  dependencyConfigs {
    libraryName {
      groupId = ""
      artifactId = ""
      headerClassifier = ""
      sourceClassifier = ""
      ext = ""
      version = ""
      // If the shared dependencies are used at runtime, or just linking
      // Defaults to true
      sharedUsedAtRuntime = true
      sharedPlatforms << ""
      staticPlatforms << ""
    }
  }
  // Add
  combinedDependencyConfigs {
    combinedName {
      // The name to use from use*Library
      libraryName = ""
      // The platforms to apply to
      targetPlatforms << ""
      // The dependencies to combine
      dependencies << ""
    }
  }
}

// Get the platform path for a binary
nativeUtils.getPlatformPath(NativeBinarySpec binary)
// Get the classifier for a dependency
nativeUtils.getDependencyClassifier(NativeBinarySpec, boolean isStaticDependnecy)
// Get the classifier for a published binary
nativeUtils.getPublishClassifier(NativeLibraryBinarySpec)

// Add libraries that are required to build, add to all binaries for a component
nativeUtils.useRequiredLibrary(ComponentSpec, String... libraries)
// Add libraries that are required to build, add to specific binary
nativeUtils.useRequiredLibrary(BinarySpec, String.. libraries)

// Add libraries that are optional, add to all binaries for a component
nativeUtils.useOptionalLibrary(ComponentSpec, String... libraries)
// Add libraries that are optional, add to specific binary
nativeUtils.useOptionalLibrary(BinarySpec, String.. libraries)
// The optional ones will be silently skipped

// Add all native utils platforms to a component
nativeUtils.useAllPlatforms(ComponentSpec)

// Update a platform (see platformsConfig block above for documentation)
// This can be used for adding or removing args.
nativeUtils.configurePlatform("platformName") {
}

// Add all arguments for a platform to the binary
nativeUtils.usePlatformArguments(NativeBinarySpec)

// Add all arguments for a platform to all components of a binary
nativeUtils.usePlatformArguments(NativeComponentSpec)

// Add WPI extensions to Native Utils
// See below for DSL of these extensions
nativeUtils.addWpiNativeUtils()

// This adds all the WPILib dependencies, along with combined deps for
// wpilib and driver. They still need to manually be added to individual
// components. These just add to the back end
nativeUtils.wpi.configureDependencies {
  // Thse are the 6 separate versions used for wpi
  // deps. They should be kept in sync.
  wpiVersion = ""
  niLibVersion = ""
  opencvVersion = ""
  googleTestVersion = ""
  imguiVersion = ""
  wpimathVersion = ""

}

// The 6 below get the string representation of the main platforms
// For use comparing to binary.targetPlatform.name
nativeUtils.wpi.platforms.roborio
nativeUtils.wpi.platforms.raspbian
nativeUtils.wpi.platforms.windowsx64
nativeUtils.wpi.platforms.windowsx86
nativeUtils.wpi.platforms.osxx64
nativeUtils.wpi.platforms.linuxx64

// An immutable list of all wpi platforms
nativeUtils.wpi.platforms.allPlatforms

// A bunch of lists of the default arguments for platforms.
nativeUtils.wpi.defaultArguments.*


// Enable warnings for all platforms. Pass specific platforms in to enable them for just those platforms
nativeUtils.wpi.addWarnings()

// Enable warnings as errors for all platforms. Pass specific platforms in to enable them for just those platforms
// Does not enable warnings, that is still handled by the call above.
nativeUtils.wpi.addWarningsAsErrors()

```

## DS Documentation for Toolchain Builder

```
toolchainsPlugin {
  // Register the platforms and build types with the model
  // Default to true
  registerPlatforms = true
  registerReleaseBuildType = true
  registerDebugBuildType = true

  // Add the roborio compiler
  withRoboRIO()
  // Add the raspbian compiler
  withRasbian()
  // The above 2 are included with nativeUtils.addWpiNativeUtils()

  crossCompilers {
    linuxaarch64 {
        architecture = "aarch64"
        compilerPrefix = "arm-frc2019-linux-gnueabi-"
        operatingSystem = "linux"
        optional = false
    }
  }
}
```

## Adding a non standard cross compiler

Use the following to add a custom cross compiler, with the same args as the rio and raspbian

```
nativeUtils.addWpiNativeUtils() // Must be called before using nativeUtils.wpi.defaultArguments

toolchainsPlugin.crossCompilers {
    linuxaarch64 {
        architecture = "aarch64"
        compilerPrefix = "aarch64-linux-gnu-"
        operatingSystem = "linux"
        optional = false
    }
}

nativeUtils.platformConfigs {
    linuxaarch64 {
        platformPath = "linux/aarch64"
        cppCompiler.args.addAll(nativeUtils.wpi.defaultArguments.linuxCrossCompilerArgs);
        cCompiler.args.addAll(nativeUtils.wpi.defaultArguments.linuxCrossCCompilerArgs);
        linker.args.addAll(nativeUtils.wpi.defaultArguments.linuxCrossLinkerArgs);
        cppCompiler.debugArgs.addAll(nativeUtils.wpi.defaultArguments.linuxCrossDebugCompilerArgs);
        cppCompiler.releaseArgs.addAll(nativeUtils.wpi.defaultArguments.linuxCrossReleaseCompilerArgs);
    }
}
```
