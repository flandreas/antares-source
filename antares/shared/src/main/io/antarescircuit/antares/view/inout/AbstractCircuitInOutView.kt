package io.antarescircuit.antares.view.inout

import io.antarescircuit.antares.model.inout.CircuitInOut
import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.jabbah.edit.Look
import io.antarescircuit.jabbah.base.StringUtils
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.event.KeyEvent
import io.antarescircuit.jabbah.base.geom.Direction
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.geom.Rectangle2D
import io.antarescircuit.jabbah.base.geom.RectangularShape
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.Drawable
import io.antarescircuit.jabbah.draw.Focusable
import io.antarescircuit.jabbah.draw.drawable.Orientable
import io.antarescircuit.jabbah.draw.drawable.RotationDirection
import io.antarescircuit.jabbah.draw.drawable.Locatable
import io.antarescircuit.jabbah.draw.graphics.Color
import io.antarescircuit.jabbah.draw.graphics.DropShadow
import io.antarescircuit.jabbah.draw.graphics.Stroke
import io.antarescircuit.jabbah.draw.style.DrawStyleModule
import io.antarescircuit.jabbah.draw.style.StyleProvider
import io.antarescircuit.jabbah.draw.style.StyleType
import io.antarescircuit.jabbah.draw.drawable.Rotatable
import io.antarescircuit.jabbah.edit.Component
import io.antarescircuit.jabbah.edit.model.ComponentMessage
import io.antarescircuit.jabbah.edit.model.ComponentMessageType
import io.antarescircuit.jabbah.edit.model.text.Alignment
import io.antarescircuit.jabbah.edit.model.text.Label
import io.antarescircuit.jabbah.edit.model.text.Labeled
import io.antarescircuit.jabbah.edit.model.text.description.Description
import io.antarescircuit.jabbah.execution.actor.ActorInteractionContext
import io.antarescircuit.jabbah.execution.actor.ActorInteractionHandler
import io.antarescircuit.jabbah.execution.actor.ActorView
import io.antarescircuit.jabbah.execution.actor.ClickableActorInteractionHandlerAdapter
import io.antarescircuit.jabbah.graph.GraphApplicationContext
import io.antarescircuit.jabbah.graph.model.GraphElementEvent
import io.antarescircuit.jabbah.graph.model.GraphPort
import io.antarescircuit.jabbah.graph.model.PortType
import io.antarescircuit.jabbah.graph.view.AbstractGraphElementView
import io.antarescircuit.jabbah.graph.view.GraphPortView
import io.antarescircuit.jabbah.graph.view.VerticeView
import io.antarescircuit.jabbah.graph.view.port.PortView
import io.antarescircuit.jabbah.graph.view.vertice.AbstractVerticeView
import io.antarescircuit.jabbah.io.Storable
import io.antarescircuit.jabbah.io.StoreReader
import io.antarescircuit.jabbah.io.StoreWriter

/**
 * An arrow-like [GraphPortView] wrapping a content that displays the state
 * of a [GraphPort].
 */
abstract class AbstractCircuitInOutView<T : CircuitInOut<*>>(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	model: T,
	protected val eventBus: EventBus = BaseModule.eventBus,
	orientation: Direction = Direction.EAST
) : AbstractVerticeView<T>(styleProvider, model), GraphPortView<T>, Labeled, Orientable {

	companion object {
		const val PROP_INPUT_ICON_PATH = "io.antarescircuit.antares.view.inout.CircuitInOut.inputIcon"
		const val PROP_OUTPUT_ICON_PATH = "io.antarescircuit.antares.view.inout.CircuitInOut.outputIcon"
		const val PROP_INOUT_ICON_PATH = "io.antarescircuit.antares.view.inout.CircuitInOut.inoutIcon"
		const val LABEL_DIST = Look.SCALE
	}

	/** Initialized in [updateView] */
	protected var arrowPath: ArrowPath? = null

	private val actorInteractionHandler: ToggleInteractionHandler = createActorInteractionHandler()

	/** ---- [Orientable] */

	override val useOrientation: Boolean get() = true

	override var orientation: Direction = orientation
		set(value) {
			if (field != value) {
				invalidate()
				field = value
				updateView()
			}
		}

	/** ---- [Locatable] */

	override var location: Point2D = Point2D.ZERO
		set(value) {
			invalidate()
			field = value
			updateBoundingBox()
			update()
		}

	protected val propertiesBackgroundColor get() = if (Look.FILL_BASIC_COMPONENTS) {
		backgroundColor
	} else {
		styleProvider.getStyle(StyleType.BACKGROUND).color.backgroundColor
	}

	/** ----  UI properties */

	var name: String?
		get() = model.name
		set(value) {
			model.name = value
		}

	var portType: PortType
		get() = model.portType
		set(value) {
			invalidate()
			model.portType = value
			updateView()
		}

	init {
		isFocusable = true
	}

	/** ---- [Labeled] */

	override val label = Label(
		font = font,
		text = model.name)

	/** ---- [Storable] */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		writer.writePoint("location", location)
		writer.writeString("orientation", orientation.customName)
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		location = if (reader.hasElement("location")) {
			reader.readPoint("location")
		} else {
			Point2D(reader.readDouble("x"), reader.readDouble("y"))
		}
		orientation = Direction.withName(reader.readString("orientation"))
	}

	/** ---- [Focusable] */

	override fun canConsume(keyEvent: KeyEvent): Boolean =
		actorInteractionHandler.canConsume(keyEvent)

	/** ---- [ActorView] */

	override fun getActorInteractionHandler(context: ActorInteractionContext): ActorInteractionHandler =
		if (model.portType.isInput) {
			actorInteractionHandler
		} else {
			super.getActorInteractionHandler(context)
		}

	/** ---- [Drawable] */

	override fun getBoundingBoxImpl(): RectangularShape = boundingBox

	override fun contains(x: Double, y: Double): Boolean =
		rotate(boundingBox).contains(x, y)

	/** ---- [Component] */

	override fun isRotatableWith(selection: Collection<*>): Boolean = true

	/** ---- [Rotatable] */

	override val useRotation: Boolean get() = false

	override fun rotate(direction: RotationDirection, pivot: Point2D?) {
		orientation = when (direction) {
			RotationDirection.Clockwise -> Direction.of(orientation.rotation.previous())
			RotationDirection.CounterClockwise -> Direction.of(orientation.rotation.next())
		}
		pivot?.let {
			location = direction.rotation.rotatePointAround(it, location)
		}
	}


	/** ---- [AbstractGraphElementView] */

	override fun handleStateChanged(event: GraphElementEvent) {
		invalidate()
		handleStateChangedImpl(event)
		label.text = StringUtils.orEmpty(name)
		updateBoundingBox()

		super.handleStateChanged(event)
	}

	protected open fun handleStateChangedImpl(event: GraphElementEvent) {
		// empty
	}

	/** ---- [AbstractVerticeView] */

	override var description: Description
		get() = model.getPort<DigitalSignal>().description
		set(value) {
			model.getPort<DigitalSignal>().description = value
			tooltip.reset()
		}

	protected abstract fun drawSimulated(context: DrawContext)

	override fun drawImpl(context: DrawContext) {
		val oldColor = context.g.color

		super.drawImpl(context)

		if (context.castedAppContext<GraphApplicationContext>()!!.isExecute) {
			drawSimulated(context)
		} else {
			if (context.useContextColors) {
				drawEdited(context, context.color!!.foregroundColor, context.color!!.backgroundColor)
			} else {
				drawEdited(context, foregroundColor, propertiesBackgroundColor)
			}
		}

		context.g.color = oldColor
	}

	/** ---- [GraphPortView] */

	override val iconPath: String
		get() = when (portType) {
			PortType.INPUT -> BaseModule.properties.getString(PROP_INPUT_ICON_PATH)
			PortType.OUTPUT -> BaseModule.properties.getString(PROP_OUTPUT_ICON_PATH)
			PortType.INOUT -> BaseModule.properties.getString(PROP_INOUT_ICON_PATH)
		}

	/** ---- [AbstractCircuitInOutView] */

	private val _boundingBox = Rectangle2D()
	override val boundingBox: RectangularShape get() = _boundingBox

	protected fun updateView() {
		invalidate()
		isFocusable = model.portType.isInput

		updateViewImpl()

		updatePortView()
		updateLabel()

		invalidate()
		update()

		validate()
	}

	protected abstract fun updateViewImpl()

	protected abstract fun createPortViewImpl(template: PortView<*>?, direction: Direction): PortView<*>

	private fun createPortView(template: PortView<*>?): PortView<*> {
		when (model.portType) {
			PortType.INPUT -> {
				val portView = createPortViewImpl(template, orientation)
				portView.setLocation(
					-portView.unconnectedLength * orientation.dx,
					-portView.unconnectedLength * orientation.dy)
				return portView
			}
			PortType.INOUT, PortType.OUTPUT -> {
				val portView = createPortViewImpl(template, orientation.opposite())
				portView.setLocation(
					portView.unconnectedLength * orientation.dx,
					portView.unconnectedLength * orientation.dy)
				return portView
			}
		}
	}

	private fun updatePortView() {
		val portView = getPortView(model.getPort())
		clearPortViews()
		addPortView(createPortView(portView))
	}

	protected fun updateLabel() {
		when (model.portType) {
			PortType.INOUT -> updateOutputLabel()
			PortType.INPUT -> updateInputLabel()
			PortType.OUTPUT -> updateOutputLabel()
		}
	}

	private fun updateInputLabel() {
		label.text = StringUtils.orEmpty(name)
		label.location = Point2D(arrowPath!!.tailLocation).subtract(orientation.multiply(LABEL_DIST.toDouble()))
		label.alignment = Alignment.forOrientation(orientation)
		updateBoundingBox()
	}

	protected open fun updateOutputLabel() {
		label.text = StringUtils.orEmpty(name)
		label.location = orientation.multiply(LABEL_DIST.toDouble())
		label.alignment = Alignment.forOrientation(orientation.opposite())
		updateBoundingBox()
	}

	protected fun updateBoundingBox() {
		invalidate()

		_boundingBox.setFrame(location.x, location.y, 0.0, 0.0)
		addPortViewsTo(_boundingBox, null)

		val pathBB = arrowPath!!.path.boundingBox
		val pathTranslation = getArrowPathTranslation()
		val pathBBoxRect = Rectangle2D(
			location.x + pathBB.x + pathTranslation.x - 1,
			location.y + pathBB.y + pathTranslation.y - 1,
			pathBB.width + 2,
			pathBB.height + 2)
		if (shadow) {
			DropShadow.expand(pathBBoxRect, rotation)
		}
		_boundingBox.add(pathBBoxRect)

		val labelBB = label.boundingBox
		_boundingBox.add(Rectangle2D(
			location.x + labelBB.x + pathTranslation.x - 1,
			location.y + labelBB.y + pathTranslation.y - 1,
			labelBB.width + 2,
			labelBB.height + 2))

		invalidate()
	}

	/**
	 * Returns the translation vector to be applied to the [ArrowPath] for drawing and bounding box calculation.
	 * @return the vector relative to this [VerticeView]'s origin
	 */
	protected fun getArrowPathTranslation(): Point2D {
		return when (model.portType) {
			PortType.INPUT -> orientation.multiply(-getOutput().unconnectedLength.toDouble())
			PortType.INOUT, PortType.OUTPUT ->
				Point2D(arrowPath!!.tailLocation)
					.multiply(-1.0)
					.add(orientation.multiply(getInput().unconnectedLength.toDouble()))
		}
	}


	private fun drawShape(context: DrawContext, foregroundColor: Color, backgroundColor: Color?, stroke: Stroke) {
		context.translated(getArrowPathTranslation()) { c ->
			if (shadow) {
				DropShadow.draw(context, transparency) {
					if (backgroundColor != null) {
						c.g.fill(arrowPath!!.path)
					}
					c.g.draw(arrowPath!!.path)
				}
			}

			if (backgroundColor != null) {
				c.g.color = backgroundColor
				c.g.fill(arrowPath!!.path)
			}
			c.g.stroke = stroke
			c.g.color = foregroundColor
			c.g.draw(arrowPath!!.path)
		}
	}

	protected fun drawEdited(context: DrawContext, color: Color, backgroundColor: Color?) {
		val oldStroke = context.g.stroke

		drawShape(context, color, backgroundColor, stroke)

		context.g.stroke = oldStroke

		if (context.useContextColors) {
			context.g.color = context.color!!.foregroundColor
		} else {
			context.g.color = styleProvider.getStyle(StyleType.BACKGROUND).color.textColor
		}

		context.translated(getArrowPathTranslation()) { label.draw(it) }
	}

	protected fun drawDisabled(context: DrawContext) {
		if (model.disabled && context.castedAppContext<GraphApplicationContext>()?.isPausing == true) {
			context.g.color = Look.disabledColor()
			context.g.fill(arrowPath!!.path)
		}
	}

	protected fun checkTopLevelKey(): Boolean {
		if (!model.isToplevel) {
			eventBus.post(ComponentMessage(type = ComponentMessageType.Error, source = this@AbstractCircuitInOutView, messageKey = "antares.msg.ChildGraphInputManipulation"))
			return false
		}
		return true
	}


	protected open fun createActorInteractionHandler(): ToggleInteractionHandler = ToggleInteractionHandler()

	protected abstract fun toggle(undefine: Boolean, context: ActorInteractionContext): ActorInteractionHandler?

	/**
	 * Calls [toggle] when the user presses the mouse on this [AbstractCircuitInOutView] during simulation,
	 * unless it is not a toplevel component.
	 */
	protected open inner class ToggleInteractionHandler : ClickableActorInteractionHandlerAdapter() {

		open fun canConsume(keyEvent: KeyEvent): Boolean = keyEvent.modifiers == 0

		override fun mousePressed(context: ActorInteractionContext): ActorInteractionHandler? {
			if (!checkTopLevelKey()) {
				return null
			}

			// Don't consume event so that Canvas can gain focus
			return toggle(context.mouseEvent?.isAltDown ?: false, context) ?: this
		}
	}
}