package ch.scorpion.antares.model.net

import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.Word
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.OutputPort
import ch.scorpion.jabbah.graph.model.net.CombinedNetAccess

/**
 * A [CombinedNetAccess] that can access subword of [DigitalSignal]s. Needed as a consequence of bus splitting.
 */
class DigitalCombinedNetAccess(
	port: OutputPort<DigitalSignal>,
	val width: BitWidth,
	val index: Int
) : CombinedNetAccess<DigitalSignal>(port) {

	override val assertedSignal: DigitalSignal?
		get() = port.getOutgoingSignal()?.getSubword(width, index)

	override fun isConsistentWith(signal: DigitalSignal?): Boolean =
		port.isOutputFullyUndefined || assertedSignal?.isConsistentWith(signal) ?: false

	override val isFullyUndefined: Boolean get() = assertedSignal?.isFullyUndefined ?: true

	override val isPartiallyUndefined: Boolean get() = assertedSignal?.isPartiallyUndefined ?: true

	override fun replaceUndefinedFrom(origin: DigitalSignal?, replacement: DigitalSignal?, signalHandler: SignalHandler): DigitalSignal? {
		var signal = origin
		if (replacement != null) {
			signal = origin?.defineSubword(replacement, index)
				?: Word.undefined((port as DigitalPort).bitWidth).defineSubword(replacement, index)
		}
		return signal
	}

	override fun withOutputPort(newPort: OutputPort<DigitalSignal>): DigitalCombinedNetAccess =
		DigitalCombinedNetAccess(newPort, width, index)

	/**
	 * Creates a new [DigitalCombinedNetAccess] that attaches [other] (with smaller [BitWidth])
	 * to the specified [OutputPort]. Used by splitters for repetitive reduction of the size of the
	 * accessed signal.
	 */
	fun attach(newPort: OutputPort<DigitalSignal>, other: DigitalCombinedNetAccess): DigitalCombinedNetAccess {
		val factor = width.width / other.width.width
		val attachIndex = index * factor + other.index
		return DigitalCombinedNetAccess(newPort, other.width, attachIndex)
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (other == null || this::class != other::class) return false

		other as DigitalCombinedNetAccess

		if (width != other.width) return false
		if (index != other.index) return false
		if (port !== other.port) return false

		return true
	}

	override fun hashCode(): Int {
		var result = port.hashCode()
		result = 31 * result + width.hashCode()
		result = 31 * result + index
		return result
	}
}