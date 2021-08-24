package ch.scorpion.antares.view.inout

import ch.scorpion.antares.model.inout.CircuitInOut
import ch.scorpion.antares.model.inout.CircuitInOutImpl
import ch.scorpion.antares.model.signal.*
import ch.scorpion.antares.view.Look
import ch.scorpion.antares.view.port.DigitalPortView
import ch.scorpion.antares.view.signal.DigitalSignalSourceControlView
import ch.scorpion.antares.view.signal.NumberView
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.KeyEvent
import ch.scorpion.jabbah.base.geom.Dimension2D
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.drawable.Locatable
import ch.scorpion.jabbah.draw.drawable.Transparent
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
import ch.scorpion.jabbah.edit.model.text.description.Description
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.actor.ActorInteractionContext
import ch.scorpion.jabbah.execution.actor.ActorInteractionHandler
import ch.scorpion.jabbah.execution.actor.ActorView
import ch.scorpion.jabbah.execution.actor.ClickableActorInteractionHandlerAdapter
import ch.scorpion.jabbah.graph.GraphApplicationContext
import ch.scorpion.jabbah.graph.model.GraphElementEvent
import ch.scorpion.jabbah.graph.model.GraphPort
import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.graph.view.*
import ch.scorpion.jabbah.graph.view.port.PortView
import ch.scorpion.jabbah.graph.view.vertice.AbstractVerticeView
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter


/**
 * A [CircuitInOutView] is an arrow-like [GraphPortView] for digital [GraphPort]s.
 */
class CircuitInOutView(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	model: CircuitInOut = CircuitInOutImpl(),
	private val eventBus: EventBus = BaseModule.eventBus
) : AbstractVerticeView<CircuitInOut>(styleProvider, model), GraphPortView<CircuitInOut>, ControlViewSource<CircuitInOut> {

	companion object {
		const val PROP_INPUT_ICON_PATH = "ch.scorpion.antares.view.inout.CircuitInOut.inputIcon"
		const val PROP_OUTPUT_ICON_PATH = "ch.scorpion.antares.view.inout.CircuitInOut.outputIcon"
		const val PROP_INOUT_ICON_PATH = "ch.scorpion.antares.view.inout.CircuitInOut.inoutIcon"
		const val LABEL_DIST = Look.SCALE
		val LOG by logger(CircuitInOutView::class)
	}

	var signalRepresentation: DigitalSignalRepresentation = DigitalSignalRepresentation.BINARY
		set(value) {
			field = value
			model.signalRepresentation = value
			updateView()
		}

	var orientation: Direction = Direction.EAST
		set(value) {
			if (field != value) {
				invalidate()
				field = value
				updateView()
			}
		}

	/**
	 * Controls the interactive behaviour of this [CircuitInOutView]. If set to `true`, it
	 * stays in the new state when the user releases the mouse button. If set to `false`,
	 * it returns to 0 state.
	 */
	var toggle: Boolean = true

	override val boundingBox = Rectangle2D()

	private val actorInteractionHandler = InteractionHandler()

	private val propertiesBackgroundColor get() = if (Look.FILL_BASIC_COMPONENTS) backgroundColor else styleProvider.getStyle(StyleType.BACKGROUND).color.backgroundColor

	private val label = Label(
		font = font,
		text = model.name)

	/** Initialized in [updateView] */
	private var arrowPath: ArrowPath? = null

	/** Initialized in [updateView] */
	private var numberView: NumberView? = null

	override fun modelExchanged(oldModel: CircuitInOut?) {
		super.modelExchanged(oldModel)
		model.signalRepresentation = signalRepresentation
		updateView()
	}

	/** ----  UI properties */

	var name: String?
		get() = model.name
		set(value) {
			println("Setting name of CircuitInOut to '$value'")
			model.name = value
		}

	var bitWidth: BitWidth
		get() = model.bitWidth
		set(value) {
			println("Setting bitWidth to '$value'")
			invalidate()
			model.bitWidth = value
			updateView()
		}

	var portType: PortType
		get() = model.portType
		set(value) {
			invalidate()
			model.portType = value
			updateView()
		}

	override var description: Description
		get() = model.getPort<DigitalSignal>().description
		set(value) {
			model.getPort<DigitalSignal>().description = value
		}

	/** ---- [Transparent] */

	override var transparency: Int
		get() = super.transparency
		set(value) {
			super.transparency = value
			numberView?.transparency = value
		}

	/** ---- [GraphPortView] */

	override val iconPath: String
		get() = when (portType) {
			PortType.INPUT -> BaseModule.properties.getString(PROP_INPUT_ICON_PATH)
			PortType.OUTPUT -> BaseModule.properties.getString(PROP_OUTPUT_ICON_PATH)
			PortType.INOUT -> BaseModule.properties.getString(PROP_INOUT_ICON_PATH)
		}

	/** ---- [ActorView] */

	override fun getActorInteractionHandler(context: ActorInteractionContext): ActorInteractionHandler {
		return if (model.portType.isInput) {
			actorInteractionHandler
		} else {
			super.getActorInteractionHandler(context)
		}
	}

	/** ---- [Storable] */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		writer.writePoint("location", location)
		writer.writeString("representation", signalRepresentation.customName)
		writer.writeString("orientation", orientation.customName)
		if (!toggle) {
			writer.writeBoolean("toggle", toggle)
		}
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		location = if (reader.hasElement("location")) {
			reader.readPoint("location")
		} else {
			Point2D(reader.readDouble("x"), reader.readDouble("y"))
		}
		signalRepresentation = DigitalSignalRepresentation.withName(reader.readString("representation"))
		orientation = Direction.withName(reader.readString("orientation"))
		if (reader.hasAttribute("toggle")) {
			toggle = reader.readBoolean("toggle")
		}
	}

	/** ---- [ControlViewSource] */

	override val controlId: String get() = "circuitInOut:$id"

	override val controlName: String get() = "$type ${model.name}"

	override fun createControlView(): ControlView<CircuitInOut> {
		val controlView = DigitalSignalSourceControlView(styleProvider, controlId, signalRepresentation, model, controlName)
		controlView.location = Point2D.ZERO
		return controlView
	}

	/** ---- [Locatable] */

	override var location: Point2D = Point2D.ZERO
		set(value) {
			invalidate()
			field = value
			updateBoundingBox()
			update()
		}

	init {
		modelExchanged(null)
	}

	/** ---- [Drawable] */

	override fun getBoundingBoxImpl(): Rectangle2D {
		return boundingBox
	}

	override fun contains(x: Double, y: Double): Boolean {
		return rotate(boundingBox).contains(x, y)
	}

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

	private fun drawEdited(context: DrawContext, color: Color, backgroundColor: Color?) {
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

	private fun drawSimulated(context: DrawContext) {
		if (model.signal!!.bitWidth.width > 1) {
			drawEdited(context,
				transparent.applyTo(model.signal!!.color.foregroundColor),
				transparent.applyTo(propertiesBackgroundColor))
		} else {
			drawEdited(context,
				transparent.applyTo(model.signal!!.color.backgroundColor),
				transparent.applyTo(model.signal!!.color.foregroundColor))
		}

		val translation = getArrowPathTranslation()
		context.g.translate(translation.x, translation.y)
		numberView!!.draw(context)
		drawDisabled(context)
		context.g.translate(-translation.x, -translation.y)
	}

	private fun drawDisabled(context: DrawContext) {
		if (model.disabled && context.castedAppContext<GraphApplicationContext>()?.isPausing == true) {
			context.g.color = Look.disabledColor()
			context.g.fill(arrowPath!!.path)
		}
	}

	/** ---- [Component] */

	override val rotatable: Boolean get() = true

	override fun focusGained() {
		numberView!!.focusGained()
		super.focusGained()
	}

	override fun focusLost() {
		numberView!!.focusLost()
		super.focusLost()
	}

	override fun rotateCounterClockwise() {
		orientation = Direction.of(orientation.rotation.next())
	}

	override fun rotateClockwise() {
		orientation = Direction.of(orientation.rotation.previous())
	}

	/** ---- [AbstractGraphElementView] */

	override fun handleStateChanged(event: GraphElementEvent) {
		invalidate()
		numberView!!.setSignal(model.signal!!)
		label.text = StringUtils.orEmpty(name)
		updateBoundingBox()
		super.handleStateChanged(event)
	}

	/** ---- [CircuitInOutView] */

	/**
	 * Returns the translation vector to be applied to the [ArrowPath] for drawing and bounding box calculation.
	 * @return the vector relative to this [VerticeView]'s origin
	 */
	private fun getArrowPathTranslation(): Point2D {
		return when (model.portType) {
			PortType.INPUT -> orientation.multiply(-getOutput().unconnectedLength.toDouble())
			PortType.INOUT, PortType.OUTPUT ->
				Point2D(arrowPath!!.tailLocation)
					.multiply(-1.0)
					.add(orientation.multiply(getInput().unconnectedLength.toDouble()))
		}
	}

	/**
	 * Creates the [DigitalPortView] of this [CircuitInOut].
	 */
	private fun createPortView(template: PortView<DigitalSignal>?): DigitalPortView {
		when (model.portType) {
			PortType.INPUT -> {
				val portView = DigitalPortView(
					styleProvider = styleProvider,
					port = model.getPort(),
					direction = orientation,
					customUnconnectedLength = template?.customUnconnectedLength,
					length = template?.length)
				portView.setLocation(
					-portView.unconnectedLength * orientation.dx,
					-portView.unconnectedLength * orientation.dy)
				return portView
			}
			PortType.INOUT, PortType.OUTPUT -> {
				val portView = DigitalPortView(
					styleProvider = styleProvider,
					port = model.getInput(),
					direction = orientation.opposite(),
					customUnconnectedLength = template?.customUnconnectedLength,
					length = template?.length)
				portView.setLocation(
					portView.unconnectedLength * orientation.dx,
					portView.unconnectedLength * orientation.dy)
				return portView
			}
		}
	}

	/**
	 * Updates the text, the location and the alignments of the external [Label] depending on the current
	 * orientation of this [CircuitInOutView]}.
	 */
	private fun updateLabel() {
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

	private fun updateOutputLabel() {
		label.text = StringUtils.orEmpty(name)
		label.location = orientation.multiply(LABEL_DIST.toDouble())
		label.alignment = Alignment.forOrientation(orientation.opposite())
		updateBoundingBox()
	}

	private fun updateBoundingBox() {
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

	private fun updateView() {
		invalidate()
		isFocusable = model.portType.isInput

		numberView = NumberView(signalRepresentation, bitWidth, drawBox = bitWidth.width > 1)
		numberView!!.setSignal(model.signal!!)

		arrowPath = ArrowPath.Companion.Builder(
			orientation = orientation,
			contentDimension = Dimension2D(numberView!!.widthInt, numberView!!.heightInt))
			.build(portType === PortType.INOUT)

		numberView!!.setBounds(
			arrowPath!!.contentLocation.x, arrowPath!!.contentLocation.y,
			numberView!!.bounds.width, numberView!!.bounds.height)

		val portView = getPortView(model.getPort<DigitalSignal>())
		clearPortViews()
		addPortView(createPortView(portView))

		updateLabel()

		invalidate()
		update()

		validate()
	}

	/**
	 * Returns the index of the digit at the specified absolute coordinates.
	 * @return the index of the digit at the specified absolute coordinates, if any.
	 */
	private fun getDigitIndexAt(x: Double, y: Double): Int? {
		if (numberView!!.digitCount == 1) {
			// Use the entire arrow path as sensitive area
			val pathTranslation = getArrowPathTranslation()
			return if (arrowPath!!.path.contains(
					x - location.x - pathTranslation.x,
					y - location.y - pathTranslation.y))
				0
			else
				null
		}

		return numberView!!.getDigitIndexAt(
			x - location.x - arrowPath!!.contentLocation.x - getArrowPathTranslation().x,
			y - location.y - arrowPath!!.contentLocation.y - getArrowPathTranslation().y)
	}

	/**
	 * Allows to toggle individual [Bit]s by clicking with the mouse and entering
	 * individual digits with the keyboard.
	 */
	private inner class InteractionHandler : ClickableActorInteractionHandlerAdapter() {

		override fun mouseMoved(context: ActorInteractionContext): ActorInteractionHandler? {
			if (model.isToplevel) {
				return super.mouseMoved(context)
			}
			return null
		}

		override fun mousePressed(context: ActorInteractionContext): ActorInteractionHandler? {
			if (!model.isToplevel) {
				eventBus.post(ComponentMessage(
					type = ComponentMessageType.Error,
					source = this@CircuitInOutView,
					messageKey = "antares.msg.ChildGraphInputManipulation"))
				return null
			}
			toggle(context.mouseEvent?.isAltDown ?: false, context.signalHandler, context.x, context.y)
			// Don't consume event so that Canvas can gain focus
			return this
		}

		override fun mouseDragged(context: ActorInteractionContext): ActorInteractionHandler = this

		override fun mouseReleased(context: ActorInteractionContext): ActorInteractionHandler? {
			if (!toggle) {
				toggle(false, context.signalHandler, context.x, context.y)
				context.mouseEvent?.consume()
			}
			return null
		}

		private fun toggle(undefine: Boolean, signalHandler: SignalHandler, x: Double, y: Double) {
			val digitIndex = getDigitIndexAt(x, y)
			if (digitIndex != null) {
				if (signalRepresentation == DigitalSignalRepresentation.BINARY) {
					model.toggleBit(digitIndex, undefine, signalHandler)
				} else if (numberView!!.focusIndex == digitIndex) {
					eventBus.post(ComponentMessage(
						type = ComponentMessageType.Info,
						source = this@CircuitInOutView,
						messageKey = "antares.msg.HexInputManipulation"))
				}

				// Set the focus on the selected digit
				invalidate()
				requestFocus()
				numberView!!.setFocusTo(digitIndex)
				validate()
			}
		}

		override fun keyPressed(context: ActorInteractionContext): ActorInteractionHandler? {
			if (numberView!!.focusIndex != null) {
				LOG.trace("keyPressed '${context.keyEvent!!.key.toChar()}'")

				invalidate()
				if (context.keyEvent!!.key == KeyEvent.VK_LEFT) {
					numberView!!.transferFocusLeft()
				} else if (context.keyEvent!!.key == KeyEvent.VK_RIGHT) {
					numberView!!.transferFocusRight()
				} else if (context.keyEvent!!.key == KeyEvent.VK_ENTER && checkTopLevelKey()) {
					if (signalRepresentation == DigitalSignalRepresentation.BINARY) {
						toggleFocusBitWithEnter(context.signalHandler)
					}
				} else if (context.keyEvent!!.key == KeyEvent.VK_DELETE && portType == PortType.INOUT && checkTopLevelKey()) {
					val undefined = DigitalSignalFactory.undefined(BitWidth.of(signalRepresentation.bitCount))
					val newWord = signalRepresentation.withDigit(model.signal!!, undefined, numberView!!.focusIndex!!)
					model.setSignalManually(newWord, context.signalHandler)
					numberView!!.transferFocusRight()
				} else {
					val digitWord = signalRepresentation.digitToWord(BitWidth.of(signalRepresentation.bitCount), context.keyEvent!!.key.toChar())
					if (digitWord != null && checkTopLevelKey()) {
						val newWord = signalRepresentation.withDigit(model.signal!!, digitWord, numberView!!.focusIndex!!)
						model.setSignalManually(newWord, context.signalHandler)
						numberView!!.transferFocusRight()
					}
				}
				validate()
			}
			return null
		}

		override fun keyReleased(context: ActorInteractionContext): ActorInteractionHandler? {
			if (toggle) {
				return null
			}
			if (numberView!!.focusIndex != null) {
				if (context.keyEvent?.key == KeyEvent.VK_ENTER && checkTopLevelKey()) {
					toggleFocusBitWithEnter(context.signalHandler)
				}
			}
			return null
		}

		private fun toggleFocusBitWithEnter(signalHandler: SignalHandler) {
			model.toggleBit(numberView!!.focusIndex!!, false, signalHandler)
		}

		private fun checkTopLevelKey(): Boolean {
			if (!model.isToplevel) {
				eventBus.post(ComponentMessage(type = ComponentMessageType.Error, source = this@CircuitInOutView, messageKey = "antares.msg.ChildGraphInputManipulation"))
				return false
			}
			return true
		}
	}
}