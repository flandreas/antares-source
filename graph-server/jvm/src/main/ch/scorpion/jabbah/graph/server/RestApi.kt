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
        private const val BASE_URL = "/jabbah-graph"
        private const val LIBRARY_FILE_NAME = "library.lib"

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

	private val libraryDirectory: String
	private val projectDirectory: String

    init {
        LOG.info("Jabbah Graph REST API server started")

	    libraryDirectory = determineLibraryDirectory()
	    projectDirectory = determineProjectDirectory()

        LOG.info("Accessing drawings in directory '${cmdLine.getOptionValue("d")}'")
        LOG.info("Accessing library in directory '${cmdLine.getOptionValue("l")}'")

        //staticFiles.externalLocation("/Users/andreas/Documents/scorpion2/jabbah")

        /** Returns a GraphView with a given name as an XML string.*/
        get("$BASE_URL/graphView/:name") { request, result ->
            result.type("text/xml")
            getFile(cmdLine.getOptionValue("d"), request.params(":name"))
        }

        /** Returns the Library as an XML string. */
        get("$BASE_URL/library/contents") { _, result ->
            result.type("text/xml")
            getFile(cmdLine.getOptionValue("l"), LIBRARY_FILE_NAME)
        }

        get("$BASE_URL/library/graphView/:name") { request, result ->
            result.type("text/xml")
            getFile(cmdLine.getOptionValue("l"), "${request.params(":name")}.cir")
        }
    }

    private fun getFile(dirPath: String, fileName: String): String? {
        val path = Paths.get(dirPath, fileName)
        if (!Files.exists(path)) {
            LOG.error("Path '$path' doesn't exist")
            return null
        }
        LOG.info("Returning contents of '$path'")
        return String(Files.readAllBytes(path))
    }

	private fun determineLibraryDirectory(): String {
		return when {
			cmdLine.hasOption("l") -> cmdLine.getOptionValue("l")
			cmdLine.hasOption("d") -> FileSystems.getDefault().getPath(cmdLine.getOptionValue("d"), DEFAULT_LIBRARY_DIRECTORY).toString()
			else -> {
				LOG.error("Either option 'l' or 'd' have to be specified")
				throw IllegalArgumentException("Illegal options")
			}
		}
	}

	private fun determineProjectDirectory(): String {
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