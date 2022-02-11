package ch.scorpion.antares.model.port

import ch.scorpion.antares.model.Logic
import ch.scorpion.antares.model.OutputAnnotation
import ch.scorpion.antares.model.Trigger
import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.DigitalSignalRepresentation
import ch.scorpion.jabbah.graph.model.*
import ch.scorpion.jabbah.graph.model.oscilloscope.OscilloscopeProbeVertice

/**
 * Represents a [Port] that produces or consumes [DigitalSignal]s.
 */
interface DigitalPort : BidirectionalPort<DigitalSignal> {

	companion object {
		const val PROP_LOGIC = "logic"
		const val PROP_BIT_WIDTH = "bitWidth"
		const val PROP_SIGNAL_REPRESENTATION = "signalRepresentation"
		const val PROP_TRIGGER = "trigger"
		const val PROP_OUTPUT_ANNOTATION = "outputAnnotation"
		const val PROP_TRI_STATE_OUTPUT = "triStateOutput"
	}

	/** Holds the type of [Logic] of this [DigitalPort].*/
	var logic: Logic

	/** Holds the number of parallel [Bit]s this [DigitalPort] supports.*/
	var bitWidth: BitWidth

	var trigger: Trigger

	var signalRepresentation: DigitalSignalRepresentation

	var outputAnnotation: OutputAnnotation

	var triStateOutput: Boolean

	val defaultDigitalSignal: DigitalSignal

	/**
	 * Determines whether this [DigitalPort] can accept or produce [DigitalSignal]s of any
	 * [BitWidth]. For an adaptive [DigitalPort], the property [bitWidth] is irrelevant.
	 * This property is typically determined at creation time and not changed during the lifetime of a [DigitalPort].
	 * Most [DigitalPort] implementations are not adaptive. One known usage of adaptive [DigitalPort]s
	 * is in [OscilloscopeProbeVertice], which must probe signals of any [BitWidth].
	 */
	var isAdaptive: Boolean

	override fun canConnectToNet(net: Net<*>): Boolean =
		triStateOutput || super.canConnectToNet(net)
}