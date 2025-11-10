package edu.wpi.first.toolchain.roborio

import org.gradle.testkit.runner.GradleRunner
import static org.gradle.testkit.runner.TaskOutcome.*
import edu.wpi.first.toolchain.opensdk.OpenSdkToolchainBase

import spock.lang.Shared
import spock.lang.TempDir
import spock.lang.Specification
import spock.lang.IgnoreIf

@IgnoreIf({ !Boolean.valueOf(env['SPOCK_RUN_TOOLCHAINS']) })
class RoboRioDownloadTest extends Specification {
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
  id 'edu.wpi.first.Toolchain'
}

toolchainsPlugin.withCrossRoboRIO()
"""
    when:
    def result = GradleRunner.create()
                             .withProjectDir(testProjectDir)
                             .withArguments('installRoboRioToolchain', '--stacktrace', '-Dgradle.user.home=' + gradleUserHome)
                             .withPluginClasspath()
                             .build()

    then:
    result.task(':installRoboRioToolchain').outcome == SUCCESS
  }
}
