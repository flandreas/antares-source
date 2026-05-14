package io.antarescircuit.antares.view.analog

import io.antarescircuit.antares.model.analog.CurrentSource
import io.antarescircuit.jabbah.edit.Look.SCALE
import io.antarescircuit.antares.view.port.AbstractAntaresPortView.Companion.LENGTH
import io.antarescircuit.jabbah.base.System
import io.antarescircuit.jabbah.base.geom.Direction
import io.antarescircuit.jabbah.base.geom.Direction.*
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.geom.Rectangle2D
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.graphics.DropShadow
import io.antarescircuit.jabbah.draw.style.DrawStyleModule
import io.antarescircuit.jabbah.draw.style.StyleProvider
import io.antarescircuit.jabbah.draw.style.StyleType
import io.antarescircuit.jabbah.edit.properties.magnitude.MagnitudeValue
import io.antarescircuit.jabbah.graph.view.vertice.AbstractVerticeView

class CurrentSourceView(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	model: CurrentSource = CurrentSource()
) : AbstractAnalogVerticeView<CurrentSource>(styleProvider, model, EAST, Rectangle2D(-SIZE / 2, LENGTH, SIZE, SIZE)) {

	companion object {
		private const val SIZE = 6 * SCALE

		private val ARROW_PATH = System.createPath()
			.moveTo(0, 0)
			.lineTo(0.5 * SCALE, 1.0 * SCALE)
			.lineTo(-0.5 * SCALE, 1.0 * SCALE)
			.close()
	}

	@Suppress("unused") // Reflective bean property
	var current: MagnitudeValue
		get() = model.current
		set(value) {
			model.current = value
		}

	override val relativeExternalLabelLocation: Point2D get() = Point2D(bounds.maxX + LABEL_DIST, bounds.centerY)

	/** ---- [AbstractVerticeView] */

	override fun modelExchanged(oldModel: CurrentSource?) {
		super.modelExchanged(oldModel)
		addPortView(AnalogPortView(styleProvider, model.getPort(1), 0, LENGTH, NORTH))
		addPortView(AnalogPortView(styleProvider, model.getPort(2), 0, LENGTH + SIZE, SOUTH))
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

	override val mainPropertyValue: String get() = current.toString()

	override val mainPropertylabelLocation: Point2D
		get() = Point2D(bounds.minX - MAIN_PROPERTY_LABEL_DIST, bounds.centerY)

	override val mainPropertylabelOrientation: Direction get() = WEST
}