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
	    private const val DICTIONARY_FILE_NAME = "dictionary.xml"

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

	    /** ---- LibraryDirectory */

	    /** Returns the LibraryDictionary file as an XML string. */
	    get("$baseUrl/libraries") { _, response ->
		    response.type("text/xml")
		    getFile(libraryBaseDirectory, DICTIONARY_FILE_NAME)
	    }

	    /** Returns the ProjectDictionary file as an XML string. */
	    get("$baseUrl/projects") { _, response ->
		    response.type("text/xml")
		    getFile(projectBaseDirectory, DICTIONARY_FILE_NAME)
	    }

	    /** Stores the received XML string as LibraryDictionary file. */
	    put("$baseUrl/libraries") { request, _ ->
	    	putFile(libraryBaseDirectory, DICTIONARY_FILE_NAME, request.body())
	    }

	    /** Stores the received XML string as ProjectDictionary file. */
	    put("$baseUrl/projects") { request, _ ->
		    putFile(projectBaseDirectory, DICTIONARY_FILE_NAME, request.body())
	    }

	    /** ---- Library and Project */

	    /** Returns the Library file as an XML string. */
	    get("$baseUrl/libraries/:uuid") { request, response ->
		    response.type("text/xml")
		    getFile(buildLibraryDirectoryPath(request.params(":uuid")), LIBRARY_FILE_NAME)
	    }

	    /** Returns the Project file as an XML string. */
	    get("$baseUrl/projects/:uuid") { request, response ->
		    response.type("text/xml")
		    getFile(buildProjectDirectoryPath(request.params(":uuid")), LIBRARY_FILE_NAME)
	    }

	    /** Stores the received XML string as Library file. */
	    put("$baseUrl/libraries/:uuid") { request, _ ->
		    val uuid = request.params(":uuid")
		    ensureLibraryDirectory(uuid)
		    putFile(buildLibraryDirectoryPath(uuid), LIBRARY_FILE_NAME, request.body())
	    }

	    /** Stores the received XML string as Project file. */
	    put("$baseUrl/projects/:uuid") { request, _ ->
		    val uuid = request.params(":uuid")
		    ensureProjectDirectory(uuid)
		    putFile(buildProjectDirectoryPath(uuid), LIBRARY_FILE_NAME, request.body())
	    }

	    /** ---- MetaGraph */

	    /** Returns the MetaGraph of a Library as an XML string. */
	    get("$baseUrl/libraries/:uuid/:metaGraphUuid") { request, response ->
		    val metaGraphUuid = request.params(":metaGraphUuid")
		    response.type("text/xml")
		    getFile(buildLibraryDirectoryPath(request.params(":uuid")), "$metaGraphUuid.cir")
	    }

	    /** Returns the MetaGraph of a Project as an XML string. */
	    get("$baseUrl/projects/:uuid/:metaGraphUuid") { request, response ->
		    val metaGraphUuid = request.params(":metaGraphUuid")
		    response.type("text/xml")
		    getFile(buildProjectDirectoryPath(request.params(":uuid")), "$metaGraphUuid.cir")
	    }

	    /** Stores the received XML string as MetaGraph in a Library. */
	    post("$baseUrl/libraries/:uuid/:metaGraphUuid") { request, _ ->
		    val metaGraphUuid = request.params(":metaGraphUuid")
		    putFile(buildLibraryDirectoryPath(request.params(":uuid")), "$metaGraphUuid.cir", request.body())
	    }

	    /** Stores the received XML string as MetaGraph in a Project. */
	    post("$baseUrl/projects/:uuid/:metaGraphUuid") { request, _ ->
		    val metaGraphUuid = request.params(":metaGraphUuid")
		    putFile(buildProjectDirectoryPath(request.params(":uuid")), "$metaGraphUuid.cir", request.body())
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

	private fun ensureProjectDirectory(projectUuid: String) {
		val path = Paths.get(buildProjectDirectoryPath(projectUuid))
		if (!Files.exists(path)) {
			Files.createDirectories(path)
		}
	}

	private fun ensureLibraryDirectory(libraryUuid: String) {
		val path = Paths.get(buildLibraryDirectoryPath(libraryUuid))
		if (!Files.exists(path)) {
			Files.createDirectories(path)
		}
	}
}