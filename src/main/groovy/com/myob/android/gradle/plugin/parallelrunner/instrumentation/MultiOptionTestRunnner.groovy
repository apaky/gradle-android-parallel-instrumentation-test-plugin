package com.myob.android.gradle.plugin.parallelrunner.instrumentation

import com.android.ddmlib.*
import com.android.ddmlib.testrunner.IRemoteAndroidTestRunner
import com.android.ddmlib.testrunner.ITestRunListener
import com.android.ddmlib.testrunner.InstrumentationResultParser

import java.util.concurrent.TimeUnit

class MultiOptionTestRunnner {

  private final String mPackageName;
  private final String mRunnerName;
  private IShellEnabledDevice mRemoteDevice;
  // default to no timeout
  private long mMaxTimeToOutputResponse = 0;
  private TimeUnit mMaxTimeUnits = TimeUnit.MILLISECONDS;
  private String mRunName = null;

  /** map of name-value instrumentation argument pairs */
  private Map<String, List<String>> mArgMap;
  private InstrumentationResultParser mParser;

  private static final String LOG_TAG = "RemoteAndroidTest";
  private static final String DEFAULT_RUNNER_NAME = "android.test.InstrumentationTestRunner";

  private static final char CLASS_SEPARATOR = ',';
  private static final char METHOD_SEPARATOR = '#';
  private static final char RUNNER_SEPARATOR = '/';

  // defined instrumentation argument names
  private static final String CLASS_ARG_NAME = "class";
  private static final String LOG_ARG_NAME = "log";
  private static final String DEBUG_ARG_NAME = "debug";
  private static final String COVERAGE_ARG_NAME = "coverage";
  private static final String PACKAGE_ARG_NAME = "package";
  private static final String SIZE_ARG_NAME = "size";

  /**
   * Creates a remote Android test runner.
   *
   * @param packageName the Android application package that contains the tests to run
   * @param runnerName the instrumentation test runner to execute. If null, will use default
   *   runner
   * @param remoteDevice the Android device to execute tests on
   */
  public MultiOptionTestRunnner(String packageName,
                                 String runnerName,
                                 IShellEnabledDevice remoteDevice) {

    mPackageName = packageName;
    mRunnerName = runnerName;
    mRemoteDevice = remoteDevice;
    mArgMap = new Hashtable<String, List<String>>();
  }

  /**
   * Alternate constructor. Uses default instrumentation runner.
   *
   * @param packageName the Android application package that contains the tests to run
   * @param remoteDevice the Android device to execute tests on
   */
  public MultiOptionTestRunnner(String packageName,
                                 IShellEnabledDevice remoteDevice) {
    this(packageName, null, remoteDevice);
  }

  @Override
  public String getPackageName() {
    return mPackageName;
  }

  @Override
  public String getRunnerName() {
    if (mRunnerName == null) {
      return DEFAULT_RUNNER_NAME;
    }
    return mRunnerName;
  }

  /**
   * Returns the complete instrumentation component path.
   */
  private String getRunnerPath() {
    return getPackageName() + RUNNER_SEPARATOR + getRunnerName();
  }

  @Override
  public void setClassName(String className) {
    addInstrumentationArg(CLASS_ARG_NAME, className);
  }

  @Override
  public void setClassNames(String[] classNames) {
    StringBuilder classArgBuilder = new StringBuilder();

    for (int i = 0; i < classNames.length; i++) {
      if (i != 0) {
        classArgBuilder.append(CLASS_SEPARATOR);
      }
      classArgBuilder.append(classNames[i]);
    }
    setClassName(classArgBuilder.toString());
  }

  @Override
  public void setMethodName(String className, String testName) {
    setClassName(className + METHOD_SEPARATOR + testName);
  }

  @Override
  public void setTestPackageName(String packageName) {
    addInstrumentationArg(PACKAGE_ARG_NAME, packageName);
  }

  @Override
  public void addInstrumentationArg(String name, String value) {
    if (name == null || value == null) {
      throw new IllegalArgumentException("name or value arguments cannot be null");
    }

    List list = mArgMap.get(name, [])
    list.add(value)
    mArgMap.put(name, list)
  }

  @Override
  public void removeInstrumentationArg(String name) {
    if (name == null) {
      throw new IllegalArgumentException("name argument cannot be null");
    }
    mArgMap.remove(name);
  }

  @Override
  public void addBooleanArg(String name, boolean value) {
    addInstrumentationArg(name, Boolean.toString(value));
  }

  @Override
  public void setLogOnly(boolean logOnly) {
    addBooleanArg(LOG_ARG_NAME, logOnly);
  }

  @Override
  public void setDebug(boolean debug) {
    addBooleanArg(DEBUG_ARG_NAME, debug);
  }

  @Override
  public void setCoverage(boolean coverage) {
    addBooleanArg(COVERAGE_ARG_NAME, coverage);
  }

  @Override
  public void setTestSize(IRemoteAndroidTestRunner.TestSize size) {
    addInstrumentationArg(SIZE_ARG_NAME, size.getRunnerValue());
  }

  @Override
  public void setMaxtimeToOutputResponse(int maxTimeToOutputResponse) {
    setMaxTimeToOutputResponse(maxTimeToOutputResponse, TimeUnit.MILLISECONDS);
  }

  @Override
  public void setMaxTimeToOutputResponse(long maxTimeToOutputResponse, TimeUnit maxTimeUnits) {
    mMaxTimeToOutputResponse = maxTimeToOutputResponse;
    mMaxTimeUnits = maxTimeUnits;
  }

  @Override
  public void setRunName(String runName) {
    mRunName = runName;
  }

  @Override
  public void run(ITestRunListener... listeners)
          throws TimeoutException, AdbCommandRejectedException, ShellCommandUnresponsiveException,
                  IOException {
    run(Arrays.asList(listeners));
  }

  @Override
  public void run(Collection<ITestRunListener> listeners)
          throws TimeoutException, AdbCommandRejectedException, ShellCommandUnresponsiveException,
                  IOException {
    final String runCaseCommandStr = "am instrument -w -r ${getArgsCommand()} ${getRunnerPath()}";
    Log.i(LOG_TAG, "Running $runCaseCommandStr on ${mRemoteDevice.name}");
    String runName = mRunName == null ? mPackageName : mRunName;
    mParser = new InstrumentationResultParser(runName, listeners);

    try {
      mRemoteDevice.executeShellCommand(runCaseCommandStr, mParser, mMaxTimeToOutputResponse, mMaxTimeUnits);
    } catch (IOException e) {
      Log.w(LOG_TAG, "IOException ${e.message} when running tests ${getPackageName()} on ${mRemoteDevice.name}");
      // rely on parser to communicate results to listeners
      mParser.handleTestRunFailed(e.toString());
      throw e;
    } catch (ShellCommandUnresponsiveException e) {
      Log.w(LOG_TAG, "ShellCommandUnresponsiveException ${e.message} when running tests ${getPackageName()} on ${mRemoteDevice.name}");
      mParser.handleTestRunFailed(
              "Failed to receive adb shell test output within ${mMaxTimeToOutputResponse} ms. Test may have timed out, or adb connection to device became unresponsive");
      throw e;
    } catch (TimeoutException e) {
      Log.w(LOG_TAG, "TimeoutException when running tests ${getPackageName()} on ${mRemoteDevice.name}");
      mParser.handleTestRunFailed(e.toString());
      throw e;
    } catch (AdbCommandRejectedException e) {
      Log.w(LOG_TAG, "AdbCommandRejectedException ${e.toString()} when running tests ${getPackageName()} on ${mRemoteDevice.name}");
      mParser.handleTestRunFailed(e.toString());
      throw e;
    }
  }

  @Override
  public void cancel() {
    if (mParser != null) {
      mParser.cancel();
    }
  }

  /**
   * Returns the full instrumentation command line syntax for the provided instrumentation
   * arguments.
   * Returns an empty string if no arguments were specified.
   */
  private String getArgsCommand() {
    StringBuilder commandBuilder = new StringBuilder();
    for (Map.Entry<String, List<String>> argPair : mArgMap.entrySet()) {
      argPair.value.each {String value ->
        commandBuilder.append(" -e ${argPair.key} $value");
      }
    }
    return commandBuilder.toString();
  }
}
