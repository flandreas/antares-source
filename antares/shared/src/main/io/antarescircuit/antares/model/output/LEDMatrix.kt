package io.antarescircuit.antares.model.output

import io.antarescircuit.antares.model.port.DigitalPort
import io.antarescircuit.antares.model.port.DigitalPortImpl
import io.antarescircuit.antares.model.signal.Bit
import io.antarescircuit.antares.model.signal.BitWidth
import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.logger
import io.antarescircuit.jabbah.edit.model.text.TranslatableText
import io.antarescircuit.jabbah.edit.model.text.Translation
import io.antarescircuit.jabbah.execution.SignalHandler
import io.antarescircuit.jabbah.execution.actor.Actor
import io.antarescircuit.jabbah.graph.model.Graph
import io.antarescircuit.jabbah.graph.model.GraphActorData
import io.antarescircuit.jabbah.graph.model.InputPort
import io.antarescircuit.jabbah.graph.model.PortType
import io.antarescircuit.jabbah.graph.model.element.AbstractGraphElement
import io.antarescircuit.jabbah.graph.model.vertice.CalculatingVertice
import io.antarescircuit.jabbah.graph.model.vertice.VerticeCalculator
import io.antarescircuit.jabbah.io.Storable
import io.antarescircuit.jabbah.io.StoreReader
import io.antarescircuit.jabbah.io.StoreWriter

/**
 * A matrix of light emitting dots with a row and column addressing input, designed
 * to be used by multiplexing either rows or columns.
 *
 * The column port receives a [DigitalSignal] whose [Bit]s address the matrix columns, with the least
 * significant [Bit] identifying the rightmost column. The row port receives a [DigitalSignal] whose [Bit]s
 * address the matrix rows, with the least significant [Bit] identifying the bottom row.
 */
class LEDMatrix(
	columnWidth: BitWidth = DEF_COLUMN_WIDTH,
	rowWidth: BitWidth = DEF_ROW_WIDTH
) : CalculatingVertice(CALCULATOR), LightEmitterModel {

	companion object {

		private val LOG by logger(LEDMatrix::class)

		private const val BASE_RESOURCE_KEY = "library.element.LEDMatrix"
		private val TYPE get() = Translations.getString("$BASE_RESOURCE_KEY.name")
		private val TYPE_DESC get() = Translations.getOptionalString("$BASE_RESOURCE_KEY.desc")

		const val COLUMN_PORT_NAME = "C"
		const val ROW_PORT_NAME = "R"
		private val DEF_COLUMN_WIDTH = BitWidth.BW_8
		private val DEF_ROW_WIDTH = BitWidth.BW_8
		private const val DEF_AFTERGLOW = 10L

		private val COLUMNS_PORT_DESC get() = TranslatableText(Translation.ofStaticKey("antares.ledMatrix.columnsPort.desc"))
		private val ROWS_PORT_DESC get() = TranslatableText(Translation.ofStaticKey("antares.ledMatrix.rowsPort.desc"))

		private val CALCULATOR = Calculator()

		private class Calculator : VerticeCalculator<LEDMatrix> {
			override fun calculate(vertice: LEDMatrix, data: GraphActorData, signalHandler: SignalHandler) {
				vertice.updateBuffer(data.changedPort != null, signalHandler)
			}
		}
	}

	init {
		addPort(DigitalPortImpl(portType = PortType.INPUT, name = COLUMN_PORT_NAME, bitWidth = columnWidth, description = COLUMNS_PORT_DESC))
		addPort(DigitalPortImpl(portType = PortType.INPUT, name = ROW_PORT_NAME, bitWidth = rowWidth, description = ROWS_PORT_DESC))
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

	override fun executionInitialize(signalHandler: SignalHandler) {
		super.executionInitialize(signalHandler)
		clear()
	}

	override fun executionStopped(signalHandler: SignalHandler) {
		super.executionStopped(signalHandler)
		clear()
	}

	/** ---- [AbstractGraphElement] */

	override fun graphParamsChanged(graph: Graph) {
		super.graphParamsChanged(graph)
		stateChanged(null, LightEmitterModel.REASON_GRAPH_PARAM_CHANGED, graph)
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
		LOG.trace("LEDMatrix on ${signalHandler.executionTime}: updateBuffer with portChanged=$portChanged")

		val time = signalHandler.executionTime + afterglowDuration * 1_000_000
		val rowValue = rowPort.getIncomingSignal()!!

		var anyChanged = false
		var anySwitchedOff = false
		for ((columnIndex, columnBit) in (columnPort.getIncomingSignal()!!).bits.withIndex()) {
			for ((rowIndex, rowBit) in rowValue.bits.withIndex()) {
				val cellTime = getCellTime(columnIndex, rowIndex)
				val hasOldValue = cellTime == Long.MAX_VALUE
				val isNewValue = rowBit.isSet && columnBit.isSet
				if (portChanged) {
					val switchedOff = !isNewValue && hasOldValue
					anyChanged = anyChanged || isNewValue != hasOldValue
					anySwitchedOff = anySwitchedOff || switchedOff
					if (isNewValue) {
						LOG.trace("switched on at $columnIndex,$rowIndex")
						glowUntil(columnIndex, rowIndex, Long.MAX_VALUE)
					} else if (switchedOff) {
						LOG.trace("switched off at $columnIndex,$rowIndex, afterglowing until $time")
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
			LOG.trace("Request switch-off at ${signalHandler.executionTime + afterglowDuration * 1_000_000} ns")
			signalHandler.requestActingAfter(this, afterglowDuration * 1_000_000, createActorData(null))
		}
	}

	private fun hasExpired(cellTime: Long?, currentTime: Long): Boolean = cellTime != null && cellTime <= currentTime

	private fun fadeOut(columnIndex: Int, rowIndex: Int) {
		LOG.trace("end afterglowing of $columnIndex,$rowIndex")
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