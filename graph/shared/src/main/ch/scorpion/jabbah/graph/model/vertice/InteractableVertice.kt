package ch.scorpion.jabbah.graph.model.vertice

import ch.scorpion.jabbah.execution.scheduler.Scheduler
import ch.scorpion.jabbah.graph.model.Vertice

/**
 * A [Vertice] whose state can be changed by the user during execution.
 *
 * It is disabled between changing its state and re-calculation initiated by the [Scheduler]. Subclasses should
 * re-enable themselves at the end of their [VerticeCalculator.calculate] method.
 */
abstract class InteractableVertice(
	baseResourceKey: String,
	calculator: VerticeCalculator<*> = EmptyVerticeCalculator,
	name: String? = null
) : CalculatingVertice(baseResourceKey, calculator, name) {

	var enabled: Boolean = true
		protected set(value) {
			if (field != value) {
				field = value
				stateChanged()
			}
		}

}