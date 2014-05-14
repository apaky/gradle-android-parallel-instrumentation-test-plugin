package com.myob.android.gradle.plugin.parallelrunner.instrumentation

public class InstrumentationOption {

  public static final String PACKAGE = "package"

  String name
  String value

  static List<InstrumentationOption> fromPackageNames(def value) {
    List<InstrumentationOption> options = []
    if (value) {
      if (Collection.isAssignableFrom(value.class))
        value.each { String packageName ->
          options << new InstrumentationOption(name: PACKAGE, value: packageName)
      } else {
        options << new InstrumentationOption(name: PACKAGE, value: value)
      }
    }
    options
  }

  @Override
  public String toString() {
    return "InstrumentationOption{" +
            "name='" + name + '\'' +
            ", value='" + value + '\'' +
            '}';
  }
}
