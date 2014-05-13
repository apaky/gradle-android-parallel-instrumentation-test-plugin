package com.myob.android.gradle.plugin.parallelrunner.instrumentation

import com.android.build.gradle.internal.Sdk
import com.android.builder.testing.ConnectedDeviceProvider
import com.android.builder.testing.TestData
import com.android.builder.testing.api.DeviceConnector
import com.android.builder.testing.api.DeviceProvider
import com.myob.android.gradle.plugin.parallelrunner.ParallelInstrumentationTestExtension
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction

import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future

class InstrumentationTestTask extends DefaultTask {

  @InputFile
  File testApk

  @InputFile
  File applicationApk

  Sdk sdk

  String flavorName

  TestData testData

  File reportDir

  @TaskAction
  def runTask() {
    def startTime = System.currentTimeMillis()

    ParallelInstrumentationTestExtension config = project.extensions.findByType(ParallelInstrumentationTestExtension)
    ConnectedDeviceProvider provider = getDeviceProvider()

    List<String> packagesToExecute = config.concurrentInstrumentationPackages
    List<DeviceConnector> devices = provider.devices
    checkSufficientNumberOfDevices(packagesToExecute, devices)

    List<TestExecution> executions = []
    packagesToExecute.eachWithIndex { String packageName, i ->
      executions << new TestExecution(
              appUnderTest: applicationApk,
              projectName: project.name,
              flavorName: flavorName,
              reportDir: reportDir,
              testApp: testApk,
              device: devices[i],
              testData: testData,
              instrumentationOption: new InstrumentationOption(name: InstrumentationOption.PACKAGE, value: packageName),
              testCallbacks: new TestLifecycleCallback(beforeTest: config.onBeforeDeviceTest, afterTest: config.onAfterDeviceTest))
    }

    if (config.onBeforeAllTests) {
      config.onBeforeAllTests.call()
    }

    List<Future<Boolean>> futures = []
    Random rand = new Random()
    ExecutorService executorService = Executors.newFixedThreadPool(packagesToExecute.size())
    executions.each { TestExecution execution ->
      futures << executorService.submit(new Callable<Boolean>() {
        @Override
        Boolean call() throws Exception {
          // Wait a small amount of random time, as kicking off tests at the exact same time causes ddmlib
          // errors when deploying apps
          Thread.sleep((int)Math.abs(rand.nextFloat() * 5000))
          return execution.execute()
        }
      })
    }

    boolean success = true
    futures.each { Future<Boolean> testResult ->
      if (!testResult.get()) {
        success = false
      }
    }
    def endTime = System.currentTimeMillis()
    def timeInMinutes = (endTime-startTime)/1000/60
    println "Completed parallel tests in $timeInMinutes minutes"
    if (!success) {
      throw new GradleException("Acceptance tests failed")
    }

  }

  private ConnectedDeviceProvider getDeviceProvider() {
    DeviceProvider provider = new ConnectedDeviceProvider(sdk.parser)
    provider.init()
    provider
  }


  private void checkSufficientNumberOfDevices(List<String> packagesToExecute, List<DeviceConnector> devices) {
    if (packagesToExecute.size() > devices.size()) {
      def errorMessage = "Request to run ${packagesToExecute.size()} parallel instrumentation test suites, but only ${devices.size()} devices found.\n"
      devices.each { DeviceConnector device ->
        errorMessage += "device ID -> ${device.name}\n"
      }
      throw new IllegalStateException(errorMessage)
    }
  }

}
