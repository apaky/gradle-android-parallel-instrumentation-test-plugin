package com.myob.android.gradle.plugin.parallelrunner

import com.myob.android.gradle.plugin.parallelrunner.instrumentation.InstrumentationTestTask
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class InstrumentationTestTaskSpec extends Specification {

  InstrumentationTestTask testTask
  Project project

  def setup() {
    testTask = new InstrumentationTestTask()

    ProjectBuilder.builder().build()
    project.apply plugin: 'android'
    project.apply plugin: 'parallel-instrumentation-tests'
  }

//  def "should throw exception when no of emulators is less than the request packages"() {
//    when:
//    testTask.runTask()
//
//    then:
//    def error = thrown(IllegalStateException)
//    error.message == "Android plugin is required but not found"
//  }

}
