package ch.scorpion.jabbah.graph.model.vertice

import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.scheduler.Scheduler
import ch.scorpion.jabbah.graph.model.Vertice

/**
 * A [Vertice] whose state can be changed by the user during execution.
 *
 * It is disabled between changing its state and re-calculation initiated by the [Scheduler]. Subclasses should
 * re-enable themselves at the end of their [VerticeCalculator.calculate] method.
 */
interface InteractableVertice : Vertice {

	val enabled: Boolean

	val disabled: Boolean get() = !enabled
}

abstract class AbstractInteractableVertice(
	calculator: VerticeCalculator<*> = EmptyVerticeCalculator,
	name: String? = null
) : CalculatingVertice(calculator, name), InteractableVertice {

	private var _enabled: Boolean = true

	override val enabled: Boolean get() = _enabled

	fun setInteractionEnabled(enabled: Boolean, signalHandler: SignalHandler) {
		if (_enabled != enabled) {
			_enabled = enabled
			stateChanged(signalHandler)
		}
	}
}