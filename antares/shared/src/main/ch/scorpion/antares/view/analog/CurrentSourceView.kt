package ch.scorpion.antares.view.analog

import ch.scorpion.antares.model.analog.CurrentSource
import ch.scorpion.jabbah.edit.Look.SCALE
import ch.scorpion.antares.view.port.AbstractAntaresPortView.Companion.LENGTH
import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Direction.*
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.graphics.DropShadow
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.StyleType
import ch.scorpion.jabbah.graph.view.vertice.AbstractVerticeView

class CurrentSourceView(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	model: CurrentSource = CurrentSource()
) : AbstractAnalogVerticeView<CurrentSource>(styleProvider, model) {

	companion object {
		private const val SIZE = 6 * SCALE

		private val ARROW_PATH = System.createPath()
			.moveTo(0, 0)
			.lineTo(0.5 * SCALE, 1.0 * SCALE)
			.lineTo(-0.5 * SCALE, 1.0 * SCALE)
			.close()
	}

	@Suppress("unused") // Reflective bean property
	var current: Double
		get() = model.current
		set(value) { model.current = value }

	/** ---- [AbstractVerticeView] */

	override fun modelExchanged(oldModel: CurrentSource?) {
		super.modelExchanged(oldModel)
		addPortView(AnalogPortView(styleProvider, model.getPort(1), 0, LENGTH, NORTH))
		addPortView(AnalogPortView(styleProvider, model.getPort(2), 0, LENGTH + SIZE, SOUTH))
		setBounds(-SIZE / 2, LENGTH, SIZE, SIZE)
		updateLabel()
	}

	override fun drawImpl(context: DrawContext) {
		super.drawImpl(context)

		if (shadow) {
			DropShadow.draw(context, transparency) {
				context.g.fillOval(xInt, yInt, SIZE, SIZE)
			}
		}

		context.g.stroke = stroke
		context.g.color = context.chooseBackground(transparent.applyTo(backgroundColor))
		context.g.fillOval(xInt, yInt, SIZE, SIZE)
		context.g.color = context.chooseForeground(transparent.applyTo(foregroundColor))
		context.g.drawOval(xInt, yInt, SIZE, SIZE)

		// Arrow
		val tip = Point2D(0.0, LENGTH + h(1.5))
		context.g.stroke = styleProvider.getStyle(StyleType.ANNOTATION).stroke
		context.g.drawLine(0.0, LENGTH + h(2.5), 0.0, LENGTH + h(4.5))
		context.translated(tip) { it.g.fill(ARROW_PATH) }
	}

	/** ---- [AbstractAnalogVerticeView] */

	override val mainPropertyValue: String get() = "${model.current} A"

	override val labelLocation: Point2D
		get() = Point2D(bounds.maxX + MAIN_PROPERTY_LABEL_DIST, bounds.centerY)

	override val labelOrientation: Direction get() = EAST
}