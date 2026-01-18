package ch.scorpion.antares.model.output

import ch.scorpion.antares.model.port.DigitalPortImpl
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.edit.model.text.Translation
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.model.PortType

/**
 * Represents the supported [Port] schemes of [SevenSegmentDisplay]s.
 */
enum class SevenSegmentDisplayScheme(val customName: String) {

	SINGLE("single") {
		override fun createPorts(display: SevenSegmentDisplay) {
			display.addPort(DigitalPortImpl.createInput("a"))
			display.addPort(DigitalPortImpl.createInput("b"))
			display.addPort(DigitalPortImpl.createInput("c"))
			display.addPort(DigitalPortImpl.createInput("d"))
			display.addPort(DigitalPortImpl.createInput("e"))
			display.addPort(DigitalPortImpl.createInput("f"))
			display.addPort(DigitalPortImpl.createInput("g"))
			display.addPort(DigitalPortImpl.createInput("p"))
		}

		override fun inputValueOf(display: SevenSegmentDisplay, bitName: String): Boolean {
			val port = display.getInput<DigitalSignal>(bitName)
			return port.getIncomingSignal()!!.bitAt(0).isSet
		}
	},

	COMBINED("combined") {
		override fun createPorts(display: SevenSegmentDisplay) {
			display.addPort(DigitalPortImpl(portType = PortType.INPUT, name = "s", bitWidth = BitWidth.BW_8, description = SEGMENTS_PORT_DESC))
			display.addPort(DigitalPortImpl(portType = PortType.INPUT, name = "p", description = POINT_PORT_DESC))
		}

		override fun inputValueOf(display: SevenSegmentDisplay, bitName: String): Boolean {
			if (bitName == "p") {
				val port = display.getInput<DigitalSignal>(bitName)
				return port.getIncomingSignal()!!.bitAt(0).isSet
			}
			val port = display.getInput<DigitalSignal>("s")
			val signal = port.getIncomingSignal()!!
			val bitIndex = bitName[0].code - 'a'.code
			return if (bitIndex >= 0 && bitIndex < signal.bits.size) {
				signal.bitAt(bitIndex).isSet
			} else {
				false
			}
		}
	};

	companion object {

		private val SEGMENTS_PORT_DESC = TranslatableText(Translation.ofStaticKey("antares.sevenSegment.segmentsPort.desc"))
		private val POINT_PORT_DESC = TranslatableText(Translation.ofStaticKey("antares.sevenSegment.pointPort.desc"))

		fun withName(customName: String): SevenSegmentDisplayScheme {
			for (s in values()) {
				if (s.customName == customName) {
					return s
				}
			}
			throw IllegalArgumentException("Unknown SevenSegmentDisplayScheme '$customName'")
		}
	}

	/** Creates the necessary [Port] for and adds them to the specified [SevenSegmentDisplay]. */
	abstract fun createPorts(display: SevenSegmentDisplay)

	/**
	 * Returns the input value of the bit with the specified name.
	 * @return the value of the bit with name `bitName`
	 */
	abstract fun inputValueOf(display: SevenSegmentDisplay, bitName: String): Boolean

	override fun toString(): String {
		return when (this) {
			SINGLE -> Translations.getString("element.property.SevenSegmentDisplayScheme.single")
			COMBINED -> Translations.getString("element.property.SevenSegmentDisplayScheme.combined")
		}
	}
}
