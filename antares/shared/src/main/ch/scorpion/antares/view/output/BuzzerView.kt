package ch.scorpion.antares.view.output

import ch.scorpion.antares.model.output.Buzzer
import ch.scorpion.antares.view.DigitalComponentView
import ch.scorpion.antares.view.Look.SCALE
import ch.scorpion.antares.view.port.DigitalPortView
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.sound.WaveformType
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.graphics.DropShadow
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.StyleType
import ch.scorpion.jabbah.graph.view.port.PortLabelPosition

class BuzzerView(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	model: Buzzer = Buzzer()
) : DigitalComponentView<Buzzer>(styleProvider, model) {

	companion object {
		private const val WIDTH = 6 * SCALE
		private const val HEIGHT = 8 * SCALE
		const val BASE_KEY_WAVEFORM = "library.element.Buzzer.waveform"
	}

	init {
		modelExchanged(null)
		setBounds(DigitalPortView.LENGTH, -2 * SCALE, WIDTH, HEIGHT)
	}

	override fun modelExchanged(oldModel: Buzzer?) {
		super.modelExchanged(oldModel)
		addPortView(DigitalPortView(
			styleProvider,
			model.enablePort,
			x = DigitalPortView.LENGTH + 3 * SCALE,
			y = 6 * SCALE,
			direction = Direction.SOUTH,
			portLabelPosition = PortLabelPosition.INTERNAL))
		addPortView(DigitalPortView(
			styleProvider,
			model.frequencyPort,
			x = DigitalPortView.LENGTH,
			y = 0,
			direction = Direction.WEST,
			portLabelPosition = PortLabelPosition.INTERNAL,
			showBitWidthAnnotation = true))
		addPortView(DigitalPortView(
			styleProvider,
			model.volumePort,
			x = DigitalPortView.LENGTH,
			y = 4 * SCALE,
			direction = Direction.WEST,
			portLabelPosition = PortLabelPosition.INTERNAL,
			showBitWidthAnnotation = true))
	}

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

		val centerX = DigitalPortView.LENGTH + WIDTH / 2.0
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