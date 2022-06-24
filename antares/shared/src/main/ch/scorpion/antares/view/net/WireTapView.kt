package ch.scorpion.antares.view.net

import ch.scorpion.antares.model.net.WireTap
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.view.DigitalComponentView
import ch.scorpion.antares.view.Look
import ch.scorpion.antares.view.PortViewSpacing
import ch.scorpion.antares.view.port.DigitalPortView
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.graph.view.port.PortLabelPosition
import ch.scorpion.jabbah.graph.view.style.GraphStyleType
import ch.scorpion.jabbah.graph.view.vertice.AbstractVerticeView
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

class WireTapView(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	model: WireTap = WireTap()
) : DigitalComponentView<WireTap>(styleProvider, model) {

	companion object {
		private const val INSET = 0
		private const val X_DISPLACEMENT = 2 * Look.SCALE
		private const val Y_DISPLACEMENT = 2 * Look.SCALE
		private const val INPUT_PIN_LENGTH = 0
	}

	var inputBitWidth: BitWidth
		get() = model.inputBitWidth
		set(value) {
			if (value != inputBitWidth) {
				model.inputBitWidth = value
			}
		}

	var outputBitWidth: BitWidth
		get() = model.outputBitWidth
		set(value) {
			if (value != outputBitWidth) {
				model.outputBitWidth = value
			}
		}

	var portViewSpacing: PortViewSpacing = PortViewSpacing.Narrow
		set(value) {
			if (field != value) {
				invalidate()
				field = value
				updateGeometry()
				invalidate()
				update()
			}
		}

	init {
		modelExchanged(null)
	}

	override fun modelExchanged(oldModel: WireTap?) {
		super.modelExchanged(oldModel)

		addPortView(createInputPortView())
		for (i in 0 until model.tapCount) {
			addPortView(createOutputPortView(i))
		}

		updateGeometry()
	}

	private fun createInputPortView(): DigitalPortView {
		return DigitalPortView(
			styleProvider,
			model.getPort(1),
			direction = Direction.NORTH,
			length = INPUT_PIN_LENGTH,
			showBitWidthAnnotation = false)
	}

	private fun createOutputPortView(index: Int): DigitalPortView {
		return DigitalPortView(
			styleProvider,
			model.getPort(index + 2),
			direction = Direction.EAST,
			showBitWidthAnnotation = false,
			portLabelPosition = PortLabelPosition.EXTERNAL)
	}

	private fun updateGeometry() {
		val width = X_DISPLACEMENT
		val height = INSET + Y_DISPLACEMENT + portViewSpacing.value * (model.tapCount - 1)
		setBounds(Rectangle2D(0, INPUT_PIN_LENGTH, width, height))

		var y = INSET + Y_DISPLACEMENT
		for (i in 0 until model.tapCount) {
			getPortView(model.getPort(i + 2))!!.location = Point2D(X_DISPLACEMENT, y)
			y += portViewSpacing.value
		}
	}

	/** ---- [AbstractVerticeView] */

	override fun drawImpl(context: DrawContext) {
		super.drawImpl(context)
		if (context.useContextColors) {
			drawImpl(context, context.color!!.foregroundColor)
		} else {
			drawImpl(context, foregroundColor)
		}
	}

	private fun drawImpl(context: DrawContext, lineColor: Color) {
		/*
		// TODO Choose Stroke according to outputBitWidth
		context.g.stroke = styleProvider.getStyle(GraphStyleType.EDGE).stroke
		context.g.color = transparent.applyTo(lineColor)
		*/

		for (i in 0 until model.tapCount) {
			val pv = getPortView(model.getPort(i + 2))!!
			val pvLoc = pv.location
			pv.prepareConnectionDrawContext(context)
			
			context.g.drawLine(pvLoc.x, pvLoc.y, pvLoc.x - X_DISPLACEMENT, pvLoc.y - Y_DISPLACEMENT)
			if (i > 0) {
				context.g.drawLine(
					pvLoc.x - X_DISPLACEMENT, pvLoc.y - Y_DISPLACEMENT,
					pvLoc.x - X_DISPLACEMENT, pvLoc.y - Y_DISPLACEMENT - portViewSpacing.value)
			} else if (INSET > 0) {
				context.g.drawLine(
					pvLoc.x - X_DISPLACEMENT, pvLoc.y - Y_DISPLACEMENT,
					pvLoc.x - X_DISPLACEMENT, pvLoc.y - Y_DISPLACEMENT - INSET)
			}
		}
	}

	/** ---- [Storable] interface */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		if (portViewSpacing != PortViewSpacing.Narrow) {
			writer.writeString("portViewSpacing", portViewSpacing.customName)
		}
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		if (reader.hasAttribute("portViewSpacing")) {
			portViewSpacing = PortViewSpacing.withName(reader.readString("portViewSpacing"))
		}
		updateGeometry()
	}
}