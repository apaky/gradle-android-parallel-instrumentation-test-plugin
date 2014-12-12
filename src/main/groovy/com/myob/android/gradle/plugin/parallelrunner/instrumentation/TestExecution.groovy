package com.myob.android.gradle.plugin.parallelrunner.instrumentation

import com.android.builder.internal.testing.CustomTestRunListener
import com.android.builder.testing.TestData
import com.android.builder.testing.api.DeviceConnector
import com.android.builder.testing.api.DeviceException
import com.android.ddmlib.testrunner.RemoteAndroidTestRunner
import com.myob.android.gradle.plugin.parallelrunner.Logger

class TestExecution {

  File testApp
  File appUnderTest
  DeviceConnector device
  List<InstrumentationOption> instrumentationOptions
  TestLifecycleCallback testCallbacks
  TestData testData
  File reportDir
  String projectName
  String flavorName
  boolean installApps = true

  boolean execute() {
    def start = System.currentTimeMillis()
    try {
      installApps()
      beforeTest()
      return runTest()
    } catch (Exception exception) {
      Logger.error("Failed to run tests on device ${device.name}")
      return false;
    } finally {
      afterTest()
      def timeTaken = (System.currentTimeMillis() - start) / 1000 / 60
      println "Completed tests on $device.name in $timeTaken minutes"
    }
  }

  boolean runTest() {

    boolean testRunPassed = true

    instrumentationOptions.eachWithIndex { InstrumentationOption option , int index ->
      CustomTestRunListener runListener = new CustomTestRunListener(device.name, "${projectName}-execution-${index+1}", flavorName, Logger.getLoggerWrapper());
      runListener.reportDir = reportDir;

      RemoteAndroidTestRunner runner = createTestRunner(option)
      try {
        println "${device.name} - running test with option $option"
        runner.run(runListener)
        println "${device.name} - completed test with option $option - ${runListener.runResult.numCompleteTests} tests, ${runListener.runResult.numAllFailedTests} failures"
        if (runListener.runResult.hasFailedTests()){
          testRunPassed = false
        }
      } catch (Exception e) {
        println "Error running tests ${e.message}"
        testRunPassed = false
      }

    }
    testRunPassed

  }

  private RemoteAndroidTestRunner createTestRunner(InstrumentationOption option) {
    RemoteAndroidTestRunner runner = new RemoteAndroidTestRunner(testData.applicationId, testData.instrumentationRunner, device);
    runner.runName = device.name
    runner.setMaxtimeToOutputResponse(0);
    runner.addInstrumentationArg(option.name, option.value)
    runner
  }

  protected def beforeTest() {
    if (testCallbacks.beforeTest) {
      testCallbacks.beforeTest.call(device)
    }
  }

  protected def afterTest() {
    if (testCallbacks.afterTest) {
      testCallbacks.afterTest.call(device)
    }
  }

  private void installApps() {
    def appsToInstall = [testApp]
    if (installApps) {
      appsToInstall << appUnderTest
    }

    appsToInstall.each { File app ->
      println "Attempting to install ${app.absolutePath} to ${device.name}..."

      try {
        device.installPackage(app, 300000, Logger.getLoggerWrapper())
      } catch (DeviceException e) {
        Logger.error("Error installing $app to ${device.name} ${e.message}")
        throw e
      }

      println "Installed ${app.absolutePath} to ${device.name}..."
    }
  }

  @Override
  public String toString() {
    return "TestExecution{" +
            "testApp=" + testApp +
            ", appUnderTest=" + appUnderTest +
            ", device=" + device.properties +
            ", instrumentationOptions=" + instrumentationOptions +
            ", testCallbacks=" + testCallbacks +
            ", testData=" + testData +
            ", reportDir=" + reportDir +
            ", projectName='" + projectName + '\'' +
            ", flavorName='" + flavorName + '\'' +
            '}';
  }
}
