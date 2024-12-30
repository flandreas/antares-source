package ch.scorpion.antares.view.input

import ch.scorpion.antares.model.input.AbstractSwitch
import ch.scorpion.antares.view.Look.SCALE
import ch.scorpion.antares.view.OrientableRectangularVerticeView
import ch.scorpion.antares.view.port.AbstractAntaresPortView
import ch.scorpion.antares.view.style.AntaresTheme
import ch.scorpion.jabbah.base.event.Button
import ch.scorpion.jabbah.base.event.KeyEvent
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.Focusable
import ch.scorpion.jabbah.draw.graphics.LineCap
import ch.scorpion.jabbah.draw.graphics.LineJoin
import ch.scorpion.jabbah.draw.graphics.Stroke
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.execution.actor.ActorInteractionContext
import ch.scorpion.jabbah.execution.actor.ActorInteractionHandler
import ch.scorpion.jabbah.execution.actor.ActorView
import ch.scorpion.jabbah.execution.actor.ClickableActorInteractionHandlerAdapter
import ch.scorpion.jabbah.graph.GraphApplicationContext
import ch.scorpion.jabbah.graph.model.GraphElementEvent
import ch.scorpion.jabbah.graph.view.AbstractGraphElementView
import ch.scorpion.jabbah.graph.view.VerticeView
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter
import kotlin.jvm.JvmStatic

abstract class AbstractSwitchView<T : AbstractSwitch<T>>(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	model: T,
) : OrientableRectangularVerticeView<T>(styleProvider, model) {

	companion object {
		const val DEF_CIRCLE_RADIUS = 2.0
		private val PUSH_DASHED_STROKE = Stroke(1f, LineCap.BUTT, LineJoin.MITER, 1.0f, floatArrayOf(2.0f, 2.0f), 0.0f)

		@JvmStatic
		protected val REAL_SWITCH_WIDTH = 6 * SCALE

		@JvmStatic
		protected val REAL_SWITCH_HEIGHT_ABOVE = 4 * SCALE

		@JvmStatic
		protected val REAL_SWITCH_HEIGHT_BELOW = 1 * SCALE

		fun drawTwoPortRealSwitchShape(
			verticeView: VerticeView<*>,
			portBase: Int,
			isOn: Boolean,
			context: DrawContext,
			minX: Double,
			circleRadius: Double,
			drawHandle: Boolean = true
		) {
			// Push annotation
			context.g.stroke = Themes.get<AntaresTheme>().figure.stroke
			context.g.color = context.chooseForeground(verticeView.foregroundColor)
			if (isOn) {
				if (drawHandle) {
					context.g.drawLine(minX + w(1.75), h(-2.0), minX + w(4.25), h(-2.0))
				}
				context.g.stroke = PUSH_DASHED_STROKE
				context.g.drawLine(minX + w(3), h(-1.0), minX + w(3), 0.0)
			} else {
				if (drawHandle) {
					context.g.drawLine(minX + w(1.75), h(-3.5), minX + w(4.25), h(-3.5))
				}
				context.g.stroke = PUSH_DASHED_STROKE
				context.g.drawLine(minX + w(3), h(-3.5), minX + w(3), h(-1.25))
			}

			// Side of port 1
			(verticeView.getPortView(verticeView.model.getPort(portBase)) as AbstractAntaresPortView).prepareConnectionDrawContext(context)
			context.g.drawLine(minX, 0.0, minX + w(1.0), 0.0)
			if (isOn) {
				context.g.drawLine(minX + w(1.0), 0.0, minX + REAL_SWITCH_WIDTH - w(1.0), 0.0)
			} else {
				context.g.drawLine(minX + w(1.0), 0.0, minX + REAL_SWITCH_WIDTH - w(1.5), h(-2.0))
			}
			context.g.fillCircle(minX + w(1.0), 0.0, circleRadius)

			// Side of port 2
			(verticeView.getPortView(verticeView.model.getPort(portBase + 1)) as AbstractAntaresPortView).prepareConnectionDrawContext(context)
			context.g.drawLine(minX + REAL_SWITCH_WIDTH - w(1.0), 0.0, minX + REAL_SWITCH_WIDTH, 0.0)
			context.g.fillCircle(minX + REAL_SWITCH_WIDTH - w(1.0), 0.0, circleRadius)

			// Focus
			if (context.castedAppContext<GraphApplicationContext>()!!.isExecute) {
				verticeView.drawFocus(context)
			}
		}

		fun drawThreePortRealSwitchShape(
			verticeView: VerticeView<*>,
			portBase: Int,
			isOn: Boolean,
			context: DrawContext,
			minX: Double,
			circleRadius: Double,
			drawHandle: Boolean = true
		) {
			val maxX = minX + REAL_SWITCH_WIDTH

			// Push annotation
			context.g.stroke = Themes.get<AntaresTheme>().figure.stroke
			context.g.color = context.chooseForeground(verticeView.foregroundColor)
			if (isOn) {
				if (drawHandle) {
					context.g.drawLine(minX + w(1.75), h(-3.0), minX + w(4.25), h(-3.0))
				}
				context.g.stroke = PUSH_DASHED_STROKE
				context.g.drawLine(minX + w(3), h(-3.0), minX + w(3), h(-1.0))
			} else {
				if (drawHandle) {
					context.g.drawLine(minX + w(1.75), h(-1.0), minX + w(4.25), h(-1.0))
				}
				context.g.stroke = PUSH_DASHED_STROKE
				context.g.drawLine(minX + w(3), h(-1.0), minX + w(3), h(1.0))
			}

			// Port 1
			(verticeView.getPortView(verticeView.model.getPort(portBase)) as AbstractAntaresPortView).prepareConnectionDrawContext(context)
			context.g.drawLine(minX, 0.0, minX + 1 * SCALE, 0.0)
			if (isOn) {
				context.g.drawLine(minX + 1 * SCALE, 0.0, maxX - 1 * SCALE, -2.0 * SCALE)
			} else {
				context.g.drawLine(minX + 1 * SCALE, 0.0, maxX - 1 * SCALE, 2.0 * SCALE)
			}
			context.g.fillCircle(minX + 1 * SCALE, 0.0, circleRadius)

			// Port 2
			(verticeView.getPortView(verticeView.model.getPort(portBase + 1)) as AbstractAntaresPortView).prepareConnectionDrawContext(context)
			context.g.drawLine(maxX - 1 * SCALE, -2.0 * SCALE, maxX, -2.0 * SCALE)
			context.g.fillCircle(maxX - 1 * SCALE, -2.0 * SCALE, circleRadius)

			// Port 3
			(verticeView.getPortView(verticeView.model.getPort(portBase + 2)) as AbstractAntaresPortView).prepareConnectionDrawContext(context)
			context.g.drawLine(maxX - 1 * SCALE, 2.0 * SCALE, minX + REAL_SWITCH_WIDTH,2.0 * SCALE)
			context.g.fillCircle(maxX - 1 * SCALE, 2.0 * SCALE, circleRadius)

			if (context.castedAppContext<GraphApplicationContext>()!!.isExecute) {
				verticeView.drawFocus(context)
			}
		}
	}

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

	override fun getActorInteractionHandler(context: ActorInteractionContext): ActorInteractionHandler =
		actorInteractionHandler

	/** ---- [AbstractGraphElementView] */

	override fun handleStateChanged(event: GraphElementEvent) {
		invalidate()
		if (event.signalHandler == null) {
			updateLabels()
		}
		super.handleStateChanged(event)
	}

	/** ---- [Storable] interface */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		if (!toggle) {
			writer.writeBoolean("toggle", toggle)
		}
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		if (reader.hasAttribute("toggle")) {
			toggle = reader.readBoolean("toggle")
		}
	}

	override fun canConsume(keyEvent: KeyEvent): Boolean =
		actorInteractionHandler.canConsume(keyEvent)

	/** ---- [Focusable] interface */

	/** ---- [AbstractSwitchView] */

	/**
	 * Called by this [AbstractSwitchView] to ask subclasses to update the content and geometry
	 * of their label in edit mode.
	 */
	protected abstract fun updateLabels()

	protected open val circleRadius: Double get() = DEF_CIRCLE_RADIUS

	override fun drawFocus(context: DrawContext) {
		if (isFocusOwner) {
			context.g.color = transparent.applyTo(Themes.get<AntaresTheme>().focus.color.foregroundColor)
			context.g.stroke = Themes.get<AntaresTheme>().focus.stroke
			context.g.draw(bounds)
		}
	}

	protected fun drawTwoPortRealSwitchShape(context: DrawContext) {
		drawTwoPortRealSwitchShape(this, 1, model.isOn, context, bounds.minX, circleRadius)
	}

	protected fun drawThreePortRealSwitchShape(context: DrawContext) {
		Companion.drawThreePortRealSwitchShape(this, 1, model.isOn, context, bounds.minX, circleRadius)
	}

	private inner class InteractionHandler : ClickableActorInteractionHandlerAdapter() {

		private var keyDown = false

		override fun mousePressed(context: ActorInteractionContext): ActorInteractionHandler? {
			if (context.mouseEvent?.button != Button.BUTTON1) {
				return null
			}
			model.toggle(context.signalHandler)
			context.mouseEvent?.consumeEvent()
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
				} else {
					model.bufferSignal(false, context.signalHandler)
				}
				context.mouseEvent?.consumeEvent()
			}
			return null
		}

		override fun mouseClicked(context: ActorInteractionContext): ActorInteractionHandler? {
			if (context.mouseEvent?.button != Button.BUTTON1) {
				return null
			}
			context.mouseEvent?.consumeEvent()
			return this
		}

		fun canConsume(keyEvent: KeyEvent): Boolean {
			if (name != null) {
				if (name!!.length == 1 && name!![0].code == keyEvent.key) {
					return true
				}
			}
			return when (keyEvent.key) {
				'0'.code, '1'.code, '\n'.code -> true
				else -> false
			}
		}

		override fun keyPressed(context: ActorInteractionContext): ActorInteractionHandler? {
			if (!keyDown) {
				name?.let {
					if (it.length == 1 && it[0].code == context.keyEvent?.key) {
						toggle(context)
						keyDown = true
						return null
					}
				}
				if (isFocusOwner) {
					when (context.keyEvent?.key) {
						'0'.code -> switchOff(context)
						'1'.code -> switchOn(context)
						'\n'.code -> toggle(context)
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
						if (it.length == 1 && it[0].code == context.keyEvent?.key) {
							switchOff(context)
							keyDown = false
							return null
						}
					}
					if (isFocusOwner) {
						when (context.keyEvent?.key) {
							'1'.code -> switchOff(context)
							'\n'.code -> switchOff(context)
						}
					}
				}
			}
			keyDown = false
			return null
		}

		private fun switchOn(context: ActorInteractionContext) {
			model.on(context.signalHandler)
			context.keyEvent?.consumeEvent()
		}

		private fun switchOff(context: ActorInteractionContext) {
			model.off(context.signalHandler)
			context.keyEvent?.consumeEvent()
		}

		private fun toggle(context: ActorInteractionContext) {
			model.toggle(context.signalHandler)
			context.keyEvent?.consumeEvent()
		}
	}
}