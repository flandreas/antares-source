package ch.scorpion.antares.model.input

import ch.scorpion.antares.model.Trigger
import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.antares.model.port.DigitalPortImpl
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.Word
import ch.scorpion.jabbah.base.checkArgument
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.edit.model.text.Translation
import ch.scorpion.jabbah.edit.model.text.description.DescribableImpl
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.GraphActorData
import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.graph.model.vertice.CalculatingVertice
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

typealias TerminalRow = MutableList<Char>

/** A simple, output-only terminal that displays characters.*/
class Terminal(
	rowsCount: Int = DEFAULT_ROWS_COUNT,
	columnsCount: Int = DEFAULT_COLUMNS_COUNT
) : CalculatingVertice("library.element.Terminal", TerminalCalculator()) {

	companion object {
		private val LOG by logger(Keyboard::class)

		private const val DEFAULT_ROWS_COUNT = 24
		private const val DEFAULT_COLUMNS_COUNT = 40

		private const val CLOCK_PORT_NAME = "CLK"
		private const val CLEAR_PORT_NAME = "CLR"
		private const val WRITE_ENABLE_PORT_NAME = "EN"
		private const val DATA_PORT_NAME = "D"

		private const val BACKSPACE = 8.toChar()
		private const val LINEFEED = 10.toChar()

		private val CLOCK_PORT_DESC = DescribableImpl(Translation.ofStaticKey("antares.terminal.clockPort.desc"))
		private val CLEAR_PORT_DESC = DescribableImpl(Translation.ofStaticKey("antares.terminal.clearPort.desc"))
		private val ENABLE_PORT_DESC = DescribableImpl(Translation.ofStaticKey("antares.terminal.enablePort.desc"))
		private val DATA_PORT_DESC = DescribableImpl(Translation.ofStaticKey("antares.terminal.dataPort.desc"))
	}

	var rowsCount: Int = rowsCount
		set(value) {
			checkArgument(value >= 1)
			if (field != value) {
				field = value
				stateChanged()
			}
		}

	var columnsCount: Int = columnsCount
		set(value) {
			checkArgument(value >= 1)
			if (field != value) {
				field = value
				stateChanged()
			}
		}

	private val displayedRows: MutableList<TerminalRow> = mutableListOf()

	val clockInput: DigitalPort get() = getPort<DigitalSignal>(CLOCK_PORT_NAME) as DigitalPort

	val clearInput: DigitalPort get() = getPort<DigitalSignal>(CLEAR_PORT_NAME) as DigitalPort

	val writeEnableInput: DigitalPort get() = getPort<DigitalSignal>(WRITE_ENABLE_PORT_NAME) as DigitalPort

	val dataInput: DigitalPort get() = getPort<DigitalSignal>(DATA_PORT_NAME) as DigitalPort

	val displayedRowsCount: Int get() = displayedRows.size

	private val isWriteEnabled: Boolean get() = writeEnableInput.getIncomingSignal() == Word.of(true)

	init {
		addPort(DigitalPortImpl(portType = PortType.INPUT, name = CLOCK_PORT_NAME, trigger = Trigger.EDGE, describable = CLOCK_PORT_DESC))
		addPort(DigitalPortImpl(portType = PortType.INPUT, name = CLEAR_PORT_NAME, describable = CLEAR_PORT_DESC))
		addPort(DigitalPortImpl(portType = PortType.INPUT, name = WRITE_ENABLE_PORT_NAME, describable = ENABLE_PORT_DESC))
		addPort(DigitalPortImpl(portType = PortType.INPUT, name = DATA_PORT_NAME, bitWidth = BitWidth.BW_8, describable = DATA_PORT_DESC))

		propagationDelay = 1
	}

	/** ---- [Storable] */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		writer.writeInt("rowsCount", rowsCount)
		writer.writeInt("columnsCount", columnsCount)
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		rowsCount = reader.readInt("rowsCount")
		columnsCount = reader.readInt("columnsCount")
	}

	override fun executionStarted(signalHandler: SignalHandler) {
		super.executionStarted(signalHandler)
		clear(signalHandler)
	}

	override fun executionStopped(signalHandler: SignalHandler) {
		super.executionStopped(signalHandler)
		clear(signalHandler)
	}

	/** [Terminal] */

	fun getRow(index: Int): TerminalRow = displayedRows[index]

	private fun consumeDataInput() {
		if (isWriteEnabled) {
			(dataInput.getIncomingSignal() as DigitalSignal).toInt()?.let {
				addCharacter(it.toChar())
			}
		}
	}

	private fun addCharacter(character: Char) {
		when(character) {
			BACKSPACE -> backspace()
			LINEFEED -> linefeed()
			else -> add(character)
		}

	}

	private fun linefeed() {
		if (displayedRows.size == rowsCount) {
			displayedRows.removeAt(0)
		}
		displayedRows.add(mutableListOf())
	}

	private fun backspace() {
		if (displayedRowsCount > 0 && displayedRows.last().size > 0) {
			displayedRows.last().removeAt(displayedRows.last().lastIndex)
		}
	}

	private fun add(character: Char) {
		if (displayedRows.isEmpty() || displayedRows.last().size == columnsCount) {
			linefeed()
		}
		displayedRows.last().add(character)
	}

	private fun clear(signalHandler: SignalHandler? = null) {
		displayedRows.clear()
		stateChanged(signalHandler)
	}

	private class TerminalCalculator : VerticeCalculator<Terminal> {
		override fun calculate(vertice: Terminal, data: GraphActorData, signalHandler: SignalHandler) {
			when (data.changedPort) {
				vertice.clockInput -> {
					if (vertice.clockInput.getIncomingSignal() == Word.of(true)) {
						vertice.consumeDataInput()
					}
				}
				vertice.clearInput -> {
					if (vertice.clearInput.getIncomingSignal() == Word.of(true)) {
						vertice.clear(signalHandler)
					}
				}
			}
		}
	}
}