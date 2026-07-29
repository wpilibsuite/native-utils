package org.wpilib.toolchain

import org.gradle.api.Action
import org.gradle.nativeplatform.platform.NativePlatform
import org.gradle.nativeplatform.toolchain.Gcc
import org.gradle.nativeplatform.toolchain.GccCommandLineToolConfiguration
import org.gradle.nativeplatform.toolchain.GccPlatformToolChain
import spock.lang.Specification

class ToolchainRulesTest extends Specification {
  def "Linux default GCC toolchain uses GCC 14 executables"() {
    given:
    def gcc = new RecordingGcc()

    when:
    ToolchainRules.configureRequiredLinuxGcc(gcc)

    then:
    gcc.platformToolChain.cCompiler.executable == 'gcc-14'
    gcc.platformToolChain.cppCompiler.executable == 'g++-14'
    gcc.platformToolChain.linker.executable == 'g++-14'
    gcc.platformToolChain.assembler.executable == 'gcc-14'
  }

  private static class RecordingGcc implements Gcc {
    final RecordingPlatformToolChain platformToolChain = new RecordingPlatformToolChain()

    @Override
    List<File> getPath() {
      return []
    }

    @Override
    void path(Object... paths) {}

    @Override
    void target(String targetPlatform) {}

    @Override
    void target(String targetPlatform, Action<? super GccPlatformToolChain> action) {
      action.execute(platformToolChain)
    }

    @Override
    void setTargets(String... targets) {}

    @Override
    void eachPlatform(Action<? super GccPlatformToolChain> action) {
      action.execute(platformToolChain)
    }

    @Override
    String getName() {
      return 'gcc'
    }

    @Override
    String getDisplayName() {
      return 'gcc'
    }
  }

  private static class RecordingPlatformToolChain implements GccPlatformToolChain {
    final RecordingTool cCompiler = new RecordingTool()
    final RecordingTool cppCompiler = new RecordingTool()
    final RecordingTool objcCompiler = new RecordingTool()
    final RecordingTool objcppCompiler = new RecordingTool()
    final RecordingTool assembler = new RecordingTool()
    final RecordingTool linker = new RecordingTool()
    final RecordingTool staticLibArchiver = new RecordingTool()

    @Override
    NativePlatform getPlatform() {
      return null
    }

    @Override
    GccCommandLineToolConfiguration getcCompiler() {
      return cCompiler
    }

    @Override
    GccCommandLineToolConfiguration getCppCompiler() {
      return cppCompiler
    }

    @Override
    GccCommandLineToolConfiguration getObjcCompiler() {
      return objcCompiler
    }

    @Override
    GccCommandLineToolConfiguration getObjcppCompiler() {
      return objcppCompiler
    }

    @Override
    GccCommandLineToolConfiguration getAssembler() {
      return assembler
    }

    @Override
    GccCommandLineToolConfiguration getLinker() {
      return linker
    }

    @Override
    GccCommandLineToolConfiguration getStaticLibArchiver() {
      return staticLibArchiver
    }
  }

  private static class RecordingTool implements GccCommandLineToolConfiguration {
    String executable

    @Override
    String getExecutable() {
      return executable
    }

    @Override
    void setExecutable(String executable) {
      this.executable = executable
    }

    @Override
    void withArguments(Action<? super List<String>> action) {}
  }
}
