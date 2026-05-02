package io.antarescircuit.antares.model.net

import io.antarescircuit.antares.model.port.DigitalPort
import io.antarescircuit.antares.model.signal.BitWidth
import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.antares.model.signal.DigitalSignalFactory
import io.antarescircuit.jabbah.execution.SignalHandler
import io.antarescircuit.jabbah.graph.model.GraphExecutionContext
import io.antarescircuit.jabbah.graph.model.OutputPort
import io.antarescircuit.jabbah.graph.model.net.CombinedNetAccess

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

	override fun isConsistentWith(signal: DigitalSignal?, signalHandler: SignalHandler): Boolean {
		if (port.isOutputFullyUndefined) {
			return true
		}
		if (signalHandler.executionContext is GraphExecutionContext<*>) {
			@Suppress("UNCHECKED_CAST")
			return (signalHandler.executionContext as GraphExecutionContext<DigitalSignal>)
				.netSignalApplier.signalsAreConsistent(assertedSignal, signal)
		}
		return false
	}

	override val isFullyUndefined: Boolean get() = assertedSignal?.isFullyUndefined ?: true

	override val isPartiallyUndefined: Boolean get() = assertedSignal?.isPartiallyUndefined ?: true

	override fun replaceUndefinedFrom(origin: DigitalSignal?, replacement: DigitalSignal?, signalHandler: SignalHandler): DigitalSignal? {
		var signal = origin
		if (replacement != null) {
			signal = origin?.defineSubword(replacement, index)
				?: DigitalSignalFactory.undefined((port as DigitalPort).bitWidth).defineSubword(replacement, index)
			port.owner?.replaceUndefinedOutput(signal)
		}
		return signal
	}

	override fun withOutputPort(newPort: OutputPort<DigitalSignal>): DigitalCombinedNetAccess =
		DigitalCombinedNetAccess(newPort, width, index)

	fun contains(other: DigitalCombinedNetAccess): Boolean {
		return index * width.width <= other.index * other.width.width
			&& (index + 1) * width.width >= (other.index + 1) * other.width.width
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