package edu.wpi.first.toolchain.systemcore

import org.gradle.testkit.runner.GradleRunner
import static org.gradle.testkit.runner.TaskOutcome.*
import edu.wpi.first.toolchain.opensdk.OpenSdkToolchainBase

import spock.lang.Shared
import spock.lang.TempDir
import spock.lang.Specification
import spock.lang.IgnoreIf

@IgnoreIf({ !Boolean.valueOf(env['SPOCK_RUN_TOOLCHAINS']) })
class Arm64DownloadTest extends Specification {
  @TempDir File testProjectDir
  File buildFile
  @Shared File toolchainDir

  def setup() {
    buildFile = new File(testProjectDir, 'build.gradle')
  }

  def setupSpec() {
    String year = SystemCoreToolchainExtension.TOOLCHAIN_VERSION.split("-")[0].toLowerCase();
    toolchainDir = OpenSdkToolchainBase.toolchainInstallLoc(year, SystemCoreToolchainExtension.INSTALL_SUBDIR);
    def result = toolchainDir.deleteDir()  // Returns true if all goes well, false otherwise.
    assert result
  }

  def "Toolchain Can Download"() {
    given:
    buildFile << """plugins {
  id 'cpp'
  id 'edu.wpi.first.Toolchain'
}

toolchainsPlugin.withCrossSystemCore()
"""
    when:
    def result = GradleRunner.create()
                             .withProjectDir(testProjectDir)
                             .withArguments('installSystemCoreToolchain', '--stacktrace')
                             .withPluginClasspath()
                             .build()

    then:
    result.task(':installSystemCoreToolchain').outcome == SUCCESS
  }
}
