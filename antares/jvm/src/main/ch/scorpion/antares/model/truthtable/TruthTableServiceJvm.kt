package ch.scorpion.antares.model.truthtable

import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.antares.model.truthtable.TruthTableServiceJvm.Companion.ILLEGAL_VALUE
import ch.scorpion.antares.model.truthtable.TruthTableServiceJvm.Companion.TOO_MANY_ROWS
import ch.scorpion.antares.model.truthtable.TruthTableServiceJvm.Companion.UNKNOWN_COLUMN_NAME
import ch.scorpion.antares.model.truthtable.TruthTableServiceJvm.Companion.WRONG_NUMBER_OF_COLUMNS
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.edit.CommandManager
import ch.scorpion.jabbah.edit.module.EditModule
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVRecord
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

interface TruthTableServiceJvm {

    companion object {

        /**
         * The key in [TruthTableImportException] if a truth table value in the CSV is illegal.
         * Parameters: 0 = value, 1 = row in CSV, 2 = column in CSV.
         */
        const val ILLEGAL_VALUE = "antares.truthTable.csv.illegalValue.msg"

        /**
         * The key in [TruthTableImportException] if the number of CSV columns doesn't match the number of output columns.
         * Parameters: 0 = current number, 1 = expected number.
         */
        const val WRONG_NUMBER_OF_COLUMNS = "antares.truthTable.csv.wrongNumberOfColumns.msg"

        /**
         * The key in [TruthTableImportException] if the number of CSV rows exceeds the number of truth table rows.
         * Parameters: 0 = current number, 1 = expected number.
         */
        const val TOO_MANY_ROWS = "antares.truthTable.csv.tooManyRows.msg"

        /**
         * The key in [TruthTableImportException] if the name of a CSV row doesn't match those of the truth table.
         * Parameters: 0 = column name.
         */
        const val UNKNOWN_COLUMN_NAME = "antares.truthTable.csv.unknownColumnName.msg"
    }

    /**
     * Imports the CSV data provided by [inputStream] and writes the output values in [truthTableRef].
     * @throws TruthTableImportException if the content of the CSV data is invalid
     */
    fun importCSV(truthTableRef: TruthTableReference, params: TruthTableImportParams, inputStream: InputStream)
}

data class TruthTableImportParams(
    val headers: Boolean = true,
    val inputColumns: Boolean = false
)

data class TruthTableImportException(
    val msgKey: String,
    val params: Array<Any>,
) : RuntimeException() {

    val description: String get() = Translations.getString(msgKey, *params)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TruthTableImportException

        if (msgKey != other.msgKey) return false
        if (!params.contentEquals(other.params)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = msgKey.hashCode()
        result = 31 * result + params.contentHashCode()
        return result
    }
}

class TruthTableServiceJvmImpl(
    private val commandManager: CommandManager = EditModule.commandManager
) : TruthTableServiceJvm {

    override fun importCSV(truthTableRef: TruthTableReference, params: TruthTableImportParams, inputStream: InputStream) {
        val oldValue = truthTableRef.truthTable.doClone()
        val truthTable = truthTableRef.truthTable.doClone()

        BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8)).use { reader ->
            val formatBuilder = CSVFormat.DEFAULT.builder()
                .setTrim(true)

            if (params.headers) {
                formatBuilder
                    .setHeader()
                    .setSkipHeaderRecord(true)
            }

            val parser = formatBuilder.get().parse(reader)
            val records = parser.records

            checkNumberOfColumns(truthTable, params, records)
            checkNumberOfRows(truthTable, records)

            var csvRow = 0
            for (record in records) {
                for (ttCol in truthTable.inputColumnCount until truthTable.columnCount) {
                    val value = readOutputValueFromCSV(ttCol, truthTable, params, record)
                    writeOutputValueToTruthTable(csvRow, ttCol, value, truthTable, params, record)
                }
                csvRow++
            }

            commandManager.execute(TruthTableImportCSVCommand(truthTableRef, oldValue, truthTable))
        }
    }

    private fun readOutputValueFromCSV(ttCol: Int, truthTable: TruthTable, params: TruthTableImportParams, record: CSVRecord): Bit {
        return if (params.headers) {
            val columName = truthTable.getColumnName(ttCol)
            if (record.isMapped(columName)) {
                val csvCol = record.parser.headerMap[columName]!! + 1
                parseCell(record.get(columName), record.recordNumber.toInt(), csvCol)
            } else {
                throw TruthTableImportException(UNKNOWN_COLUMN_NAME, arrayOf(columName))
            }
        } else {
            if (params.inputColumns) {
                parseCell(record.get(ttCol), record.recordNumber.toInt(), ttCol)
            } else {
                parseCell(record.get(ttCol - truthTable.inputColumnCount), record.recordNumber.toInt(), ttCol - truthTable.inputColumnCount)
            }
        }
    }

    private fun writeOutputValueToTruthTable(csvRow: Int, col: Int, value: Bit, truthTable: TruthTable,
         params: TruthTableImportParams, record: CSVRecord
    ) {
        if (params.inputColumns) {
            val truthTableRow = if (params.headers) {
                calculateTruthTableRowWithHeaders(truthTable, record)
            } else {
                calculateTruthTableRowWithoutHeaders(truthTable, record)
            }
            truthTable.setValue(truthTableRow, col, value)
        } else {
            truthTable.setValue(csvRow, col, value)
        }
    }

    private fun calculateTruthTableRowWithHeaders(truthTable: TruthTable, record: CSVRecord): Int {
        var row = 0
        var factor = 1

        for (col in truthTable.inputColumnCount - 1 downTo 0) {
            val columName = truthTable.getColumnName(col)
            val bit = if (record.isMapped(columName)) {
                parseCell(record.get(columName), record.recordNumber.toInt(), col)
            } else {
                throw TruthTableImportException(UNKNOWN_COLUMN_NAME, arrayOf(columName))
            }
            if (!bit.isDefined) {
                throw TruthTableImportException(ILLEGAL_VALUE, arrayOf(record.get(columName), record.recordNumber.toInt(), col))
            }
            val value = if (bit.isSet) 1 else 0
            row += factor * value
            factor *= 2
        }

        return row
    }

    private fun calculateTruthTableRowWithoutHeaders(truthTable: TruthTable, record: CSVRecord): Int {
        var row = 0
        var factor = 1

        for (col in truthTable.inputColumnCount - 1 downTo 0) {
            val bit = parseCell(record.get(col), record.recordNumber.toInt(), col)
            if (!bit.isDefined) {
                throw TruthTableImportException(ILLEGAL_VALUE, arrayOf(record.get(col), record.recordNumber.toInt(), col))
            }
            val value = if (bit.isSet) 1 else 0
            row += factor * value
            factor *= 2
        }

        return row
    }

    private fun checkNumberOfColumns(truthTable: TruthTable, params: TruthTableImportParams, records: List<CSVRecord>) {
        records.firstOrNull()?.size()?.let { size ->
            if (params.inputColumns) {
                if (size != truthTable.columnCount) {
                    throw TruthTableImportException(WRONG_NUMBER_OF_COLUMNS, arrayOf(size, truthTable.columnCount))
                }
            } else {
                if (size != truthTable.outputColumnCount) {
                    throw TruthTableImportException(WRONG_NUMBER_OF_COLUMNS, arrayOf(size, truthTable.outputColumnCount))
                }
            }
        }
    }

    private fun checkNumberOfRows(truthTable: TruthTable, records: List<CSVRecord>) {
        if (records.size > truthTable.rowsCount) {
            throw TruthTableImportException(TOO_MANY_ROWS, arrayOf(records.size, truthTable.rowsCount))
        }
    }

    private fun parseCell(value: String, csvRow: Int, column: Int): Bit {
        if (value.length != 1) {
            throw TruthTableImportException(ILLEGAL_VALUE, arrayOf(value, csvRow, column))
        }
        try {
            return Bit.of(value.uppercase()[0])
        } catch (_: Throwable) {
            throw TruthTableImportException(ILLEGAL_VALUE, arrayOf(value, csvRow, column))
        }
    }
}