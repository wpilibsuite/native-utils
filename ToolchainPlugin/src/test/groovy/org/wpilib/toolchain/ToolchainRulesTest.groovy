package org.wpilib.toolchain

import org.gradle.api.Action
import org.gradle.nativeplatform.platform.NativePlatform
import org.gradle.nativeplatform.toolchain.Gcc
import org.gradle.nativeplatform.toolchain.GccCommandLineToolConfiguration
import org.gradle.nativeplatform.toolchain.GccPlatformToolChain
import org.gradle.testkit.runner.GradleRunner
import static org.gradle.testkit.runner.TaskOutcome.*
import spock.lang.TempDir
import spock.lang.Specification

class ToolchainRulesTest extends Specification {
  @TempDir File testProjectDir
  File buildFile

  def setup() {
    buildFile = new File(testProjectDir, 'build.gradle')
  }

  def "Versioned Linux GCC requirement can be configured from DSL"() {
    given:
    buildFile << """plugins {
  id 'cpp'
  id 'org.wpilib.Toolchain'
}

toolchainsPlugin {
  requireVersionedLinuxGcc = false
  linuxCCompilerExecutable = 'gcc-custom'
  linuxCppCompilerExecutable = 'g++-custom'
}

tasks.register('verifyLinuxGccSettings') {
  doLast {
    assert !toolchainsPlugin.requireVersionedLinuxGcc
    assert toolchainsPlugin.linuxCCompilerExecutable == 'gcc-custom'
    assert toolchainsPlugin.linuxCppCompilerExecutable == 'g++-custom'
  }
}
"""

    when:
    def result = GradleRunner.create()
                             .withProjectDir(testProjectDir)
                             .withArguments('verifyLinuxGccSettings', '--stacktrace')
                             .withPluginClasspath()
                             .build()

    then:
    result.task(':verifyLinuxGccSettings').outcome == SUCCESS
  }

  def "Linux default GCC toolchain uses GCC 14 executables"() {
    given:
    def gcc = new RecordingGcc()

    when:
    ToolchainRules.configureRequiredLinuxGcc(gcc, 'gcc-14', 'g++-14')

    then:
    gcc.platformToolChain.cCompiler.executable == 'gcc-14'
    gcc.platformToolChain.cppCompiler.executable == 'g++-14'
    gcc.platformToolChain.linker.executable == 'g++-14'
    gcc.platformToolChain.assembler.executable == 'gcc-14'
  }

  def "Linux default GCC toolchain can use custom executables"() {
    given:
    def gcc = new RecordingGcc()

    when:
    ToolchainRules.configureRequiredLinuxGcc(gcc, 'gcc-custom', 'g++-custom')

    then:
    gcc.platformToolChain.cCompiler.executable == 'gcc-custom'
    gcc.platformToolChain.cppCompiler.executable == 'g++-custom'
    gcc.platformToolChain.linker.executable == 'g++-custom'
    gcc.platformToolChain.assembler.executable == 'gcc-custom'
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
