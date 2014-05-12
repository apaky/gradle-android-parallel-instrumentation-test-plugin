package com.myob.android.gradle.plugin.parallelrunner

class ParallelInstrumentationTestExtension {

  def concurrentInstrumentationPackages = []

  Closure onBeforeDeviceTest

  Closure onAfterDeviceTest

  Closure onBeforeAllTests
}
