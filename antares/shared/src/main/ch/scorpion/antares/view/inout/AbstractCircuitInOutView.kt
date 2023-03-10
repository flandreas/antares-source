package ch.scorpion.antares.view.inout

import ch.scorpion.antares.model.inout.CircuitInOut
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.view.Look
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.drawable.RotationDirection
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.DropShadow
import ch.scorpion.jabbah.draw.graphics.Stroke
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.StyleType
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.model.ComponentMessage
import ch.scorpion.jabbah.edit.model.ComponentMessageType
import ch.scorpion.jabbah.edit.model.text.Alignment
import ch.scorpion.jabbah.edit.model.text.Label
import ch.scorpion.jabbah.edit.model.text.Labeled
import ch.scorpion.jabbah.edit.model.text.description.Description
import ch.scorpion.jabbah.execution.actor.ActorInteractionContext
import ch.scorpion.jabbah.execution.actor.ActorInteractionHandler
import ch.scorpion.jabbah.execution.actor.ActorView
import ch.scorpion.jabbah.execution.actor.ClickableActorInteractionHandlerAdapter
import ch.scorpion.jabbah.graph.GraphApplicationContext
import ch.scorpion.jabbah.graph.model.GraphElementEvent
import ch.scorpion.jabbah.graph.model.GraphPort
import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.graph.view.AbstractGraphElementView
import ch.scorpion.jabbah.graph.view.GraphPortView
import ch.scorpion.jabbah.graph.view.VerticeView
import ch.scorpion.jabbah.graph.view.port.PortView
import ch.scorpion.jabbah.graph.view.vertice.AbstractVerticeView
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

/**
 * An arrow-like [GraphPortView] wrapping a content that displays the state
 * of a [GraphPort].
 */
abstract class AbstractCircuitInOutView<T : CircuitInOut<*>>(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	model: T,
	protected val eventBus: EventBus = BaseModule.eventBus,
	orientation: Direction = Direction.EAST
) : AbstractVerticeView<T>(styleProvider, model), GraphPortView<T>, Labeled {

	companion object {
		const val PROP_INPUT_ICON_PATH = "ch.scorpion.antares.view.inout.CircuitInOut.inputIcon"
		const val PROP_OUTPUT_ICON_PATH = "ch.scorpion.antares.view.inout.CircuitInOut.outputIcon"
		const val PROP_INOUT_ICON_PATH = "ch.scorpion.antares.view.inout.CircuitInOut.inoutIcon"
		const val LABEL_DIST = Look.SCALE
	}

	/** Initialized in [updateView] */
	protected var arrowPath: ArrowPath? = null

	private val actorInteractionHandler = createActorInteractionHandler()

	var orientation: Direction = orientation
		set(value) {
			if (field != value) {
				invalidate()
				field = value
				updateView()
			}
		}

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

	/** ---- [ActorView] */

	override fun getActorInteractionHandler(context: ActorInteractionContext): ActorInteractionHandler =
		if (model.portType.isInput) {
			actorInteractionHandler
		} else {
			super.getActorInteractionHandler(context)
		}

	/** ---- [Drawable] */

	override fun getBoundingBoxImpl(): Rectangle2D = boundingBox

	override fun contains(x: Double, y: Double): Boolean =
		rotate(boundingBox).contains(x, y)

	/** ---- [Component] */

	override val rotatable: Boolean get() = true

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

	override val boundingBox = Rectangle2D()

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

		boundingBox.setFrame(location.x, location.y, 0.0, 0.0)
		addPortViewsTo(boundingBox, null)

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
		boundingBox.add(pathBBoxRect)

		val labelBB = label.boundingBox
		boundingBox.add(Rectangle2D(
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


	fun drawShape(context: DrawContext, foregroundColor: Color, backgroundColor: Color?, stroke: Stroke) {
		val translation = getArrowPathTranslation()
		context.g.translate(translation.x, translation.y)

		if (shadow) {
			DropShadow.draw(context, transparency) {
				if (backgroundColor != null) {
					context.g.fill(arrowPath!!.path)
				}
				context.g.draw(arrowPath!!.path)
			}
		}

		if (backgroundColor != null) {
			context.g.color = backgroundColor
			context.g.fill(arrowPath!!.path)
		}
		context.g.stroke = stroke
		context.g.color = foregroundColor
		context.g.draw(arrowPath!!.path)

		context.g.translate(-translation.x, -translation.y)
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

		val translation = getArrowPathTranslation()
		context.g.translate(translation.x, translation.y)
		label.draw(context)
		context.g.translate(-translation.x, -translation.y)
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


	protected open fun createActorInteractionHandler(): ActorInteractionHandler = ToggleInteractionHandler()

	protected abstract fun toggle(undefine: Boolean, context: ActorInteractionContext): ActorInteractionHandler?

	/**
	 * Calls [toggle] when the user presses the mouse on this [AbstractCircuitInOutView] during simulation,
	 * unless it is not a toplevel component.
	 */
	protected open inner class ToggleInteractionHandler : ClickableActorInteractionHandlerAdapter() {

		override fun mousePressed(context: ActorInteractionContext): ActorInteractionHandler? {
			if (!model.isToplevel) {
				eventBus.post(
					ComponentMessage(
						type = ComponentMessageType.Error,
						source = this@AbstractCircuitInOutView,
						messageKey = "antares.msg.ChildGraphInputManipulation")
					)
				return null
			}

			// Don't consume event so that Canvas can gain focus
			return toggle(context.mouseEvent?.isAltDown ?: false, context) ?: this
		}
	}
}