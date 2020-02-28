package ch.scorpion.antares.model.output

import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.antares.model.port.DigitalPortImpl
import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.Word
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.edit.model.text.Translation
import ch.scorpion.jabbah.edit.model.text.description.DescribableImpl
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.actor.Actor
import ch.scorpion.jabbah.graph.model.GraphActorData
import ch.scorpion.jabbah.graph.model.InputPort
import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.graph.model.vertice.CalculatingVertice
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

/**
 * A matrix of light emitting dots with a row and column addressing input, designed
 * to be used by multiplexing either rows or columns.
 *
 * The column port receives a [Word] whose [Bit]s address the matrix columns, with the least
 * significant [Bit] identifying the rightmost column. The row port receives a [Word] whose [Bit]s
 * address the matrix rows, with the least significant [Bit] identifying the bottom row.
 */
class LEDMatrix(
	columnWidth: BitWidth = DEF_COLUMN_WIDTH,
	rowWidth: BitWidth = DEF_ROW_WIDTH
) : CalculatingVertice(CALCULATOR) {

	companion object {

		private val LOG by logger(LEDMatrix::class)

		private const val BASE_RESOURCE_KEY = "library.element.LEDMatrix"
		private val TYPE = Translations.getString("$BASE_RESOURCE_KEY.name")
		private val TYPE_DESC = Translations.getOptionalString("$BASE_RESOURCE_KEY.desc")

		const val COLUMN_PORT_NAME = "C"
		const val ROW_PORT_NAME = "R"
		private val DEF_COLUMN_WIDTH = BitWidth.BW_8
		private val DEF_ROW_WIDTH = BitWidth.BW_8
		private const val DEF_AFTERGLOW = 10L

		private val COLUMNS_PORT_DESC = DescribableImpl(Translation.ofStaticKey("antares.ledMatrix.columnsPort.desc"))
		private val ROWS_PORT_DESC = DescribableImpl(Translation.ofStaticKey("antares.ledMatrix.rowsPort.desc"))

		private val CALCULATOR = Calculator()

		private class Calculator : VerticeCalculator<LEDMatrix> {
			override fun calculate(vertice: LEDMatrix, data: GraphActorData, signalHandler: SignalHandler) {
				vertice.updateBuffer(data.changedPort != null, signalHandler)
			}
		}
	}

	init {
		addPort(DigitalPortImpl(portType = PortType.INPUT, name = COLUMN_PORT_NAME, bitWidth = columnWidth, describable = COLUMNS_PORT_DESC))
		addPort(DigitalPortImpl(portType = PortType.INPUT, name = ROW_PORT_NAME, bitWidth = rowWidth, describable = ROWS_PORT_DESC))
	}

	override val type: String get() = TYPE
	override val typeDesc: String? get() = TYPE_DESC

	val columnPort: DigitalPort
		get() = getInput<DigitalSignal>(COLUMN_PORT_NAME) as DigitalPort

	var columnWidth: BitWidth
		get() = columnPort.bitWidth
		set(value) {
			columnPort.bitWidth = value
			stateChanged()
		}

	val rowPort: DigitalPort
		get() = getInput<DigitalSignal>(ROW_PORT_NAME) as DigitalPort

	var rowWidth: BitWidth
		get() = rowPort.bitWidth
		set(value) {
			rowPort.bitWidth = value
			stateChanged()
		}

	/** The duration (in ms) the LED dots still glow after they are not addressed any more. */
	var afterglowDuration: Long = DEF_AFTERGLOW

	/** ---- [Storable] */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		writer.writeString("columnWidth", columnWidth.customName)
		writer.writeString("rowWidth", rowWidth.customName)
		writer.writeLong("afterglow", afterglowDuration)
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		columnWidth = BitWidth.withName(reader.readString("columnWidth"))
		rowWidth = BitWidth.withName(reader.readString("rowWidth"))
		if (reader.hasAttribute("afterglow")) {
			afterglowDuration = reader.readLong("afterglow")
		}
	}

	/** ---- [Actor] */

	override fun executionStarted(signalHandler: SignalHandler) {
		super.executionStarted(signalHandler)
		clear()
	}

	override fun executionStopped(signalHandler: SignalHandler) {
		super.executionStopped(signalHandler)
		clear()
	}

	/** ---- [LEDMatrix] */

	/**
	 * Buffers the time until which a single dot will glow.
	 * Maps the column index to a map that maps the row index to the time when
	 * glowing of this point will fade. Column 0 is the rightmost column, row 0 is the
	 * bottommost row.
	 */
	private val buffer: MutableMap<Int, MutableMap<Int, Long>> = mutableMapOf()

	/** Determines whether the dot at the specified coordinate is currently on or not.*/
	fun isOn(columnIndex: Int, rowIndex: Int): Boolean {
		return getCellTime(columnIndex, rowIndex) != null
	}

	fun clear() {
		buffer.clear()
	}

	/** Updates the internal buffer after any [InputPort] has changed.*/
	private fun updateBuffer(portChanged: Boolean, signalHandler: SignalHandler) {
		LOG.debug("LEDMatrix on ${signalHandler.executionTime}: updateBuffer with portChanged=$portChanged")

		val time = signalHandler.executionTime + afterglowDuration * 1_000_000
		val rowValue = rowPort.getIncomingSignal() as Word

		var anyChanged = false
		var anySwitchedOff = false
		for ((columnIndex, columnBit) in (columnPort.getIncomingSignal() as Word).bits.withIndex()) {
			for ((rowIndex, rowBit) in rowValue.bits.withIndex()) {
				val cellTime = getCellTime(columnIndex, rowIndex)
				val hasOldValue = cellTime == Long.MAX_VALUE
				val isNewValue = rowBit.isSet && columnBit.isSet
				if (portChanged) {
					val switchedOff = !isNewValue && hasOldValue
					anyChanged = anyChanged || isNewValue != hasOldValue
					anySwitchedOff = anySwitchedOff || switchedOff
					if (isNewValue) {
						LOG.debug("LEDMatrix: switched on at $columnIndex,$rowIndex")
						glowUntil(columnIndex, rowIndex, Long.MAX_VALUE)
					} else if (switchedOff) {
						LOG.debug("LEDMatrix: switched off at $columnIndex,$rowIndex, afterglowing until $time")
						glowUntil(columnIndex, rowIndex, time)
					}
				}
				if (hasExpired(cellTime, signalHandler.executionTime)) {
					fadeOut(columnIndex, rowIndex)
					anyChanged = true
				}
			}
		}
		if (anyChanged) {
			stateChanged(signalHandler)
		}
		if (anySwitchedOff) {
			LOG.debug("LEDMatrix: Request switch-off at ${signalHandler.executionTime + afterglowDuration * 1_000_000} ns")
			signalHandler.requestActingAfter(this, afterglowDuration * 1_000_000, createActorData(null))
		}
	}

	private fun hasExpired(cellTime: Long?, currentTime: Long): Boolean = cellTime != null && cellTime <= currentTime

	private fun fadeOut(columnIndex: Int, rowIndex: Int) {
		LOG.debug("LEDMatrix: end afterglowing of $columnIndex,$rowIndex")
		buffer[columnIndex]!!.remove(rowIndex)
	}

	/**
	 * Returns the time until the cell at the specified location will glow, or `null` if it doesn't glow at all.
	 * Indexes start with 0.
	 */
	private fun getCellTime(columnIndex: Int, rowIndex: Int): Long? {
		val column = buffer[columnIndex] ?: return null
		return column[rowIndex]
	}

	private fun ensureColumn(columnIndex: Int): MutableMap<Int, Long> {
		var column = buffer[columnIndex]
		if (column == null) {
			column = mutableMapOf()
			buffer[columnIndex] = column
		}
		return column
	}

	/** Updates the specified matrix dot with the simulation time at which glowing will fade */
	private fun glowUntil(columnIndex: Int, rowIndex: Int, time: Long) {
		ensureColumn(columnIndex)[rowIndex] = time
	}
}