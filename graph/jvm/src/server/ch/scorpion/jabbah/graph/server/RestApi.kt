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

            return options
        }
    }

    init {
        LOG.info("Jabbah REST API server started")
        LOG.info("Accessing drawings in directory '${cmdLine.getOptionValue("d")}'")

        get("/jabbah-graph/graphView/:name") {req, res ->
            res.type("text/xml")
            getDrawing(req.params(":name"))
        }
    }

    fun getDrawing(name: String): String? {
        val path = Paths.get(cmdLine.getOptionValue("d"), name)
        if (!Files.exists(path)) {
            LOG.error("Path '$path' doesn't exist")
            return null
        }
        LOG.info("Returning contents of '$path'")
        return String(Files.readAllBytes(path))
    }
}