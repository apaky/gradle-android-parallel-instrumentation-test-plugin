String command = "adb devices | sed 1d | cut -f 1"

def processBuilder = new java.lang.ProcessBuilder("/bin/bash", "-c", command)
def output = new StringWriter()
def error = new StringWriter()

def process = processBuilder.start()
process.consumeProcessOutput(output, error)
process.waitFor()

def devices = output.toString().readLines().findAll { line -> 
    !line.isEmpty()
}

println "devices -> ${devices}"

