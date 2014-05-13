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
  InstrumentationOption instrumentationOption
  TestLifecycleCallback testCallbacks
  TestData testData
  File reportDir
  String projectName
  String flavorName

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
      println "Completed tests for ${instrumentationOption.value} in $timeTaken minutes"
    }
  }

  boolean runTest() {
    CustomTestRunListener runListener = new CustomTestRunListener(device.name, projectName, flavorName, Logger.getLoggerWrapper());
    runListener.reportDir = reportDir;

    RemoteAndroidTestRunner runner = createTestRunner()
    boolean testRunPassed
    try {
      println "Running test for package ${instrumentationOption.value}"
      runner.run(runListener)
      println "Complete tests for package ${instrumentationOption.value}.  There were ${runListener.runResult.numFailedTests} failures"
      testRunPassed = !runListener.runResult.hasFailedTests()
    } catch (Exception e) {
      println "Error running tests!!!! ${e.message}"
      testRunPassed = false
    }
    testRunPassed
  }

  private RemoteAndroidTestRunner createTestRunner() {
    RemoteAndroidTestRunner runner = new RemoteAndroidTestRunner(testData.packageName, testData.instrumentationRunner, device);
    runner.runName = device.name
    runner.setMaxtimeToOutputResponse(0);
    runner.addInstrumentationArg(instrumentationOption.name, instrumentationOption.value)
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

  private ArrayList<File> installApps() {
    [testApp, appUnderTest].each { File app ->
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
            ", instrumentationOption=" + instrumentationOption +
            ", testCallbacks=" + testCallbacks +
            ", testData=" + testData +
            ", reportDir=" + reportDir +
            ", projectName='" + projectName + '\'' +
            ", flavorName='" + flavorName + '\'' +
            '}';
  }
}
