package ch.scorpion.antares.view.inout

import ch.scorpion.antares.model.inout.DigitalCircuitInOut
import ch.scorpion.antares.model.inout.DigitalCircuitInOutImpl
import ch.scorpion.antares.model.signal.*
import ch.scorpion.antares.view.port.DigitalPortView
import ch.scorpion.antares.view.signal.DigitalSignalSourceControlView
import ch.scorpion.antares.view.signal.NumberView
import ch.scorpion.jabbah.animation.AnimationTask
import ch.scorpion.jabbah.animation.AnimationTaskAdapter
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.KeyEvent
import ch.scorpion.jabbah.base.event.PropertyChangeListener
import ch.scorpion.jabbah.base.geom.Dimension2D
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.drawable.ShakeLocatableAnimation
import ch.scorpion.jabbah.draw.drawable.Transparent
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.model.ComponentMessage
import ch.scorpion.jabbah.edit.model.ComponentMessageType
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.actor.ActorInteractionContext
import ch.scorpion.jabbah.execution.actor.ActorInteractionHandler
import ch.scorpion.jabbah.execution.actor.ActorView
import ch.scorpion.jabbah.execution.actor.ClickableActorInteractionHandlerAdapter
import ch.scorpion.jabbah.graph.GraphApplicationContextHolder
import ch.scorpion.jabbah.graph.model.GraphElementEvent
import ch.scorpion.jabbah.graph.model.GraphPort
import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.graph.view.AbstractGraphElementView
import ch.scorpion.jabbah.graph.view.ControlView
import ch.scorpion.jabbah.graph.view.ControlViewSource
import ch.scorpion.jabbah.graph.view.GraphPortView
import ch.scorpion.jabbah.graph.view.port.PortView
import ch.scorpion.jabbah.graph.view.vertice.AbstractVerticeView
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

/**
 * A [DigitalCircuitInOutView] is an arrow-like [GraphPortView] for digital [GraphPort]s.
 */
class DigitalCircuitInOutView(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	model: DigitalCircuitInOut = DigitalCircuitInOutImpl(),
	eventBus: EventBus = BaseModule.eventBus,
	orientation: Direction = Direction.EAST
) : AbstractCircuitInOutView<DigitalCircuitInOut>(styleProvider, model, eventBus, orientation), ControlViewSource<DigitalCircuitInOut> {

	companion object {
		const val PROP_INPUT_ICON_PATH = "ch.scorpion.antares.view.inout.CircuitInOut.inputIcon"
		const val PROP_OUTPUT_ICON_PATH = "ch.scorpion.antares.view.inout.CircuitInOut.outputIcon"
		const val PROP_INOUT_ICON_PATH = "ch.scorpion.antares.view.inout.CircuitInOut.inoutIcon"
		val LOG by logger(DigitalCircuitInOutView::class)
	}

	var signalRepresentation: DigitalSignalRepresentation = DigitalSignalRepresentation.BINARY
		set(value) {
			field = value
			model.signalRepresentation = value
			updateView()
		}

	/**
	 * Controls the interactive behaviour of this [DigitalCircuitInOutView]. If set to `true`, it
	 * stays in the new state when the user releases the mouse button. If set to `false`,
	 * it returns to 0 state.
	 */
	var toggle: Boolean = true

	private val actorInteractionHandler = InteractionHandler()

	/** Initialized in [updateView] */
	private var numberView: NumberView? = null

	/** Redirects validation requests from [NumberView] during shake animation by [rejectSignal]. */
	private var numberViewOwner: DrawableOwner? = null

	/** Opened when user clicks on digit if [DigitalSignalRepresentation] is not binary.*/
	private var popupKeyboard: CircuitInOutKeyboard? = null

	/** The [DrawingView] in which [popupKeyboard] is displayed.*/
	private var popupKeyboardView: DrawingView<*>? = null

	/** Determines if there is a [ShakeLocatableAnimation] running due to an invalid data entry.*/
	private var isShaking: Boolean = false

	private val numberViewFocusListener: PropertyChangeListener<Any> = PropertyChangeListener {
		if (numberView?.focusIndex == null) {
			hideKeyboard()
		}
	}

	/** ----  UI properties */

	var bitWidth: BitWidth
		get() = model.bitWidth
		set(value) {
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

	@Suppress("unused")
	var customCanBeUndefined: Boolean
		get() = model.customCanBeUndefined
		set(value) {
			model.customCanBeUndefined = value
		}

	init {
		modelExchanged(null)
	}

	override fun modelExchanged(oldModel: DigitalCircuitInOut?) {
		super.modelExchanged(oldModel)
		model.signalRepresentation = signalRepresentation
		updateView()
	}

	/** ---- [ActorView] */

	override fun getActorInteractionHandler(context: ActorInteractionContext): ActorInteractionHandler =
		if (model.portType.isInput) {
			actorInteractionHandler
		} else {
			super.getActorInteractionHandler(context)
		}

	/** ---- [Storable] */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		writer.writeString("representation", signalRepresentation.customName)
		if (!toggle) {
			writer.writeBoolean("toggle", toggle)
		}
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		signalRepresentation = DigitalSignalRepresentation.withName(reader.readString("representation"))
		if (reader.hasAttribute("toggle")) {
			toggle = reader.readBoolean("toggle")
		}
	}

	/** ---- [ControlViewSource] */

	override val controlId: String get() = "circuitInOut:$id"

	override val controlName: String get() = "$type: ${model.name}"

	override fun createControlView(): ControlView<DigitalCircuitInOut> {
		val controlView = DigitalSignalSourceControlView(styleProvider, controlId, signalRepresentation, model, controlName)
		controlView.location = Point2D.ZERO
		return controlView
	}

	/** ---- [Transparent] */

	override var transparency: Int
		get() = super.transparency
		set(value) {
			super.transparency = value
			numberView?.transparency = value
		}

	/** ---- [Component] */

	override fun focusGained() {
		numberView!!.focusGained()
		super<AbstractCircuitInOutView>.focusGained()
	}

	override fun focusLost() {
		numberView!!.focusLost()
		super<AbstractCircuitInOutView>.focusLost()
	}

	/** ---- [AbstractGraphElementView] */

	override fun handleStateChangedImpl(event: GraphElementEvent) {
		if (model.bitWidth.width != numberView!!.bitWidth.width) {
			updateView()
		}
		numberView!!.setSignal(model.signal!!)
	}

	/** ---- [AbstractVerticeView] */

	override fun executionStarted(signalHandler: SignalHandler) {
		numberView?.addPropertyChangeListener(numberViewFocusListener)
	}

	override fun executionStopped(signalHandler: SignalHandler) {
		numberView?.removePropertyChangeListener(numberViewFocusListener)
		hideKeyboard()
	}

	/** ---- [GraphPortView] */

	override val iconPath: String
		get() = when (portType) {
			PortType.INPUT -> BaseModule.properties.getString(PROP_INPUT_ICON_PATH)
			PortType.OUTPUT -> BaseModule.properties.getString(PROP_OUTPUT_ICON_PATH)
			PortType.INOUT -> BaseModule.properties.getString(PROP_INOUT_ICON_PATH)
		}

	/** ---- [AbstractCircuitInOutView] */

	override fun drawSimulated(context: DrawContext) {
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

	/** ---- [DigitalCircuitInOutView] */

	fun clearByUser(signalHandler: SignalHandler) {
		model.setSignalManually(DigitalSignalFactory.of(bitWidth, 0), signalHandler)
	}

	/** Returns the [DigitalSignal] actually displayed. Mainly for testing.*/
	fun getDigitSignal(digitIndex: Int): DigitalSignal =
		numberView!!.getDigitSignal(digitIndex)

	override fun createPortView(template: PortView<*>?): PortView<*> {
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

	override fun updateViewImpl() {
		numberView = NumberView(signalRepresentation, bitWidth, drawBox = bitWidth.width > 1)
		numberViewOwner?.dispose()
		numberViewOwner = DrawableOwner(this, numberView!!)

		numberView!!.setSignal(model.signal!!)

		arrowPath = ArrowPath.Companion.Builder(
			orientation = orientation,
			contentDimension = Dimension2D(numberView!!.widthInt, numberView!!.heightInt)
		)
			.build(portType === PortType.INOUT)

		numberView!!.setBounds(
			arrowPath!!.contentLocation.x, arrowPath!!.contentLocation.y,
			numberView!!.bounds.width, numberView!!.bounds.height)
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

	/** Consumes a key the user pressed during simulation while this [DigitalCircuitInOutView] has focus.*/
	fun consumeKey(key: Int, contextHolder: GraphApplicationContextHolder, keyEvent: KeyEvent? = null, skipAnimation: Boolean = false) {
		invalidate()
		if (keyEvent != null && keyEvent.modifiers != 0) {
			// Ignore everything that should be handled by MenuItem accelerators
		} else if (key == KeyEvent.VK_ESCAPE) {
			hideKeyboard()
		} else if (key == KeyEvent.VK_SPACE) {
			// Ignore. This triggers PauseOrResumeAction. A key that triggers the KeyAccelerator of a MenuItem
			// shouldn't be forwarded to the focus component, but didn't find out why it is.
		} else if (key == KeyEvent.VK_META || key == KeyEvent.VK_SHIFT || key == KeyEvent.VK_ALT || key == KeyEvent.VK_CTRL || key == KeyEvent.VK_ALT_GRAPH) {
			// Ignore menu item accelerator meta keys
		} else if (key == KeyEvent.VK_LEFT) {
			numberView!!.transferFocusLeft()
		} else if (key == KeyEvent.VK_RIGHT) {
			numberView!!.transferFocusRight()
		} else if (key == KeyEvent.VK_ENTER && checkTopLevelKey()) {
			if (signalRepresentation == DigitalSignalRepresentation.BINARY) {
				toggleFocusBitWithEnter(contextHolder.scheduler)
			}
		} else if (key == KeyEvent.VK_DELETE && portType == PortType.INOUT && checkTopLevelKey()) {
			consumeSignal(
				DigitalSignalFactory.undefined(BitWidth.of(signalRepresentation.bitCount)),
				contextHolder)
		} else {
			if (checkTopLevelKey()) {
				consumeSignal(
					signalRepresentation.digitToWord(BitWidth.of(signalRepresentation.bitCount), key.toChar()),
					contextHolder,
					skipAnimation)
			} else {
				rejectSignal(contextHolder, skipAnimation)
			}
		}
		validate()
	}

	private fun consumeSignal(signal: DigitalSignal?, contextHolder: GraphApplicationContextHolder, skipAnimation: Boolean = false) {
		signal?.let {
			signalRepresentation.withDigit(model.signal!!, it, numberView!!.focusIndex!!)
		}?.let {
			model.setSignalManually(it, contextHolder.scheduler)
			numberView!!.transferFocusRight()
		} ?: rejectSignal(contextHolder, skipAnimation)
	}

	private fun rejectSignal(contextHolder: GraphApplicationContextHolder, skipAnimation: Boolean) {
		if (!skipAnimation && !isShaking) {
			isShaking = true
			contextHolder.animator
				.schedule(ShakeLocatableAnimation(numberView!!))
				.addListener(object : AnimationTaskAdapter() {
					override fun ended(task: AnimationTask) {
						isShaking = false
					}
				})
				.start()
		}
	}

	private fun checkTopLevelKey(): Boolean {
		if (!model.isToplevel) {
			eventBus.post(ComponentMessage(type = ComponentMessageType.Error, source = this@DigitalCircuitInOutView, messageKey = "antares.msg.ChildGraphInputManipulation"))
			return false
		}
		return true
	}

	private fun toggleFocusBitWithEnter(signalHandler: SignalHandler) {
		model.toggleBit(numberView!!.focusIndex!!, false, signalHandler)
	}

	private fun displayKeyboard(context: ActorInteractionContext): ActorInteractionHandler {
		hideKeyboard()

		with(context.view as DrawingView<*>) {
			popupKeyboardView = this
			popupKeyboard = CircuitInOutKeyboard(
				this@DigitalCircuitInOutView,
				context.view,
				applicationContextHolder as GraphApplicationContextHolder
			).also { keyboard ->
				animationContainer.add(keyboard)
				animationContainer.validate()
			}
		}
		return popupKeyboard!!.getActorInteractionHandler(context)
	}

	private fun hideKeyboard() {
		popupKeyboard?.let { keyboard ->
			popupKeyboardView?.animationContainer?.remove(keyboard)
		}
		popupKeyboard
		popupKeyboardView = null
	}

	private fun toggle(undefine: Boolean, context: ActorInteractionContext): ActorInteractionHandler? {
		var handler: ActorInteractionHandler? = null
		val digitIndex = getDigitIndexAt(context.x, context.y)
		if (digitIndex != null) {
			if (signalRepresentation == DigitalSignalRepresentation.BINARY) {
				model.toggleBit(digitIndex, undefine, context.signalHandler)
			} else {
				if (context.view is DrawingView<*>) {
					context.mouseEvent?.consume()
					handler = displayKeyboard(context)
				}
			}

			// Set the focus on the selected digit
			invalidate()
			requestFocus()
			numberView!!.setFocusTo(digitIndex)
			validate()

		}
		return handler
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
					source = this@DigitalCircuitInOutView,
					messageKey = "antares.msg.ChildGraphInputManipulation"))
				return null
			}

			// Don't consume event so that Canvas can gain focus
			return toggle(context.mouseEvent?.isAltDown ?: false, context) ?: this
		}

		override fun mouseDragged(context: ActorInteractionContext): ActorInteractionHandler = this

		override fun mouseReleased(context: ActorInteractionContext): ActorInteractionHandler? {
			if (!toggle) {
				context.mouseEvent?.consume()
				return toggle(false, context)
			}
			return null
		}

		override fun keyPressed(context: ActorInteractionContext): ActorInteractionHandler? {
			if (numberView!!.focusIndex != null) {
				consumeKey(context.keyEvent!!.key, context.view.applicationContextHolder as GraphApplicationContextHolder, context.keyEvent)
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
	}
}