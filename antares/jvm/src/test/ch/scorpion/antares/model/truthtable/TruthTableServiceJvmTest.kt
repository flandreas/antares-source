package ch.scorpion.antares.model.truthtable

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.antares.model.signal.Bit.True
import ch.scorpion.antares.model.signal.Bit.False
import ch.scorpion.antares.model.signal.Bit.Error
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.UndoableDataHolder
import ch.scorpion.jabbah.edit.command.SourcingCommandManager
import ch.scorpion.jabbah.edit.model.DrawingImpl
import ch.scorpion.jabbah.io.Storable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TruthTableServiceJvmTest {

    private val commandManager = SourcingCommandManager()

    private val service = TruthTableServiceJvmImpl(commandManager)

    private var dummyStorable: Storable

    private val dataHolder = object : UndoableDataHolder {
        override fun getUndoableState(): Storable? = dummyStorable
        override fun setUndoableState(state: Storable) {
            dummyStorable = state
        }
        override fun undoableStateEstablished(state: Storable) {}
    }

    init {
        AntaresTestRule.configure()
        dummyStorable = DrawingImpl<Component>()
        commandManager.bindDataHolder(dataHolder)
    }

    // ---- Without input columns

    @Test
    fun shouldReadOutputsWithHeader() {
        val params = TruthTableImportParams(true)
        val truthTable = TruthTable("Test", listOf("A", "B"), listOf("O1", "O2"))
        val csv = """
            O1, O2
            1,0
            0,1
            0,X
            X,1
        """.trimIndent()

        service.importCSV(TruthTableReference({ truthTable }), params, csv.byteInputStream())

        assertOutputs(truthTable, 0, True, False)
        assertOutputs(truthTable, 1, False, True)
        assertOutputs(truthTable, 2, False, Error)
        assertOutputs(truthTable, 3, Error, True)
    }

    @Test
    fun shouldReadOutputsWithoutHeader() {
        val params = TruthTableImportParams(false)
        val truthTable = TruthTable("Test", listOf("A", "B"), listOf("O1", "O2"))
        val csv = """
            1,0
            0,1
            0,X
            X,1
        """.trimIndent()

        service.importCSV(TruthTableReference({ truthTable }), params, csv.byteInputStream())

        assertOutputs(truthTable, 0, True, False)
        assertOutputs(truthTable, 1, False, True)
        assertOutputs(truthTable, 2, False, Error)
        assertOutputs(truthTable, 3, Error, True)
    }

    @Test
    fun shouldRejectIllegalValue() {
        val exception = assertFailsWith<TruthTableImportException> {
            val params = TruthTableImportParams(false)
            val truthTable = TruthTable("Test", listOf("A", "B"), listOf("O1", "O2"))
            val csv = """
                2,0
            """.trimIndent()

            service.importCSV(TruthTableReference({ truthTable }), params, csv.byteInputStream())
        }
        assertEquals(TruthTableServiceJvm.ILLEGAL_VALUE, exception.msgKey)
    }

    @Test
    fun shouldRejectWrongNumberOfOutputs() {
        val exception = assertFailsWith<TruthTableImportException> {
            val params = TruthTableImportParams(false)
            val truthTable = TruthTable("Test", listOf("A", "B"), listOf("O1", "O2"))
            val csv = """
                1
            """.trimIndent()

            service.importCSV(TruthTableReference({ truthTable }), params, csv.byteInputStream())
        }
        assertEquals(TruthTableServiceJvm.WRONG_NUMBER_OF_COLUMNS, exception.msgKey)
    }

    @Test
    fun shouldRejectTooManyRows() {
        val exception = assertFailsWith<TruthTableImportException> {
            val params = TruthTableImportParams(false)
            val truthTable = TruthTable("Test", listOf("A", "B"), listOf("O1", "O2"))
            val csv = """
                1, 1
                1, 1
                1, 1
                1, 1
                1, 1
            """.trimIndent()

            service.importCSV(TruthTableReference({ truthTable }), params, csv.byteInputStream())
        }
        assertEquals(TruthTableServiceJvm.TOO_MANY_ROWS, exception.msgKey)
    }

    @Test
    fun shouldRejectUnknownColumnName() {
        val exception = assertFailsWith<TruthTableImportException> {
            val params = TruthTableImportParams(true)
            val truthTable = TruthTable("Test", listOf("A", "B"), listOf("O1", "O2"))
            val csv = """
                O1,X
                1,1
            """.trimIndent()

            service.importCSV(TruthTableReference({ truthTable }), params, csv.byteInputStream())
        }
        assertEquals(TruthTableServiceJvm.UNKNOWN_COLUMN_NAME, exception.msgKey)
    }

    // ---- With input columns

    @Test
    fun shouldReadInputColumnsWithHeaders() {
        val params = TruthTableImportParams(headers = true, inputColumns = true)
        val truthTable = TruthTable("Test", listOf("A", "B"), listOf("O1", "O2"))

        // Columns and rows intentionally shuffled
        val csv = """
            B,A,O1,O2
            0,1,0,1
            0,0,1,0
            1,1,X,1            
            1,0,0,X
        """.trimIndent()

        service.importCSV(TruthTableReference({ truthTable }), params, csv.byteInputStream())

        assertOutputs(truthTable, 0, True, False)
        assertOutputs(truthTable, 1, False, Error)
        assertOutputs(truthTable, 2, False, True)
        assertOutputs(truthTable, 3, Error, True)
    }

    @Test
    fun shouldReadInputColumnsWithoutHeaders() {
        val params = TruthTableImportParams(headers = false, inputColumns = true)
        val truthTable = TruthTable("Test", listOf("A", "B"), listOf("O1", "O2"))

        val csv = """
            1,0,0,1
            0,0,1,0
            1,1,X,1            
            0,1,0,X
        """.trimIndent()

        service.importCSV(TruthTableReference({ truthTable }), params, csv.byteInputStream())

        assertOutputs(truthTable, 0, True, False)
        assertOutputs(truthTable, 1, False, Error)
        assertOutputs(truthTable, 2, False, True)
        assertOutputs(truthTable, 3, Error, True)
    }

    @Test
    fun shouldUndoImport() {
        val truthTable = doImport()

        commandManager.undo()

        assertOutputs(truthTable, 0, False, False)
        assertOutputs(truthTable, 1, False, False)
        assertOutputs(truthTable, 2, False, False)
        assertOutputs(truthTable, 3, False, False)
    }

    @Test
    fun shouldRedoImport() {
        val truthTable = doImport()

        commandManager.undo()
        commandManager.redo()

        assertOutputs(truthTable, 0, True, False)
        assertOutputs(truthTable, 1, False, True)
        assertOutputs(truthTable, 2, False, Error)
        assertOutputs(truthTable, 3, Error, True)
    }

    private fun doImport(): TruthTable {
        val params = TruthTableImportParams(true)
        val truthTable = TruthTable("Test", listOf("A", "B"), listOf("O1", "O2"))
        val csv = """
            O1, O2
            1,0
            0,1
            0,X
            X,1
        """.trimIndent()

        service.importCSV(TruthTableReference({ truthTable }), params, csv.byteInputStream())

        return truthTable
    }

    // ---- Helpers

    private fun assertOutputs(table: TruthTable, row: Int, vararg bits: Bit) {
        bits.forEachIndexed() { col, bit ->
            assertEquals(bit, table.getValue(row, table.inputColumnCount + col))
        }
    }
}