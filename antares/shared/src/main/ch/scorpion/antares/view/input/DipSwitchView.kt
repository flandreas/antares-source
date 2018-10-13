package ch.scorpion.antares.view.input

import ch.scorpion.antares.model.input.DipSwitch
import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.Word
import ch.scorpion.antares.view.DigitalComponentView
import ch.scorpion.antares.view.Look
import ch.scorpion.antares.view.port.DigitalPortView
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.geom.RectangularShape
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.drawable.AbstractRectangle
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.StyleType
import ch.scorpion.jabbah.edit.model.text.HorizontalAlignment
import ch.scorpion.jabbah.edit.model.text.Label
import ch.scorpion.jabbah.edit.model.text.VerticalAlignment
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.actor.ActorInteractionContext
import ch.scorpion.jabbah.execution.actor.ActorInteractionHandler
import ch.scorpion.jabbah.execution.actor.ActorView
import ch.scorpion.jabbah.execution.actor.ClickableActorInteractionHandlerAdapter
import ch.scorpion.jabbah.graph.model.GraphElementEvent
import ch.scorpion.jabbah.graph.view.style.GraphStyleType

/** A view representation of a [DipSwitch].*/
class DipSwitchView(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	model: DipSwitch = DipSwitch()
) : DigitalComponentView<DipSwitch>(styleProvider, model) {

	companion object {
		const val PROP_ICON_PATH = "ch.scorpion.antares.view.input.DipSwitchView.iconPath"
		private const val CASE_INSET = Look.SCALE
		private const val LABEL_HEIGHT = 10
	}

	/** Contains the individual [BitView]s, starting with the lowest priority [Bit] at index 0.*/
	private val bitViews = mutableListOf<BitView>()

	private val actorInteractionHandler = InteractionHandler()

	/** Single instance used as flyweight to draw the index number above [BitView]s.*/
	private val labelFlyweight = Label(
		font = styleProvider.getStyle(GraphStyleType.ANNOTATION).font,
		text = "",
		horizontalAlignment = HorizontalAlignment.CENTER,
		verticalAlignment = VerticalAlignment.BOTTOM)

	init {
		modelExchanged(null)
	}

	override fun modelExchanged(oldModel: DipSwitch?) {
		super.modelExchanged(oldModel)
		val portView = DigitalPortView(
			styleProvider = styleProvider,
			port = model!!.getOutput(),
			direction = Direction.NORTH)
		portView.setLocation(0, portView.length)
		addPortView(portView)
		buildUI()
	}

	/** ---- UI properties */

	var bitWidth: BitWidth
		get() = model!!.bitWidth
		set(value) {
			if (value != bitWidth) {
				invalidate()
				model!!.bitWidth = value
				buildUI()
				invalidate()
				validate()
			}
		}

	/** ---- [ActorView] */

	override fun getActorInteractionHandler(context: ActorInteractionContext): ActorInteractionHandler? {
		return actorInteractionHandler
	}

	/** ---- [AbstractGraphElementView] */

	override fun handleStateChanged(event: GraphElementEvent) {
		invalidate()
		for ((i,view) in bitViews.withIndex()) {
			view.bit = model!!.value.bitAt(i)
		}
		super.handleStateChanged(event)
	}

	/** ---- [Drawable] interface */

	override fun drawImpl(context: DrawContext) {
		super.drawImpl(context)
		/*
		drawFill(context, bounds, backgroundColor)
		drawStroke(context, bounds, foregroundColor, stroke)
		*/
		drawFill(context, bounds, context.choose(color).backgroundColor)
		drawStroke(context, bounds, context.choose(color).foregroundColor, stroke)
		bitViews.forEach {
			it.draw(context)
			labelFlyweight.location = Point2D(it.bounds.centerX, it.bounds.minY - 2)
			labelFlyweight.text = it.index.toString()
			labelFlyweight.color = context.choose(color).textColor
			labelFlyweight.draw(context)
		}
	}

	private fun buildUI() {
		setBounds(calculateBounds())
		bitViews.clear()
		for (index in 0 until model!!.bitWidth.width) {
			val x = width / 2 - (index + 1) * BitView.WIDTH - CASE_INSET
			val y = bounds.y + LABEL_HEIGHT + CASE_INSET
			bitViews.add(BitView(index, x, y, styleProvider))
		}
	}

	/** ---- [DipSwitchView] */

	private fun calculateBounds(): RectangularShape {
		val width = calculateWidth()
		val height = calculateHeight()
		return Rectangle2D(-width / 2, DigitalPortView.LENGTH.toDouble(), width, height)
	}

	private fun calculateWidth(): Double {
		return model!!.bitWidth.width * BitView.WIDTH + 2 * CASE_INSET
	}

	private fun calculateHeight(): Double {
		return BitView.HEIGHT + LABEL_HEIGHT + 2 * CASE_INSET
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

	/** Allows to toggle individual [BitView]s during execution.*/
	private inner class InteractionHandler : ClickableActorInteractionHandlerAdapter() {

		override fun mousePressed(context: ActorInteractionContext): ActorInteractionHandler? {
			toggle(context.signalHandler, context.x, context.y)
			return null
		}

		private fun toggle(signalHandler: SignalHandler, x: Double, y: Double) {
			getBitViewIndexAt(x - location.x, y - location.y)?.let {
				var signal = model!!.value as Word?
				if (signal == null) {
					signal = Word.allOf(model!!.bitWidth, Bit.Undefined)
				}
				var bit = signal.bitAt(it)
				if (!bit.isDefined) {
					bit = Bit.False
				}
				bit = bit.not()
				model!!.setBit(it, bit, signalHandler)
			}
		}
	}

	/**
	 * Displays a single [Bit] as a small switch.
	 * @property index the index (starting with 0) of the displayed [Bit] within a [Word]
	 * @param x the x coordinate of the upper-left corner
	 * @param y the y coordinate of the upper-left corner
	 */
	private class BitView(
		val index: Int,
		x: Double,
		y: Double,
		private val styleProvider: StyleProvider
	) : AbstractRectangle(x, y, WIDTH, HEIGHT) {

		companion object {
			const val HEIGHT = 5.0 * Look.SCALE
			const val WIDTH = 2.0 * Look.SCALE
			private val KNOB_INSET = 2
		}

		var bit: Bit = Bit.False

		override val lineWidth: Double get() = 0.0

		override fun draw(context: DrawContext) {
			context.g.color = context.choose(styleProvider.getStyle(StyleType.BACKGROUND).color).backgroundColor
			context.g.fillRect(x, y, width, height)

			context.g.color = context.choose(styleProvider.getStyle(StyleType.FIGURE).color).foregroundColor
			context.g.stroke = styleProvider.getStyle(GraphStyleType.ANNOTATION).stroke
			context.g.drawRect(x, y, width, height)

			val bitY = if (bit.isSet) {
				y + KNOB_INSET
			} else {
				y + height / 2
			}
			context.g.color = context.choose(bit.color).foregroundColor

			context.g.fillRect(
				x + KNOB_INSET,
				bitY,
				width - 2 * KNOB_INSET,
				height / 2 - 1 * KNOB_INSET)
		}
	}
}