//*****************************************************************************
// Shell Commands
//*****************************************************************************
private def executeOnShell(String command) {
    return executeOnShell(command, new File("${System.properties.'user.dir'}"))
}

private def executeOnShell(String command, File workingDir) {
    println command
    def process = new ProcessBuilder(addShellPrefix(command))
            .directory(workingDir)
            .redirectErrorStream(true)
            .start()
    def StringBuilder sbOutput = new StringBuilder()
    process.inputStream.eachLine {
        println "\t${it}"
        sbOutput.append("${it}\n")
    }
    process.waitFor();
    return [exitValue: process.exitValue(), output: sbOutput.toString()]
}

private def addShellPrefix(String command) {
    commandArray = new String[3]
    if (checkOS() == 'windows') {
        commandArray[0] = "cmd"
        commandArray[1] = "/c"
    } else {
        commandArray[0] = "sh"
        commandArray[1] = "-c"
    }
    commandArray[2] = command
    return commandArray
}

private def String checkOS() {
    if (System.getProperty('os.name').toLowerCase(Locale.US).contains('windows')) {
        return 'windows'
    } else if (System.getProperty( "os.name" ).toLowerCase( Locale.US ).contains("linux")) {
        return 'linux'
    } else if (System.getProperty( "os.name" ).toLowerCase( Locale.US ).contains("aix")) {
        return 'aix'
    } else {
        throw new Exception('Unknown OS')
    }
}

def upload(File pomFile, repository) {

    // read POM to determine details of the file to upload
    def pom = new XmlSlurper().parseText(pomFile.getText())
    def artifactId = pom.artifactId.text()
    def version = pom.version.text()

    def artifactPath
    def artifactType
    // check for existence of IAR
    def iarFileName = "${artifactId}-${version}.iar"
    def iarFile = new File(pomFile.getParentFile().getAbsolutePath(), iarFileName)
    if (iarFile.exists()) {
        println "IAR path: ${iarFile.getAbsolutePath()}"
        artifactPath = iarFile.getAbsolutePath()
        artifactType = 'iar'
    } else {

        // check for existence of JAR
        def jarFileName = "${artifactId}-${version}.jar"
        def jarFile = new File(pomFile.getParentFile().getAbsolutePath(), jarFileName)
        if (jarFile.exists()) {
            println "JAR path: ${jarFile.getAbsolutePath()}"
            artifactPath = jarFile.getAbsolutePath()
            artifactType = 'jar'
        }
    }

    // Construct the upload command (curl)
    def commandLine = new StringWriter()
    commandLine << "curl --fail -F r=${repository}"         // Set repository
    commandLine << " -F hasPom=true"                        // Upload includes POM
    commandLine << " -F e=${artifactType}"                  // Set extension to iar
    commandLine << " -F file=@${pomFile.getAbsolutePath()}" // Add POM as mime part
    commandLine << " -F file=@${artifactPath}"              // Add IAR as mime part
    commandLine << " -u darwin:darwin"                      // Set user details
    commandLine << " http://lnxs0592.uk.b-and-q.com:8088/nexus/service/local/artifact/maven/content"

    // execute the command through shell (sensitive to OS)
    def response = executeOnShell(commandLine.toString())

    if (response.get('exitValue') != 0) {
        throw new Exception("Failed with response code [${response.get('exitValue')}]")
    }
}

//*****************************************************************************
// Main
//*****************************************************************************

// Define the CLI options
def cli = new CliBuilder(usage: 'uploadNexus.groovy -r <repository>')
cli.with {
    h(longOpt: 'help', 'Show usage information')
    r(longOpt: 'repository', 'Nexus repository Name', args: 1)
    d(longOpt: 'directory', 'Root Directory Path', args: 1)
}

def options = cli.parse(args)

if (!options.r) {
    throw new Exception('No Repository defined, -r option must be provided')
}

def rootPath = (options.d) ? "${options.d}" : '.'

// Find all build pom.xml files
def pomFiles = new FileNameFinder().getFileNames(rootPath, '**/libs/pom.xml **/iar/pom.xml');

// Upload each pom.xml and related IAR to nexus
pomFiles.each {
    def File myPom = new File(it)
    upload(myPom, options.r)
}

