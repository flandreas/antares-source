package ch.scorpion.antares.hdl

import ch.scorpion.antares.model.DigitalGraph
import ch.scorpion.antares.model.testcase.Testcase
import ch.scorpion.jabbah.base.dsl.DslError
import ch.scorpion.jabbah.base.io.CodePrinter
import ch.scorpion.jabbah.base.logger
import java.io.FileOutputStream
import java.nio.file.Path

abstract class HDLGenerator(
    protected val params: HDLExportParams,
) {
    companion object {
        private val LOG by logger(HDLGenerator::class)
    }

    protected abstract fun writeCircuit(model: HDLModel, printer: CodePrinter)

    protected abstract fun writeTestBench(model: HDLModel, printer: CodePrinter)

    /**
     * @throws [DslError] if the [Testcase] used for test bench creation contains an error
     */
    // Used by the UI
    fun generate(circuit: DigitalGraph) {
        LOG.userTrail("Exporting VHDL for '${circuit.name}' (${circuit.uuid.id})")

        val model = createModel(circuit)
        writeCircuitFile(model, params.hdlFile)
        if (params.testBenchParams != null) {
            writeTestBenchFile(model, params.testBenchParams.tbFile)
        }
    }

    // Used for testing
    fun generateHDL(hdlPrinter: CodePrinter, circuit: DigitalGraph) {
        val model = createModel(circuit)
        writeCircuit(model, hdlPrinter)
    }

    private fun writeCircuitFile(model: HDLModel, hdlFile: Path) {
        FileOutputStream(hdlFile.toFile()).use {
            CodePrinter(it).also { printer ->
                writeCircuit(model, printer)
            }
        }
    }

    private fun writeTestBenchFile(model: HDLModel, tbFile: Path) {
        FileOutputStream(tbFile.toFile()).use {
            CodePrinter(it).also { printer ->
                writeTestBench(model, printer)
            }
        }
    }

    private fun createModel(circuit: DigitalGraph): HDLModel =
        HDLModel(circuit, params.renaming)
            .create()
            .apply { renameLabels() }
}