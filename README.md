[![Build Status](https://travis-ci.org/MYOB-Technology/gradle-android-parallel-instrumentation-test-plugin.svg?branch=master)](https://travis-ci.org/MYOB-Technology/gradle-android-parallel-instrumentation-test-plugin)

# A gradle android plugin capable of running instrumentation tests in parallel

Plugin runs instrumentation tests in parallel.  Deploys the app and test app to each emulator and runs a different set of tests on each emulator.  

## Requirements

  - Enough emulators running based on the maximum concurrent executions as configured in the plugin dsl

 - Gradle anroid build tools plugin 0.10.2+

#Usage

Requires the android plugin to be loaded.


````
apply plugin: 'parallel-instrumentation-tests'

parallelInstrumentationTests {
  concurrentInstrumentationPackages = [	  
	  "com.acme.instrumentation.test.foo",
  	  "com.acme.instrumentation.test.bar"
  	  ]

  onBeforeAllTests {
    println "Hi, I'm executed before any test is run"
  }

  onBeforeDeviceTest { device ->
    println "Before test on device ${device.name}"
  }

  onAfterDeviceTest { device ->
    println "After test on device ${device.name}"
  }
}
````

The above configuration will execute two concurrent instrumentation tests.  One execution will run the tests in package *com.acme.instrument.test.foo*, and the other will run the tests in *com.acme.instrumentation.test.bar*
