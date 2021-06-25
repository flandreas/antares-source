package ch.scorpion.antares.view.input

import ch.scorpion.antares.model.input.AbstractSwitch
import ch.scorpion.antares.view.DigitalComponentView
import ch.scorpion.jabbah.base.event.Button
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.execution.actor.ActorInteractionContext
import ch.scorpion.jabbah.execution.actor.ActorInteractionHandler
import ch.scorpion.jabbah.execution.actor.ActorView
import ch.scorpion.jabbah.execution.actor.ClickableActorInteractionHandlerAdapter
import ch.scorpion.jabbah.graph.model.GraphElementEvent
import ch.scorpion.jabbah.graph.view.AbstractGraphElementView

abstract class AbstractSwitchView<T : AbstractSwitch<T>>(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	model: T,
	protected val eventBus: EventBus = BaseModule.eventBus
) : DigitalComponentView<T>(styleProvider, model) {

	/** Handles mouse interactions during execution*/
	private val actorInteractionHandler = InteractionHandler()

	/** ---- UI properties */

	open var name: String?
		get() = model.name
		set(value) {
			if (value != model.name) {
				model.name = value
			}
		}

	/**
	 * Controls the interactive behaviour of this [SwitchView]. If set to `true`, the [AbstractSwitchView]
	 * stays in the new state when the user releases the mouse button. If set to `false`,
	 * the [AbstractSwitchView] returns to 0 state.
	 */
	open var toggle: Boolean = true
		set(value) {
			if (field != value) {
				field = value
			}
		}

	/** ---- [ActorView] interface */

	override fun getActorInteractionHandler(context: ActorInteractionContext): ActorInteractionHandler {
		return actorInteractionHandler
	}

	/** ---- [AbstractGraphElementView] */

	override fun handleStateChanged(event: GraphElementEvent) {
		invalidate()
		updateLabels()
		super.handleStateChanged(event)
	}

	protected abstract fun updateLabels()

	private inner class InteractionHandler : ClickableActorInteractionHandlerAdapter() {

		private var keyDown = false

		override fun mousePressed(context: ActorInteractionContext): ActorInteractionHandler? {
			if (context.mouseEvent?.button != Button.BUTTON1) {
				return null
			}
			model.toggle(context.signalHandler)
			context.mouseEvent?.consume()
			requestFocus()
			return this
		}

		override fun mouseDragged(context: ActorInteractionContext): ActorInteractionHandler {
			return this
		}

		override fun mouseReleased(context: ActorInteractionContext): ActorInteractionHandler? {
			if (context.mouseEvent?.button != Button.BUTTON1) {
				return null
			}
			if (!toggle) {
				if (model.isOn) {
					model.off(context.signalHandler)
					context.mouseEvent?.consume()
				}
			}
			return null
		}

		override fun mouseClicked(context: ActorInteractionContext): ActorInteractionHandler? {
			if (context.mouseEvent?.button != Button.BUTTON1) {
				return null
			}
			context.mouseEvent?.consume()
			return this
		}

		override fun keyPressed(context: ActorInteractionContext): ActorInteractionHandler? {
			if (!keyDown) {
				name?.let {
					if (it.length == 1 && it[0].toInt() == context.keyEvent?.key) {
						toggle(context)
						keyDown = true
						return null
					}
				}
				if (isFocusOwner) {
					when (context.keyEvent?.key) {
						'0'.toInt() -> switchOff(context)
						'1'.toInt() -> switchOn(context)
						'\n'.toInt() -> toggle(context)
					}
				}
				keyDown = true
			}
			return null
		}

		override fun keyReleased(context: ActorInteractionContext): ActorInteractionHandler? {
			if (!toggle) {
				if (keyDown) {
					name?.let {
						if (it.length == 1 && it[0].toInt() == context.keyEvent?.key) {
							switchOff(context)
							keyDown = false
							return null
						}
					}
					if (isFocusOwner) {
						when (context.keyEvent?.key) {
							'1'.toInt() -> switchOff(context)
							'\n'.toInt() -> switchOff(context)
						}
					}
				}
			}
			keyDown = false
			return null
		}

		private fun switchOn(context: ActorInteractionContext) {
			model.on(context.signalHandler)
			context.keyEvent?.consume()
		}

		private fun switchOff(context: ActorInteractionContext) {
			model.off(context.signalHandler)
			context.keyEvent?.consume()
		}

		private fun toggle(context: ActorInteractionContext) {
			model.toggle(context.signalHandler)
			context.keyEvent?.consume()
		}
	}
}