package io.antarescircuit.antares.view.input

import io.antarescircuit.antares.model.input.DipSwitch
import io.antarescircuit.antares.model.signal.Bit
import io.antarescircuit.antares.model.signal.BitWidth
import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.antares.model.signal.DigitalSignalFactory
import io.antarescircuit.antares.view.port.AbstractAntaresPortView
import io.antarescircuit.antares.view.port.DigitalPortView
import io.antarescircuit.antares.view.style.AntaresTheme
import io.antarescircuit.jabbah.base.StringUtils
import io.antarescircuit.jabbah.base.event.KeyEvent
import io.antarescircuit.jabbah.base.geom.Direction
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.logger
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.Drawable
import io.antarescircuit.jabbah.draw.drawable.AbstractRectangle
import io.antarescircuit.jabbah.draw.graphics.DropShadow
import io.antarescircuit.jabbah.draw.style.DrawStyleModule
import io.antarescircuit.jabbah.draw.style.StyleProvider
import io.antarescircuit.jabbah.draw.style.StyleType
import io.antarescircuit.jabbah.draw.style.Themes
import io.antarescircuit.jabbah.edit.Look
import io.antarescircuit.jabbah.edit.model.AbstractComponent
import io.antarescircuit.jabbah.edit.model.text.HorizontalAlignment
import io.antarescircuit.jabbah.edit.model.text.Label
import io.antarescircuit.jabbah.edit.model.text.VerticalAlignment
import io.antarescircuit.jabbah.execution.SignalHandler
import io.antarescircuit.jabbah.execution.actor.ActorInteractionContext
import io.antarescircuit.jabbah.execution.actor.ActorInteractionHandler
import io.antarescircuit.jabbah.execution.actor.ActorView
import io.antarescircuit.jabbah.execution.actor.ClickableActorInteractionHandlerAdapter
import io.antarescircuit.jabbah.graph.GraphApplicationContext
import io.antarescircuit.jabbah.graph.model.Graph
import io.antarescircuit.jabbah.graph.model.GraphElementEvent
import io.antarescircuit.jabbah.graph.model.vertice.VerticeLink
import io.antarescircuit.jabbah.graph.view.AbstractGraphElementView
import io.antarescircuit.jabbah.graph.view.ControlView
import io.antarescircuit.jabbah.graph.view.ControlViewSource
import io.antarescircuit.jabbah.graph.view.OrientableExternallyLabeledRectangularVerticeView
import io.antarescircuit.jabbah.graph.view.port.PortView
import io.antarescircuit.jabbah.graph.view.vertice.SubGraphVerticeView
import io.antarescircuit.jabbah.io.Storable
import io.antarescircuit.jabbah.io.StoreReader
import io.antarescircuit.jabbah.io.StoreWriter

/**
 * A view representation of a [DipSwitch].
 *
 * The [orientation] property determines the [Direction] into which the single [PortView] points.
 */
class DipSwitchView(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	model: DipSwitch = DipSwitch(),
	orientation: Direction = Direction.NORTH
) : OrientableExternallyLabeledRectangularVerticeView<DipSwitch>(styleProvider, model, orientation),
	ControlViewSource<DipSwitch>,
	ControlView<DipSwitch>
{

	companion object {
		private val LOG by logger(DipSwitchView::class)
		const val PROP_ICON_PATH = "io.antarescircuit.antares.view.input.DipSwitchView.iconPath"

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

	var interactivePropagationDelay: Long
		get() = model.interactivePropagationDelay
		set(value) {
			model.interactivePropagationDelay = value
		}

	var bitWidth: BitWidth
		get() = model.bitWidth
		set(value) {
			if (value != bitWidth) {
				handleBitWidthChanged(value)
			}
		}

	private fun handleBitWidthChanged(bitWidth: BitWidth) {
		invalidate()
		clear()
		model.bitWidth = bitWidth
		updateView()
		postControlViewSourceChangeEvent(BaseModule.eventBus)
		invalidate()
		validate()
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

	/** ---- [ActorView] */

	override fun getActorInteractionHandler(context: ActorInteractionContext): ActorInteractionHandler {
		return actorInteractionHandler
	}

	/** ---- [AbstractGraphElementView] */

	override fun handleStateChanged(event: GraphElementEvent) {
		if (model.bitWidth.width != bitViews.size) {
			handleBitWidthChanged(model.bitWidth)
		}
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

	override fun canConsume(keyEvent: KeyEvent): Boolean =
		actorInteractionHandler.canConsume(keyEvent)

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

		context.g.color = context.chooseText(textColor)
		label.draw(context)
	}

	private fun drawDisabled(context: DrawContext) {
		context.g.color = Look.disabledColor()
		context.g.fillRect(xInt, yInt, widthInt, heightInt)
	}

	/** ---- [AbstractComponent] */

	override fun focusGained() {
		updateFocusIndex(bitsCount - 1)
		super<OrientableExternallyLabeledRectangularVerticeView>.focusGained()
	}

	override fun focusLost() {
		updateFocusIndex(null)
		super<OrientableExternallyLabeledRectangularVerticeView>.focusLost()
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

	override fun bindControlView(subGraphVerticeView: SubGraphVerticeView<*>, link: VerticeLink, startGraph: Graph) {
		this.model = link.getLinkedObject(startGraph) as DipSwitch
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

	/** ---- [OrientableExternallyLabeledRectangularVerticeView] */

	override fun updateViewImpl() {
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

	/** Allows toggling individual [BitView]s during execution.*/
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

		fun canConsume(keyEvent: KeyEvent): Boolean {
			return when (keyEvent.key) {
				KeyEvent.VK_LEFT -> true
				KeyEvent.VK_RIGHT -> true
				KeyEvent.VK_ENTER -> true
				KeyEvent.VK_LEFT -> true
				KeyEvent.VK_0 -> true
				KeyEvent.VK_1 -> true
				else -> false
			}
		}

		override fun keyPressed(context: ActorInteractionContext): ActorInteractionHandler? {
			LOG.trace("keyPressed '${context.keyEvent!!.key.toChar()}'")
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
			context.g.color = if (context.castedAppContext<GraphApplicationContext>()!!.showNetState) {
				transparent.applyTo(context.choose(styleProvider.getStyle(StyleType.BACKGROUND).color).backgroundColor)
			} else {
				transparent.applyTo(context.choose(this@DipSwitchView.color).backgroundColor)
			}
			context.g.fillRect(x, y, width, height)

			context.g.color = transparent.applyTo(context.choose(this@DipSwitchView.color).foregroundColor)
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
				context.g.color = context.chooseForeground(foregroundColor)
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
					transparent.applyTo(context.choose(this@DipSwitchView.color).textColor)
				}
			} else {
				transparent.applyTo(context.chooseText(this@DipSwitchView.textColor))
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