package io.antarescircuit.antares.model.input

import io.antarescircuit.jabbah.execution.SignalHandler
import io.antarescircuit.jabbah.execution.actor.Actor
import io.antarescircuit.jabbah.graph.model.GraphActorData
import io.antarescircuit.jabbah.graph.model.vertice.AbstractInteractableVertice
import io.antarescircuit.jabbah.graph.model.vertice.InteractableVertice
import io.antarescircuit.jabbah.graph.model.vertice.VerticeCalculator
import io.antarescircuit.jabbah.io.Storable
import io.antarescircuit.jabbah.io.StoreReader
import io.antarescircuit.jabbah.io.StoreWriter

abstract class AbstractSwitch<T : AbstractSwitch<T>>(
	calculator: VerticeCalculator<T>
) : AbstractInteractableVertice<Boolean>(calculator) {

	companion object {
		open class AbstractSwitchCalculator<T : AbstractSwitch<T>> : VerticeCalculator<T> {
			override fun calculate(vertice: T, data: GraphActorData, signalHandler: SignalHandler) {
				vertice.calculate(signalHandler)
			}
		}
	}

	val isOn: Boolean get() = signal ?: false

	var closedOnStart: Boolean = false

	/** ---- [InteractableVertice] interface */

	override var interactivePropagationDelay: Long = Switch.DEF_PROP_DELAY.value

	/** ---- [Actor] interface */

	override fun executionInitialize(signalHandler: SignalHandler) {
		super.executionInitialize(signalHandler)
		setSignal(closedOnStart, signalHandler)
	}

	override fun executionStart(signalHandler: SignalHandler) {
		super.executionStart(signalHandler)
		requestSetSignal(closedOnStart, signalHandler)
	}

	override fun executionStopped(signalHandler: SignalHandler) {
		super.executionStopped(signalHandler)
		setSignal(false, signalHandler)
		setInteractionEnabled(true, signalHandler)
	}

	/** ---- [AbstractSwitch] */

	open fun toggle(signalHandler: SignalHandler) {
		if (isOn) {
			off(signalHandler)
		} else {
			on(signalHandler)
		}
	}

	open fun on(signalHandler: SignalHandler) {
		if (enabled && !isOn) {
			requestSetSignal(true, signalHandler)
		}
	}

	open fun off(signalHandler: SignalHandler) {
		if (enabled && isOn) {
			requestSetSignal(false, signalHandler)
		}
	}

	protected fun delayedOff(signalHandler: SignalHandler, delayedBy: Long) {
		if (enabled && isOn) {
			requestSetSignalAfter(false, signalHandler, delayedBy)
		}
	}

	/** ---- [Storable] interface */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		if (closedOnStart) {
			writer.writeBoolean("closedOnStart", closedOnStart)
		}
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		if (reader.hasAttribute("closedOnStart")) {
			closedOnStart = reader.readBoolean("closedOnStart")
		}
	}
}