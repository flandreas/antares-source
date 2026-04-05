package io.antarescircuit.antares.view.input

import io.antarescircuit.antares.model.input.AbstractSwitch
import io.antarescircuit.antares.view.port.AbstractAntaresPortView
import io.antarescircuit.antares.view.style.AntaresTheme
import io.antarescircuit.jabbah.base.event.Button
import io.antarescircuit.jabbah.base.event.KeyEvent
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.Focusable
import io.antarescircuit.jabbah.draw.graphics.LineCap
import io.antarescircuit.jabbah.draw.graphics.LineJoin
import io.antarescircuit.jabbah.draw.graphics.Stroke
import io.antarescircuit.jabbah.draw.style.DrawStyleModule
import io.antarescircuit.jabbah.draw.style.StyleProvider
import io.antarescircuit.jabbah.draw.style.Themes
import io.antarescircuit.jabbah.edit.Look.SCALE
import io.antarescircuit.jabbah.execution.actor.ActorInteractionContext
import io.antarescircuit.jabbah.execution.actor.ActorInteractionHandler
import io.antarescircuit.jabbah.execution.actor.ActorView
import io.antarescircuit.jabbah.execution.actor.ClickableActorInteractionHandlerAdapter
import io.antarescircuit.jabbah.graph.GraphApplicationContext
import io.antarescircuit.jabbah.graph.view.LabeledRectangularVerticeView
import io.antarescircuit.jabbah.graph.view.VerticeView
import io.antarescircuit.jabbah.io.Storable
import io.antarescircuit.jabbah.io.StoreReader
import io.antarescircuit.jabbah.io.StoreWriter
import kotlin.jvm.JvmStatic

abstract class AbstractSwitchView<T : AbstractSwitch<T>>(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	model: T,
	internalLabelText: String? = null
) : LabeledRectangularVerticeView<T>(styleProvider, model, internalLabelText = internalLabelText) {

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
			drawHandle: Boolean = true,
			leftHanded: Boolean = true
		) {
			// Push annotation
			context.g.stroke = Themes.get<AntaresTheme>().figure.stroke
			context.g.color = context.chooseForeground(verticeView.foregroundColor)

			// Movable element
			if (isOn) {
				if (drawHandle) {
					context.g.drawLine(minX + w(1.75), h(-2.0), minX + w(4.25), h(-2.0))
				}
				context.g.stroke = PUSH_DASHED_STROKE
				context.g.drawLine(minX + w(3), h(-2.0), minX + w(3), 0.0)
			} else {
				if (drawHandle) {
					if (leftHanded) {
						context.g.drawLine(minX + w(1.75), h(-3.5), minX + w(4.25), h(-3.5))
					} else {
						context.g.drawLine(minX + w(1.75), h(-1.5), minX + w(4.25), h(-1.5))
					}
				}
				context.g.stroke = PUSH_DASHED_STROKE
				if (leftHanded) {
					context.g.drawLine(minX + w(3), h(-3.5), minX + w(3), h(-1.25))
				} else {
					context.g.drawLine(minX + w(3), h(1.0), minX + w(3), h(-1.25))
				}
			}

			// Side of port 1
			(verticeView.getPortView(verticeView.model.getPort(portBase)) as AbstractAntaresPortView).prepareConnectionDrawContext(context)
			if (!context.castedAppContext<GraphApplicationContext>()!!.showNetState) {
				context.g.color = context.chooseForeground(verticeView.foregroundColor)
			}
			context.g.drawLine(minX, 0.0, minX + w(1.0), 0.0)
			if (isOn) {
				context.g.drawLine(minX + w(1.0), 0.0, minX + REAL_SWITCH_WIDTH - w(1.0), 0.0)
			} else {
				val switchEndY = if (leftHanded) h(-2.0) else h(2.0)
				context.g.drawLine(minX + w(1.0), 0.0, minX + REAL_SWITCH_WIDTH - w(1.5), switchEndY)
			}
			context.g.fillCircle(minX + w(1.0), 0.0, circleRadius)

			// Side of port 2
			(verticeView.getPortView(verticeView.model.getPort(portBase + 1)) as AbstractAntaresPortView).prepareConnectionDrawContext(context)
			if (!context.castedAppContext<GraphApplicationContext>()!!.showNetState) {
				context.g.color = context.chooseForeground(verticeView.foregroundColor)
			}
			context.g.drawLine(minX + REAL_SWITCH_WIDTH - w(1.5), 0.0, minX + REAL_SWITCH_WIDTH, 0.0)
			context.g.fillCircle(minX + REAL_SWITCH_WIDTH - w(1.5), 0.0, circleRadius)

			// Focus
			if (context.castedAppContext<GraphApplicationContext>()!!.isExecute) {
				verticeView.drawFocus(context)
			}
		}

		/**
		 * Input and output are NOT at the same y coordinate. Always right-handed.
		 * Never draw handle.
		 */
		fun drawTwoPortRealSwitchNonColinearShape(
			verticeView: VerticeView<*>,
			portBase: Int,
			isOn: Boolean,
			context: DrawContext,
			minX: Double,
			circleRadius: Double
		) {

			// Push annotation
			context.g.stroke = PUSH_DASHED_STROKE
			context.g.color = context.chooseForeground(verticeView.foregroundColor)
			if (isOn) {
				context.g.drawLine(minX + w(3), h(1.0), minX + w(3), h(-1.25))
			} else {
				context.g.drawLine(minX + w(3), h(-2.0), minX + w(3), 0.0)
			}

			// Side of port 1
			(verticeView.getPortView(verticeView.model.getPort(portBase)) as AbstractAntaresPortView).prepareConnectionDrawContext(context)
			context.g.drawLine(minX, 0.0, minX + w(1.0), 0.0)
			if (isOn) {
				context.g.drawLine(minX + w(1.0), 0.0, minX + REAL_SWITCH_WIDTH - w(1.5), h(2))
			} else {
				context.g.drawLine(minX + w(1.0), 0.0, minX + REAL_SWITCH_WIDTH - w(1), 0.0)
			}
			context.g.fillCircle(minX + w(1.0), 0.0, circleRadius)

			// Side of port 2
			(verticeView.getPortView(verticeView.model.getPort(portBase + 1)) as AbstractAntaresPortView).prepareConnectionDrawContext(context)
			context.g.drawLine(minX + REAL_SWITCH_WIDTH - w(1.5), w(2), minX + REAL_SWITCH_WIDTH, w(2))
			context.g.fillCircle(minX + REAL_SWITCH_WIDTH - w(1.5), w(2), circleRadius)

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

	@Suppress("unused") // Reflection
	var closedOnStart: Boolean
		get() = model.closedOnStart
		set(value) {
			if (value != model.closedOnStart) {
				model.closedOnStart = value
			}
		}

	/** ---- [ActorView] interface */

	override fun getActorInteractionHandler(context: ActorInteractionContext): ActorInteractionHandler =
		actorInteractionHandler

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
		drawThreePortRealSwitchShape(this, 1, model.isOn, context, bounds.minX, circleRadius)
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