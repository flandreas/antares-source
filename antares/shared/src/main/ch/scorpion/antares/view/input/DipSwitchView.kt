package ch.scorpion.antares.view.input

import ch.scorpion.antares.model.input.DipSwitch
import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.Word
import ch.scorpion.antares.view.DigitalComponentView
import ch.scorpion.antares.view.Look
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
import ch.scorpion.jabbah.draw.graphics.DropShadow
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.StyleType
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.model.AbstractComponent
import ch.scorpion.jabbah.edit.model.text.Alignment
import ch.scorpion.jabbah.edit.model.text.HorizontalAlignment
import ch.scorpion.jabbah.edit.model.text.Label
import ch.scorpion.jabbah.edit.model.text.VerticalAlignment
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
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

/** A view representation of a [DipSwitch].*/
class DipSwitchView(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	model: DipSwitch = DipSwitch(),
	orientation: Direction = Direction.NORTH
) : DigitalComponentView<DipSwitch>(styleProvider, model), ControlViewSource<DipSwitch>, ControlView<DipSwitch> {

	companion object {
		private val LOG by logger(DipSwitchView::class)
		const val PROP_ICON_PATH = "ch.scorpion.antares.view.input.DipSwitchView.iconPath"
		private const val CASE_INSET = Look.SCALE
		private const val BIT_LABEL_HEIGHT = 10
		private const val LABEL_DIST = Look.SCALE

		private const val KNOB_HEIGHT = 5.0 * Look.SCALE
		private const val KNOB_WIDTH = 2.0 * Look.SCALE
		private const val KNOB_INSET = 2
	}

	/** Contains the individual [BitView]s, starting with the lowest priority [Bit] at index 0.*/
	private val bitViews = mutableListOf<BitView>()

	/** The index of [bitViews] that has the focus, or `null` if none has the focus.*/
	private var focusIndex: Int? = null

	private val actorInteractionHandler = InteractionHandler()

	/** Single instance used as flyweight to draw the index number above [BitView]s.*/
	private val bitLabelFlyweight = Label(
		font = styleProvider.getStyle(StyleType.ANNOTATION).font,
		text = "",
		horizontalAlignment = HorizontalAlignment.CENTER,
		verticalAlignment = VerticalAlignment.BOTTOM)

	/** The [Label] that displays the name of this [DipSwitchView].*/
	private val label = Label(
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
				invalidate()
				validate()
			}
		}

	var initialValue: Long
		get() = model.initialValue.getValue()
		set(value) {
			if (value != initialValue) {
				model.initialValue = Word.of(bitWidth, value)
				invalidate()
				validate()
			}
		}

	/** ---- [Component] */

	override val useRotation: Boolean get() = false

	override val boundingBox: Rectangle2D
		get() {
			val bb = super.boundingBox
			if (StringUtils.isNotEmpty(label.text)) {
				val lbb = label.boundingBox.moveBy(location)
				bb.add(lbb)
			}
			return bb
		}

	/** ---- [ActorView] */

	override fun getActorInteractionHandler(context: ActorInteractionContext): ActorInteractionHandler? {
		return actorInteractionHandler
	}

	/** ---- [AbstractGraphElementView] */

	override fun handleStateChanged(event: GraphElementEvent) {
		invalidate()
		for ((i, view) in bitViews.withIndex()) {
			view.bit = model.value.bitAt(i)
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
		if (shadow) {
			DropShadow.draw(context, transparency) {
				context.g.fill(bounds)
			}
		}
		super.drawImpl(context)
		drawFill(context, bounds, transparent.applyTo(if (context.useContextColors) context.choose(color).backgroundColor else propertiesBackgroundColor))
		drawStroke(context, bounds, transparent.applyTo(context.choose(color).foregroundColor), stroke)
		bitViews.forEach {
			it.draw(context)
			with(bitLabelFlyweight) {
				location = Point2D(it.bounds.centerX, it.bounds.minY - 2)
				text = it.index.toString()
				color = transparent.applyTo(context.choose(this@DipSwitchView.color).textColor)
				draw(context)
			}
		}

		if (context.castedAppContext<GraphApplicationContext>()!!.isExecute) {
			if (isDisabledFor(context) || model.inactive) {
				drawDisabled(context)
			}
		}

		context.g.color = context.choose(color).textColor
		label.draw(context)
	}

	private fun isDisabledFor(context: DrawContext): Boolean =
		model.disabled && context.castedAppContext<GraphApplicationContext>()?.isPausing == true

	private fun drawDisabled(context: DrawContext) {
		context.g.color = Look.disabledColor()
		context.g.fillRect(xInt, yInt, widthInt, heightInt)
	}

	/** ---- [AbstractComponent] */

	override fun focusGained() {
		updateFocusIndex(bitsCount - 1)
		super.focusGained()
	}

	override fun focusLost() {
		updateFocusIndex(null)
		super.focusLost()
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

	override fun bindToModel(model: DipSwitch) {
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
			Direction.WEST -> Point2D(getOutput().length.toDouble(), -calculateHeight() / 2)
			Direction.SOUTH -> Point2D(-calculateWidth() / 2, -getOutput().length.toDouble() - calculateHeight())
			Direction.EAST -> Point2D(-getOutput().length.toDouble() - calculateWidth(), -calculateHeight() / 2)
			Direction.NORTH -> Point2D(-calculateWidth() / 2, getOutput().length.toDouble())
		}

	private fun updateView() {
		val edge = upperLeftBoundsEdge
		setBounds(edge.x, edge.y, calculateWidth(), calculateHeight())

		bitViews.clear()
		val maxIndex = model.bitWidth.width - 1
		for (index in 0..maxIndex) {
			val xx = x + (maxIndex - index) * KNOB_WIDTH + CASE_INSET
			val yy = y + BIT_LABEL_HEIGHT + CASE_INSET
			bitViews.add(BitView(index, model.value.bitAt(index), xx, yy, styleProvider))
		}

		getOutput().direction = orientation
		getOutput().setLocation(getOutput().length * orientation.opposite().dx, getOutput().length * orientation.opposite().dy)

		updateLabel()
	}

	private fun calculateWidth(): Double {
		return model.bitWidth.width * KNOB_WIDTH + 2 * CASE_INSET
	}

	private fun calculateHeight(): Double {
		return KNOB_HEIGHT + BIT_LABEL_HEIGHT + 2 * CASE_INSET
	}

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
	 * Updates the text, the location nand the alignments of the external [Label] depending
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
			toggle(context.signalHandler, context.x, context.y)
			return null
		}

		private fun toggle(signalHandler: SignalHandler, x: Double, y: Double) {
			getBitViewIndexAt(x - location.x, y - location.y)?.let {
				toggleImpl(it, signalHandler)
				requestFocus()
				setFocusTo(it)
			}
		}

		private fun toggleImpl(index: Int, signalHandler: SignalHandler) {
			var signal = model.value as Word?
			if (signal == null) {
				signal = Word.allOf(model.bitWidth, Bit.Undefined)
			}
			var bit = signal.bitAt(index)
			if (!bit.isDefined) {
				bit = Bit.False
			}
			bit = bit.not()
			model.setBit(index, bit, signalHandler)
		}

		override fun keyPressed(context: ActorInteractionContext): ActorInteractionHandler? {
			LOG.debug("DipSwitchView: keyPressed '${context.keyEvent!!.key.toChar()}'")
			if (focusIndex != null) {
				when (context.keyEvent!!.key) {
					KeyEvent.VK_LEFT -> transferFocusLeft()
					KeyEvent.VK_RIGHT -> transferFocusRight()
					KeyEvent.VK_ENTER -> toggleImpl(focusIndex!!, context.signalHandler)
					KeyEvent.VK_0 -> {
						model.setBit(focusIndex!!, Bit.False, context.signalHandler)
						transferFocusRight()
					}
					KeyEvent.VK_1 -> {
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
	 * @property index the index (starting with 0) of the displayed [Bit] within a [Word]
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