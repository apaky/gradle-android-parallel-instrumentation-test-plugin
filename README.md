# A gradle android plugin capable of running instrumentation tests in parallel

Plugin runs instrumentation tests in parallel.  Deploys the app and test app to each emulator and runs a different set of tests on each emulator.  

## Requirements
Enough emulators running based on the maximum concurrent executions as configured in the plugin dsl

#Usage

Requires the android plugin to be used


````
apply plugin: 'parallel-instrumentation-tests'

parallelInstrumentationTests {
  concurrentInstrumentationPackages = [
	  "com.acme.instrumentation.test.foo",
  	  "com.acme.instrumentation.test.bar",
	  "com.acme.instrumentation.test.baz"
  ]

  onBeforeAllTests {
    printtln "Hi, I'm executed before any test is run"
  }

  onBeforeDeviceTest { device ->
    println "Before test on device ${device.name}"
  }

  onAfterDeviceTest { device ->
    println "After test on device ${device.name}"
  }
}
````