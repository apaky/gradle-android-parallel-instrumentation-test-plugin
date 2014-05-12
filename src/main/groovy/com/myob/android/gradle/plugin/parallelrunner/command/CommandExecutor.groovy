package com.myob.android.gradle.plugin.parallelrunner.command

import com.myob.android.gradle.plugin.parallelrunner.Logger

class CommandExecutor {

  static CommandResult execute(String command) {

    def processBuilder = new ProcessBuilder("/bin/bash", "-c", command)
    def output = new StringWriter()
    def error = new StringWriter()

    Logger.info("About to execute command ${command}")

    def process = processBuilder.start()
    process.consumeProcessOutput(output, error)
    process.waitFor()

    new CommandResult(output: output.toString(), exit_value: process.exitValue())
  }
}
