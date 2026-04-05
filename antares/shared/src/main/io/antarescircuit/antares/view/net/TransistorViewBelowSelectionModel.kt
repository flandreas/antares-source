package io.antarescircuit.antares.view.net

import io.antarescircuit.jabbah.edit.Look
import io.antarescircuit.jabbah.base.geom.Ellipse2D
import io.antarescircuit.jabbah.base.geom.RectangularShape
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.style.DrawStyleModule
import io.antarescircuit.jabbah.draw.style.StyleProvider
import io.antarescircuit.jabbah.draw.style.StyleType
import io.antarescircuit.jabbah.edit.select.AbstractBelowSelectionModel
import io.antarescircuit.jabbah.edit.style.EditStyleType

class TransistorViewBelowSelectionModel(
	transistorView: AbstractTransistorView<*>,
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	styleType: StyleType = EditStyleType.SELECTION
) : AbstractBelowSelectionModel<AbstractTransistorView<*>>(transistorView, styleProvider, styleType) {

	private var bounds = Ellipse2D()

	override val boundingBox: RectangularShape get() = bounds

	override fun draw(context: DrawContext) {
		context.g.color = color.foregroundColor
		context.g.fill(bounds)
	}

	override fun contains(x: Double, y: Double): Boolean = bounds.contains(x, y)

	override fun componentUpdated() {
		invalidate()
		val pos = component.location
		bounds = Ellipse2D(
			pos.x + 2.0 * Look.SCALE - outset,
			pos.y -5.0 * Look.SCALE - outset,
			AbstractTransistorView.WIDTH + 2.0 * outset,
			AbstractTransistorView.HEIGHT + 2.0 * outset)
		invalidate()
		validate()
	}
}