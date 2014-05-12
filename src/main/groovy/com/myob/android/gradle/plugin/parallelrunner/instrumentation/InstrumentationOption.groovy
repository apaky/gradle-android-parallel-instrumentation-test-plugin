package com.myob.android.gradle.plugin.parallelrunner.instrumentation

public class InstrumentationOption {

  public static final String PACKAGE = "package"

  String name
  String value

  @Override
  public String toString() {
    return "InstrumentationOption{" +
            "name='" + name + '\'' +
            ", value='" + value + '\'' +
            '}';
  }
}
