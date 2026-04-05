package io.antarescircuit.jabbah.graph.model.vertice

import io.antarescircuit.jabbah.execution.SignalHandler
import io.antarescircuit.jabbah.execution.scheduler.Scheduler
import io.antarescircuit.jabbah.execution.speed.SystemSpeedCategory
import io.antarescircuit.jabbah.graph.GraphApplicationContext
import io.antarescircuit.jabbah.graph.model.Vertice
import io.antarescircuit.jabbah.graph.view.GraphView

/**
 * A [Vertice] whose state can be changed by the user during execution.
 *
 * Supports disabling itself between requesting a state change and effectively processing it
 * by calculating its new state. Can also store a single incoming state change while the
 * previous one is still being processed. This is to support implementations like clickable switches,
 * where the user can release the mouse button while the request after pressing the button
 * is still processed.
 *
 * Updating the state of an [InteractableVertice] is also deferred until the state change
 * has been processed by the [Scheduler].
 *
 * Implementations should re-enable themselves at the end of their [VerticeCalculator.calculate] method.
 *
 * @param S the type of signal stored and deferred
 */
interface InteractableVertice<S: Any> : Vertice {

	companion object {
		const val BASE_KEY_INTERACTIVE_PROPAGATION_DELAY = "graph.property.interactivePropagationDelay"
	}

	val enabled: Boolean

	val disabled: Boolean get() = !enabled

	/** The propagation delay to be applied with user interactions such as mouse clicks.*/
	var interactivePropagationDelay: Long

	/**
	 * Determines whether views of this [InteractableVertice] should draw themselves
	 * in a disabled state to indicate to the user that this [InteractableVertice] currently
	 * can't accept input from the user.
	 */
	fun shouldDrawDisabled(context: GraphApplicationContext): Boolean =
		disabled && (inactive || context.isPausing || context.systemSpeedCategory.systemSpeedCategory == SystemSpeedCategory.Explore)
}

abstract class AbstractInteractableVertice<S: Any>(
	calculator: VerticeCalculator<*> = EmptyVerticeCalculator,
	name: String? = null
) : CalculatingVertice(calculator, name), InteractableVertice<S> {

	companion object {

		/**
		 * A flag to globally enable or disable [interactivePropagationDelay] usage.
		 * If disabled, propagation delay 0 is applied. Useful when analysing and fine-tuning graph timing behaviour.
		 */
		var enableInteractivePropagationDelay: Boolean = true
	}

	/**
	 * Holds the current signal that determines the state of this [AbstractInteractableVertice].
	 * Changes to this property are delayed by storing the new value in [delayedSignal] until
	 * the state change has been processed by the [Scheduler].
	 */
	open var signal: S? = null
		protected set

	/** Captures a state change to delay it until propagation delay is over.*/
	private var delayedSignal: S? = null

	/** Buffers a state change while a previous state change has not yet been processed.*/
	private var bufferedSignal: S? = null

	private var _enabled: Boolean = true

	/** ---- [InteractableVertice] interface */

	override val enabled: Boolean get() = _enabled

	/** ---- [AbstractInteractableVertice] */

	fun setInteractionEnabled(enabled: Boolean, signalHandler: SignalHandler) {
		if (_enabled != enabled) {
			_enabled = enabled
			stateChanged(signalHandler)
		}
	}

	protected fun setSignal(signal: S?, signalHandler: SignalHandler?) {
		this.signal = signal
		stateChanged(signalHandler)
		bufferedSignal = null
	}

	protected fun resetSignal(signal: S?, signalHandler: SignalHandler) {
		this.signal = signal
		stateChanged(signalHandler)
		bufferedSignal = null
		delayedSignal = null
	}

	fun bufferSignal(signal: S, signalHandler: SignalHandler) {
		bufferedSignal = signal
		requestSetSignal(signal, signalHandler)
	}

	protected open fun requestSetSignal(signal: S, signalHandler: SignalHandler) {
		if (enableInteractivePropagationDelay) {
			requestSetSignalAfter(signal, signalHandler, interactivePropagationDelay)
		} else {
			requestSetSignalAfter(signal, signalHandler, 0)
		}
	}

	protected open fun requestSetSignalAfter(signal: S, signalHandler: SignalHandler, delay: Long) {
		delayedSignal = signal
		setInteractionEnabled(false, signalHandler)
		requestActingAfter(signalHandler, delay, createActorData(null, signal = signal))
	}

	private fun completeSetState(signalHandler: SignalHandler) {
		if (delayedSignal != null) {
			setSignal(delayedSignal, signalHandler)
		}
		setInteractionEnabled(true, signalHandler)
	}

	protected fun calculate(signalHandler: SignalHandler) {
		if (bufferedSignal != null) {
			requestSetSignal(bufferedSignal!!, signalHandler)
			bufferedSignal = null
		} else {
			completeSetState(signalHandler)
		}
	}
}