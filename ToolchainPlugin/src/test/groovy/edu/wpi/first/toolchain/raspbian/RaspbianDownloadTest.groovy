package edu.wpi.first.toolchain.raspbian

import org.gradle.testkit.runner.GradleRunner
import static org.gradle.testkit.runner.TaskOutcome.*
import org.junit.Rule
import spock.lang.Shared
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import spock.lang.IgnoreIf

@IgnoreIf({ !Boolean.valueOf(env['SPOCK_RUN_TOOLCHAINS']) })
class RaspbianDownloadTest extends Specification {
  @Rule TemporaryFolder testProjectDir = new TemporaryFolder()
  File buildFile
  @Shared File toolchainDir

  def setup() {
    buildFile = testProjectDir.newFile('build.gradle')
  }

  def setupSpec() {
    RaspbianToolchainExtension ext = new RaspbianToolchainExtension()
    String raspbianVersion = ext.toolchainVersion.split("-")[0].toLowerCase();
    toolchainDir = RaspbianToolchainPlugin.toolchainInstallLoc(raspbianVersion)
    def result = toolchainDir.deleteDir()  // Returns true if all goes well, false otherwise.
    assert result
  }

  def "Toolchain Can Download"() {
    given:
    buildFile << """plugins {
  id 'cpp'
  id 'edu.wpi.first.Toolchain'
}

toolchainsPlugin.withRaspbian()
"""
    when:
    def result = GradleRunner.create()
                             .withProjectDir(testProjectDir.root)
                             .withArguments('installRaspbianToolchain', '--stacktrace')
                             .withPluginClasspath()
                             .build()

    then:
    result.task(':installRaspbianToolchain').outcome == SUCCESS
  }
}
