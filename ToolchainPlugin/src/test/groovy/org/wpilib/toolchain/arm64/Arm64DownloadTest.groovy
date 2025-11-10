package org.wpilib.toolchain.arm64

import org.gradle.testkit.runner.GradleRunner
import static org.gradle.testkit.runner.TaskOutcome.*
import org.wpilib.toolchain.opensdk.OpenSdkToolchainBase

import spock.lang.Shared
import spock.lang.TempDir
import spock.lang.Specification
import spock.lang.IgnoreIf

@IgnoreIf({ !Boolean.valueOf(env['SPOCK_RUN_TOOLCHAINS']) })
class Arm64DownloadTest extends Specification {
  @TempDir File testProjectDir
  @TempDir File gradleUserHome
  File buildFile
  @Shared File toolchainDir

  def setup() {
    buildFile = new File(testProjectDir, 'build.gradle')
  }

  def "Toolchain Can Download"() {
    given:
    buildFile << """plugins {
  id 'cpp'
  id 'org.wpilib.Toolchain'
}

toolchainsPlugin.withCrossLinuxArm64()
"""
    when:
    def result = GradleRunner.create()
                             .withProjectDir(testProjectDir)
                             .withArguments('installArm64Toolchain', '--stacktrace', '-Dgradle.user.home=' + gradleUserHome)
                             .withPluginClasspath()
                             .build()

    then:
    result.task(':installArm64Toolchain').outcome == SUCCESS
  }
}
