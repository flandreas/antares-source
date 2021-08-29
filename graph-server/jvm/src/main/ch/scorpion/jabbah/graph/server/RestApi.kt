package ch.scorpion.jabbah.graph.server

import org.apache.commons.cli.*
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.nio.file.Paths
import spark.Spark.*
import java.lang.IllegalArgumentException
import java.nio.file.FileSystems
import kotlin.system.exitProcess

/**
 * The server that implements the REST API using spark.
 */
class RestApi(private val cmdLine: CommandLine) {

    companion object {

        private val LOG = LoggerFactory.getLogger(RestApi::class.java)

	    private const val DEFAULT_LIBRARY_DIRECTORY = "libraries"
	    private const val DEFAULT_PROJECT_DIRECTORY = "projects"
        private const val LIBRARY_FILE_NAME = "library.xml"

        @JvmStatic fun main(args: Array<String>) {
            val options = defineOptions()
            try {
                RestApi(DefaultParser().parse(options, args))
            } catch (x: ParseException) {
                LOG.error("Error while parsing options: '${x.message}'")
                HelpFormatter().printHelp("jabbah.graph REST API", options)
                exitProcess(1)
            }
        }

        private fun defineOptions(): Options {
            val options = Options()

	        options.addOption(Option.builder("url")
		        .required(true)
		        .longOpt("url")
		        .desc("Base URL")
		        .hasArg()
		        .build())

            options.addOption(Option.builder("d")
                    .required(false)
                    .longOpt("dataDir")
                    .desc("Data directory")
                    .hasArg()
                    .build())

            options.addOption(Option.builder("l")
                    .required(false)
                    .longOpt("libraryDir")
                    .desc("Library directory")
                    .hasArg()
                    .build())

	        options.addOption(Option.builder("p")
		        .required(false)
		        .longOpt("projectDir")
		        .desc("Project directory")
		        .hasArg()
		        .build())

            return options
        }
    }

	private val baseUrl: String
	private val libraryBaseDirectory: String
	private val projectBaseDirectory: String

    init {
        LOG.info("Jabbah Graph REST API server started")

	    baseUrl = cmdLine.getOptionValue("url")
	    libraryBaseDirectory = determineLibraryBaseDirectory()
	    projectBaseDirectory = determineProjectBaseDirectory()

	    LOG.info("Serving $baseUrl")
        LOG.info("Accessing drawings in directory '${cmdLine.getOptionValue("d")}'")
        LOG.info("Accessing library in directory '${cmdLine.getOptionValue("l")}'")

        //staticFiles.externalLocation("/Users/andreas/Documents/scorpion2/jabbah")

	    /** Returns the Library file as an XML string. */
	    get("$baseUrl/libraries/:uuid/contents") { request, response ->
		    val uuid = request.params(":uuid")
		    println("Serving Library $uuid")
		    response.type("text/xml")
		    getFile(buildLibraryDirectoryPath(uuid), LIBRARY_FILE_NAME)
	    }

	    /** Returns the Project file as an XML string. */
	    get("$baseUrl/projects/:uuid/contents") { request, response ->
		    val uuid = request.params(":uuid")
		    println("Serving Project $uuid")
		    response.type("text/xml")
		    getFile(buildProjectDirectoryPath(uuid), LIBRARY_FILE_NAME)
	    }

	    /** Returns the MetaGraph of a Library as an XML string. */
	    get("$baseUrl/libraries/:uuid/:metaGraphUuid") { request, response ->
		    val uuid = request.params(":uuid")
		    val metaGraphUuid = request.params(":metaGraphUuid")
		    println("Serving MetaGraph $metaGraphUuid in Library $uuid")
		    response.type("text/xml")
		    getFile(buildLibraryDirectoryPath(uuid), "$metaGraphUuid.cir")
	    }

	    /** Returns the MetaGraph of a Project as an XML string. */
	    get("$baseUrl/projects/:uuid/:metaGraphUuid") { request, response ->
		    val uuid = request.params(":uuid")
		    val metaGraphUuid = request.params(":metaGraphUuid")
		    println("Serving MetaGraph $metaGraphUuid in Project $uuid")
		    response.type("text/xml")
		    getFile(buildProjectDirectoryPath(uuid), "$metaGraphUuid.cir")
	    }

	    /** Stores the received XML string as MetaGraph in a Library. */
	    post("$baseUrl/libraries/:uuid/:metaGraphUuid") { request, response ->
		    val uuid = request.params(":uuid")
		    val metaGraphUuid = request.params(":metaGraphUuid")
		    println("Saving MetaGraph $metaGraphUuid in Library $uuid")
		    putFile(buildLibraryDirectoryPath(uuid), "$metaGraphUuid.cir", request.body())
	    }

	    /** Stores the received XML string as MetaGraph in a Project. */
	    post("$baseUrl/projects/:uuid/:metaGraphUuid") { request, response ->
		    val uuid = request.params(":uuid")
		    val metaGraphUuid = request.params(":metaGraphUuid")
		    println("Saving MetaGraph $metaGraphUuid in Project $uuid")
		    putFile(buildProjectDirectoryPath(uuid), "$metaGraphUuid.cir", request.body())
	    }
    }

	private fun buildLibraryDirectoryPath(uuid: String): String = "$libraryBaseDirectory/$uuid"

	private fun buildProjectDirectoryPath(uuid: String): String = "$projectBaseDirectory/$uuid"

    private fun getFile(dirPath: String, fileName: String): String? {
        val path = Paths.get(dirPath, fileName)
        if (!Files.exists(path)) {
            LOG.error("Path '$path' doesn't exist")
            return null
        }
        LOG.info("Returning contents of '$path'")
        return String(Files.readAllBytes(path))
    }

	private fun putFile(dirPath: String, fileName: String, content: String) {
		val path = Paths.get(dirPath, fileName)
		Files.write(path, content.toByteArray())
	}

	private fun determineLibraryBaseDirectory(): String {
		return when {
			cmdLine.hasOption("l") -> cmdLine.getOptionValue("l")
			cmdLine.hasOption("d") -> FileSystems.getDefault().getPath(cmdLine.getOptionValue("d"), DEFAULT_LIBRARY_DIRECTORY).toString()
			else -> {
				LOG.error("Either option 'l' or 'd' have to be specified")
				throw IllegalArgumentException("Illegal options")
			}
		}
	}

	private fun determineProjectBaseDirectory(): String {
		return when {
			cmdLine.hasOption("p") -> cmdLine.getOptionValue("p")
			cmdLine.hasOption("d") -> FileSystems.getDefault().getPath(cmdLine.getOptionValue("d"), DEFAULT_PROJECT_DIRECTORY).toString()
			else -> {
				LOG.error("Either option 'p' or 'd' have to be specified")
				throw IllegalArgumentException("Illegal options")
			}
		}
	}
}