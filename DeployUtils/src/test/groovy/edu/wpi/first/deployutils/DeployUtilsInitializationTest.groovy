package edu.wpi.first.deployutils

import org.gradle.testkit.runner.GradleRunner
import static org.gradle.testkit.runner.TaskOutcome.*
import spock.lang.TempDir
import spock.lang.Specification

class DeployUtilsInitializationTest extends Specification {
  @TempDir File testProjectDir
  File buildFile

  def setup() {
    buildFile = new File(testProjectDir, 'build.gradle')
  }

  def "Project Initializes Correctly"() {
    given:
    buildFile << """plugins {
  id 'cpp'
  id 'edu.wpi.first.DeployUtils'
}
"""
    when:
    def result = GradleRunner.create()
                             .withProjectDir(testProjectDir)
                             .withArguments('tasks', '--stacktrace')
                             .withPluginClasspath()
                             .build()

    then:
    result.task(':tasks').outcome == SUCCESS
  }
}
