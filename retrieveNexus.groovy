@Grapes([
        @Grab('org.codehaus.groovy.modules.http-builder:http-builder:0.7' ),
        @Grab(group='commons-io', module='commons-io', version='2.4'),
        @Grab( 'com.bloidonia:groovy-common-extensions:0.6.0' )
])

import groovyx.net.http.*
import groovy.xml.*
import org.apache.commons.io.FileUtils

import java.util.zip.ZipFile

// Define the CLI options
def cli = new CliBuilder(usage: 'retrieveNexus.groovy [-h] -v <version>')
cli.with {
    h(longOpt: 'help', 'Show usage information')
    v(longOpt: 'version', 'Artifact Version Id', args: 1)
}

def client = new HTTPBuilder( 'http://lnxs0592.uk.b-and-q.com:8088' )
def options = cli.parse(args)

// Determine the list of artifacts
def artifactsList = findArtifacts(options, client)

// Retrieve and Extract IAR for all artifacts
artifactsList.each { entry ->
    if (entry['type'] == 'iar') {
        retrieveIAR(entry, client)
    } else if (entry['type'] == 'jar') {
        retrieveJAR(entry, client)
    }
}

def retrieveIAR(pArtifact, client) {

    // Determine File Details
    def String fileName = "${pArtifact['name']}-${pArtifact['version']}.iar"
    def String projectPath = "./${pArtifact['name']}/"
    def String filePath ="${projectPath}build/iar/"
    def File outputFile = new File(filePath, fileName)

    println "Output: ${outputFile.getAbsolutePath()}"

    // Retrieve the IAR file
    nexusGet(pArtifact, "iar", client, outputFile)

    // Unzip the IAR file
    def ant = new AntBuilder()
    ant.unzip(src: "${outputFile.getAbsolutePath()}",
            dest: "${projectPath}",
            overwrite: "true")

    // Retrieve the POM file
    def File pomFile = new File(filePath, "pom.xml")
    nexusGet(pArtifact, "pom", client, pomFile)
}

def retrieveJAR(pArtifact, client) {

    // Determine File Details
    def String fileName = "${pArtifact['name']}-${pArtifact['version']}.jar"
    def String projectPath = "./${pArtifact['name']}/"
    def String filePath ="${projectPath}build/libs/"
    def File outputFile = new File(filePath, fileName)

    println "Output: ${outputFile.getAbsolutePath()}"

    // Retrieve the JAR file
    nexusGet(pArtifact, "jar", client, outputFile)

    // Retrieve the POM file
    def File pomFile = new File(filePath, "pom.xml")
    nexusGet(pArtifact, "pom", client, pomFile)
}

def nexusGet (pArtifact, extension, client, outputFile) {

    def queryMap = [:]
    queryMap.put('g', pArtifact['group'])
    queryMap.put('v', pArtifact['version'])
    queryMap.put('a', pArtifact['name'])
    queryMap.put('r', pArtifact['repository'])
    queryMap.put('e', extension)

    println "QueryMap: ${queryMap}"

    // Perform Nexus request (GET)
    client.request( Method.GET, ContentType.BINARY ) { req ->
        uri.path = "/nexus/service/local/artifact/maven/redirect"
        //uri.path = "/nexus/service/local/lucene/search"
        uri.query = queryMap
        headers.Accept = 'application/xml'

        response.success = {resp, inputStream ->
            FileUtils.copyInputStreamToFile(inputStream, outputFile)
        }
    }

}

def List findArtifacts(options, client) {

    def artifacts = []
    def queryMap = [:]

    // Set up the query map
    if (options.g) {queryMap.put('g', options.g)}
    if (options.v) {
        queryMap.put('v', options.v)
    } else {
        throw new Exception("No Version Defined: -v option must be provided")
    }

    def xml = client.get(path: '/nexus/service/local/lucene/search', query: queryMap )

    if (xml.totalCount != '0') {

        xml.data.artifact.each { entry ->
            def myArtifact = [:]

            myArtifact['group'] = entry.groupId.text()
            myArtifact['name'] = entry.artifactId.text()
            myArtifact['repository'] = entry.latestReleaseRepositoryId.text()
            myArtifact['version'] = options.v

            entry.artifactHits.artifactHit.artifactLinks.artifactLink.each { link ->
                def artifactType = link.extension.text()
                if (artifactType != 'pom') {
                    myArtifact['type'] = artifactType
                }
            }

            println "\n----------------"
            println "Artifact: " + myArtifact['name']
            println "\tVersion:   " + myArtifact['version']
            println "\tGroup:     " +  myArtifact['group']
            println "\tRepo:      " + myArtifact['repository']
            println "\tType:      " + myArtifact['type']

            artifacts.push(myArtifact)
        }
    }

    return artifacts
}