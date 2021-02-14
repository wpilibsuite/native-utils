package edu.wpi.first.nativeutils

import org.gradle.testkit.runner.GradleRunner
import static org.gradle.testkit.runner.TaskOutcome.*
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification


class NativeUtilsPluginInitializationTest extends Specification {
  @Rule TemporaryFolder testProjectDir = new TemporaryFolder()
  File buildFile

  def setup() {
    buildFile = testProjectDir.newFile('build.gradle')
  }

  def "Project Initializes Correctly"() {
    given:
    buildFile << """plugins {
  id 'cpp'
  id 'edu.wpi.first.NativeUtils'
}
"""
    when:
    def result = GradleRunner.create()
                             .withProjectDir(testProjectDir.root)
                             .withArguments('tasks', '--stacktrace')
                             .withPluginClasspath()
                             .build()

    then:
    result.task(':tasks').outcome == SUCCESS
  }
}
