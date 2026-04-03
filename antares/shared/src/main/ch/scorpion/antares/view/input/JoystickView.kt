package ch.scorpion.antares.view.input

import ch.scorpion.antares.model.input.Joystick
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.view.port.AbstractAntaresPortView
import ch.scorpion.antares.view.port.DigitalPortView
import ch.scorpion.antares.view.style.AntaresTheme
import ch.scorpion.jabbah.base.event.Button
import ch.scorpion.jabbah.base.event.KeyEvent
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.geom.RectangularShape
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.Focusable
import ch.scorpion.jabbah.draw.InputEventHandler
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.DropShadow
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.edit.Look.SCALE
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.actor.ActorInteractionContext
import ch.scorpion.jabbah.execution.actor.ActorInteractionHandler
import ch.scorpion.jabbah.execution.actor.ActorView
import ch.scorpion.jabbah.execution.actor.ClickableActorInteractionHandlerAdapter
import ch.scorpion.jabbah.graph.GraphApplicationContext
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.model.vertice.VerticeLink
import ch.scorpion.jabbah.graph.view.ControlView
import ch.scorpion.jabbah.graph.view.ControlViewSource
import ch.scorpion.jabbah.graph.view.LabeledRectangularVerticeView
import ch.scorpion.jabbah.graph.view.port.PortLabelPosition
import ch.scorpion.jabbah.graph.view.vertice.AbstractVerticeView
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter
import kotlin.math.abs

class JoystickView(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	model: Joystick = Joystick()
) : LabeledRectangularVerticeView<Joystick>(styleProvider, model), ControlView<Joystick>, ControlViewSource<Joystick> {

	companion object {
		const val PROP_ICON_PATH = "ch.scorpion.antares.view.input.JoystickView.iconPath"
		const val SIZE = 8
		const val KNOB_RADIUS = 1.0 * SCALE
		const val MAX_DISPLACEMENT = 2.0 * SCALE
		private const val FOCUS_INSET = 3
	}

	/** The position of the knob relative to the center of the rectangle.*/
	private var knobPosition: Point2D = Point2D.ZERO
		set(value) {
			field = value
			invalidate()
			validate()
		}

	/** Handles mouse interactions during execution*/
	private val actorInteractionHandler = InteractionHandler()

	init {
		initExternalLabel(orientation = Direction.WEST)
		modelExchanged(null)
		setBounds(calculateBounds())
	}

	override val relativeExternalLabelLocation: Point2D get() =
		Point2D(-AbstractAntaresPortView.LENGTH - w(SIZE) - LABEL_DIST, -h(2) + w(SIZE) / 2)

	override fun modelExchanged(oldModel: Joystick?) {
		super.modelExchanged(oldModel)
		val pvX = DigitalPortView(
			styleProvider,
			model.getOutput(Joystick.PORT_NAME_X),
			direction = Direction.EAST,
			portLabelPosition = PortLabelPosition.EXTERNAL,
			showBitWidthAnnotation = false)
		pvX.location = Point2D(-AbstractAntaresPortView.LENGTH, 0)
		addPortView(pvX)
		val pvY = DigitalPortView(styleProvider,
			model.getOutput(Joystick.PORT_NAME_Y),
			direction = Direction.EAST,
			portLabelPosition = PortLabelPosition.EXTERNAL,
			showBitWidthAnnotation = false)
		pvY.location = Point2D(-AbstractAntaresPortView.LENGTH.toDouble(), h(4))
		addPortView(pvY)
	}

	/** ---- UI properties */

	var bitWidth: BitWidth
		get() = model.bitWidth
		set(value) {
			invalidate()
			model.bitWidth = value
			invalidate()
			update()
			validate()
		}

	var deflection: JoystickDeflection = JoystickDeflection.RECTANGULAR
		set(value) {
			if (field != value) {
				invalidate()
				field = value
				validate()
			}
		}

	/** ---- [Focusable] interface */

	override var isFocusable: Boolean = true

	/** ---- [AbstractVerticeView] */

	override val useRotation: Boolean get() = false

	override val useOrientation: Boolean get() = false

	override fun drawImpl(context: DrawContext) {
		super.drawImpl(context)

		if (shadow) {
			DropShadow.draw(context, transparency) {
				context.g.fillRect(xInt, yInt, widthInt, heightInt)
			}
		}
		drawBackground(context, transparent.applyTo(if (context.useContextColors) context.chooseBackground(backgroundColor) else propertiesBackgroundColor))
		deflection.drawDeflection(this, context, transparent.applyTo(context.chooseForeground(foregroundColor)))
		drawContent(context)
		drawBorder(context, transparent.applyTo(context.chooseForeground(foregroundColor)))

		if (context.castedAppContext<GraphApplicationContext>()!!.isExecute) {
			drawFocus(context)
		}
	}

	private fun drawBackground(context: DrawContext, color: Color) {
		context.g.color = color
		context.g.fillRect(xInt, yInt, widthInt, heightInt)
	}

	private fun drawContent(context: DrawContext) {
		drawKnob(context)
	}

	private fun drawBorder(context: DrawContext, color: Color) {
		context.g.color = color
		context.g.stroke = stroke
		context.g.drawRect(xInt, yInt, widthInt, heightInt)
	}

	private fun drawKnob(context: DrawContext) {
		context.g.fillCircle(bounds.centerX, bounds.centerY, 0.5 * SCALE)
		context.g.stroke = stroke
		context.g.drawLine(bounds.centerX, bounds.centerY, bounds.centerX + knobPosition.x, bounds.centerY + knobPosition.y)

		context.g.color = transparent.applyTo(context.chooseForeground(foregroundColor))
		context.g.fillCircle(bounds.centerX + knobPosition.x, bounds.centerY + knobPosition.y, KNOB_RADIUS)
	}

	override fun drawFocus(context: DrawContext) {
		if (isFocusOwner) {
			context.g.color = transparent.applyTo(Themes.get<AntaresTheme>().focus.color.foregroundColor)
			context.g.stroke = Themes.get<AntaresTheme>().focus.stroke
			context.g.drawRect(bounds.x + FOCUS_INSET, bounds.y + FOCUS_INSET, bounds.width - 2 * FOCUS_INSET, bounds.height - 2 * FOCUS_INSET)
		}
	}

	/** ---- [Storable] interface */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		if (deflection != JoystickDeflection.RECTANGULAR) {
			writer.writeString("deflection", deflection.customName)
		}
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		if (reader.hasAttribute("deflection")) {
			deflection = JoystickDeflection.withName(reader.readString("deflection"))
		}
	}

	/** ---- [ActorView] interface */

	override fun getActorInteractionHandler(context: ActorInteractionContext): ActorInteractionHandler {
		return actorInteractionHandler
	}

	/** ---- [ControlViewSource] */

	override val controlId: String
		get() {
			// Don't use GraphElementView#getId() as part of the controlId, because that one might be changed
			// when ControlViews (event as part of a wrapping Component) are added to a Drawing
			return "joystick:" + model.id
		}

	override val controlName: String get() = super.controlName

	override val iconPath: String get() = BaseModule.properties.getString(PROP_ICON_PATH)

	override fun createControlView(): ControlView<Joystick> {
		val clone = JoystickView(styleProvider, model)
		clone.isShowPortViews = false
		clone.location = Point2D.ZERO
		copyControlViewProperties(this, clone)
		return clone
	}

	/** ---- [ControlView] */

	override var isActiveControlView: Boolean = false

	override val mirrorWidth: Double get() = -(2 * AbstractAntaresPortView.LENGTH + width)

	override val mirrorHeight: Double get() = -abs(abs(bounds.maxY) - abs(bounds.minY))

	override fun bindControlView(subGraphVerticeView: SubGraphVerticeView<*>, link: VerticeLink, startGraph: Graph) {
		this.model = link.getLinkedObject(startGraph) as Joystick
	}

	override fun writeModelProperties(writer: StoreWriter) { }

	override fun readModelProperties(reader: StoreReader) { }

	override fun sourcePropertiesChanged(source: ControlViewSource<Joystick>) {
		if (source is JoystickView) {
			copyControlViewProperties(source, this)
		}
	}

	private fun copyControlViewProperties(source: JoystickView, dest: JoystickView) {
		dest.bitWidth = source.bitWidth
		dest.deflection = source.deflection
	}

	/** ----- [JoystickView] */

	private fun calculateBounds(): RectangularShape =
		Rectangle2D(
			- AbstractAntaresPortView.LENGTH.toDouble() - w(SIZE), - h(2),
			w(SIZE), h(SIZE))

	/**
	 * Transforms view location in range -MAX_DISPLACEMENT .. +MAX_DISPLACEMENT to model location
	 * in range -1 .. +1.
	 */
	private fun normalize(location: Point2D): Point2D =
		Point2D(location.x / MAX_DISPLACEMENT, location.y / MAX_DISPLACEMENT)

	private fun setKnobPosition(position: Point2D, signalHandler: SignalHandler) {
		knobPosition = position
		model.setKnobPosition(normalize(position), signalHandler)
	}

	private inner class InteractionHandler : ClickableActorInteractionHandlerAdapter() {

		private var mousePressed = false
		private var leftDown = false
		private var rightDown = false
		private var upDown = false
		private var downDown = false

		override fun mousePressed(context: ActorInteractionContext): InputEventHandler<ActorInteractionContext>? {
			if (context.mouseEvent?.button != Button.BUTTON1) {
				return null
			}
			requestFocus()
			mousePressed = true
			return this
		}

		override fun mouseDragged(context: ActorInteractionContext): InputEventHandler<ActorInteractionContext>? {
			if (!mousePressed) {
				return null
			}
			setKnobPosition(calculateMouseKnobPosition(context.location), context.signalHandler)
			return this
		}

		override fun mouseReleased(context: ActorInteractionContext): InputEventHandler<ActorInteractionContext>? {
			if (context.mouseEvent?.button != Button.BUTTON1) {
				return null
			}
			mousePressed = false
			setKnobPosition(Point2D.ZERO, context.signalHandler)
			return null
		}

		override fun keyPressed(context: ActorInteractionContext): InputEventHandler<ActorInteractionContext>? {
			when (context.keyEvent?.key) {
				KeyEvent.VK_LEFT -> leftDown = true
				KeyEvent.VK_RIGHT -> rightDown = true
				KeyEvent.VK_UP -> upDown = true
				KeyEvent.VK_DOWN -> downDown = true
			}
			setKnobPosition(calculateKeyKnobPosition(), context.signalHandler)
			return null
		}

		override fun keyReleased(context: ActorInteractionContext): InputEventHandler<ActorInteractionContext>? {
			when (context.keyEvent?.key) {
				KeyEvent.VK_LEFT -> leftDown = false
				KeyEvent.VK_RIGHT -> rightDown = false
				KeyEvent.VK_UP -> upDown = false
				KeyEvent.VK_DOWN -> downDown = false
			}
			setKnobPosition(calculateKeyKnobPosition(), context.signalHandler)
			return null
		}

		private fun calculateMouseKnobPosition(mouseLocation: Point2D): Point2D =
			deflection.calculateContinuousKnobPosition(this@JoystickView, mouseLocation)

		private fun calculateKeyKnobPosition(): Point2D =
			deflection.calculateKeyKnobPosition(leftDown, rightDown, upDown, downDown)
	}
}