
//------------------------------------------------------------------
// findApplications
//------------------------------------------------------------------
def findApplications() {
    def applications = [];

    // Find all build pom.xml files
    def pomFiles = new FileNameFinder().getFileNames('.', '**/iar/pom.xml');

    pomFiles.each {
        def File myPom = new File(it)

        // read POM to determine details of the Application
        def pom = new XmlSlurper().parseText(myPom.getText())
        def artifactId = pom.artifactId.text()
        def version = pom.version.text()

        if ((m = it =~ /(.*${artifactId})/)) {
            def myApp = [name: "${artifactId}", version: "${version}", path: "${m.group(1)}"]
            applications.push(myApp)
        }

    }

    return applications
}

// Determine the list of applications
def applications = findApplications()

// Extract the IAR for each application
def ant = new AntBuilder()
applications.each { app ->
    def iarPath = "${app['path']}/build/iar/${app['name']}-${app['version']}.iar"
    ant.unzip(src: "${iarPath}",
            dest: "${app['path']}",
            overwrite: "true")
}

