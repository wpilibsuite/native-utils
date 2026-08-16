package org.wpilib.nativeutils

import org.gradle.testkit.runner.GradleRunner
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS
import spock.lang.Specification
import spock.lang.TempDir

class WPINativeUtilsExtensionTest extends Specification {
  @TempDir File testProjectDir
  File buildFile

  def setup() {
    buildFile = new File(testProjectDir, 'build.gradle')
  }

  def "Systemcore links to libsystemd"() {
    given:
    buildFile << """plugins {
  id 'cpp'
  id 'org.wpilib.NativeUtils'
}

nativeUtils.addWpiNativeUtils()

tasks.register('verifySystemcoreLinkerArguments') {
  doLast {
    def systemcoreArgs = nativeUtils.platformConfigs.getByName('linuxsystemcore').linker.args
    def linuxArm64Args = nativeUtils.platformConfigs.getByName('linuxarm64').linker.args

    assert systemcoreArgs.contains('-lsystemd')
    assert !linuxArm64Args.contains('-lsystemd')
  }
}
"""

    when:
    def result = GradleRunner.create()
                             .withProjectDir(testProjectDir)
                             .withArguments('verifySystemcoreLinkerArguments', '--stacktrace')
                             .withPluginClasspath()
                             .build()

    then:
    result.task(':verifySystemcoreLinkerArguments').outcome == SUCCESS
  }
}
