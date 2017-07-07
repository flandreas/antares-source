package ch.scorpion.jabbah.graph.server

import org.apache.commons.cli.*
import org.slf4j.LoggerFactory
import spark.Spark.*
import java.nio.file.Files
import java.nio.file.Paths

/**
 * The server that implements the REST API using sparc.
 */
class RestApi(val cmdLine: CommandLine) {

    companion object {

        @JvmStatic fun main(args: Array<String>) {
            val options = defineOptions()
            try {
                RestApi(DefaultParser().parse(options, args))
            } catch (x: ParseException) {
                System.out.println("Error while parsing options: '${x.message}'")
                HelpFormatter().printHelp("Jabbah REST API", options)
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

    val LOG = LoggerFactory.getLogger(RestApi::class.java)

    init {
        System.out.println("Jabbah REST API server started")
        System.out.println("Accessing drawings in directory '${cmdLine.getOptionValue("d")}'")

        get("/hello/:name") {req, res ->
            res.type("text/xml")
            getDrawing(req.params(":name"))
        }
    }

    fun getDrawing(name: String): String? {
        val path = Paths.get(cmdLine.getOptionValue("d"), name)
        if (!Files.exists(path)) {
            System.out.println("Path '$path' doesn't exist")
            return null
        }
        System.out.println("Returning contents of '$path'")
        return String(Files.readAllBytes(path))
    }
}