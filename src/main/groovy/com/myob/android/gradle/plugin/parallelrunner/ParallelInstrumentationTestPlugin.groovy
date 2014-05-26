package com.myob.android.gradle.plugin.parallelrunner

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.api.TestVariant
import com.android.build.gradle.internal.SdkHandler
import com.android.builder.model.ProductFlavor
import com.android.builder.testing.TestData
import com.myob.android.gradle.plugin.parallelrunner.instrumentation.InstrumentationTestTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaBasePlugin

class ParallelInstrumentationTestPlugin implements Plugin<Project> {

  @Override
  void apply(Project project) {
    AppPlugin androidPlugin = project.plugins.findPlugin(AppPlugin)
    addExtension(project)
    androidPluginCheck(androidPlugin)
    addParallelInstrumentationTasks(project)
  }

  void addParallelInstrumentationTasks(Project project) {
    AppExtension androidExtension = project.android
    SdkHandler sdk = new SdkHandler(project, Logger.getLoggerWrapper())

    TestData testData = createTestData(androidExtension.defaultConfig)
    androidExtension.testVariants.all { TestVariant variant ->
      createTask(project, sdk, variant, testData)
    }
  }

  def createTestData(ProductFlavor flavor) {
    return new TestData(){
      @Override
      String getPackageName() {
        flavor.testPackageName
      }

      @Override
      String getTestedPackageName() {
        flavor.packageName
      }

      @Override
      String getInstrumentationRunner() {
        flavor.testInstrumentationRunner
      }

      @Override
      Boolean getHandleProfiling() {
        flavor.testHandleProfiling
      }

      @Override
      Boolean getFunctionalTest() {
        flavor.testFunctionalTest
      }

      @Override
      boolean isTestCoverageEnabled() {
        return false
      }

      @Override
      int getMinSdkVersion() {
        flavor.minSdkVersion
      }

      @Override
      Set<String> getSupportedAbis() {
        null
      }
    }
  }

  private ParallelInstrumentationTestExtension addExtension(Project project) {
    project.extensions.create("parallelInstrumentationTests", ParallelInstrumentationTestExtension)
  }

  void createTask(final Project project, final SdkHandler sdk, final TestVariant variant, final TestData testData) {
    InstrumentationTestTask task = project.tasks.create("parallel${variant.name.capitalize()}", InstrumentationTestTask)
    task.dependsOn variant.assemble, variant.testedVariant.assemble
    task.group = JavaBasePlugin.VERIFICATION_GROUP
    task.description = "Parellelises instrumentation tests across all connected devices for '${variant.name.capitalize()}'"

    task.applicationApk = variant.testedVariant.outputFile
    task.testApk = variant.outputFile
    task.sdk = sdk
    task.testData = testData
    task.flavorName = variant.flavorName
    task.reportDir = project.buildDir
  }

  private void androidPluginCheck(Plugin<Project> aPlugin) {
    if (aPlugin == null) {
      throw new IllegalStateException("Android plugin is required but not found")
    }
  }
}
