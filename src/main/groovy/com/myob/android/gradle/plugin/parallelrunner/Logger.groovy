package com.myob.android.gradle.plugin.parallelrunner

import com.android.build.gradle.internal.LoggerWrapper
import com.android.utils.ILogger
import org.gradle.api.logging.Logging

class Logger {

  private static final org.gradle.api.logging.Logger logger = Logging.getLogger("gradle-parallel-instrumentation-tests")
  private static ILogger wrapper

  public static debug(String message) {
    logger.debug(message)
  }

  public static info(String message) {
    logger.info(message)
  }

  public static warn(String message) {
    logger.warn(message)
  }

  public static error(String message) {
    logger.error(message)
  }

  public static ILogger getLoggerWrapper() {
    if (wrapper == null) {
      wrapper = new LoggerWrapper(logger)
    }
    wrapper
  }
}
