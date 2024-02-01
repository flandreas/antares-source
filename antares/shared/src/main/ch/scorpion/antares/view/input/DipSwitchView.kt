package ch.scorpion.antares.view.input

import ch.scorpion.antares.model.input.DipSwitch
import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.DigitalSignalFactory
import ch.scorpion.antares.view.Look
import ch.scorpion.antares.view.OrientableRectangularVerticeView
import ch.scorpion.antares.view.port.AbstractAntaresPortView
import ch.scorpion.antares.view.port.DigitalPortView
import ch.scorpion.antares.view.style.AntaresTheme
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.event.KeyEvent
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.drawable.AbstractRectangle
import ch.scorpion.jabbah.draw.drawable.RotationDirection
import ch.scorpion.jabbah.draw.graphics.DropShadow
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.StyleType
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.model.AbstractComponent
import ch.scorpion.jabbah.edit.model.text.*
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.actor.ActorInteractionContext
import ch.scorpion.jabbah.execution.actor.ActorInteractionHandler
import ch.scorpion.jabbah.execution.actor.ActorView
import ch.scorpion.jabbah.execution.actor.ClickableActorInteractionHandlerAdapter
import ch.scorpion.jabbah.graph.GraphApplicationContext
import ch.scorpion.jabbah.graph.model.GraphElementEvent
import ch.scorpion.jabbah.graph.view.AbstractGraphElementView
import ch.scorpion.jabbah.graph.view.ControlView
import ch.scorpion.jabbah.graph.view.ControlViewSource
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

/** A view representation of a [DipSwitch].*/
class DipSwitchView(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	model: DipSwitch = DipSwitch(),
	orientation: Direction = Direction.NORTH
) : OrientableRectangularVerticeView<DipSwitch>(styleProvider, model), ControlViewSource<DipSwitch>, ControlView<DipSwitch>, Labeled {

	companion object {
		private val LOG by logger(DipSwitchView::class)
		const val PROP_ICON_PATH = "ch.scorpion.antares.view.input.DipSwitchView.iconPath"
		private const val LABEL_DIST = Look.SCALE

		private const val KNOB_HEIGHT = 5.0 * Look.SCALE
		private const val KNOB_WIDTH = 2.0 * Look.SCALE
		private const val KNOB_INSET = 3
	}

	/** Contains the individual [BitView]s, starting with the lowest priority [Bit] at index 0.*/
	private val bitViews = mutableListOf<BitView>()

	/** The index of [bitViews] that has the focus, or `null` if none has the focus.*/
	private var focusIndex: Int? = null

	private val actorInteractionHandler = InteractionHandler()

	/** Single instance used as flyweight to draw the index number above [BitView]s.*/
	private val bitLabelFlyweight = Label(
		font = styleProvider.getStyle(StyleType.ANNOTATION).font.deriveFont(
			styleProvider.getStyle(StyleType.ANNOTATION).font.size + 2
		),
		text = "",
		horizontalAlignment = HorizontalAlignment.CENTER,
		verticalAlignment = VerticalAlignment.TOP)

	/** The [Label] that displays the name of this [DipSwitchView].*/
	override val label = Label(
		font = font,
		text = model.name)

	override var orientation: Direction = orientation
		set(value) {
			if (value != field) {
				invalidate()
				field = value
				updateView()
				invalidate()
				validate()
			}
		}

	private val bitsCount: Int get() = model.bitWidth.width

	init {
		isFocusable = true
		modelExchanged(null)
	}

	override fun modelExchanged(oldModel: DipSwitch?) {
		super.modelExchanged(oldModel)
		val portView = DigitalPortView(
			styleProvider = styleProvider,
			port = model.getOutput())
		addPortView(portView)
		updateView()
	}

	/** ---- UI properties */

	var name: String?
		get() = model.name
		set(value) {
			model.name = value
		}

	var bitWidth: BitWidth
		get() = model.bitWidth
		set(value) {
			if (value != bitWidth) {
				invalidate()
				clear()
				model.bitWidth = value
				updateView()
				postControlViewSourceChangeEvent(BaseModule.eventBus)
				invalidate()
				validate()
			}
		}

	@Suppress("unused") // Reflection
	var initialValue: Long
		get() = model.initialValue.getValue().toLong()
		set(value) {
			if (value != initialValue) {
				model.initialValue = DigitalSignalFactory.of(bitWidth, value)
				invalidate()
				validate()
			}
		}

	@Suppress("unused") // Reflection
	var retainValue: Boolean
		get() = model.retainValue
		set(value) {
			if (value != retainValue) {
				model.retainValue = value
			}
		}

	/** ---- [Component] */

	override val useOrientation: Boolean get() = true

	override val boundingBox: Rectangle2D
		get() {
			val bb = super.boundingBox
			if (StringUtils.isNotEmpty(label.text)) {
				val lbb = label.boundingBox.moveBy(location)
				bb.add(lbb)
			}
			return bb
		}

	override fun rotate(direction: RotationDirection, pivot: Point2D?) {
		orientation = when (direction) {
			RotationDirection.Clockwise -> Direction.of(orientation.rotation.previous())
			RotationDirection.CounterClockwise -> Direction.of(orientation.rotation.next())
		}
		pivot?.let {
			location = direction.rotation.rotatePointAround(it, location)
		}
	}

	/** ---- [ActorView] */

	override fun getActorInteractionHandler(context: ActorInteractionContext): ActorInteractionHandler {
		return actorInteractionHandler
	}

	/** ---- [AbstractGraphElementView] */

	override fun handleStateChanged(event: GraphElementEvent) {
		invalidate()
		for ((i, view) in bitViews.withIndex()) {
			view.bit = model.signal!!.bitAt(i)
		}
		label.text = StringUtils.orEmpty(model.name)
		super.handleStateChanged(event)
	}

	/** ---- [Storable] interface */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		writer.writeString("orientation", orientation.customName)
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		orientation = Direction.withName(reader.readString("orientation"))
	}

	/** ---- [Drawable] interface */

	override fun drawImpl(context: DrawContext) {
		val appContext = context.castedAppContext<GraphApplicationContext>()!!
		if (shadow) {
			DropShadow.draw(context, transparency) {
				context.g.fill(bounds)
			}
		}
		super.drawImpl(context)

		bitViews.forEach {
			it.draw(context)
		}

		if (appContext.isExecute) {
			if (model.shouldDrawDisabled(appContext)) {
				drawDisabled(context)
			}
		}

		context.g.color = context.choose(color).textColor
		label.draw(context)
	}

	private fun drawDisabled(context: DrawContext) {
		context.g.color = Look.disabledColor()
		context.g.fillRect(xInt, yInt, widthInt, heightInt)
	}

	/** ---- [AbstractComponent] */

	override fun focusGained() {
		updateFocusIndex(bitsCount - 1)
		super<OrientableRectangularVerticeView>.focusGained()
	}

	override fun focusLost() {
		updateFocusIndex(null)
		super<OrientableRectangularVerticeView>.focusLost()
	}

	fun transferFocusRight() {
		if (focusIndex != null) {
			updateFocusIndex(if (focusIndex == 0) bitsCount - 1 else focusIndex!! - 1)
		}
	}

	fun transferFocusLeft() {
		if (focusIndex != null) {
			updateFocusIndex(if (focusIndex == bitsCount - 1) 0 else focusIndex!! + 1)
		}
	}

	fun setFocusTo(newFocusIndex: Int) {
		updateFocusIndex(newFocusIndex)
	}

	private fun updateFocusIndex(newIndex: Int?) {
		invalidate()
		if (focusIndex != null) {
			bitViews[focusIndex!!].hasFocus = false
		}
		focusIndex = newIndex
		if (focusIndex != null) {
			bitViews[focusIndex!!].hasFocus = true
		}
		validate()
	}

	/** ---- [ControlViewSource] */

	override val controlId: String get() = "dipSwitch:" + model.id

	override val controlName: String get() = super.controlName

	override val iconPath: String get() = BaseModule.properties.getString(PROP_ICON_PATH)

	override fun createControlView(): ControlView<DipSwitch> {
		val clone = DipSwitchView(styleProvider, model, orientation)
		clone.isShowPortViews = false
		clone.location = Point2D.ZERO
		copyControlViewProperties(this, clone)
		return clone
	}

	/** ---- [ControlView] */

	override var isActiveControlView: Boolean = false

	override val mirrorWidth: Double get() = -(2 * AbstractAntaresPortView.LENGTH + width)

	override fun bindControlView(subGraphVerticeView: SubGraphVerticeView<*>, model: DipSwitch) {
		this.model = model
	}

	override fun writeModelProperties(writer: StoreWriter) {
		if (StringUtils.isNotEmpty(name)) {
			writer.writeString("name", name!!)
		}
		writer.writeInt("bitWidth", bitWidth.width)
	}

	override fun readModelProperties(reader: StoreReader) {
		if (reader.hasAttribute("name")) {
			name = reader.readString("name")
		}
		bitWidth = BitWidth.of(reader.readInt("bitWidth"))
	}

	override fun sourcePropertiesChanged(source: ControlViewSource<DipSwitch>) {
		if (source is DipSwitchView) {
			copyControlViewProperties(source, this)
		}
	}

	private fun copyControlViewProperties(source: DipSwitchView, dest: DipSwitchView) {
		dest.name = source.name
		dest.orientation = source.orientation
		dest.bitWidth = source.bitWidth
	}

	/** ---- [DipSwitchView] */

	private fun clear() {
		bitViews.clear()
		focusIndex = null
	}

	private val upperLeftBoundsEdge: Point2D
		get() = when (orientation) {
			Direction.WEST -> Point2D(getOutput().length.toDouble(), -KNOB_HEIGHT / 2)
			Direction.SOUTH -> Point2D(-calculateWidth() / 2, -getOutput().length.toDouble() - KNOB_HEIGHT)
			Direction.EAST -> Point2D(-getOutput().length.toDouble() - calculateWidth(), -KNOB_HEIGHT / 2)
			Direction.NORTH -> Point2D(-calculateWidth() / 2, getOutput().length.toDouble())
		}

	private fun updateView() {
		val edge = upperLeftBoundsEdge
		setBounds(edge.x, edge.y, calculateWidth(), KNOB_HEIGHT)

		bitViews.clear()
		val maxIndex = model.bitWidth.width - 1
		for (index in 0..maxIndex) {
			val xx = x + (maxIndex - index) * KNOB_WIDTH
			bitViews.add(BitView(index, model.signal!!.bitAt(index), xx, y, styleProvider))
		}

		getOutput().direction = orientation
		getOutput().setLocation(getOutput().length * orientation.opposite().dx, getOutput().length * orientation.opposite().dy)

		updateLabel()
	}

	private fun calculateWidth(): Double = model.bitWidth.width * KNOB_WIDTH

	/**
	 * Returns the bit index of the [BitView] at the specified relative coordinates.
	 */
	private fun getBitViewIndexAt(x: Double, y: Double): Int? {
		for ((i, view) in bitViews.withIndex()) {
			if (view.contains(x, y)) {
				return i
			}
		}
		return null
	}

	/**
	 * Updates the text, the location and the alignments of the external [Label] depending
	 * on the orientation of this [DipSwitchView].
	 */
	private fun updateLabel() {
		label.text = StringUtils.orEmpty(model.name)
		label.alignment = Alignment.forOrientation(orientation)
		label.location = when (orientation) {
			Direction.EAST -> Point2D(-getOutput().length - bounds.width - LABEL_DIST, 0.0)
			Direction.NORTH -> Point2D(0.0, getOutput().length + bounds.height + LABEL_DIST)
			Direction.WEST -> Point2D(getOutput().length + bounds.width + LABEL_DIST, 0.0)
			Direction.SOUTH -> Point2D(0.0, -getOutput().length - bounds.height - LABEL_DIST)
		}
	}

	/** Allows to toggle individual [BitView]s during execution.*/
	private inner class InteractionHandler : ClickableActorInteractionHandlerAdapter() {

		override fun mousePressed(context: ActorInteractionContext): ActorInteractionHandler? {
			getBitViewIndexAt(context.x - location.x, context.y - location.y)?.let {
				toggleImpl(it, context.signalHandler, all = context.mouseEvent?.isAltDown == true)
				requestFocus()
				setFocusTo(it)
			}
			context.mouseEvent?.consumeEvent()
			return null
		}

		override fun mouseClicked(context: ActorInteractionContext): ActorInteractionHandler {
			context.mouseEvent?.consumeEvent()
			return this
		}

		private fun toggleImpl(index: Int, signalHandler: SignalHandler, all: Boolean) {
			val bit = model.signal!!.bitAt(index).not()
			if (all) {
				model.setValue(DigitalSignalFactory.allOf(model.bitWidth, bit), signalHandler)
			} else {
				model.setBit(index, bit, signalHandler)
			}
		}

		override fun keyPressed(context: ActorInteractionContext): ActorInteractionHandler? {
			LOG.trace("keyPressed '${context.keyEvent!!.key.toChar()}'")
			val graphView = (context.view as DrawingView<*>).drawing as GraphView
			if (focusIndex != null) {
				when (context.keyEvent?.key) {
					KeyEvent.VK_LEFT -> transferFocusLeft()
					KeyEvent.VK_RIGHT -> transferFocusRight()
					KeyEvent.VK_ENTER -> toggleImpl(focusIndex!!, context.signalHandler, all = context.keyEvent?.isAltDown == true)
					KeyEvent.VK_0, KeyEvent.VK_NUMPAD_0 -> {
						model.setBit(focusIndex!!, Bit.False, context.signalHandler)
						transferFocusRight()
					}
					KeyEvent.VK_1, KeyEvent.VK_NUMPAD_1 -> {
						model.setBit(focusIndex!!, Bit.True, context.signalHandler)
						transferFocusRight()
					}
				}
			}
			return null
		}
	}

	/**
	 * Displays a single [Bit] as a small switch.
	 * @property index the index (starting with 0) of the displayed [Bit] within a [DigitalSignal]
	 * @param bit Contains the value this [BitView] displays
	 * @param x the x coordinate of the upper-left corner
	 * @param y the y coordinate of the upper-left corner
	 */
	private inner class BitView(
		val index: Int,
		var bit: Bit = Bit.False,
		x: Double,
		y: Double,
		private val styleProvider: StyleProvider
	) : AbstractRectangle(x, y, KNOB_WIDTH, KNOB_HEIGHT) {

		/** Controls whether this [BitView] has the focus and should draw a focus border.*/
		var hasFocus: Boolean = false

		/** ---- [Drawable] interface */

		override val lineWidth: Double get() = 0.0

		override fun draw(context: DrawContext) {
			context.g.color = transparent.applyTo(context.choose(styleProvider.getStyle(StyleType.BACKGROUND).color).backgroundColor)
			context.g.fillRect(x, y, width, height)

			context.g.color = transparent.applyTo(context.choose(styleProvider.getStyle(StyleType.FIGURE).color).foregroundColor)
			context.g.stroke = styleProvider.getStyle(StyleType.ANNOTATION).stroke
			context.g.drawRect(x, y, width, height)

			val bitY = if (bit.isSet) {
				y + KNOB_INSET
			} else {
				y + height / 2
			}

			if (context.castedAppContext<GraphApplicationContext>()!!.isExecute) {
				context.g.color = transparent.applyTo(context.choose(bit.color).foregroundColor)
			} else {
				context.g.color = context.choose(color).foregroundColor
			}

			context.g.fillRect(
				x + KNOB_INSET,
				bitY,
				width - 2 * KNOB_INSET,
				height / 2 - 1 * KNOB_INSET)

			val bitLabelColor = if (bit.isSet) {
				if (context.castedAppContext<GraphApplicationContext>()!!.isExecute) {
					bit.color.textColor
				} else {
					transparent.applyTo(context.choose(styleProvider.getStyle(StyleType.BACKGROUND).color).backgroundColor)
				}
			} else {
				transparent.applyTo(context.choose(this@DipSwitchView.color).textColor)
			}

			// Draw index label
			with(bitLabelFlyweight) {
				location = Point2D(this@BitView.bounds.centerX, this@BitView.bounds.minY + 4)
				text = (index % 10).toString()
				color = bitLabelColor
				draw(context)
			}

			if (hasFocus && context.castedAppContext<GraphApplicationContext>()!!.isExecute) {
				drawFocus(context)
			}
		}

		private fun drawFocus(context: DrawContext) {
			context.g.color = transparent.applyTo(Themes.get<AntaresTheme>().focus.color.foregroundColor)
			context.g.stroke = Themes.get<AntaresTheme>().focus.stroke
			context.g.drawRect(x + 1, y + 1, KNOB_WIDTH - 2, KNOB_HEIGHT - 2)
		}
	}
}