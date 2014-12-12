package com.myob.android.gradle.plugin.parallelrunner

import org.gradle.api.Project
import org.gradle.api.internal.plugins.PluginApplicationException
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class ParallelInstrumentationTestPluginSpec extends Specification {

  def "should throw exception when android plugin not applied"() {
    Project project = ProjectBuilder.builder().build()

    when:
    project.apply plugin: 'parallel-instrumentation-tests'

    then:
    def error = thrown(PluginApplicationException)
    error.cause.message == 'Android plugin is required but not found'
  }

}
