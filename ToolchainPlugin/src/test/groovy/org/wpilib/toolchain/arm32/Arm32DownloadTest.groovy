package org.wpilib.toolchain.arm32

import org.gradle.testkit.runner.GradleRunner
import static org.gradle.testkit.runner.TaskOutcome.*
import org.wpilib.toolchain.opensdk.OpenSdkToolchainBase

import spock.lang.Shared
import spock.lang.TempDir
import spock.lang.Specification
import spock.lang.IgnoreIf

@IgnoreIf({ !Boolean.valueOf(env['SPOCK_RUN_TOOLCHAINS']) })
class Arm32DownloadTest extends Specification {
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

toolchainsPlugin.withCrossLinuxArm32()
"""
    when:
    def result = GradleRunner.create()
                             .withProjectDir(testProjectDir)
                             .withArguments('installArm32Toolchain', '--stacktrace', '-Dgradle.user.home=' + gradleUserHome)
                             .withPluginClasspath()
                             .build()

    then:
    result.task(':installArm32Toolchain').outcome == SUCCESS
  }
}
