package io.antarescircuit.antares.view.output

import io.antarescircuit.antares.model.output.Buzzer
import io.antarescircuit.antares.view.port.AbstractAntaresPortView
import io.antarescircuit.antares.view.port.DigitalPortView
import io.antarescircuit.jabbah.base.geom.Direction
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.sound.WaveformType
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.graphics.DropShadow
import io.antarescircuit.jabbah.draw.style.DrawStyleModule
import io.antarescircuit.jabbah.draw.style.StyleProvider
import io.antarescircuit.jabbah.draw.style.StyleType
import io.antarescircuit.jabbah.edit.Look.SCALE
import io.antarescircuit.jabbah.graph.view.LabeledRectangularVerticeView
import io.antarescircuit.jabbah.graph.view.port.PortLabelPosition

class BuzzerView(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	model: Buzzer = Buzzer()
) : LabeledRectangularVerticeView<Buzzer>(styleProvider, model) {

	companion object {
		private const val WIDTH = 6 * SCALE
		private const val HEIGHT = 8 * SCALE
		const val BASE_KEY_WAVEFORM = "library.element.Buzzer.waveform"
	}

	init {
		initExternalLabel(Direction.NORTH)
		modelExchanged(null)
		setBounds(AbstractAntaresPortView.LENGTH, -2 * SCALE, WIDTH, HEIGHT)
	}

	override val relativeExternalLabelLocation: Point2D
		get() = Point2D(AbstractAntaresPortView.LENGTH + WIDTH / 2.0, -2.0 * SCALE - LABEL_DIST)

	override fun modelExchanged(oldModel: Buzzer?) {
		super.modelExchanged(oldModel)
		addPortView(DigitalPortView(
			styleProvider,
			model.enablePort,
			x = AbstractAntaresPortView.LENGTH + 3 * SCALE,
			y = 6 * SCALE,
			direction = Direction.SOUTH,
			portLabelPosition = PortLabelPosition.INTERNAL))
		addPortView(DigitalPortView(
			styleProvider,
			model.frequencyPort,
			x = AbstractAntaresPortView.LENGTH,
			y = 0,
			direction = Direction.WEST,
			portLabelPosition = PortLabelPosition.INTERNAL,
			showBitWidthAnnotation = true))
		addPortView(DigitalPortView(
			styleProvider,
			model.volumePort,
			x = AbstractAntaresPortView.LENGTH,
			y = 4 * SCALE,
			direction = Direction.WEST,
			portLabelPosition = PortLabelPosition.INTERNAL,
			showBitWidthAnnotation = true))
	}

	@Suppress("unused") // Reflection
	var waveformType: WaveformType
		get() = model.waveformType
		set(value) {
			model.waveformType = value
		}

	override fun drawImpl(context: DrawContext) {
		drawImplBeforeBorder(context)
		drawShape(context)
		drawImplAfterBorder(context)
	}

	private fun drawShadow(context: DrawContext) {
		if (shadow) {
			DropShadow.draw(context, transparency) {
				context.g.fill(bounds)
			}
		}
	}

	private fun drawShape(context: DrawContext) {
		drawShadow(context)

		context.g.color = getApplicableBackgroundColor(context)
		context.g.fill(bounds)
		context.g.color = getApplicableForegroundColor(context)
		context.g.stroke = stroke
		context.g.draw(bounds)

		val centerX = AbstractAntaresPortView.LENGTH + WIDTH / 2.0
		val centerY = 1.5 * SCALE

		context.g.color = getApplicableForegroundColor(context)
		context.g.fillCircle(centerX, centerY, 0.5 * SCALE.toDouble())

		context.g.stroke = styleProvider.getStyle(StyleType.ANNOTATION).stroke
		context.g.drawCircle(centerX, centerY, 1.0 * SCALE.toDouble())
		context.g.drawCircle(centerX, centerY, 1.5 * SCALE.toDouble())

		context.g.drawLine(centerX, centerY - 1.5 * SCALE, centerX, centerY + 1.5 * SCALE)
		context.g.drawLine(centerX - 1.5 * SCALE, centerY, centerX + 1.5 * SCALE, centerY)
	}
}