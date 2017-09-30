package ch.scorpion.jabbah.graph.server

import org.apache.commons.cli.*
import org.slf4j.LoggerFactory
import spark.Spark.*
import java.nio.file.Files
import java.nio.file.Paths

/**
 * The server that implements the REST API using spark.
 */
class RestApi(val cmdLine: CommandLine) {

    companion object {

        val LOG = LoggerFactory.getLogger(RestApi::class.java)!!

        private val BASE_URL = "/jabbah-graph"
        private val LIBRARY_FILE_NAME = "library.lib"

        @JvmStatic fun main(args: Array<String>) {
            val options = defineOptions()
            try {
                RestApi(DefaultParser().parse(options, args))
            } catch (x: ParseException) {
                LOG.error("Error while parsing options: '${x.message}'")
                HelpFormatter().printHelp("jabbah.graph REST API", options)
                System.exit(1)
            }
        }

        private fun defineOptions(): Options {
            val options = Options()

            options.addOption(Option.builder("d")
                    .required()
                    .longOpt("drawingDir")
                    .desc("Drawing directory")
                    .hasArg()
                    .build())

            options.addOption(Option.builder("l")
                    .required()
                    .longOpt("libraryDir")
                    .desc("Library directory")
                    .hasArg()
                    .build())

            return options
        }
    }

    init {
        LOG.info("Jabbah Graph REST API server started")
        LOG.info("Accessing drawings in directory '${cmdLine.getOptionValue("d")}'")
        LOG.info("Accessing library in directory '${cmdLine.getOptionValue("l")}'")

        staticFiles.externalLocation("/Users/andreas/Documents/scorpion2/jabbah")

        /** Returns a GraphView with a given name as an XML string.*/
        get("$BASE_URL/graphView/:name") { request, result ->
            result.type("text/xml")
            getFile(cmdLine.getOptionValue("d"), request.params(":name"))
        }

        /** Returns the Library as an XML string. */
        get("$BASE_URL/library/contents") { request, result ->
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
}