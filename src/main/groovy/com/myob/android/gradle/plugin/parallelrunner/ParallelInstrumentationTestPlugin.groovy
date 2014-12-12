package com.myob.android.gradle.plugin.parallelrunner

import com.android.annotations.NonNull
import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.api.TestVariant
import com.android.build.gradle.internal.SdkHandler
import com.android.builder.model.ApiVersion
import com.android.builder.model.ProductFlavor
import com.android.builder.testing.TestData
import com.google.common.collect.ImmutableList
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
      String getApplicationId() {
        return flavor.testApplicationId
      }

      @Override
      String getTestedApplicationId() {
        return flavor.applicationId;
      }

      @Override
      String getInstrumentationRunner() {
        return flavor.testInstrumentationRunner;
      }

      @Override
      Boolean getHandleProfiling() {
        return flavor.testHandleProfiling;
      }

      @Override
      Boolean getFunctionalTest() {
        return flavor.testFunctionalTest;
      }

      @Override
      boolean isTestCoverageEnabled() {
        return true;
      }

      @Override
      ApiVersion getMinSdkVersion() {
        return flavor.minSdkVersion;
      }

      @Override
      boolean isLibrary() {
        return false
      }

      @Override
      ImmutableList<File> getTestedApks(int density, @NonNull List<String> abis) {
        return null
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

    task.applicationApk = variant.testedVariant.outputs.first().outputFile
    task.testApk = variant.outputs.first().outputFile
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
