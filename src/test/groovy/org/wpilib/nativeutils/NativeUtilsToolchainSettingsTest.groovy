package org.wpilib.nativeutils

import org.gradle.testkit.runner.GradleRunner
import static org.gradle.testkit.runner.TaskOutcome.*
import spock.lang.TempDir
import spock.lang.Specification

class NativeUtilsToolchainSettingsTest extends Specification {
  @TempDir File testProjectDir
  File buildFile

  def setup() {
    buildFile = new File(testProjectDir, 'build.gradle')
  }

  def "Versioned Linux GCC requirement can be configured from NativeUtils DSL"() {
    given:
    buildFile << """plugins {
  id 'cpp'
  id 'org.wpilib.NativeUtils'
}

nativeUtils {
  requireVersionedLinuxGcc = false
  linuxCCompilerExecutable = 'gcc-custom'
  linuxCppCompilerExecutable = 'g++-custom'
}

tasks.register('verifyLinuxGccSettings') {
  doLast {
    assert !nativeUtils.requireVersionedLinuxGcc
    assert nativeUtils.linuxCCompilerExecutable == 'gcc-custom'
    assert nativeUtils.linuxCppCompilerExecutable == 'g++-custom'
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
}
