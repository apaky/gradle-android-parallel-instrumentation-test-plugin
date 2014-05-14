package com.myob.android.gradle.plugin.parallelrunner.instrumentation

import spock.lang.Specification

class InstrumentationOptionSpec extends Specification {

  def "should return single package option"() {

    when:
    List<InstrumentationOption> options = InstrumentationOption.fromPackageNames("com.foo")

    then:
    options.size() == 1
    options.first().name == InstrumentationOption.PACKAGE
    options.first().value == "com.foo"
  }

  def "should return list of package options"() {
    when:
    List<InstrumentationOption> options = InstrumentationOption.fromPackageNames(["com.package", "com.package.foo"])

    then:
    options.size() == 2
    options[0].name == InstrumentationOption.PACKAGE
    options[1].name == InstrumentationOption.PACKAGE
    options[0].value == "com.package"
    options[1].value == "com.package.foo"
  }

  def "should handle empty list"() {
    when:
    List<InstrumentationOption> options = InstrumentationOption.fromPackageNames([])

    then:
    options.isEmpty()
  }

  def "should handle null value"() {
    when:
    List<InstrumentationOption> options = InstrumentationOption.fromPackageNames(null)

    then:
    options.isEmpty()
  }
}
