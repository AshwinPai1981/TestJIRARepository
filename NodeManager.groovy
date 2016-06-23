import java.text.SimpleDateFormat

logFile = new File("nodemanager.log")

//*****************************************************************************
// Prepare
//*****************************************************************************
//------------------------------------------------------------------
// unzipIAR - unzip the IIB Archive file
//------------------------------------------------------------------
def unzipIAR(pSourceFolderName, pDestinationFolderName) {
    println "\nunzipIAR: Start"
    def inputDir  = new File("${pSourceFolderName}")
    def ant = new AntBuilder()
    inputDir.listFiles().each {
        if ((m = it.getName() =~ /.*iar$/)) {
            ant.unzip(src: "${it.getAbsolutePath()}",
                    dest: pDestinationFolderName,
                    overwrite: "true")
        }
    }
    println "unzipIAR: Complete"
}

//------------------------------------------------------------------
// prepareInstall - prep the install directory
//------------------------------------------------------------------
def prepareInstall (pInstallFolderName, pEnv) {
    println "\nprepareInstall: Preparing for ${pEnv}"
    def destinationDirPath = "${pInstallFolderName}/config"
    def unzipPath = "${pInstallFolderName}/unzipIAR"
    def globalSourceDirPath = "${unzipPath}/config/global"
    def envSourceDirPath = "${unzipPath}/config/${pEnv}"

    // Check for presence of config directory, only attempt preparation if one is found
    def configDir = new File("${unzipPath}/config")
    if (configDir.exists()) {
        def envSourceDir = new File(envSourceDirPath)
        if (envSourceDir.exists()) {
            println " - Copy global fileset"
            new AntBuilder().copy(todir: destinationDirPath) {
                fileset(dir: globalSourceDirPath)
            }

            println " - Copy environment fileset [${pEnv}]"
            new AntBuilder().copy(todir: destinationDirPath) {
                fileset(dir: envSourceDirPath)
            }

            println " - Copy deleted indicator file"
            new AntBuilder().copy(todir: pInstallFolderName) {
                fileset(dir: unzipPath) {
                    include(name: "deleted")
                }
            }

        } else {
            throw new Exception("Environment configuration not available [${pEnv}]")
        }
    } else {
        println "\t[prepare] No work required"
    }
    println "prepareInstall: Complete"
}


//*****************************************************************************
// Shell Commands
//*****************************************************************************
private def executeOnShell(String command) {
  return executeOnShell(command, new File("${System.properties.'user.dir'}"), false)
}

private def executeQuiet(String command) {
    return executeOnShell(command, new File("${System.properties.'user.dir'}"), true)
}

private def executeOnShell(String command, File workingDir, Boolean quiet) {
    println quiet
    if (quiet) print "[quiet] "
    println command
    def process = new ProcessBuilder(addShellPrefix(command))
                                    .directory(workingDir)
                                    .redirectErrorStream(true) 
                                    .start()
    def StringBuilder sbOutput = new StringBuilder()
    process.inputStream.eachLine {
        if (verbose && !quiet) println "\t${it}"
        sbOutput.append("${it}\n")
    }
    process.waitFor();
    println "Result: ${process.exitValue()}"
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

private def addMQSIPrefix(String command) {
    if (!System.getenv('MQSI_VERSION')) {
        def mqsiPath = System.getenv('MQSI_FILEPATH')
        if (mqsiPath) {
            def separator = System.getProperty('file.separator');
            def mqsiprofile = "${System.getenv('MQSI_FILEPATH')}${separator}bin${separator}mqsiprofile"
            if (checkOS() == 'windows') {
                return "\"${mqsiprofile}\" && ${command}"
            } else {
                return ". ${mqsiprofile} ; ${command}"
            }
        } else {
            throw new Exception("MQSI Profile Unavailable: Please define the MQSI_FILEPATH environment variable")
        }
    } else {
        return command
    }
}

//*****************************************************************************
// IIB Commands
//*****************************************************************************
////------------------------------------------------------------------
//// IIBListApplication
////------------------------------------------------------------------
//def IIBListApplication (pIntegrationNode, pIntegrationServer) {
//    def commandLine = "mqsilist ${pIntegrationNode} -e ${pIntegrationServer} -w 600"
//    return executeOnShell(addMQSIPrefix(commandLine))
//}

//------------------------------------------------------------------
// IIBReportPrevious
//------------------------------------------------------------------
def IIBReportPrevious (pIntegrationNode, pIntegrationServer, pApplication) {
    println "\nIIBReportPrevious: Reporting on ${pApplication}"

    installedApps.each{ app ->
        if (app['name'] == pApplication && app['broker'] == pIntegrationNode && app['execgrp'] == pIntegrationServer) {
            println "\t[${app['broker']}:${app['execgrp']}] Previous BAR: ${app['bar']} (deployed: ${app['date']})"
            logFile << "\t[${app['broker']}:${app['execgrp']}] Previous BAR: ${app['bar']} (deployed: ${app['date']})"
        }
    }

    println "IIBReportPrevious: Complete"
}

//------------------------------------------------------------------
// IIBStopApplication
//------------------------------------------------------------------
def IIBStopApplication(pIntegrationNode, pIntegrationServer, pApplication) {
    println "\nIIBStopApplication: Stopping ${pApplication}"

    def commandOutput
    installedApps.each { app ->
        if (app['name'] == pApplication && app['broker'] == pIntegrationNode && app['execgrp'] == pIntegrationServer && app['status'] == 'running') {
            def commandLine = "mqsistopmsgflow ${pIntegrationNode} -e ${pIntegrationServer} -k ${pApplication} -w 600"
            commandOutput = executeOnShell(addMQSIPrefix(commandLine))
            if (commandOutput['exitValue'] == 0) {
                logFile << "\tIIB Stopped: ${pApplication}\n"
            } else {
                logFile << "\tIIB Stop Failed: ${pApplication}\n"
                throw new Exception("IIB Stop Failed: ${pApplication} [code: ${commandOutput['exitValue']}]")
            }
        }
    }

    println "IIBStopApplication: Complete"
    return commandOutput
}

//------------------------------------------------------------------
// IIBStartApplication
//------------------------------------------------------------------
def IIBStartApplication(pIntegrationNode, pIntegrationServer, pApplication) {
    println "\nIIBStartApplication: Starting ${pApplication}"

    def commandOutput
    installedApps.each { app ->
        if (app['name'] == pApplication && app['broker'] == pIntegrationNode && app['execgrp'] == pIntegrationServer && app['status'] == 'stopped') {
            def commandLine = "mqsistartmsgflow ${pIntegrationNode} -e ${pIntegrationServer} -k ${pApplication} -w 600"
            commandOutput = executeOnShell(addMQSIPrefix(commandLine))
            if (commandOutput['exitValue'] == 0) {
                logFile << "\tIIB Started: ${pApplication}\n"
            } else {
                logFile << "\tIIB Start Failed: ${pApplication}\n"
                throw new Exception("IIB Start Failed: ${pApplication} [code: ${commandOutput['exitValue']}]")
            }
        }
    }

    println "IIBStartApplication: Complete"
    return commandOutput
}

//------------------------------------------------------------------
// IIBDeleteApplication
//------------------------------------------------------------------
def IIBDeleteApplication(pIntegrationNode, pIntegrationServer, pApplication) {
    println "\nIIBDeleteApplication: Deleting ${pApplication}"
    def listOutput = IIBListApplication(pIntegrationNode, pIntegrationServer)
    def search = "'${pApplication}'"

    def commandOutput
    installedApps.each { app ->
        if (app['name'] == pApplication && app['broker'] == pIntegrationNode && app['execgrp'] == pIntegrationServer) {
            if (app['status'] == 'stopped') {
                def commandLine = "mqsideploy ${pIntegrationNode} -e ${pIntegrationServer} -d ${pApplication} -w 600"
                commandOutput = executeOnShell(addMQSIPrefix(commandLine))
                logFile << "\tIIB Deleted: ${pApplication}\n"
                if (commandOutput['exitValue'] == 0) {
                    logFile << "\tIIB Deleted: ${pApplication}\n"
                } else {
                    logFile << "\tIIB Delete Failed: ${pApplication}\n"
                    throw new Exception("IIB Delete Failed: ${pApplication} [code: ${commandOutput['exitValue']}]")
                }
            } else {
                logFile << "\tIIB Delete Failed: ${pApplication} is not stopped\n"
                throw new Exception("IIB Delete Failed: ${pApplication} is not stopped")
            }
        }
    }

    println "IIBDeleteApplication: Complete"
    return commandOutput
}

//------------------------------------------------------------------
// IIBApplyBarOverride
//------------------------------------------------------------------
def IIBApplyBarOverride(pInstallFolderName, pApplication) {
    println "\nIIBApplyBarOverride: Applying Overrides for ${pApplication}"
    def destinationDirPath = "${pInstallFolderName}/bar"
    def barSourceDirPath = "${pInstallFolderName}/unzipIAR/bar"

    // Check for overrides file and apply if present
    def overridesFilePath = "${pInstallFolderName}/config/iib/${pApplication}.properties"
    File overridesFile = new File(overridesFilePath)
    if (overridesFile.exists()) {
        println "\t[override] Applying overrides from file: ${pApplication}.properties"
        def barSourceDir = new File(barSourceDirPath)
        barSourceDir.listFiles().each {
            def commandLine = "mqsiapplybaroverride -b ${it.getAbsolutePath()} -p ${overridesFilePath} -k ${pApplication} -r"
            executeOnShell(addMQSIPrefix(commandLine))
        }
    } else {
        println "\t[override] No overrides defined"
    }
    
    // Move BAR file to install directory
	File barSourceDir = new File(barSourceDirPath)
	if (barSourceDir.exists()) {
        if (verbose) println " - Copy generated BAR file"
		new AntBuilder().copy(todir: destinationDirPath) {
			fileset(dir: barSourceDirPath,
					includes: "*.bar")
		}
	}
    println "IIBApplyBarOverride: Complete"
}

//------------------------------------------------------------------
// IIBDeployApplication
//------------------------------------------------------------------
def IIBDeployApplication(pInstallFolderName, pApplication, pIntegrationNode, pIntegrationServer) {
    println "\nIIBDeployApplication: Deploying ${pApplication}"
    
    def barSourceDirPath = "${pInstallFolderName}/bar"
    def barSourceDir = new File(barSourceDirPath)
    def files = barSourceDir.listFiles() 
    assert 1 == files.size()

    def commandLine = "mqsideploy ${pIntegrationNode} -e ${pIntegrationServer} -a ${files[0].getAbsolutePath()} -w 600"
    def commandOutput = executeOnShell(addMQSIPrefix(commandLine))

    if (commandOutput['exitValue'] == 0) {
        logFile << "\tIIB Deployed BAR: ${files[0].getAbsolutePath()}\n"
    } else {
        logFile << "\tIIB Deploy Failed: ${files[0].getAbsolutePath()}\n"
        throw new Exception("IIB Deploy Failed: ${files[0].getAbsolutePath()} [code: ${commandOutput['exitValue']}]")
    }

    println "IIBDeployApplication: Complete"
    return commandOutput
}

//------------------------------------------------------------------
// IIBList - builds a list of brokers and applications
//------------------------------------------------------------------
def IIBList () {
    def commandLine = "mqsilist -r -d 2 -w 600"
    def listOutput = executeQuiet(addMQSIPrefix(commandLine))

    def text = listOutput['output']

    def entries = ReadMQSIListEntries(text)
    return entries
}

//------------------------------------------------------------------
// ReadMQSIListEntries - processes the output of the mqsilist command
//------------------------------------------------------------------
def ReadMQSIListEntries (pText) {
    def entries = []
    def entry
    pText.eachLine { line ->
        if (line =~ /^BIP1/) {
            entry = line
        } else if (line =~ /^--------/) {
            // entry complete
            def entryMap = ProcessEntry(entry)
            if (entryMap) entries.push(entryMap)
        } else if (line =~ /BIP8071I/) {
            // process final entry of list
            def entryMap = ProcessEntry(entry)
            if (entryMap) entries.push(entryMap)
        } else {
            entry = "${entry}\n${line}"
        }
    }

    return entries
}

//------------------------------------------------------------------
// ProcessEntry - processes individual mqsilist entries
//------------------------------------------------------------------
def ProcessEntry (pEntry) {
    if (pEntry =~ /^BIP127[5-6]I/) {
        return ProcessApplication(pEntry)
    } else if (pEntry =~ /^BIP128[4-5]I/) {
        return ProcessBroker(pEntry)
    }
}

//------------------------------------------------------------------
// ProcessApplication - processes the mqsilist entry for an application
//------------------------------------------------------------------
def ProcessApplication (pText) {
    def dateFormat = new SimpleDateFormat("dd/MM/yy HH:mm")
    def myApp = [type: "Application"]
    pText.eachLine { line ->
        if ((m = line =~ /BIP127[5-6]I: Application '(.*)' on execution group '(.*)' is (.*)\./)) {
            myApp['name'] = m.group(1)
            myApp['execgrp'] = m.group(2)
            myApp['status'] = m.group(3)
        }
        if ((m = line =~ /Deployed: '(.*)' in Bar file '(.*)'/)) {
            myApp['date'] = dateFormat.parse(m.group(1))
            myApp['bar'] = m.group(2)
        }
    }

    return myApp
}

//------------------------------------------------------------------
// ProcessBroker - processes the mqsilist entry for a broker
//------------------------------------------------------------------
def ProcessBroker (pText) {
    def myBkr = [type: "broker"]
    pText.eachLine { line ->
        if ((m = line =~ /BIP128[4-5]I: Broker '(.*)' on queue manager '(.*)' is (.*)\./)) {
            myBkr['name'] = m.group(1)
            myBkr['qmgr'] = m.group(2)
            myBkr['status'] = m.group(3)
        }
        if ((m = line =~ /Broker version: '(.*)' \(build '(.*)'\)/)) {
            myBkr['version'] = m.group(1)
            myBkr['build'] = m.group(2)
        }
        if ((m = line =~ /Process ID: '(.*)'/)) {
            myBkr['pid'] = m.group(1)
        }
    }

    return myBkr
}

//*****************************************************************************
// MQSC Commands
//*****************************************************************************
//------------------------------------------------------------------
// MQCommand - execute an MQSC command
//------------------------------------------------------------------
def MQCommand(pCommand, pQMName, pInstallFolderName) {
    println "\nMQCommand: Executing MQSC Script ${pCommand}.mqsc"
    def mqscFilePath = "${pInstallFolderName}/config/mq/${pCommand}.mqsc"
    def mqscFile = new File(mqscFilePath)
    def commandOutput
    if (mqscFile.exists()) {
        def commandLine = "runmqsc ${pQMName} < ${mqscFilePath}"
        commandOutput =  executeOnShell(commandLine)
        logFile << "\tMQSC Executed: ${mqscFile.getAbsolutePath()}\n"
        if (commandOutput['exitValue'] == 0) {
            logFile << "\tMQSC Executed: ${mqscFile.getAbsolutePath()}\n"
        } else {
            logFile << "\tMQSC Execute Failed: ${mqscFile.getAbsolutePath()}\n"
            throw new Exception("MQSC Execute Failed: ${mqscFile.getAbsolutePath()} [code: ${commandOutput['exitValue']}]")
        }
    }
    println "MQCommand: Complete"
    return commandOutput
}

//------------------------------------------------------------------
// WMQList - builds a list of MQ Queue Managers
//------------------------------------------------------------------
def WMQList () {
    def commandLine = "dspmq"
    def listOutput = executeQuiet(commandLine)

    def text = listOutput['output']

    def entries = ReadDSPMQEntries(text)
    return entries
}

//------------------------------------------------------------------
// ReadDSPMQEntries - processes the output of the dspmq command
//------------------------------------------------------------------
def ReadDSPMQEntries (pText) {
    def entries = [:]
    pText.eachLine { line ->
        if ((m = line =~ /QMNAME\((.*)\).*STATUS\((.*)\)/)) {
            entries[(m.group(1))] = m.group(2)
        }
    }
    return entries
}

//*****************************************************************************
// JAR Commands
//*****************************************************************************
//------------------------------------------------------------------
// JARDeleteApplication
//------------------------------------------------------------------
def JARDeleteApplication(pApplication) {
    println "\nJARDeleteApplication: Deleting ${pApplication}"

    def workpath = System.getenv("MQSI_WORKPATH")

    if (workpath) {
        def ant = new AntBuilder()
        def filePath = "${workpath}/shared-classes"
        println "\t[delete] ${filePath}/${pApplication}*"
        try {
            ant.delete() {
                fileset(dir: "${filePath}", includes: "${pApplication}*")
            }
        } catch (Exception e) {
            logFile << "\tJAR Delete Failed: ${e.getMessage()}\n"
            println "\tJAR Delete Failed: ${e.getMessage()}\n"
        }
    } else {
        logFile << "\tJAR Delete Failed: Environment variable MQSI_WORKPATH is unset\n"
        throw new Exception("Environment variable MQSI_WORKPATH is unset, unable to process JAR delete")
    }

    println "JARDeleteApplication: Complete"
}

//------------------------------------------------------------------
// JARDeployApplication
//------------------------------------------------------------------
def JARDeployApplication(pInstallFolderName, pApplication) {
    println "\nJARDeployApplication: Deploying ${pApplication}"

    def workpath = System.getenv("MQSI_WORKPATH")

    if (workpath) {
        //Delete any previous versions before install
        JARDeleteApplication(pApplication)

        def ant = new AntBuilder()

        def filePath = "${workpath}/shared-classes"
        def files = new FileNameFinder().getFileNames(pInstallFolderName, "**/${pApplication}*")

        files.each {
            def File myFile = new File(it)
            def destination = "${filePath}/${myFile.getName()}"
            ant.copy(file: "${myFile.getAbsolutePath()}", todir: "${filePath}")

            println "\t[chgrp] Setting group of: ${destination} -> mqbrkrs"
            ant.chgrp(file: "${destination}", group: "mqbrkrs")
            logFile << "\tJAR Deployed: ${destination}\n"
        }
    } else {
        logFile << "\tJAR Deploy Failed: Environment variable MQSI_WORKPATH is unset\n"
        throw new Exception("Environment variable MQSI_WORKPATH is unset, unable to process JAR deployment")
    }
    println "JARDeployApplication: Complete"
}

//*****************************************************************************
// NodeManager Tasks (Prepare/Stop/Delete/Install/Start)
//*****************************************************************************

//------------------------------------------------------------------
// Initialise
//------------------------------------------------------------------
def init (pOptions, pApp, pType) {
    def map = [:]
    def appDirPath = pApp['path']
    def appInstallDirPath = "${appDirPath}/install"
    def appDir = new File(appDirPath)
//    def configDir = new File("${pApp['path']}/config")

    if (appDir.exists()) {
        def iibPropertiesFile = new File("${appInstallDirPath}/config/iib", "configuration.properties")
        def iibProperties = new Properties();
        if (iibPropertiesFile.exists()) iibProperties.load(new FileInputStream(iibPropertiesFile))

        def mqPropertiesFile = new File("${appInstallDirPath}/config/mq", "configuration.properties")
        def mqProperties = new Properties();
        if (mqPropertiesFile.exists()) mqProperties.load(new FileInputStream(mqPropertiesFile))

        def myQMName = pOptions.q ?: mqProperties['deploy.QMName']
        def myIntegrationNode = pOptions.n ?: iibProperties['deploy.IntegrationNode']
        def myIntegrationServer = pOptions.s ?: iibProperties['deploy.IntegrationServer']

        def deletedFile = new File(appInstallDirPath, "deleted")
        if (deletedFile.exists()) {
            map.put('deleted', true)
        } else {
            map.put('deleted', false)
        }

        if (pType == 'install') {
            // Test for presence of MQ QMgr node
            if (qmgrs[(myQMName)]) {
                map.put('qmName', myQMName)
            }

            // Test for presence of broker node
            if (brokers[(myIntegrationNode)]) {
                map.put('integrationNode', myIntegrationNode)
                map.put('integrationServer', myIntegrationServer)
            }
        }

        map.put('appName', pApp['name'])
        map.put('appVersion', pApp['version'])
        map.put('appType', pApp['type'])
        map.put('appPath', appDirPath)
        map.put('appInstallPath', appInstallDirPath)
        map.put('environment', pOptions.e)

        println '------------------------------------------------------------'
        println '-- MQ/IIB Deployment properties'
        println '------------------------------------------------------------'
        if (pType == 'install') {
            println "QMName:                ${map['qmName']}"
            println "Integration Node:      ${map['integrationNode']}"
            println "Integration Server:    ${map['integrationServer']}"
        }
        println "Application Name:      ${map['appName']}"
        println "Application Type:      ${map['appType']}"
        println "Deleted                ${map['deleted']}"

        logFile << "\nApplication: ${map['appName']}\n"
    }

    return map
}

//------------------------------------------------------------------
// NodeManagerPrepare
//------------------------------------------------------------------
def NodeManagerPrepare(propsMap) {
    println "==> NodeManagerPrepare: ${propsMap['appName']} <=="

    if (propsMap['environment']) {
        // Delete existing install directory
        new AntBuilder().delete(dir: propsMap['appInstallPath'])
        
        // Check for presence of IAR
        def iarSourcePath = "${propsMap['appPath']}/build/iar"
        if (new File(iarSourcePath).exists()) {
            // Unizip IAR
            def unzipPath = "${propsMap['appInstallPath']}/unzipIAR"
            unzipIAR(iarSourcePath, unzipPath)

            // Prepare for install
            prepareInstall(propsMap['appInstallPath'], propsMap['environment'])

            // Apply BAR Overrides for target environment
            IIBApplyBarOverride(propsMap['appInstallPath'], propsMap['appName'])

            def ant = new AntBuilder()
            if (verbose) println " - Copy generated IAR file"
            ant.copy(file: "${iarSourcePath}/pom.xml", todir: "${propsMap['appInstallPath']}/iar")
        }

        // Copy JARs
        def jarSourcePath = "${propsMap['appPath']}/build/libs"
        if (new File(jarSourcePath).exists()) {
            def ant = new AntBuilder()
            ant.copy(todir: "${propsMap['appInstallPath']}/libs") {
                fileset(dir: "${jarSourcePath}")
            }
        }
    } else {
        throw new Exception ('Environment name must be provided (-e)}')
    }
    println "==> NodeManagerPrepare: Complete <=="
}

//------------------------------------------------------------------
// NodeManagerStop
//------------------------------------------------------------------
def NodeManagerStop(propsMap) {
    println "==> NodeManagerStop: ${propsMap['appName']} <=="

    // Report existing Applications
    if (propsMap['integrationNode']) IIBReportPrevious(propsMap['integrationNode'], propsMap['integrationServer'], propsMap['appName'])

    // Stop IIB Application
    if (propsMap['integrationNode']) IIBStopApplication(propsMap['integrationNode'], propsMap['integrationServer'], propsMap['appName'])

    // Stop MQ Objects
    if (propsMap['qmName']) MQCommand("close", propsMap['qmName'], propsMap['appInstallPath'])
    
    // Stop os objects
    // OSCommand( InstallConfigDirName, project.OSConfiFolderName, 'close');

    println "==> NodeManagerStop: Complete <=="
}

//------------------------------------------------------------------
// NodeManagerDelete
//------------------------------------------------------------------
def NodeManagerDelete(propsMap, pDeleteAll) {
    println "==> NodeManagerDelete: ${propsMap['appName']} <=="
    if (propsMap['deleted'] || pDeleteAll) {
        //Execute a Stop before deleting an application
        NodeManagerStop(propsMap)

        // Delete IIB Application
        if (propsMap['integrationNode']) IIBDeleteApplication(propsMap['integrationNode'], propsMap['integrationServer'], propsMap['appName'])

        // Delete MQ Objects
        if (propsMap['qmName']) MQCommand("delete1", propsMap['qmName'], propsMap['appInstallPath'])
        if (propsMap['qmName']) MQCommand("delete2", propsMap['qmName'], propsMap['appInstallPath'])

        // Delete java objects
        if (propsMap['appType'] == 'jar') JARDeleteApplication(propsMap['appName']);

        // Delete os objects
        // OSCommand( InstallConfigDirName, project.OSConfiFolderName, 'close');
    } else {
        println "Skipping delete of application"
        println "\tDeleteAll - ${pDeleteAll}"
        println "\tdelete File - ${propsMap['deleted']}"
    }
    println "==> NodeManagerDelete: Complete <=="
}

//------------------------------------------------------------------
// NodeManagerInstall
//------------------------------------------------------------------
def NodeManagerInstall(propsMap) {
    println "==> NodeManagerInstall: ${propsMap['appName']} <=="

    // Check for 'deleted' application and skip install if true.
    if (propsMap['deleted']) {
        println "Skipping deleted application"
    } else {

        // Install IIB Application
        if (propsMap['integrationNode']) IIBDeployApplication(propsMap['appInstallPath'], propsMap['appName'], propsMap['integrationNode'], propsMap['integrationServer'])

        // Install MQ Objects
        if (propsMap['qmName']) MQCommand("define", propsMap['qmName'], propsMap['appInstallPath'])

        // Install java objects
        if (propsMap['appType'] == 'jar') JARDeployApplication(propsMap['appInstallPath'], propsMap['appName']);

        // Install os objects
        // OSCommand( InstallConfigDirName, project.OSConfiFolderName, 'close');
    }
    println "==> NodeManagerInstall: Complete <=="
}

//------------------------------------------------------------------
// NodeManagerStart
//------------------------------------------------------------------
def NodeManagerStart(propsMap) {
    println "==> NodeManagerStart: ${propsMap['appName']} <=="

    // Check for 'deleted' application and skip start if true.
    if (propsMap['deleted']) {
        println "Skipping deleted application [${propsMap['appName']}]"
    } else {

        // Start IIB Application
        if (propsMap['integrationNode']) IIBStartApplication(propsMap['integrationNode'], propsMap['integrationServer'], propsMap['appName'])

        // Start MQ Objects
        if (propsMap['qmName']) MQCommand("open", propsMap['qmName'], propsMap['appInstallPath'])

        // Start os objects
        // OSCommand( InstallConfigDirName, project.OSConfiFolderName, 'close');
    }
    println "==> NodeManagerStart: Complete <=="
}

//*****************************************************************************
// Main 
//*****************************************************************************

//------------------------------------------------------------------
// findApplications - Construct a list of applications determined by
//    presence of a build pom.xml
//------------------------------------------------------------------
def findApplications(pOptions, pType) {
    def applications = [];

    logFile << "\nApplication List:\n"

    // Find all build pom.xml files
    def pomFiles = new FileNameFinder().getFileNames('.', "**/${pType}/iar/pom.xml **/${pType}/libs/pom.xml");

    pomFiles.each {
        def File myPom = new File(it)
        def dirPath = myPom.getParentFile()

        def artifactType
        dirPath.listFiles().each { myFile ->
            if (myFile.getName() != 'pom.xml') {
                artifactType = myFile.getName().tokenize('.').last()
            }
        }

        // read POM to determine details of the Application
        def pom = new XmlSlurper().parseText(myPom.getText())
        def artifactId = pom.artifactId.text()
        def version = pom.version.text()

        if (!pOptions.a || pOptions.a =~ /${artifactId}/) {

            if ((m = it =~ /(.*${artifactId})/)) {
                def myApp = [name: "${artifactId}", type: "${artifactType}", version: "${version}", path: "${m.group(1)}"]
                applications.push(myApp)
                logFile << "\t${myApp}\n"
            }
        }
    }

    if (verbose) println "Applications: ${applications}"
    return applications
}

def buildBrokerList(pList) {
    def entries = [:]
    pList.each { entry ->
        if (entry['type'] == 'broker') {
            entries[(entry['name'])] = entry['status']
        }
    }

    return entries
}

def buildApplicationList(pList) {
    def entries = []
    def broker
    pList.each { entry ->

        if (entry['type'] == 'broker') {
            broker = entry['name']
        } else if (entry['type'] == 'Application') {
            entry['broker'] = broker
            entries.push(entry)
        }
    }

    return entries
}

// Define the CLI options
def cli = new CliBuilder(usage: 'NodeManager.groovy [-h] [-q <qmgr> -n <node> -s <server>] [-e <environment>] (-a <application>|-f <file>) <action>')
cli.with {
    h(longOpt: 'help', 'Show usage information')
    v(longOpt: 'verbose', 'Enable verbose logging')
    q(longOpt: 'qmgr', 'overrides the QueueManager proprty to force deployment to a specific MQ instance', args: 1)
    n(longOpt: 'node', 'overrides the IntegrationNode property to force deployment to a specific WMB instance', args: 1)
    s(longOpt: 'server', 'overrides the IntegrationServer property to force deployment to a specific execution group', args: 1)
    e(longOpt: 'environment', 'target deployment environment', args: 1)
    a(longOpt: 'application', 'target individual application for deployment', args: 1)
    f(longOpt: 'file', 'target includes file that defines multiple applications for deployment', args: 1)
}

def buildDate = new Date();
def options = cli.parse(args)
def action = options.arguments()[0]
def deployApps
verbose = options.v

logFile << "\n==> Executing @ ${buildDate}\n"
logFile << "\tAction: ${action}\n"

if (action == "Prepare") {
    deployApps = findApplications(options, "build")

    deployApps.each { entry ->
        def propsMap = init(options, entry, "build")
        NodeManagerPrepare(propsMap)
    }
} else {
    deployApps = findApplications(options, "install")

    // determine the current status of this node
    def iiblist = IIBList()
    brokers = buildBrokerList(iiblist)
    installedApps = buildApplicationList(iiblist)
    qmgrs = WMQList()

    // Apply the requested command against all deployable applications
    deployApps.each { entry ->
        def propsMap = init(options, entry, "install")
        if (propsMap.size() > 0) {
            switch (action) {
                case ~/Stop$/:
                    NodeManagerStop(propsMap)
                    break
                case ~/^Delete$/:
                    NodeManagerDelete(propsMap, false)
                    break
                case ~/^DeleteAll$/:
                    NodeManagerDelete(propsMap, true)
                    break
                case ~/^Install$/:
                    NodeManagerInstall(propsMap)
                    break
                case ~/^Start$/:
                    NodeManagerStart(propsMap)
                    break
                default:
                    println "Please provide a command [Prepare|Stop|Delete|DeleteAll|Install|Start]"
                    break
            }
        }
    }
}





