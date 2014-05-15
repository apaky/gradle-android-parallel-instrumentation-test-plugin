package com.myob.android.gradle.plugin.parallelrunner

class ParallelInstrumentationTestExtension {

  boolean installApplication = true

  def concurrentInstrumentationPackages = []

  Closure onBeforeDeviceTest

  Closure onAfterDeviceTest

  Closure onBeforeAllTests
}
