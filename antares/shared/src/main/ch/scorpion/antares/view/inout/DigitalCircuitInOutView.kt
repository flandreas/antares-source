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
import ch.scorpion.jabbah.base.geom.*
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.drawable.ShakeLocatableAnimation
import ch.scorpion.jabbah.draw.drawable.Transparent
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.actor.ActorInteractionContext
import ch.scorpion.jabbah.execution.actor.ActorInteractionHandler
import ch.scorpion.jabbah.graph.GraphApplicationContextHolder
import ch.scorpion.jabbah.graph.model.GraphElementEvent
import ch.scorpion.jabbah.graph.model.GraphPort
import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.graph.view.*
import ch.scorpion.jabbah.graph.view.port.PortView
import ch.scorpion.jabbah.graph.view.vertice.AbstractVerticeView
import ch.scorpion.jabbah.io.*

/**
 * A [DigitalCircuitInOutView] is an arrow-like [GraphPortView] for digital [GraphPort]s.
 */
class DigitalCircuitInOutView(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	model: DigitalCircuitInOut = DigitalCircuitInOutImpl(),
	eventBus: EventBus = BaseModule.eventBus,
	orientation: Direction = Direction.EAST
) : AbstractCircuitInOutView<DigitalCircuitInOut>(styleProvider, model, eventBus, orientation), ControlViewSource<DigitalCircuitInOut>,
	DigitalKeyboard.Target {

	companion object {
		val LOG by logger(DigitalCircuitInOutView::class)
	}

	override var signalRepresentation: DigitalSignalRepresentation = DigitalSignalRepresentation.BINARY
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

	/** Initialized in [updateView] */
	private var numberView: NumberView? = null

	/** Redirects validation requests from [NumberView] during shake animation by [rejectSignal]. */
	private var numberViewOwner: DrawableOwner? = null

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

	@Suppress("unused") // Reflection
	var customCanBeUndefined: Boolean
		get() = model.customCanBeUndefined
		set(value) {
			model.customCanBeUndefined = value
		}

	@Suppress("unused") // Reflection
	var startValue: Long?
		get() = model.startValue?.getValue()?.toLong()
		set(value) {
			model.startValue = if (value != null) {
				DigitalSignalFactory.of(bitWidth, value)
			} else {
				null
			}
		}

	@Suppress("unused") // Reflection
	var interactivePropagationDelay: Long
		get() = model.interactivePropagationDelay
		set(value) {
			model.interactivePropagationDelay = value
		}

	init {
		modelExchanged(null)
	}

	override fun modelExchanged(oldModel: DigitalCircuitInOut?) {
		super.modelExchanged(oldModel)
		model.signalRepresentation = signalRepresentation
		updateView()
	}

	/** ---- [DigitalKeyboard.Target] */

	override val keyboardTargetBoundingBox: RectangularShape get() = boundingBox

	override fun consumeKey(key: Int, contextHolder: GraphApplicationContextHolder, graphView: GraphView?) {
		consumeKey(key, contextHolder, null, false, graphView)
	}

	override fun clear(contextHolder: GraphApplicationContextHolder) {
		clearByUser(contextHolder.scheduler)
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
		// signalRepresentation is forwarded to the model, so resolve it after the model has been read
		reader.requestResolution(this, Reference(
			name = "signalRepresentation",
			additionalInfo = DigitalSignalRepresentation.withName(reader.readString("representation")),
			resolveAfter = listOf(reader.readInt("modelId"))))
		if (reader.hasAttribute("toggle")) {
			toggle = reader.readBoolean("toggle")
		}
	}

	override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) {
		super.resolve(reference, referenceResolver)
		if (reference.name == "signalRepresentation") {
			signalRepresentation = reference.additionalInfo as DigitalSignalRepresentation
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

		context.translated(getArrowPathTranslation()) {
			numberView!!.draw(it)
			drawDisabled(it)
		}
	}

	/** ---- [DigitalCircuitInOutView] */

	fun clearByUser(signalHandler: SignalHandler) {
		model.setSignalManually(DigitalSignalFactory.of(bitWidth, 0), signalHandler)
	}

	/** Returns the [DigitalSignal] actually displayed. Mainly for testing.*/
	fun getDigitSignal(digitIndex: Int): DigitalSignal =
		numberView!!.getDigitSignal(digitIndex)

	override fun createPortViewImpl(template: PortView<*>?, direction: Direction): PortView<*> =
		DigitalPortView(
			styleProvider = styleProvider,
			port = model.getPort(),
			direction = direction,
			customUnconnectedLength = template?.customUnconnectedLength,
			length = template?.length
		)

	override fun updateViewImpl() {
		numberView = NumberView(signalRepresentation, bitWidth, drawBox = bitWidth.width > 1)
		numberViewOwner?.dispose()
		numberViewOwner = DrawableOwner(this, numberView!!)

		numberView!!.setSignal(model.signal!!)

		arrowPath = ArrowPath.Companion.Builder(
			orientation = orientation,
			contentDimension = Dimension2D(numberView!!.widthInt, numberView!!.heightInt)
		).build(portType === PortType.INOUT)

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

	private fun canConsumeKey(key: Int): Boolean {
		return when (key) {
			KeyEvent.VK_SPACE -> false
			else -> true
		}
	}

	/** Consumes a key the user pressed during simulation while this [DigitalCircuitInOutView] has focus.*/
	fun consumeKey(key: Int, contextHolder: GraphApplicationContextHolder, keyEvent: KeyEvent? = null, skipAnimation: Boolean = false, graphView: GraphView? = null) {
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
				toggleFocusBitWithEnter(contextHolder.scheduler, graphView)
			}
		} else if (key == KeyEvent.VK_DELETE && portType == PortType.INOUT && checkTopLevelKey()) {
			consumeSignal(
				DigitalSignalFactory.undefined(BitWidth.of(signalRepresentation.bitCount)),
				contextHolder)
		} else {
			if (checkTopLevelKey()) {
				val keyChar = if (key >= KeyEvent.VK_NUMPAD_0 && key <= KeyEvent.VK_NUMPAD_9) {
					(KeyEvent.VK_0 + (key - KeyEvent.VK_NUMPAD_0)).toChar()
				} else {
					key.toChar()
				}
				consumeSignal(
					signalRepresentation.digitToWord(BitWidth.of(signalRepresentation.bitCount), keyChar),
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

	private fun toggleFocusBitWithEnter(signalHandler: SignalHandler, graphView: GraphView?) {
		model.toggleBit(numberView!!.focusIndex!!, false, signalHandler, graphView)
	}

	private fun displayKeyboard(context: ActorInteractionContext): ActorInteractionHandler {
		DigitalKeyboard.show(
			this,
			context.view as DrawingView<*>,
			context.view.applicationContextHolder as GraphApplicationContextHolder
		)
		return DigitalKeyboard.getActorInteractionHandler(context)
	}

	private fun hideKeyboard() {
		DigitalKeyboard.hide()
	}

	override fun toggle(undefine: Boolean, context: ActorInteractionContext): ActorInteractionHandler? {
		var handler: ActorInteractionHandler? = null
		val digitIndex = getDigitIndexAt(context.x, context.y)
		if (digitIndex != null) {
			if (signalRepresentation == DigitalSignalRepresentation.BINARY) {
				model.toggleBit(digitIndex, undefine, context.signalHandler, (context.view as DrawingView<*>).drawing as GraphView)
			} else {
				if (context.view is DrawingView<*>) {
					context.mouseEvent?.consumeEvent()
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

	override fun createActorInteractionHandler(): ToggleInteractionHandler = InteractionHandler()

	/**
	 * Allows to toggle individual [Bit]s by clicking with the mouse and entering
	 * individual digits with the keyboard.
	 */
	private inner class InteractionHandler : ToggleInteractionHandler() {

		override fun mouseMoved(context: ActorInteractionContext): ActorInteractionHandler? {
			if (model.isToplevel) {
				return super.mouseMoved(context)
			}
			return null
		}

		override fun mouseDragged(context: ActorInteractionContext): ActorInteractionHandler = this

		override fun mouseReleased(context: ActorInteractionContext): ActorInteractionHandler? {
			if (!toggle) {
				context.mouseEvent?.consumeEvent()
				return toggle(false, context)
			}
			return null
		}

		override fun canConsume(keyEvent: KeyEvent): Boolean =
			super.canConsume(keyEvent) && canConsumeKey(keyEvent.key)

		override fun keyPressed(context: ActorInteractionContext): ActorInteractionHandler? {
			if (numberView!!.focusIndex != null) {
				consumeKey(context.keyEvent!!.key, context.view.applicationContextHolder as GraphApplicationContextHolder,
					context.keyEvent, graphView = (context.view as DrawingView<*>).drawing as GraphView)
			}
			return null
		}

		override fun keyReleased(context: ActorInteractionContext): ActorInteractionHandler? {
			if (toggle) {
				return null
			}
			if (numberView!!.focusIndex != null) {
				if (context.keyEvent?.key == KeyEvent.VK_ENTER && checkTopLevelKey()) {
					toggleFocusBitWithEnter(context.signalHandler, (context.view as DrawingView<*>).drawing as GraphView)
				}
			}
			return null
		}
	}
}