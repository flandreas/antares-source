package ch.scorpion.antares.view.gate

import ch.scorpion.antares.model.Logic
import ch.scorpion.antares.model.gate.TriStateBufferGate
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.view.DigitalComponentView
import ch.scorpion.antares.view.Handedness
import ch.scorpion.antares.view.port.DigitalPortView
import ch.scorpion.antares.view.symbolstyle.SymbolStyle
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.graph.view.port.PortLabelPosition
import ch.scorpion.jabbah.graph.view.vertice.AbstractRectangularVerticeView
import ch.scorpion.jabbah.graph.view.vertice.AbstractVerticeView
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter


/**
 * A view of a [TriStateBufferGate]
 */
class TriStateBufferGateView(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	model: TriStateBufferGate = TriStateBufferGate()
) : DigitalComponentView<TriStateBufferGate>(styleProvider, model) {

	var handedness: Handedness = Handedness.RIGHT
		set(value) {
			if (field != value) {
				invalidate()
				field = value
				modelExchanged(model)
				invalidate()
				update()
			}
		}

	init {
		//width = SymbolStyle.NOT_PATH.boundingBox.width
		//height = SymbolStyle.NOT_PATH.boundingBox.height
		modelExchanged(null)
	}

	/** ---- [AbstractVerticeView] */

	override fun modelExchanged(oldModel: TriStateBufferGate?) {
		super.modelExchanged(oldModel)

		val bounds = SymbolStyle.NOT_PATH.boundingBox

		val inputPortView = DigitalPortView(
			styleProvider = styleProvider,
			port = model.getInput(),
			direction = Direction.WEST)
		inputPortView.setLocation(inputPortView.unconnectedLength.toDouble(), 0.0)
		addPortView(inputPortView)

		addPortView(DigitalPortView(
			styleProvider = styleProvider,
			port = model.getOutput(),
			direction = Direction.EAST,
			x = inputPortView.unconnectedLength + bounds.width.toInt(),
			y = 0))

		val controlPorViewOffset = (DigitalPortView.LENGTH * 0.75).toInt()
		addPortView(DigitalPortView(
			styleProvider = styleProvider,
			port = model.getInput(2),
			direction = getDirectionOfHandedness(),
			portLabelPosition = PortLabelPosition.HIDE,
			x = (inputPortView.unconnectedLength + bounds.width / 2).toInt(),
			y = if (handedness == Handedness.RIGHT) controlPorViewOffset else -controlPorViewOffset,
			length = DigitalPortView.LENGTH - (DigitalPortView.LENGTH * 0.25).toInt()))

		setBounds(
			DigitalPortView.LENGTH.toDouble(), -SymbolStyle.NOT_PATH.boundingBox.height / 2,
			SymbolStyle.NOT_PATH.boundingBox.width, SymbolStyle.NOT_PATH.boundingBox.height)
	}

	/** ---- [Storable] interface */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		writer.writeString("controlOrientation", handedness.customName)
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		if (reader.hasAttribute("controlOrientation")) {
			handedness = Handedness.withName(reader.readString("controlOrientation"))
		}
	}

	/** ---- [AbstractRectangularVerticeView] */

	override fun drawImpl(context: DrawContext) {
		val oldColor = context.g.color
		val bounds = SymbolStyle.NOT_PATH.boundingBox

		super.drawImpl(context)

		if (context.useContextColors) {
			SymbolStyle.drawAmericanGate(this, x, y, bounds.height, SymbolStyle.NOT_PATH, context,
				context.color!!.foregroundColor, context.color!!.backgroundColor, stroke, false, transparency)
			GateMnemonic.drawTriStateBuffer(this, context, context.color!!.foregroundColor)
		} else {
			SymbolStyle.drawAmericanGate(this, x, y, bounds.height, SymbolStyle.NOT_PATH, context, foregroundColor,
				backgroundColor, stroke, false, transparency)
			GateMnemonic.drawTriStateBuffer(this, context, foregroundColor)
		}
		context.g.color = oldColor
	}

	/** ---- [TriStateBufferGateView] */

	var enableLogic: Logic
		get() = model.enableLogic
		set(value) {
			model.enableLogic = value
		}

	var bitWidth: BitWidth
		get() = model.bitWidth
		set(value) {
			model.bitWidth = value
		}

	private fun getDirectionOfHandedness(): Direction {
		return when (handedness) {
			Handedness.RIGHT -> Direction.SOUTH
			Handedness.LEFT -> Direction.NORTH
		}
	}
}